package if5.datasystems.core.models.streamingGraph;

import org.junit.jupiter.api.Test;

import java.sql.Time;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import if5.datasystems.core.models.aliases.Label;

import static org.junit.jupiter.api.Assertions.*;

// Tests simples pour SnapshotGraph : champs edges et timestamp
public class SnapshotGraphTest {

    @Test
    void testSetAndGetEdgesAndTimestamp() {
        Label lbl = new Label("snap");
        Edge e = new Edge("S1", "T1", lbl, Instant.parse("2026-01-01T05:00:00Z"), null);

        List<Edge> edges = new ArrayList<>();
        edges.add(e);

        SnapshotGraph snap = new SnapshotGraph();
        snap.setEdges(edges);
        snap.setTimestamp(Instant.parse("2026-01-01T05:00:00Z"));

        assertEquals(1, snap.getEdges().size());
        assertEquals(e, snap.getEdges().get(0));
        assertEquals(Instant.parse("2026-01-01T05:00:00Z"), snap.getTimestamp());
    }
}
