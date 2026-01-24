package if5.datasystems.core.models.processors.algebraFunctions;

import if5.datasystems.core.models.aliases.Label;
import if5.datasystems.core.models.streamingGraph.Edge;
import if5.datasystems.core.models.streamingGraph.StreamingGraph;
import if5.datasystems.core.models.streamingGraph.StreamingGraphTuple;
import if5.datasystems.core.processors.AlgebraFunctions;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class SnapshotTest {

    @Test
    void testSnapshotReturnsEmptyGraphWhenInputIsEmpty() {
        StreamingGraph graph = new StreamingGraph();
        Instant snapTime = Instant.now();

        StreamingGraph result = AlgebraFunctions.Snapshot(graph, snapTime);

        assertNotNull(result);
        assertTrue(result.getTuples().isEmpty());
    }

    @Test
    void testSnapshotKeepsTupleWhenSnapTimeInsideInterval() {
        StreamingGraph graph = new StreamingGraph();

        Instant start = Instant.now().minusSeconds(10);
        Instant end = Instant.now().plusSeconds(10);
        Instant snapTime = Instant.now();

        Edge repr = new Edge("A", "B", new Label("L"), start, end);
        StreamingGraphTuple tuple = new StreamingGraphTuple(repr);

        graph.add(tuple);

        StreamingGraph result = AlgebraFunctions.Snapshot(graph, snapTime);

        assertEquals(1, result.getTuples().size());
        assertSame(tuple, result.getTuples().getFirst());
    }

    @Test
    void testSnapshotExcludesTupleBeforeStartTime() {
        StreamingGraph graph = new StreamingGraph();

        Instant start = Instant.now().plusSeconds(5);
        Instant end = Instant.now().plusSeconds(20);
        Instant snapTime = Instant.now();

        Edge repr = new Edge("A", "B", new Label("L"), start, end);
        StreamingGraphTuple tuple = new StreamingGraphTuple(repr);

        graph.add(tuple);

        StreamingGraph result = AlgebraFunctions.Snapshot(graph, snapTime);

        assertTrue(result.getTuples().isEmpty());
    }

    @Test
    void testSnapshotExcludesTupleAfterExpiryTime() {
        StreamingGraph graph = new StreamingGraph();

        Instant start = Instant.now().minusSeconds(20);
        Instant end = Instant.now().minusSeconds(5);
        Instant snapTime = Instant.now();

        Edge repr = new Edge("A", "B", new Label("L"), start, end);
        StreamingGraphTuple tuple = new StreamingGraphTuple(repr);

        graph.add(tuple);

        StreamingGraph result = AlgebraFunctions.Snapshot(graph, snapTime);

        assertTrue(result.getTuples().isEmpty());
    }

    @Test
    void testSnapshotStartInclusiveEndExclusive() {
        StreamingGraph graph = new StreamingGraph();

        Instant start = Instant.now();
        Instant end = start.plusSeconds(10);

        Edge repr = new Edge("A", "B", new Label("L"), start, end);
        StreamingGraphTuple tuple = new StreamingGraphTuple(repr);
        graph.add(tuple);

        // start <= t -> included
        StreamingGraph atStart = AlgebraFunctions.Snapshot(graph, start);
        assertEquals(1, atStart.getTuples().size());

        // t == expiry -> excluded
        StreamingGraph atEnd = AlgebraFunctions.Snapshot(graph, end);
        assertTrue(atEnd.getTuples().isEmpty());
    }

    @Test
    void testSnapshotFiltersMultipleTuplesCorrectly() {
        StreamingGraph graph = new StreamingGraph();
        Instant snapTime = Instant.now();

        StreamingGraphTuple valid =
                new StreamingGraphTuple(
                        new Edge("A", "B", new Label("L1"),
                                snapTime.minusSeconds(10),
                                snapTime.plusSeconds(10)));

        StreamingGraphTuple expired =
                new StreamingGraphTuple(
                        new Edge("B", "C", new Label("L2"),
                                snapTime.minusSeconds(20),
                                snapTime.minusSeconds(5)));

        StreamingGraphTuple future =
                new StreamingGraphTuple(
                        new Edge("C", "D", new Label("L3"),
                                snapTime.plusSeconds(5),
                                snapTime.plusSeconds(20)));

        graph.add(valid);
        graph.add(expired);
        graph.add(future);

        StreamingGraph result = AlgebraFunctions.Snapshot(graph, snapTime);

        assertEquals(1, result.getTuples().size());
        assertSame(valid, result.getTuples().getFirst());
    }
}
