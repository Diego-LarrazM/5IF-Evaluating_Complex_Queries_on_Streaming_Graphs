package if5.datasystems.core.processors;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;

import if5.datasystems.core.models.streamingGraph.Edge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.Iterator;

import org.apache.flink.api.common.RuntimeExecutionMode;
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
        private final long SLIDE;
        private long maxTimestampSeen;
        private StreamingGraph currentSnapshotGraph;
        private List<SPathProcessor> queryProcessors;
        private HashMap<String, HashSet<Edge>> vertexEdges; // Per Vertex (v), all current existing edges where v = e.src, for efficiency in spath
        private final List<Pair<Label, Label>> queries;

        // Metrics
        private HashMap<Label, Long> totalQueryTimeNs;
        private HashMap<Label, Integer> queryCount;
        private int eventCounter = 0;
        private int serializerCounter = 0;
        private final int SERIALIZE_EVERY = 200;
        private long startProcessingTimeNs; // throughput measurement

        public QueryProcessor(long windowSize, long slide, List<Pair<Label, Label>> queries) {
            this.WINDOW_SIZE = windowSize;
            this.SLIDE = slide;
            this.queries = queries;
            this.maxTimestampSeen = Long.MIN_VALUE;
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
            this.maxTimestampSeen = Math.max(this.maxTimestampSeen, edge_event.timestamp); // iterative time advancement
            StreamingGraphTuple sgt = AlgebraFunctions.getWindowedTuple(edge, edge_event.timestamp, WINDOW_SIZE, SLIDE, maxTimestampSeen);
            if (sgt == null) return;
            
            // Update snapshot
            this.currentSnapshotGraph.updateStreamingGraph(sgt);
            this.vertexEdges.computeIfAbsent(edge.getSource(), k -> new HashSet<>()).add(edge);
            expire(out);
            
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
            serializerCounter++;

            if (serializerCounter == SERIALIZE_EVERY) { // Saves metrics and results to parent_dir/results/
                serializerCounter = 0; // restart counter
                String fileName = "./results/results_" + eventCounter + ".txt";
                try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {

                    writer.println("SnapshotSize: " + currentSnapshotGraph.getTuples().size() + "\n");

                    // --- write the results in human-readable form ---
                    writer.println("=== RESULTS AT EVENT #" + eventCounter + " ===");
                    for (SPathProcessor spathProcessor : this.queryProcessors) {
                        writer.printf("\n--- Results for query %s: %s ---\n", spathProcessor.getOutLabel(), spathProcessor.getQueryLabel());
                        writer.println(spathProcessor.getDeltaPath());
                    }

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
                "ADD    " + edge.toString() + ", ts:" + edge.getStartTime()   + ", exp:" + edge.getExpiricy() +
                " | maxT=" + this.maxTimestampSeen
            );
        }

        public void expire(
            Collector<String> out
        ) throws Exception 
        {
            long countExpired = 0;
            Iterator<StreamingGraphTuple> iterator = this.currentSnapshotGraph.getTuples().iterator();
            while (iterator.hasNext()) {
                StreamingGraphTuple tuple = iterator.next();
                if (tuple.getExpiricy() <= this.maxTimestampSeen) {
                    countExpired++;
                    Edge e = tuple.getRepr();
                    String src = e.getSource();

                    // Remove from vertexTargets
                    this.vertexEdges.get(src).remove(e);

                    // Remove from snapshot
                    out.collect(
                        "REMOVE " + tuple.toString() + ", exp:" + tuple.getExpiricy()  +
                        " | maxT=" + this.maxTimestampSeen
                    );
                    iterator.remove();
                } else {
                    // Arrêter l'itération dès qu'un tuple non expiré est trouvé
                    break;
                }
            }
            if (countExpired == 0) return; // No expirations

            // Update from indexPath
            for (SPathProcessor spathprocessor : this.queryProcessors) {
                // Expire all path final targets from smallest exp to largest exp <= (current_ts), exp excluded in validity interval
                // We only expire path target node given we suppose childNode.ts <= parentNode.ts, thus the parents will be eliminated along or will be eliminated later on.
                TreeMap<Long, Set<NodeKey>> ALL_RS = spathprocessor.getResultTimestamps();
                HashMap<NodeKey, Long> node_lookup = spathprocessor.getLookup();
                IndexPath queryDeltaPath = spathprocessor.getDeltaPath();
                Long result_ts = ALL_RS.isEmpty() ? null : ALL_RS.firstKey();
                while (result_ts != null && AlgebraFunctions.wscanExp(result_ts, WINDOW_SIZE, SLIDE) <= this.maxTimestampSeen) {
                    // Remove entry and expire all
                    Entry<Long, Set<NodeKey>> timestamp_results = ALL_RS.pollFirstEntry();
                    for (NodeKey pathKey : timestamp_results.getValue()) {
                        String root = pathKey.pathSource();
                        // System.out.println("EXPIRE PATH " + root + "--*-->" + pathKey.pathTargetKey().toString()); // To un-comment for debugging
                        SpanningTree Tx = queryDeltaPath.getTree(root);
                        node_lookup.remove(pathKey);
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
    public StreamProcessor(long windowSize, long slide, List<Pair<Label, Label>> queries, int streamPort) {
        this.env = StreamExecutionEnvironment.getExecutionEnvironment();
        this.env.setParallelism(1);
        this.env.setRuntimeMode(RuntimeExecutionMode.STREAMING);
        if (slide <= 0) slide = 1; // Default slide
        this.queryProcessor = new QueryProcessor(windowSize,  slide, queries);

        DataStream<String> socketStream = this.env.socketTextStream("localhost", streamPort);
        socketStream
        .filter(s->(s != "" && s != null))
        .map(EdgeEventFormat::new)
        .keyBy(edge -> 0)
        .process(this.queryProcessor)//;
        .print(); // for debugging
    }

    public void execute(String job_name) throws Exception {
        this.env.execute(job_name);
    }

}
