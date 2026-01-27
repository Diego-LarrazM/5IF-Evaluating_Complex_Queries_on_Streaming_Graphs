package if5.datasystems.core.models.streamingGraph;

import org.junit.jupiter.api.Test;

import java.sql.Time;
import java.time.Instant;

import if5.datasystems.core.models.aliases.Label;

import static org.junit.jupiter.api.Assertions.*;

// Tests pour StreamingGraphTuple : add(), content et equals/hashCode via repr
public class StreamingGraphTupleTest {

    @Test
    void testAddAndContentSize() {
        Label lbl = new Label("edge");
        Edge e1 = new Edge("A", "B", lbl, 3600000L, 0);
        Edge e2 = new Edge("A", "C", lbl, 3630000L, 0);

        StreamingGraphTuple tuple = new StreamingGraphTuple();
        tuple.setRepr(e1); // repr must être défini pour equals/hashCode safely
        assertTrue(tuple.getContent().isEmpty());

        tuple.add(e1);
        tuple.add(e2);

        assertEquals(2, tuple.getContent().size());
        assertTrue(tuple.getContent().contains(e1));
        assertTrue(tuple.getContent().contains(e2));
    }

    @Test
    void testEqualsBasedOnRepr() {
        Label lbl = new Label("same");
        Edge repr = new Edge("N", "M", lbl, 2*3600000L, 0);

        StreamingGraphTuple t1 = new StreamingGraphTuple();
        t1.setRepr(repr);
        t1.add(repr);

        StreamingGraphTuple t2 = new StreamingGraphTuple();
        t2.setRepr(repr); // same repr => should be equal
        t2.add(repr);

        assertEquals(t1, t2);
        assertEquals(t1.hashCode(), t2.hashCode());
    }
}
