package if5.datasystems;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    static private Edge edge(
            String src, String tgt, String label,
            long start, long exp
    ) {
        return new Edge(
                src,
                tgt,
                new Label(label),
                Instant.ofEpochMilli(start),
                Instant.ofEpochMilli(exp)
        );
    }

    static private StreamingGraph graph(Edge... edges) {
        StreamingGraph g = new StreamingGraph();
        for (Edge e : edges) {
            g.add(new StreamingGraphTuple(e));
        }
        return g;
    }
    
    public static void main(String[] args) {
        /*StreamProcessor processor = new StreamProcessor(10, 0);
        try {
            processor.execute("Stream Graph Processor");
        } catch (Exception e) {
            e.printStackTrace();
        }*/
       SPath spath = new SPath();

        StreamingGraph g = graph(
                edge("A", "B", "a", 0, 10),
                edge("B", "C", "a", 0, 10)
        );

        StreamingGraph result = spath.apply(
                new Triple<>(g, new Label("a+"), new Label("p"))

        );

        System.out.println(result.getTuples());

        /*TreeSet<StreamingGraphTuple> results = new TreeSet<>(StreamingGraphTuple.BY_EXPIRICY);
        //  resulmt = sgt(AB),sgt (ABC)
        results.add(new StreamingGraphTuple(
                edge("A", "B", "a", 0, 10)
        ));
        results.add(new StreamingGraphTuple(
                edge("B", "C", "a", 0, 10)
        ));
        for (StreamingGraphTuple sgt: results){
            System.out.println(sgt);
        }
        TreeSet<StreamingGraphTuple> resultsTS = new TreeSet<>(StreamingGraphTuple.BY_EXPIRICY);
        resultsTS.addAll(results);
        System.out.println("--- Sorted ---");
        for (StreamingGraphTuple sgt: resultsTS){
            System.out.println(sgt);
        }*/

    }
}