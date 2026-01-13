package if5.datasystems.core.processors;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.ProcessFunction;

import if5.datasystems.core.models.streamingGraph.Edge;

import java.sql.Time;
import java.time.Duration;
import java.util.ArrayList;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;

public class StreamProcessor {

    StreamExecutionEnvironment env;
    DataStream<Edge> streamGraph;

    class GraphExpirer extends KeyedProcessFunction<Integer, Edge, String> {
        private final long WINDOW_SIZE;
        private ListState<Edge> graph;

        public GraphExpirer(long windowSize) {
            this.WINDOW_SIZE = windowSize;
        }

        @Override
        public void open(Configuration config){
            // Initialization logic here
            graph = getRuntimeContext().getListState(
                new ListStateDescriptor<>("graph", Edge.class)
            );
        }

        @Override
        public void processElement(
            Edge edge, 
            Context context,
            Collector<String> out) throws Exception {
            // Processing of each event (Edge + timestamp + Watermark)

            // Update State
            edge.setExpiricy(edge.getStartTime() + WINDOW_SIZE);
            graph.add(edge);
            
            // Downstream to output for printing
            out.collect(
                "ADD    " + edge +
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
            // Timer triggered when currentWatermark >= expirationTimer
            ArrayList<Edge> remaining  = new ArrayList<>();
            for (Edge e: graph.get()){
                if (e.getExpiricy() <= timestamp) {
                    // Expire edge
                    out.collect(
                        "REMOVE " + e +
                        " | watermark=" +
                        context.timerService().currentWatermark()
                    );
                } else {
                    // Keep edge
                    remaining.add(e);
                }
            }
            // Update state
            graph.update(remaining);
        }
    }

    public StreamProcessor(ArrayList<Edge> edges, long window_size, int watermarkDelta) {
        this.env = StreamExecutionEnvironment.getExecutionEnvironment();
        this.env.setParallelism(1);

        ArrayList<Edge> safeEdges = edges;

        this.streamGraph = env.fromCollection(edges);

        WatermarkStrategy<Edge> watermarkStrategy = WatermarkStrategy
            .<Edge>forBoundedOutOfOrderness(Duration.ofSeconds(watermarkDelta))
            //.<Edge>forMonotonousTimestamps()
            .withTimestampAssigner((edge,ts)-> edge.getStartTime()); //.getTime()

        this.streamGraph
            .assignTimestampsAndWatermarks(watermarkStrategy)
            .keyBy(edge -> 0)
            .process(new GraphExpirer(window_size))
            .print();
    }

    public void execute(String job_name) throws Exception {
        this.env.execute(job_name);
    }

}
