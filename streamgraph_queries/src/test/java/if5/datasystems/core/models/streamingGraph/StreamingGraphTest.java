package if5.datasystems.core.models.streamingGraph;

import org.junit.jupiter.api.Test;

import java.sql.Time;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

import if5.datasystems.core.models.aliases.Label;

import static org.junit.jupiter.api.Assertions.*;

// Tests pour StreamingGraph : add(), coalescence via equals/hashcode sur StreamingGraphTuple
public class StreamingGraphTest {

    @Test
    void testAddNullDoesNothing() {
        StreamingGraph sg = new StreamingGraph();
        assertTrue(sg.getTuples().isEmpty());

        sg.add(null);
        assertTrue(sg.getTuples().isEmpty());
    }

    @Test
    void testUpdateStreamingGraphCoalescesTuples() {
        Label lbl = new Label("co");
        Edge repr = new Edge("P", "Q", lbl,
                Instant.parse("2026-01-01T03:00:00Z"), Instant.parse("2026-01-02T03:00:00Z"));

        StreamingGraphTuple t1 = new StreamingGraphTuple();
        t1.setRepr(repr);
        t1.add(repr);

        StreamingGraphTuple t2 = new StreamingGraphTuple();
        t2.setRepr(repr);
        t2.add(repr);

        StreamingGraph sg = new StreamingGraph();
        sg.updateStreamingGraph(t1);
        sg.updateStreamingGraph(t2);

        assertEquals(1, sg.getTuples().size());
    }

    @Test
    void testConstructorWithSet() {
        //Set<StreamingGraphTuple> initial = new HashSet<>();
        //StreamingGraph sg = new StreamingGraph(initial);
        //assertSame(initial, sg.getTuples());
    }
}
