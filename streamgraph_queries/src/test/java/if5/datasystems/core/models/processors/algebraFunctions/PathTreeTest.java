package if5.datasystems.core.models.processors.algebraFunctions;

import if5.datasystems.core.models.aliases.Label;
import if5.datasystems.core.models.aliases.Pair;
import if5.datasystems.core.models.aliases.State;
import if5.datasystems.core.models.queries.IndexNode;
import if5.datasystems.core.models.queries.SpanningTree;
import if5.datasystems.core.models.streamingGraph.Edge;
import if5.datasystems.core.models.streamingGraph.StreamingGraph;
import if5.datasystems.core.models.streamingGraph.StreamingGraphTuple;
import if5.datasystems.core.processors.AlgebraFunctions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PathTreeTest {

    @Test
    void testPathTreeReturnsEmptyGraphWhenTargetDoesNotExist() {
        State state = new State("S");
        IndexNode root = new IndexNode("root", state);
        SpanningTree tree = new SpanningTree(root);

        StreamingGraph result =
                AlgebraFunctions.PathTree(
                        tree,
                        new Pair<>("missing", state),
                        new Label("OUT"));

        assertNotNull(result);
        assertTrue(result.getTuples().isEmpty());
    }

    @Test
    void testPathTreeSingleHop() {
        State state = new State("S");
        Instant start = Instant.now().minusSeconds(10);
        Instant end = Instant.now().plusSeconds(10);

        IndexNode root = new IndexNode("A", state);
        SpanningTree tree = new SpanningTree(root);

        IndexNode child = new IndexNode("B", state, start, end);
        child.setParent(new Pair<>("A", state));
        child.setFromLabel(new Label("L1"));

        tree.addNode(child);

        StreamingGraph result =
                AlgebraFunctions.PathTree(
                        tree,
                        new Pair<>("B", state),
                        new Label("OUT"));

        assertEquals(1, result.getTuples().size());

        StreamingGraphTuple tuple = result.getTuples().getFirst();
        List<Edge> edges = tuple.getContent();

        assertEquals(1, edges.size());

        Edge e = edges.get(0);
        assertEquals("A", e.getSource());
        assertEquals("B", e.getTarget());
        assertEquals(new Label("L1"), e.getLabel());
        assertEquals(start, e.getStartTime());
        assertEquals(end, e.getExpiricy());
    }

    @Test
    void testPathTreeMultiHopPath() {
        State state = new State("S");
        Instant t1 = Instant.now().minusSeconds(30);
        Instant t2 = Instant.now().minusSeconds(20);
        Instant t3 = Instant.now().plusSeconds(20);

        IndexNode root = new IndexNode("A", state);
        SpanningTree tree = new SpanningTree(root);

        IndexNode n1 = new IndexNode("B", state, t1, t3);
        n1.setParent(new Pair<>("A", state));
        n1.setFromLabel(new Label("L1"));

        IndexNode n2 = new IndexNode("C", state, t2, t3);
        n2.setParent(new Pair<>("B", state));
        n2.setFromLabel(new Label("L2"));

        tree.addNode(n1);
        tree.addNode(n2);

        StreamingGraph result =
                AlgebraFunctions.PathTree(
                        tree,
                        new Pair<>("C", state),
                        new Label("OUT"));

        assertEquals(1, result.getTuples().size());

        StreamingGraphTuple tuple = result.getTuples().getFirst();
        List<Edge> edges = tuple.getContent();

        assertEquals(2, edges.size());

        // Order with child -> parent traversal
        Edge e1 = edges.get(0);
        Edge e2 = edges.get(1);

        assertEquals("B", e1.getSource());
        assertEquals("C", e1.getTarget());
        assertEquals(new Label("L2"), e1.getLabel());

        assertEquals("A", e2.getSource());
        assertEquals("B", e2.getTarget());
        assertEquals(new Label("L1"), e2.getLabel());
    }

    @Test
    void testPathTreeTargetIsRootProducesEmptyPathTuple() {
        State state = new State("S");
        IndexNode root = new IndexNode("A", state);
        SpanningTree tree = new SpanningTree(root);

        StreamingGraph result =
                AlgebraFunctions.PathTree(
                        tree,
                        new Pair<>("A", state),
                        new Label("OUT"));

        assertEquals(1, result.getTuples().size());

        StreamingGraphTuple tuple = result.getTuples().getFirst();
        assertTrue(tuple.getContent().isEmpty());
    }
}
