package if5.datasystems.core.processors;

import java.io.ObjectOutputStream;
import java.io.FileOutputStream;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;

import if5.datasystems.core.models.streamingGraph.Edge;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Iterator;

import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.OpenContext;
import org.apache.flink.util.Collector;

import if5.datasystems.core.models.streamingGraph.StreamingGraph;
import if5.datasystems.core.models.streamingGraph.StreamingGraphTuple;
import if5.datasystems.core.models.aliases.Label;
import if5.datasystems.core.models.aliases.Pair;
import if5.datasystems.core.models.aliases.Triple;
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
        private SPath spathProcessor;

        // Metrics
        private HashMap<Label, Long> totalQueryTimeNs;
        private HashMap<Label, Integer> queryCount;
        private int eventCounter = 0;
        private final int SERIALIZE_EVERY = 100;

        public QueryProcessor(long windowSize, List<Pair<Label, Label>> queries) {
            this.WINDOW_SIZE = windowSize;
            this.queries = queries;
        }

        @Override
        public void open(OpenContext ctx) throws Exception {
            this.streamingGraph = new StreamingGraph();
            this.results = new HashMap<>();
            this.spathProcessor = new SPath();

            this.totalQueryTimeNs = new HashMap<>();
            this.queryCount = new HashMap<>();
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
                try (ObjectOutputStream oos = new ObjectOutputStream(
                        new FileOutputStream("results_" + eventCounter + ".ser"))) {

                    oos.writeObject(this.results);
                    oos.flush();

                    System.out.println("Serialized results at event #" + eventCounter);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                // --- optional: print avg time per query for logging ---
                for (Label label : totalQueryTimeNs.keySet()) {
                    long totalNs = totalQueryTimeNs.get(label);
                    int count = queryCount.getOrDefault(label, 1); // safety
                    double avgMs = totalNs / 1_000_000.0 / count;

                    System.out.println(
                            "Query " + label + " avg time: " + avgMs + " ms over " + count + " runs"
                    );
                }
            }
            // Downstream to output for printing
            out.collect(
                "ADD    " + edge.getStartTime() + ", " + edge.getExpiricy()  +
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
                        "REMOVE " + tuple.getStartTime() + ", " + tuple.getExpiricy() +
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
