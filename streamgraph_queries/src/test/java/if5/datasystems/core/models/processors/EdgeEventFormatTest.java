package if5.datasystems.core.models.processors;

import if5.datasystems.core.models.aliases.Label;
import if5.datasystems.core.models.streamingGraph.Edge;
import if5.datasystems.core.processors.EdgeEventFormat;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EdgeEventFormatTest {

    @Test
    void testValidEdgeEventParsing() {
        long ts = System.currentTimeMillis();
        String input = "A;B;L1;" + ts;

        EdgeEventFormat event = new EdgeEventFormat(input);

        assertNotNull(event);
        assertEquals(ts, event.timestamp);

        Edge edge = event.edge;
        assertNotNull(edge);

        assertEquals("A", edge.getSource());
        assertEquals("B", edge.getTarget());
        assertEquals(new Label("L1"), edge.getLabel());
        assertEquals(ts, edge.getStartTime());
    }

    @Test
    void testParsingWithExtraSpaces() {
        long ts = 1_700_000_000_000L;
        String input = "  A  ;  B ;  LABEL_X  ;  " + ts + " ";

        EdgeEventFormat event = new EdgeEventFormat(input);

        Edge edge = event.edge;

        assertEquals("A", edge.getSource());
        assertEquals("B", edge.getTarget());
        assertEquals(new Label("LABEL_X"), edge.getLabel());
        assertEquals(ts, event.timestamp);
        assertEquals(ts, edge.getStartTime());
    }

    @Test
    void testTimestampConsistencyBetweenEventAndEdge() {
        long ts = 42L;
        String input = "X;Y;L;" + ts;

        EdgeEventFormat event = new EdgeEventFormat(input);

        assertEquals(
                event.timestamp,
                event.edge.getStartTime()
        );
    }

    @Test
    void testEdgeHasNoExpirySet() {
        String input = "A;B;L;1000";

        EdgeEventFormat event = new EdgeEventFormat(input);

        assertEquals(0L, event.edge.getExpiricy());
    }

    @Test
    void testLabelValueEquality() {
        String input = "A;B;L;1000";

        EdgeEventFormat event = new EdgeEventFormat(input);

        assertEquals(
                new Label("L"),
                event.edge.getLabel()
        );
    }
}
