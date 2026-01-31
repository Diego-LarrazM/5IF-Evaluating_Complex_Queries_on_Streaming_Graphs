package if5.datasystems.core.models.processors;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import java.util.TreeMap;

import if5.datasystems.core.processors.BatchSPath;
import if5.datasystems.core.processors.SPathProcessor;

import org.apache.flink.shaded.zookeeper3.org.apache.jute.Index;
import org.junit.jupiter.api.Test;

import if5.datasystems.core.models.aliases.Label;
import if5.datasystems.core.models.aliases.NodeKey;
import if5.datasystems.core.models.aliases.Pair;
import if5.datasystems.core.models.queries.IndexPath;
import if5.datasystems.core.models.streamingGraph.Edge;
import if5.datasystems.core.models.streamingGraph.StreamingGraph;
import if5.datasystems.core.models.streamingGraph.StreamingGraphTuple;

class SPathProcessorTest {

    private Edge edge(
            String src, String tgt, String label,
            long start, long exp
    ) {
        return new Edge(
                src,
                tgt,
                new Label(label),
                start,
                exp
        );
    }

    private StreamingGraph graph(Edge... edges) {
        StreamingGraph g = new StreamingGraph();
        for (Edge e : edges) {
            g.add(new StreamingGraphTuple(e));
        }
        return g;
    }

    @Test
    void spath_multiple_labels_propagation_story() {
        SPathProcessor spath = new SPathProcessor(new Label("a,b*"), new Label("out"));

        // ─────────────────────────
        // Step 1
        // ─────────────────────────
        Edge e1 = edge("x", "y", "a", 0, 1000);
        Edge e2 = edge("y", "z", "a", 1, 1001);
        Edge eNo = edge("y", "asdasd", "s", 1, 1001);
        StreamingGraph g1 = graph(
                e1,
                e2,
                eNo
        );

        spath.apply(
                new Pair<>(g1,e1)
        );
        spath.apply(
                new Pair<>(g1,e2)
        );
        spath.apply(
                new Pair<>(g1,eNo)
        );

        System.out.println("Result1 tuples: " + spath.getDeltaPath().toString() + "\n");
        System.out.println("Result2 tuples: " + spath.getDeltaPath().toString() + "\n");
        // assertEquals(1, r1.getTuples().size());
        // assertTrue(r1.containsPath("x", "y"));

        // ─────────────────────────
        // Step 2
        // ─────────────────────────
        Edge e3 = edge("y", "l", "a", 2, 1002);
        StreamingGraph g2 = graph(
                e1,
                e2,
                eNo,
                e3
        );

        spath.apply(
                new Pair<>(g2,e3)
        );

        System.out.println("Result3 tuples: " + spath.getDeltaPath().toString() + "\n");
        

        // assertEquals(3, r2.getTuples().size());
        // assertTrue(r2.containsPath("x", "y"));
        // assertTrue(r2.containsPath("y", "z"));
        // assertTrue(r2.containsPath("y", "z", "l"));

        // ─────────────────────────
        // Step 3
        // ─────────────────────────
        Edge e4 = edge("z", "l", "b", 3, 1003);
        Edge e5 = edge("l", "m", "b", 4, 1004);
        StreamingGraph g3 = graph(
                e1,
                e2,
                eNo,
                e3,
                e4,
                e5
        );

        spath.apply(
                new Pair<>(g3,e4)
        );
        spath.apply(
                new Pair<>(g3,e5)
        );
        System.out.println("Result4 tuples: " + spath.getDeltaPath().toString() + "\n");
    }
/* 
    @Test
    void propagate_test_multiple_incomes_single_label() {
        BatchSPath spath = new BatchSPath();
        IndexPath deltaPath = new IndexPath();
        StreamingGraph g = graph(
                edge("x", "y", "a", 0, 1000),
                edge("z", "l", "a", 10, 1010),
                edge("l", "m", "a", 10, 1010),
                edge("y", "z", "a", 11, 1011)                      
        );

        StreamingGraph r1 = spath.apply(
                new Tuple4<>(deltaPath, g, new Label("a+"), new Label("out"))
        );

        assertEquals(10, r1.getTuples().size());

        assertTrue(r1.containsPath("x", "y"));
        assertTrue(r1.containsPath("y", "z"));
        assertTrue(r1.containsPath("z", "l"));
        assertTrue(r1.containsPath("l", "m"));

        assertTrue(r1.containsPath("x", "y", "z"));
        assertTrue(r1.containsPath("y", "z", "l"));
        assertTrue(r1.containsPath("z", "l", "m"));

        assertTrue(r1.containsPath("x", "y", "z", "l"));
        assertTrue(r1.containsPath("y", "z", "l", "m"));

        assertTrue(r1.containsPath("x", "y", "z", "l", "m"));

        System.out.println("Result1 tuples: " + r1.getTuples());

        StreamingGraph g2 = graph(
                edge("x", "y", "a", 0, 1000),
                edge("z", "l", "a", 10, 1010),
                edge("l", "m", "a", 10, 1010),
                edge("y", "z", "a", 11, 1011),   
                edge("y", "l", "a", 15, 1015)
        );

        StreamingGraph r2 = spath.apply(
                new Tuple4<>(new IndexPath(), g2, new Label("a+"), new Label("out"))
        );

        System.out.println("Result2 tuples: " + r2.getTuples());

        assertEquals(11, r2.getTuples().size());

        
        assertTrue(r2.containsPath("x", "y"));
        assertTrue(r2.containsPath("y", "z"));
        assertTrue(r2.containsPath("z", "l"));
        assertTrue(r2.containsPath("l", "m"));
        assertTrue(r2.containsPath("y", "l"));

        assertTrue(r2.containsPath("x", "y", "z"));
        assertTrue(r2.containsPath("y", "z", "l"));
        assertTrue(r2.containsPath("z", "l", "m"));
        assertTrue(r2.containsPath("x", "y", "z", "l"));
        assertTrue(r2.containsPath("y", "z", "l", "m"));
        assertTrue(r1.containsPath("x", "y", "z", "l", "m"));

        
    }

    @Test
    void emptyGraph_returnsEmptyResult() {
        BatchSPath spath = new BatchSPath();
        StreamingGraph g = new StreamingGraph();

        StreamingGraph result = spath.apply(
                new Tuple4<>(new IndexPath(), g, new Label("a"), new Label("out"))
        );

        assertTrue(result.getTuples().isEmpty());
    }

    @Test
    void singleEdge_matchingLabel_producesPath() {
        BatchSPath spath = new BatchSPath();
        StreamingGraph g = graph(
                edge("A", "B", "a", 0, 10)
        );

        StreamingGraph result = spath.apply(
                new Tuple4<>(new IndexPath(), g, new Label("a"), new Label("p"))
        );

        assertEquals(1, result.getTuples().size());

        StreamingGraphTuple t = result.getTuples().getFirst();
        assertEquals("A", t.getRepr().getSource());
        assertEquals("B", t.getRepr().getTarget());
        assertEquals(new Label("p"), t.getRepr().getLabel());
    }

    @Test
    void singleEdge_wrongLabel_producesNothing() {
        BatchSPath spath = new BatchSPath();
        StreamingGraph g = graph(
                edge("A", "B", "b", 0, 10)
        );

        StreamingGraph result = spath.apply(
                new Tuple4<>(new IndexPath(), g, new Label("a"), new Label("p"))
        );

        assertTrue(result.getTuples().isEmpty());
    }

    @Test
    void twoEdgePath_produces_all_valid_paths_withPlusAutomaton() {
        BatchSPath spath = new BatchSPath();

        StreamingGraph g = graph(
                edge("A", "B", "a", 0, 10),
                edge("B", "C", "a", 0, 10)
        );

        StreamingGraph result = spath.apply(
                new Tuple4<>(new IndexPath(), g, new Label("a+"), new Label("p"))
        );

        StreamingGraph expected = new StreamingGraph();

        expected.add(new StreamingGraphTuple(
                new Edge("A","B",new Label("p"),
                        0,
                        10)
        ));
        expected.add(new StreamingGraphTuple(
                new Edge("B","C",new Label("p"),
                        0,
                        10)
        ));
        expected.add(new StreamingGraphTuple(
                new Edge("A","C",new Label("p"),
                        0,
                        10)
        ));

        assertEquals(
                expected.getTuples().size(),
                result.getTuples().size(),
                "Number of paths produced is incorrect"
        );

        for (StreamingGraphTuple expectedTuple : expected.getTuples()) {
            assertTrue(
                    result.getTuples().contains(expectedTuple),
                    "Missing expected path: " + expectedTuple
            );
        }
    }

    @Test
    void expiredEdges_doNotFormPath_dueToSnapshot() {
        BatchSPath spath = new BatchSPath();
        StreamingGraph g = graph(
                edge("A", "B", "a", 0, 5),
                edge("B", "C", "a", 6, 10)
        );

        StreamingGraph result = spath.apply(
                new Tuple4<>(new IndexPath(), g, new Label("a+"), new Label("p"))
        );

        assertTrue(
                result.getTuples().stream()
                        .noneMatch(t ->
                                t.getRepr().getSource().equals("A") &&
                                        t.getRepr().getTarget().equals("C")
                        )
        );
    }

    @Test
    void atMostOnePathPerVertexStateIsMaintained() {
        BatchSPath spath = new BatchSPath();

        StreamingGraph g = new StreamingGraph();
        g.add(new StreamingGraphTuple(new Edge("A","B",new Label("a"),0,10)));
        g.add(new StreamingGraphTuple(new Edge("B","C",new Label("a"),0,10)));
        g.add(new StreamingGraphTuple(new Edge("A","D",new Label("a"),0,10)));
        g.add(new StreamingGraphTuple(new Edge("D","C",new Label("a"),0,10)));

        StreamingGraph result = spath.apply(
                new Tuple4<>(new IndexPath(), g, new Label("a+"), new Label("p"))
        );

        long countAC =
                result.getTuples().stream()
                        .filter(t -> t.getRepr().getSource().equals("A")
                                && t.getRepr().getTarget().equals("C"))
                        .count();

        assertEquals(1, countAC);
    }

    @Test
    void cyclesAreHandledUnderArbitraryPathSemantics() {
        BatchSPath spath = new BatchSPath();

        StreamingGraph g = new StreamingGraph();
        g.add(new StreamingGraphTuple(
                new Edge("A","B",new Label("a"),
                        0,10)
        ));
        g.add(new StreamingGraphTuple(
                new Edge("B","A",new Label("a"),
                        0,10)
        ));

        StreamingGraph result = spath.apply(
                new Tuple4<>(new IndexPath(), g, new Label("a+"), new Label("p"))
        );

        assertFalse(result.getTuples().isEmpty());
    }*/
}