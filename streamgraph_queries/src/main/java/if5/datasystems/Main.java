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

        ArrayList<Edge> edges = new ArrayList<>();
        edges.add(new Edge("1", "2", new Label("a"), 10L));
        edges.add(new Edge("2", "3", new Label("b"), 12L));
        edges.add(new Edge("1", "3", new Label("a"), 9L));   //late
        edges.add(new Edge("1", "3", new Label("a"), 8L));   //too late
        edges.add(new Edge("3", "4", new Label("a"), 22L));

        StreamProcessor processor = new StreamProcessor(edges, 10, 3);
        try {
            processor.execute("Stream Graph Processor");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}