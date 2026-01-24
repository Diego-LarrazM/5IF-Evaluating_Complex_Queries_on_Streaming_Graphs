package if5.datasystems.core.models.processors;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;

import if5.datasystems.core.processors.SPath;
import org.junit.jupiter.api.Test;

import if5.datasystems.core.models.aliases.Label;
import if5.datasystems.core.models.aliases.Triple;
import if5.datasystems.core.models.streamingGraph.Edge;
import if5.datasystems.core.models.streamingGraph.StreamingGraph;
import if5.datasystems.core.models.streamingGraph.StreamingGraphTuple;

class SPathTest {

    private Edge edge(
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

    private StreamingGraph graph(Edge... edges) {
        StreamingGraph g = new StreamingGraph();
        for (Edge e : edges) {
            g.add(new StreamingGraphTuple(e));
        }
        return g;
    }

    @Test
    void emptyGraph_returnsEmptyResult() {
        SPath spath = new SPath();
        StreamingGraph g = new StreamingGraph();

        StreamingGraph result = spath.apply(
                new Triple<>(g, new Label("a"), new Label("out"))
        );

        assertTrue(result.getTuples().isEmpty());
    }

    @Test
    void singleEdge_matchingLabel_producesPath() {
        SPath spath = new SPath();
        StreamingGraph g = graph(
                edge("A", "B", "a", 0, 10)
        );

        StreamingGraph result = spath.apply(
                new Triple<>(g, new Label("a"), new Label("p"))
        );

        assertEquals(1, result.getTuples().size());

        StreamingGraphTuple t = result.getTuples().getFirst();
        assertEquals("A", t.getRepr().getSource());
        assertEquals("B", t.getRepr().getTarget());
        assertEquals(new Label("p"), t.getRepr().getLabel());
    }

    @Test
    void singleEdge_wrongLabel_producesNothing() {
        SPath spath = new SPath();
        StreamingGraph g = graph(
                edge("A", "B", "b", 0, 10)
        );

        StreamingGraph result = spath.apply(
                new Triple<>(g, new Label("a"), new Label("p"))
        );

        assertTrue(result.getTuples().isEmpty());
    }

    @Test
    void twoEdgePath_isCollapsedIntoLongestFinalPath_withPlusAutomaton() {
        SPath spath = new SPath();

        StreamingGraph g = graph(
                edge("A", "B", "a", 0, 10),
                edge("B", "C", "a", 0, 10)
        );

        StreamingGraph result = spath.apply(
                new Triple<>(g, new Label("a+"), new Label("p"))
        );

        assertEquals(1, result.getTuples().size());

        StreamingGraphTuple t = result.getTuples().getFirst();

        assertEquals("A", t.getRepr().getSource());
        assertEquals("C", t.getRepr().getTarget());
        assertEquals(new Label("p"), t.getRepr().getLabel());

        assertEquals(0, t.getStartTime_ms());
        assertEquals(10, t.getExpiricy_ms());
    }

    @Test
    void expiredEdges_doNotFormPath_dueToSnapshot() {
        SPath spath = new SPath();
        StreamingGraph g = graph(
                edge("A", "B", "a", 0, 5),
                edge("B", "C", "a", 6, 10)
        );

        StreamingGraph result = spath.apply(
                new Triple<>(g, new Label("a+"), new Label("p"))
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
    void pathExistsOnlyOnIntervalIntersection() {
        SPath spath = new SPath();

        StreamingGraph g = new StreamingGraph();
        g.add(new StreamingGraphTuple(
                new Edge("A","B",new Label("a"),
                        Instant.ofEpochMilli(0),
                        Instant.ofEpochMilli(20))
        ));
        g.add(new StreamingGraphTuple(
                new Edge("B","C",new Label("a"),
                        Instant.ofEpochMilli(0),
                        Instant.ofEpochMilli(10))
        ));

        StreamingGraph result = spath.apply(
                new Triple<>(g, new Label("a+"), new Label("p"))
        );

        assertEquals(1, result.getTuples().size());

        StreamingGraphTuple ac = result.getTuples().getFirst();

        assertEquals("A", ac.getRepr().getSource());
        assertEquals("C", ac.getRepr().getTarget());
        assertEquals(0, ac.getStartTime_ms());
        assertEquals(10, ac.getExpiricy_ms());
    }

    @Test
    void atMostOnePathPerVertexStateIsMaintained() {
        SPath spath = new SPath();

        StreamingGraph g = new StreamingGraph();
        g.add(new StreamingGraphTuple(new Edge("A","B",new Label("a"),
                Instant.ofEpochMilli(0),Instant.ofEpochMilli(10))));
        g.add(new StreamingGraphTuple(new Edge("B","C",new Label("a"),
                Instant.ofEpochMilli(0),Instant.ofEpochMilli(10))));
        g.add(new StreamingGraphTuple(new Edge("A","D",new Label("a"),
                Instant.ofEpochMilli(0),Instant.ofEpochMilli(10))));
        g.add(new StreamingGraphTuple(new Edge("D","C",new Label("a"),
                Instant.ofEpochMilli(0),Instant.ofEpochMilli(10))));

        StreamingGraph result = spath.apply(
                new Triple<>(g, new Label("a+"), new Label("p"))
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
        SPath spath = new SPath();

        StreamingGraph g = new StreamingGraph();
        g.add(new StreamingGraphTuple(
                new Edge("A","B",new Label("a"),
                        Instant.ofEpochMilli(0),Instant.ofEpochMilli(10))
        ));
        g.add(new StreamingGraphTuple(
                new Edge("B","A",new Label("a"),
                        Instant.ofEpochMilli(0),Instant.ofEpochMilli(10))
        ));

        StreamingGraph result = spath.apply(
                new Triple<>(g, new Label("a+"), new Label("p"))
        );

        assertFalse(result.getTuples().isEmpty());
    }

    @Test
    void pathWithMaximumExpiryIsSelected() {
        SPath spath = new SPath();

        StreamingGraph g = new StreamingGraph();
        g.add(new StreamingGraphTuple(
                new Edge("A","B",new Label("a"),
                        Instant.ofEpochMilli(0),
                        Instant.ofEpochMilli(5))
        ));
        g.add(new StreamingGraphTuple(
                new Edge("A","C",new Label("a"),
                        Instant.ofEpochMilli(0),
                        Instant.ofEpochMilli(10))
        ));

        StreamingGraph result = spath.apply(
                new Triple<>(g, new Label("a"), new Label("p"))
        );

        assertEquals(1, result.getTuples().size());

        StreamingGraphTuple t = result.getTuples().getFirst();
        assertEquals("A", t.getRepr().getSource());
        assertEquals("C", t.getRepr().getTarget());
        assertEquals(10, t.getExpiricy_ms());
    }

}