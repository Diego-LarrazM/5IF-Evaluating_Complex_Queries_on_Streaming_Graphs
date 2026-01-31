package if5.datasystems.core.processors;

import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.nio.file.Paths;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;

import if5.datasystems.core.models.streamingGraph.Edge;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.Iterator;

import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.util.Collector;

import if5.datasystems.core.models.streamingGraph.StreamingGraph;
import if5.datasystems.core.models.streamingGraph.StreamingGraphTuple;
import if5.datasystems.core.models.aliases.Label;
import if5.datasystems.core.models.aliases.NodeKey;
import if5.datasystems.core.models.aliases.Pair;
import if5.datasystems.core.models.queries.IndexPath;
import if5.datasystems.core.models.queries.SpanningTree;

public class StreamProcessor {

    StreamExecutionEnvironment env;
    DataStream<Edge> streamGraph;
    QueryProcessor queryProcessor;

    class QueryProcessor extends KeyedProcessFunction<Integer, EdgeEventFormat, String> {
        private final long WINDOW_SIZE;
        private StreamingGraph currentSnapshotGraph;
        private List<SPathProcessor> queryProcessors;
        private HashMap<String, HashSet<Edge>> vertexEdges; // Per Vertex (v), all current existing edges where v = e.src, for efficiency in spath
        private final List<Pair<Label, Label>> queries;

        // Metrics
        private HashMap<Label, Long> totalQueryTimeNs;
        private HashMap<Label, Integer> queryCount;
        private int eventCounter = 0;
        private final int SERIALIZE_EVERY = 100;
        private long startProcessingTimeNs; // throughput measurement

        public QueryProcessor(long windowSize, List<Pair<Label, Label>> queries) {
            this.WINDOW_SIZE = windowSize;
            this.queries = queries;
        }

        @Override
        public void open(OpenContext ctx) throws Exception {
            this.currentSnapshotGraph = new StreamingGraph();
            this.vertexEdges = new HashMap<>();
            this.queryProcessors = new ArrayList<>();
            for (int i = 0; i < this.queries.size(); i++) {
                this.queryProcessors.add(new SPathProcessor(this.queries.get(i).first(), this.queries.get(i).second()));
            }

            this.totalQueryTimeNs = new HashMap<>();
            this.queryCount = new HashMap<>();
            this.startProcessingTimeNs = System.nanoTime();
        }

        @Override
        public void processElement(
            EdgeEventFormat edge_event, 
            Context context,
            Collector<String> out
        ) throws Exception {

            // Update State
            Edge edge = edge_event.edge;
            edge.setExpiricy(edge_event.timestamp + WINDOW_SIZE);
            StreamingGraphTuple sgt = new StreamingGraphTuple(edge);
            
            // Update snapshot
            this.currentSnapshotGraph.updateStreamingGraph(sgt);
            this.vertexEdges.computeIfAbsent(edge.getSource(), k -> new HashSet<>()).add(edge);
            
            for (SPathProcessor spathProcessor : this.queryProcessors) {
                long startTime = System.nanoTime();
                spathProcessor.apply( // Update query result from new edge
                    new Pair<>(this.vertexEdges, edge)
                );
                long duration = System.nanoTime() - startTime;
                
                // Metrics: accumulate total time and count
                totalQueryTimeNs.put(
                    spathProcessor.getOutLabel(),
                    totalQueryTimeNs.getOrDefault(spathProcessor.getOutLabel(), 0L) + duration
                );
                queryCount.put(
                    spathProcessor.getOutLabel(),
                    queryCount.getOrDefault(spathProcessor.getOutLabel(), 0) + 1
                );
            }

            eventCounter++;

            if (eventCounter % SERIALIZE_EVERY == 0) {
                String fileName = "results_" + eventCounter + ".txt";
                try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {

                    // --- write the results in human-readable form ---
                    writer.println("=== RESULTS AT EVENT #" + eventCounter + " ===");
                    for (SPathProcessor spathProcessor : this.queryProcessors) {
                        writer.printf("\n--- Results for query %s ---\n", spathProcessor.getOutLabel());
                        writer.println(spathProcessor.getDeltaPath().toString());
                    }
                    writer.println(this.queryProcessors.toString());

                    // --- write average time per query ---
                    writer.println("\n=== QUERY TIME METRICS ===");
                    for (Label label : totalQueryTimeNs.keySet()) {
                        long totalNs = totalQueryTimeNs.get(label);
                        int count = queryCount.getOrDefault(label, 1); // safety
                        double avgMs = totalNs / 1_000_000.0 / count;

                        writer.printf("Query %s avg time: %.3f ms over %d runs%n",
                                label, avgMs, count);
                    }
                    
                    writer.printf("\n=== PERFORMANCE METRICS ===\n");
                    long nowNs = System.nanoTime();
                    double throughput = SERIALIZE_EVERY / ((nowNs - startProcessingTimeNs) / 1_000_000_000.0); // events/sec
                    writer.printf("Throughput: %.2f events/sec%n", throughput);
                    startProcessingTimeNs = nowNs; // reset for next batch

                    System.out.println("Saved human-readable results at " + fileName);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            // Downstream to output for printing
            out.collect(
                "ADD    " + edge.toString() + ", ts:" + edge.getStartTime()   +
                " | watermark=" +
                context.timerService().currentWatermark()
            );

            // Register expiration timer to call onTimer when currentWatermark >= expirationTimer
            context.timerService().registerEventTimeTimer(edge.getExpiricy());
        }

        @Override
        public void onTimer(
            long timestamp,
            OnTimerContext context,
            Collector<String> out
        ) throws Exception 
        {
            Iterator<StreamingGraphTuple> iterator = this.currentSnapshotGraph.getTuples().iterator();
            while (iterator.hasNext()) {
                StreamingGraphTuple tuple = iterator.next();
                if (tuple.getExpiricy() <= timestamp) {
                    Edge e = tuple.getRepr();
                    String src = e.getSource();

                    // Remove from vertexTargets
                    this.vertexEdges.get(src).remove(e);

                    // Remove from snapshot
                    out.collect(
                        "REMOVE " + tuple.toString() + ", ts:" + tuple.getStartTime()  +
                        " | watermark=" +
                        context.timerService().currentWatermark()
                    );
                    iterator.remove();
                } else {
                    // Arrêter l'itération dès qu'un tuple non expiré est trouvé
                    break;
                }
            }
            // Update from indexPath
            for (SPathProcessor spathprocessor : this.queryProcessors) {
                // Expire all path final targets from smallest ts to largest ts < (current_ts - WINDOW_SIZE)
                // We only expire path target node given we suppose childNode.ts <= parentNode.ts, thus the parents will be eliminated along or will be eliminated later on.
                TreeMap<Long, Set<NodeKey>> ALL_RS = spathprocessor.getResultTimestamps();
                HashMap<NodeKey, Long> node_lookup = spathprocessor.getLookup();
                IndexPath queryDeltaPath = spathprocessor.getDeltaPath();
                Long result_ts = ALL_RS.isEmpty() ? null : ALL_RS.firstKey();
                while (result_ts != null && result_ts < timestamp - WINDOW_SIZE) {
                    // Remove entry and expire all
                    Entry<Long, Set<NodeKey>> timestamp_results = ALL_RS.pollFirstEntry(); 
                    for (NodeKey pathKey : timestamp_results.getValue()) {
                        String root = pathKey.pathSource();
                        SpanningTree Tx = queryDeltaPath.getTree(root);
                        node_lookup.remove(pathKey);
                        if (Tx == null) {
                            // Parent tree was expired/removed
                            continue;  // safe: this path is no longer valid
                        }
                        Tx.removeNode(pathKey.pathTargetKey());
                        if (Tx.getNodes().size() == 1){
                            queryDeltaPath.removeTree(root);
                        }
                    }
                    result_ts = ALL_RS.isEmpty() ? null : ALL_RS.firstKey(); // check next
                }
            }
        }

        
    }
    public StreamProcessor(long window_size, int watermarkDelta, List<Pair<Label, Label>> queries, int stream_port) {
        this.env = StreamExecutionEnvironment.getExecutionEnvironment();
        this.env.setParallelism(1);
        this.env.setRuntimeMode(RuntimeExecutionMode.STREAMING);
        this.queryProcessor = new QueryProcessor(window_size, queries);

        DataStream<String> socketStream = this.env.socketTextStream("localhost", stream_port);
        socketStream
        .filter(s->(s != "" && s != null))
        .map(EdgeEventFormat::new)
        .assignTimestampsAndWatermarks(
            WatermarkStrategy //Sets up time for expiration given edge start times and lateness available
            .<EdgeEventFormat>forBoundedOutOfOrderness(Duration.ofMillis(watermarkDelta))
            .withTimestampAssigner((event, ts) -> event.timestamp))
        .keyBy(edge -> 0)
        .process(this.queryProcessor)
        .print();
    }

    public void execute(String job_name) throws Exception {
        this.env.execute(job_name);
    }

}
