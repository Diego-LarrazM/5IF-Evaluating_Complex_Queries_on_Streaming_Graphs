package if5.datasystems.core.models.queries;

import if5.datasystems.core.models.aliases.Pair;
import if5.datasystems.core.models.aliases.State;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class IndexPathTest {

    @Test
    void testConstructorInitializesEmptyPath() {
        IndexPath path = new IndexPath();

        assertNotNull(path.getIndexPath());
        assertTrue(path.getIndexPath().isEmpty());
    }

    @Test
    void testCreateAndRetrieveTree() {
        IndexPath path = new IndexPath();
        IndexNode root = new IndexNode("root", new State("S"));

        path.createTree(root);

        assertTrue(path.contains("root"));
        assertNotNull(path.getTree("root"));
        assertEquals(root, path.getTree("root").getRoot());
    }

    @Test
    void testContainsReturnsFalseForUnknownRoot() {
        IndexPath path = new IndexPath();

        assertFalse(path.contains("unknown"));
    }

    @Test
    void testExpandableTreesReturnsTreeWhenNodeExistsAndTimeIsValid() {
        IndexPath path = new IndexPath();

        State state = new State("ACTIVE");
        IndexNode root = new IndexNode("root", state);

        path.createTree(root);
        SpanningTree tree = path.getTree("root");

        Instant start = Instant.now().minusSeconds(10);
        Instant end = Instant.now().plusSeconds(10);

        IndexNode searchNode =
                new IndexNode("n1", state, start, end);

        tree.addNode(searchNode);

        Pair<String, State> key =
                new Pair<>("n1", state);

        Instant t = Instant.now();

        ArrayList<SpanningTree> result =
                path.expandableTrees(key, t);

        assertEquals(1, result.size());
        assertEquals(tree, result.get(0));
    }

    @Test
    void testExpandableTreesDoesNotReturnTreeWhenTimeIsOutsideInterval() {
        IndexPath path = new IndexPath();

        State state = new State("ACTIVE");
        IndexNode root = new IndexNode("root", state);

        path.createTree(root);
        SpanningTree tree = path.getTree("root");

        Instant start = Instant.now().minusSeconds(20);
        Instant end = Instant.now().minusSeconds(10);

        IndexNode searchNode =
                new IndexNode("n1", state, start, end);

        tree.addNode(searchNode);

        Pair<String, State> key =
                new Pair<>("n1", state);

        Instant t = Instant.now();

        ArrayList<SpanningTree> result =
                path.expandableTrees(key, t);

        assertTrue(result.isEmpty());
    }

    @Test
    void testExpandableTreesMultipleTreesOnlyOneMatches() {
        IndexPath path = new IndexPath();

        State state = new State("ACTIVE");

        // Tree 1
        IndexNode root1 = new IndexNode("root1", state);
        path.createTree(root1);
        SpanningTree t1 = path.getTree("root1");

        // Tree 2
        IndexNode root2 = new IndexNode("root2", state);
        path.createTree(root2);
        SpanningTree t2 = path.getTree("root2");

        Instant now = Instant.now();

        IndexNode validNode =
                new IndexNode("n", state,
                        now.minusSeconds(5),
                        now.plusSeconds(5));

        IndexNode expiredNode =
                new IndexNode("n", state,
                        now.minusSeconds(20),
                        now.minusSeconds(10));

        t1.addNode(validNode);
        t2.addNode(expiredNode);

        Pair<String, State> key =
                new Pair<>("n", state);

        ArrayList<SpanningTree> result =
                path.expandableTrees(key, now);

        assertEquals(1, result.size());
        assertEquals(t1, result.get(0));
    }

    @Test
    void testExpandableTreesReturnsEmptyWhenNoTreeContainsNode() {
        IndexPath path = new IndexPath();

        State state = new State("ACTIVE");
        IndexNode root = new IndexNode("root", state);

        path.createTree(root);

        Pair<String, State> missingKey =
                new Pair<>("missing", state);

        ArrayList<SpanningTree> result =
                path.expandableTrees(missingKey, Instant.now());

        assertTrue(result.isEmpty());
    }
}
