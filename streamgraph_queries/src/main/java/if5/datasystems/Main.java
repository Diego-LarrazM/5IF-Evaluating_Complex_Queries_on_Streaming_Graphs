package if5.datasystems;
import java.util.ArrayList;

import if5.datasystems.core.models.aliases.Label;
import if5.datasystems.core.models.aliases.Pair;
import if5.datasystems.core.processors.StreamProcessor;

public class Main {
    
    public static void main(String[] args) {
        long windowSize = 3600*24; // 1 day window (in s for stack overflow)
        //long windowSize = 5; // (in s for integration test)
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


        // Integration Test query
        /*queries.add(
            new Pair<>(
                new Label("a,b*"),
                new Label("Q1")
            )
        );*/


        StreamProcessor processor = new StreamProcessor(windowSize, 0, queries, 8080);
        try {
            processor.execute("Stream Graph Processor");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}