package if5.datasystems;

import java.sql.Time;
import java.util.ArrayList;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;

import if5.datasystems.core.models.aliases.Label;
import if5.datasystems.core.models.streamingGraph.Edge;
import if5.datasystems.core.processors.StreamProcessor;

public class Main {
    public static void main(String[] args) {
        StreamProcessor processor = new StreamProcessor(10, 0);
        try {
            processor.execute("Stream Graph Processor");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}