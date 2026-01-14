package if5.datasystems;

import java.sql.Time;
import java.util.ArrayList;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;

import if5.datasystems.core.models.aliases.Label;
import if5.datasystems.core.models.streamingGraph.Edge;
import if5.datasystems.core.processors.StreamProcessor;

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");

        String abs_path = "file:///home/dlarraz/Data/Projects/5IF-Evaluating_Complex_Queries_on_Streaming_Graphs/streamgraph_queries/src/main/java/if5/datasystems/Edges.csv";
        StreamProcessor processor = new StreamProcessor(abs_path, 10, 0);
        try {
            processor.execute("Stream Graph Processor");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}