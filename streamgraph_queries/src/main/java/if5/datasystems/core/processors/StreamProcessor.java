package if5.datasystems.core.processors;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;

import if5.datasystems.core.models.streamingGraph.Edge;

import java.time.Instant;
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

        public QueryProcessor(long windowSize, List<Pair<Label, Label>> queries) {
            this.WINDOW_SIZE = windowSize;
            this.queries = queries;
        }

        @Override
        public void open(OpenContext ctx) throws Exception {
            this.streamingGraph = new StreamingGraph();
            this.results = new HashMap<>();
            this.spathProcessor = new SPath();
        }

        @Override
        public void processElement(
            EdgeEventFormat edge_event, 
            Context context,
            Collector<String> out
        ) throws Exception {

            // Update State
            Edge edge = edge_event.edge;
            edge.setExpiricy(Instant.ofEpochMilli(edge_event.timestamp + WINDOW_SIZE));
            StreamingGraphTuple sgt = new StreamingGraphTuple(edge);

            this.streamingGraph.updateStreamingGraph(sgt);
            
            for (Pair<Label, Label> query : this.queries) {
                StreamingGraph queryResult = spathProcessor.apply(
                    new Triple<>(
                        this.streamingGraph,
                        query.first(),
                        query.second()
                    )
                );
                this.results.put(
                    query.second().l,
                    queryResult
                );
            }
            // Downstream to output for printing
            out.collect(
                "ADD    " + edge.getStartTime_ms() + ", " + edge.getExpiricy_ms()  +
                " | watermark=" +
                context.timerService().currentWatermark()
            );

            // Register expiration timer to call onTimer when currentWatermark >= expirationTimer
            context.timerService().registerEventTimeTimer(edge.getExpiricy_ms());
        }

        @Override
        public void onTimer(
            long timestamp,
            OnTimerContext context,
            Collector<String> out) throws Exception {
            Iterator<StreamingGraphTuple> iterator = this.streamingGraph.getTuples().iterator();
            while (iterator.hasNext()) {
                StreamingGraphTuple tuple = iterator.next();
                if (tuple.getExpiricy_ms() <= timestamp) {
                    out.collect(
                        "REMOVE " + tuple.getStartTime_ms() + ", " + tuple.getExpiricy_ms() +
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
