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
        Instant start = Instant.parse("2026-01-01T10:00:00Z");
        Instant end = Instant.parse("2026-01-01T11:00:00Z");

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
        Edge e1 = new Edge("X", "Y", lbl, Instant.parse("2026-01-01T00:00:00Z"), Instant.parse("2026-01-01T00:10:00Z"));
        Edge e2 = new Edge("X", "Y", lbl, Instant.parse("2026-01-01T00:05:00Z"), Instant.parse("2026-01-01T00:15:00Z"));

        // equals compares source, target and label only
        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
    }

    @Test
    void testNotEqualsDifferentLabel() {
        Label lbl1 = new Label("a");
        Label lbl2 = new Label("b");
        Edge e1 = new Edge("S", "T", lbl1, Instant.parse("2026-01-01T00:00:00Z"), null);
        Edge e2 = new Edge("S", "T", lbl2, Instant.parse("2026-01-01T00:00:00Z"), null);

        assertNotEquals(e1, e2);
    }

    @Test
    void testNotEqualsDifferentEndpoints() {
        Label lbl = new Label("r");
        Edge e1 = new Edge("U", "V", lbl, Instant.parse("2026-01-01T00:00:00Z"), null);
        Edge e2 = new Edge("U", "W", lbl, Instant.parse("2026-01-01T00:00:00Z"), null);

        assertNotEquals(e1, e2);
    }
}
