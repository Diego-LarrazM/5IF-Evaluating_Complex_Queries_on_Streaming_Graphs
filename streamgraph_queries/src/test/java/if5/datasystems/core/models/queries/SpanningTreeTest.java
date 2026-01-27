package if5.datasystems.core.models.queries;

import if5.datasystems.core.models.aliases.Pair;
import if5.datasystems.core.models.aliases.State;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpanningTreeTest {

    @Test
    void testConstructorSetsRootAndAddsItToTree() {
        State state = new State("ROOT");
        IndexNode root = new IndexNode("root", state);

        SpanningTree tree = new SpanningTree(root);

        assertEquals(root, tree.getRoot());
        assertTrue(tree.contains("root", state));
        assertEquals(root, tree.getNode("root", state));
    }

    @Test
    void testAddNodeAddsNodeToTree() {
        State state = new State("S");
        IndexNode root = new IndexNode("root", state);
        SpanningTree tree = new SpanningTree(root);

        IndexNode node = new IndexNode("n1", state);
        tree.addNode(node);

        assertTrue(tree.contains("n1", state));
        assertEquals(node, tree.getNode("n1", state));
    }

    @Test
    void testAddNodeWithNullDoesNothing() {
        State state = new State("S");
        IndexNode root = new IndexNode("root", state);
        SpanningTree tree = new SpanningTree(root);

        int sizeBefore = tree.getNodes().size();

        tree.addNode(null);

        assertEquals(sizeBefore, tree.getNodes().size());
    }

    @Test
    void testGetNodeByNameAndState() {
        State state = new State("S");
        IndexNode root = new IndexNode("root", state);
        SpanningTree tree = new SpanningTree(root);

        IndexNode node = new IndexNode("n1", state);
        tree.addNode(node);

        IndexNode result = tree.getNode("n1", state);

        assertEquals(node, result);
    }

    @Test
    void testGetNodeByPair() {
        State state = new State("S");
        IndexNode root = new IndexNode("root", state);
        SpanningTree tree = new SpanningTree(root);

        IndexNode node = new IndexNode("n1", state);
        tree.addNode(node);

        Pair<String, State> key =
                new Pair<>("n1", state);

        IndexNode result = tree.getNode(key);

        assertEquals(node, result);
    }

    @Test
    void testGetNodeByIndexNode() {
        State state = new State("S");
        IndexNode root = new IndexNode("root", state);
        SpanningTree tree = new SpanningTree(root);

        IndexNode node = new IndexNode("n1", state);
        tree.addNode(node);

        IndexNode result = tree.getNode(node);

        assertEquals(node, result);
    }

    @Test
    void testGetNodeByNameAndStateWithNullReturnsNull() {
        SpanningTree tree =
                new SpanningTree(new IndexNode("root", new State("S")));

        assertNull(tree.getNode(null, new State("S")));
        assertNull(tree.getNode("root", null));
    }

    @Test
    void testGetNodeByPairWithNullReturnsNull() {
        SpanningTree tree =
                new SpanningTree(new IndexNode("root", new State("S")));

        assertNull(tree.getNode((Pair<String, State>) null));
    }

    @Test
    void testGetNodeByIndexNodeWithNullReturnsNull() {
        SpanningTree tree =
                new SpanningTree(new IndexNode("root", new State("S")));

        assertNull(tree.getNode((IndexNode) null));
    }

    @Test
    void testContainsReturnsTrueWhenNodeExists() {
        State state = new State("S");
        SpanningTree tree =
                new SpanningTree(new IndexNode("root", state));

        assertTrue(tree.contains("root", state));
    }

    @Test
    void testContainsReturnsFalseWhenNodeDoesNotExist() {
        State state = new State("S");
        SpanningTree tree =
                new SpanningTree(new IndexNode("root", state));

        assertFalse(tree.contains("unknown", state));
    }

    @Test
    void testAddNodeOverridesNodeWithSameKey() {
        State state = new State("S");
        SpanningTree tree =
                new SpanningTree(new IndexNode("root", state));

        IndexNode n1 = new IndexNode("n", state);
        IndexNode n2 = new IndexNode("n", state);

        tree.addNode(n1);
        tree.addNode(n2);

        assertEquals(n2, tree.getNode("n", state));
        assertEquals(2, tree.getNodes().size());
    }
}
