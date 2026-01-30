package if5.datasystems;
import java.sql.Time;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import org.apache.flink.api.common.eventtime.WatermarkStrategy;

import if5.datasystems.core.models.aliases.Label;
import if5.datasystems.core.models.aliases.Pair;
import if5.datasystems.core.processors.StreamProcessor;
import if5.datasystems.core.models.streamingGraph.StreamingGraph;
import if5.datasystems.core.models.streamingGraph.StreamingGraphTuple;
import if5.datasystems.core.models.aliases.Triple;
import if5.datasystems.core.processors.SPath;
import if5.datasystems.core.models.streamingGraph.Edge;

public class Main {
    
    public static void main(String[] args) {
        long windowSize = 3600*24*30; // 1 day window
        ArrayList<Pair<Label, Label>> queries = new ArrayList<>();

        // a*
        queries.add(
            new Pair<>(
                new Label("a2q*"),
                new Label("Q1")
            )
        );

        // a , b*
        queries.add(
            new Pair<>(
                new Label("c2q,a2q*"),
                new Label("Q2" )
            )
        );

        // a , b* , c*
        queries.add(
            new Pair<>(
                new Label("c2a,c2q*,a2q*"),
                new Label("Q3" )
            )
        );

        StreamProcessor processor = new StreamProcessor(windowSize, 0, queries, 8080);
        try {
            processor.execute("Stream Graph Processor");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}