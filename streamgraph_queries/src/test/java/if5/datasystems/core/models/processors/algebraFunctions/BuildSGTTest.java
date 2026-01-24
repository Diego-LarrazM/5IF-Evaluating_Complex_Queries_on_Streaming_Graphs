package if5.datasystems.core.models.processors.algebraFunctions;

import if5.datasystems.core.models.aliases.Label;
import if5.datasystems.core.models.streamingGraph.Edge;
import if5.datasystems.core.models.streamingGraph.StreamingGraphTuple;
import if5.datasystems.core.processors.AlgebraFunctions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BuildSGTTest {

    @SuppressWarnings("unchecked")
    private StreamingGraphTuple invokeBuildSGT(
            ArrayList<Edge> edges, Label label) throws Exception {

        Method m = AlgebraFunctions.class.getDeclaredMethod(
                "buildSGT", ArrayList.class, Label.class);

        m.setAccessible(true);
        return (StreamingGraphTuple) m.invoke(null, edges, label);
    }

    @Test
    void testBuildSGTWithEmptyEdges() throws Exception {
        ArrayList<Edge> edges = new ArrayList<>();
        Label out = new Label("OUT");

        StreamingGraphTuple sgt = invokeBuildSGT(edges, out);

        assertNotNull(sgt);
        assertTrue(sgt.getContent().isEmpty());
        assertNull(sgt.getRepr());
    }

    @Test
    void testBuildSGTSingleEdge() throws Exception {
        Instant start = Instant.now().minusSeconds(10);
        Instant end = Instant.now().plusSeconds(10);

        Edge e = new Edge("A", "B", new Label("L1"), start, end);
        ArrayList<Edge> edges = new ArrayList<>(List.of(e));

        Label out = new Label("OUT");

        StreamingGraphTuple sgt = invokeBuildSGT(edges, out);

        assertEquals(1, sgt.getContent().size());
        assertSame(e, sgt.getContent().get(0));

        Edge repr = sgt.getRepr();
        assertNotNull(repr);

        assertEquals("A", repr.getSource());
        assertEquals("B", repr.getTarget());
        assertEquals(out, repr.getLabel());
        assertEquals(start, repr.getStartTime());
        assertEquals(end, repr.getExpiricy());
    }

    @Test
    void testBuildSGTMultipleEdges() throws Exception {
        Instant t1 = Instant.now().minusSeconds(30);
        Instant t2 = Instant.now().minusSeconds(20);
        Instant t3 = Instant.now().plusSeconds(20);

        Edge e1 = new Edge("A", "B", new Label("L1"), t1, t3);
        Edge e2 = new Edge("B", "C", new Label("L2"), t2, t3);
        Edge e3 = new Edge("C", "D", new Label("L3"), t2, t3);

        ArrayList<Edge> edges = new ArrayList<>(List.of(e1, e2, e3));
        Label out = new Label("OUT");

        StreamingGraphTuple sgt = invokeBuildSGT(edges, out);

        // content
        assertEquals(3, sgt.getContent().size());
        assertEquals(edges, sgt.getContent());

        // repr
        Edge repr = sgt.getRepr();
        assertNotNull(repr);

        assertEquals("A", repr.getSource());   // first edge source
        assertEquals("D", repr.getTarget());   // last edge target
        assertEquals(out, repr.getLabel());

        // from first edge
        assertEquals(t1, repr.getStartTime());
        assertEquals(t3, repr.getExpiricy());
    }

    @Test
    void testBuildSGTDoesNotAliasEdgeList() throws Exception {
        Edge e = new Edge("A", "B", new Label("L"), Instant.now(), Instant.now());
        ArrayList<Edge> edges = new ArrayList<>(List.of(e));

        StreamingGraphTuple sgt =
                invokeBuildSGT(edges, new Label("OUT"));

        edges.clear();

        assertEquals(1, sgt.getContent().size());
    }
}

