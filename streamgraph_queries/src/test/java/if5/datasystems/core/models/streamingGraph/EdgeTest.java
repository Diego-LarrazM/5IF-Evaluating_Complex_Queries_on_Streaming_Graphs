package if5.datasystems.core.models.streamingGraph;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import if5.datasystems.core.models.aliases.Label;

import static org.junit.jupiter.api.Assertions.*;

// Tests for Edge: basic getters/setters and equals/hashCode behavior.
public class EdgeTest {

    @Test
    void testEdgeSettersAndGetters() {
        Label lbl = new Label("knows"); // ...assumes simple Label constructor
        long start = 10*3600000L;
        long end = 11*3600000L;

        Edge e = new Edge();
        e.setSource("A");
        e.setTarget("B");
        e.setLabel(lbl);
        e.setStartTime(start);
        e.setExpiricy(end);

        assertEquals("A", e.getSource());
        assertEquals("B", e.getTarget());
        assertEquals(lbl, e.getLabel());
        assertEquals(start, e.getStartTime());
        assertEquals(end, e.getExpiricy());
    }

    @Test
    void testEqualsAndHashCodeSameValues() {
        Label lbl = new Label("likes");
        Edge e1 = new Edge("X", "Y", lbl, 0L, 600000L);
        Edge e2 = new Edge("X", "Y", lbl, 300000L, 900000L);

        // equals compares source, target and label only
        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
    }

    @Test
    void testNotEqualsDifferentLabel() {
        Label lbl1 = new Label("a");
        Label lbl2 = new Label("b");
        Edge e1 = new Edge("S", "T", lbl1, 0L, 0L);
        Edge e2 = new Edge("S", "T", lbl2, 0L, 0L);

        assertNotEquals(e1, e2);
    }

    @Test
    void testNotEqualsDifferentEndpoints() {
        Label lbl = new Label("r");
        Edge e1 = new Edge("U", "V", lbl, 0L, 0L);
        Edge e2 = new Edge("U", "W", lbl, 0L, 0L);

        assertNotEquals(e1, e2);
    }
}
