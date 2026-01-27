package if5.datasystems;

import java.sql.Time;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeSet;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;

import if5.datasystems.core.models.aliases.Label;
import if5.datasystems.core.processors.StreamProcessor;
import if5.datasystems.core.models.streamingGraph.StreamingGraph;
import if5.datasystems.core.models.streamingGraph.StreamingGraphTuple;
import if5.datasystems.core.models.aliases.Triple;
import if5.datasystems.core.processors.SPath;
import if5.datasystems.core.models.streamingGraph.Edge;

public class Main {
    
    public static void main(String[] args) {
        /*StreamProcessor processor = new StreamProcessor(10, 0);
        try {
            processor.execute("Stream Graph Processor");
        } catch (Exception e) {
            e.printStackTrace();
        }*/

    }
}