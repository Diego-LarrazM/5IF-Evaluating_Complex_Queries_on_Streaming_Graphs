package if5.datasystems.core.models.queries;

import if5.datasystems.core.models.aliases.Label;
import if5.datasystems.core.models.aliases.Pair;
import if5.datasystems.core.models.aliases.State;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class IndexNodeTest {

    @Test
    void testMinimalConstructor() {
        State state = new State("ACTIVE");

        IndexNode node = new IndexNode("node1", state);

        assertEquals("node1", node.getName());
        assertEquals(state, node.getState());
        assertNull(node.getStartTime());
        assertNull(node.getExpiricy());
        assertNull(node.getParent());
        assertNull(node.getFromLabel());
    }

    @Test
    void testFullConstructor() {
        State state = new State("ACTIVE");
        Instant start = Instant.now();
        Instant end = start.plusSeconds(60);

        IndexNode node = new IndexNode("node1", state, start, end);

        assertEquals("node1", node.getName());
        assertEquals(state, node.getState());
        assertEquals(start, node.getStartTime());
        assertEquals(end, node.getExpiricy());
    }

    @Test
    void testSettersAndGetters() {
        IndexNode node = new IndexNode("node1", new State("S1"));

        Pair<String, State> parent =
                new Pair<>("parentNode", new State("PARENT"));

        Label label = new Label("fromA");

        node.setParent(parent);
        node.setFromLabel(label);

        assertEquals(parent, node.getParent());
        assertEquals(label, node.getFromLabel());
    }

    @Test
    void testEqualsSameNameAndState() {
        State state = new State("ACTIVE");

        IndexNode n1 = new IndexNode("node1", state);
        IndexNode n2 = new IndexNode("node1", new State("ACTIVE"));

        assertEquals(n1, n2);
        assertEquals(n1.hashCode(), n2.hashCode());
    }

    @Test
    void testNotEqualsDifferentName() {
        State state = new State("ACTIVE");

        IndexNode n1 = new IndexNode("node1", state);
        IndexNode n2 = new IndexNode("node2", state);

        assertNotEquals(n1, n2);
    }

    @Test
    void testNotEqualsDifferentState() {
        IndexNode n1 = new IndexNode("node1", new State("ACTIVE"));
        IndexNode n2 = new IndexNode("node1", new State("INACTIVE"));

        assertNotEquals(n1, n2);
    }

    @Test
    void testEqualsIgnoresTimestamps() {
        State state = new State("ACTIVE");

        IndexNode n1 =
                new IndexNode("node1", state,
                        Instant.now(),
                        Instant.now().plusSeconds(10));

        IndexNode n2 =
                new IndexNode("node1", state,
                        Instant.now().minusSeconds(100),
                        Instant.now().plusSeconds(500));

        assertEquals(n1, n2);
    }

    @Test
    void testEqualsIgnoresOtherFields() {
        State state = new State("ACTIVE");

        IndexNode n1 = new IndexNode("node1", state);
        n1.setFromLabel(new Label("A"));
        n1.setParent(new Pair<>("p1", new State("P1")));

        IndexNode n2 = new IndexNode("node1", state);
        n2.setFromLabel(new Label("B"));
        n2.setParent(new Pair<>("p2", new State("P2")));

        assertEquals(n1, n2);
    }

    @Test
    void testEqualsWithSameReference() {
        IndexNode node = new IndexNode("node1", new State("ACTIVE"));

        assertEquals(node, node);
    }

    @Test
    void testEqualsWithNull() {
        IndexNode node = new IndexNode("node1", new State("ACTIVE"));

        assertNotEquals(null, node);
    }

    @Test
    void testEqualsWithDifferentType() {
        IndexNode node = new IndexNode("node1", new State("ACTIVE"));

        assertNotEquals("node1", node);
    }
}
