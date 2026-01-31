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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.util.Collector;

import if5.datasystems.core.models.streamingGraph.StreamingGraph;
import if5.datasystems.core.models.streamingGraph.StreamingGraphTuple;
import if5.datasystems.core.models.aliases.Label;
import if5.datasystems.core.models.aliases.Pair;
import if5.datasystems.core.models.aliases.Tuple4;
import if5.datasystems.core.models.queries.IndexPath;

public class StreamProcessor {

    StreamExecutionEnvironment env;
    DataStream<Edge> streamGraph;
    QueryProcessor queryProcessor;

    class QueryProcessor extends KeyedProcessFunction<Integer, EdgeEventFormat, String> {
        private final long WINDOW_SIZE;
        private StreamingGraph streamingGraph;
        private List<Pair<Label, Label>> queries;
        private HashMap<String, StreamingGraph> results;
        private BatchSPath spathProcessor;

        // Metrics
        private HashMap<Label, Long> totalQueryTimeNs;
        private HashMap<Label, Integer> queryCount;
        private int eventCounter = 0;
        private final int SERIALIZE_EVERY = 50;
        private long startProcessingTimeNs; // throughput measurement

        public QueryProcessor(long windowSize, List<Pair<Label, Label>> queries) {
            this.WINDOW_SIZE = windowSize;
            this.queries = queries;
        }

        @Override
        public void open(OpenContext ctx) throws Exception {
            this.streamingGraph = new StreamingGraph();
            this.results = new HashMap<>();
            this.spathProcessor = new BatchSPath();

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

            this.streamingGraph.updateStreamingGraph(sgt);
            
            for (Pair<Label, Label> query : this.queries) {
                long startTime = System.nanoTime();
                StreamingGraph queryResult = spathProcessor.apply(
                    new Tuple4<>(
                        new IndexPath(),
                        this.streamingGraph,
                        query.first(),
                        query.second()
                    )
                );
                long duration = System.nanoTime() - startTime;
                // accumulate total time and count
                totalQueryTimeNs.put(
                    query.second(),
                    totalQueryTimeNs.getOrDefault(query.second(), 0L) + duration
                );
                queryCount.put(
                    query.second(),
                    queryCount.getOrDefault(query.second(), 0) + 1
                );

                this.results.put(
                    query.second().l,
                    queryResult
                );
                
            }

            eventCounter++;

            if (eventCounter % SERIALIZE_EVERY == 0) {
                String fileName = "results_" + eventCounter + ".txt";
                try (PrintWriter writer = new PrintWriter(new FileWriter(fileName))) {

                    // --- write the results in human-readable form ---
                    writer.println("=== RESULTS AT EVENT #" + eventCounter + " ===");
                    writer.println(this.results.toString());  // assuming results.toString() is meaningful

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
            Collector<String> out) throws Exception {
            Iterator<StreamingGraphTuple> iterator = this.streamingGraph.getTuples().iterator();
            while (iterator.hasNext()) {
                StreamingGraphTuple tuple = iterator.next();
                if (tuple.getExpiricy() <= timestamp) {
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
