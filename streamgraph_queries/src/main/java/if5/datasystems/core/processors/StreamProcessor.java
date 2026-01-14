package if5.datasystems.core.processors;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.streaming.api.functions.ProcessFunction;

import if5.datasystems.core.models.streamingGraph.Edge;

import java.time.Instant;
import java.time.Duration;
import java.util.ArrayList;

import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.core.fs.Path;
import org.apache.flink.util.Collector;

import org.apache.flink.connector.file.src.FileSource;
import org.apache.flink.connector.file.src.reader.TextLineInputFormat;

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
            edge.setExpiricy(Instant.ofEpochMilli(edge.getStartTime_ms() + WINDOW_SIZE));
            graph.add(edge);
            
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
            // Timer triggered when currentWatermark >= expirationTimer
            ArrayList<Edge> remaining  = new ArrayList<>();
            for (Edge e: graph.get()){
                if (e.getExpiricy_ms() <= timestamp) {
                    // Expire edge
                    out.collect(
                        "REMOVE " + e.getStartTime_ms() + ", " + e.getExpiricy_ms() +
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

    public StreamProcessor(String stream_file_path, long window_size, int watermarkDelta) {
        this.env = StreamExecutionEnvironment.getExecutionEnvironment();
        this.env.setParallelism(1);
        this.env.setRuntimeMode(RuntimeExecutionMode.STREAMING);

        DataStream<String> socketStream = this.env.socketTextStream("localhost", 8080);

        // this.streamGraph =  
                            // env.fromSource(
                            //     FileSource.forRecordStreamFormat(
                            //         new EdgeStreamFormat(), // Reads CSV to Edge
                            //         new Path(stream_file_path)
                            //     ).build()
                            
                                // WatermarkStrategy // Sets up time for expiration given edge start times and lateness available
                                //     .<Edge>forBoundedOutOfOrderness(Duration.ofMillis(watermarkDelta))
                                //     .withTimestampAssigner((edge, ts) -> edge.getStartTime_ms())
                                // ,
                                // "CSV Test Source"
                            // );
        WatermarkStrategy ws = WatermarkStrategy //Sets up time for expiration given edge start times and lateness available
            .<MyEvent>forBoundedOutOfOrderness(Duration.ofMillis(watermarkDelta))
            .withTimestampAssigner((evnt, ts) -> evnt.f2);
        socketStream
        .filter(s->(s != "" && s != null))
        .map(MyEvent::new)
        .assignTimestampsAndWatermarks(ws)
        .process(new ProcessFunction<MyEvent, String>() {
            @Override
            public void processElement(MyEvent event, Context ctx, Collector<String> out) {
                out.collect(event.f1 + "| WS: " + ctx.timerService().currentWatermark());
            }
        })
        .print();
        // .map(MyEvent::new).process(new ProcessFunction<MyEvent, String>() {
        //     @Override
        //     public void processElement(MyEvent event, Context ctx, Collector<String> out) {
        //         out.collect(event.toString());
        //     }
        // }).print();
        // this.streamGraph.process(new ProcessFunction<Edge, Edge>() {
        //     @Override
        //     public void processElement(Edge edge, Context ctx, Collector<Edge> out) {
        //         System.out.println("EDGE: " + edge.getStartTime_ms() + " | WM=" + ctx.timerService().currentWatermark());
        //         out.collect(edge);
        //     }
        // });
            //.assignTimestampsAndWatermarks(watermarkStrategy)
            // .keyBy(edge -> 0)
            // .process(new GraphExpirer(window_size))
            // .print();
    }

    public void execute(String job_name) throws Exception {
        this.env.execute(job_name);
    }

}
