package if5.datasystems.core.models.processors.algebraFunctions;

import if5.datasystems.core.models.aliases.Label;
import if5.datasystems.core.models.streamingGraph.Edge;
import if5.datasystems.core.models.streamingGraph.StreamingGraph;
import if5.datasystems.core.models.streamingGraph.StreamingGraphTuple;
import if5.datasystems.core.processors.AlgebraFunctions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;

class SnapshotTest {

    @Test
    void testSnapshotReturnsEmptyGraphWhenInputIsEmpty() {
        StreamingGraph graph = new StreamingGraph();
        long snapTime = Instant.now().toEpochMilli();

        StreamingGraph result = AlgebraFunctions.Snapshot(graph, snapTime);

        assertNotNull(result);
        assertTrue(result.getTuples().isEmpty());
    }

    @Test
    void testSnapshotKeepsTupleWhenSnapTimeInsideInterval() {
        StreamingGraph graph = new StreamingGraph();

        long start = Instant.now().minusSeconds(10).toEpochMilli();
        long end = Instant.now().plusSeconds(10).toEpochMilli();
        long snapTime = Instant.now().toEpochMilli();

        Edge repr = new Edge("A", "B", new Label("L"), start, end);
        StreamingGraphTuple tuple = new StreamingGraphTuple(repr);

        graph.add(tuple);

        StreamingGraph result = AlgebraFunctions.Snapshot(graph, snapTime);

        assertEquals(1, result.getTuples().size());
        assertSame(tuple, result.getTuples().getFirst());
    }

    @Test
    void testSnapshotExcludesTupleBeforestartTime() {
        StreamingGraph graph = new StreamingGraph();

        long start = Instant.now().plusSeconds(5).toEpochMilli();
        long end = Instant.now().plusSeconds(20).toEpochMilli();
        long snapTime = Instant.now().toEpochMilli();

        Edge repr = new Edge("A", "B", new Label("L"), start, end);
        StreamingGraphTuple tuple = new StreamingGraphTuple(repr);

        graph.add(tuple);

        StreamingGraph result = AlgebraFunctions.Snapshot(graph, snapTime);

        assertTrue(result.getTuples().isEmpty());
    }

    @Test
    void testSnapshotExcludesTupleAfterExpiryTime() {
        StreamingGraph graph = new StreamingGraph();

        long start = Instant.now().minusSeconds(20).toEpochMilli();
        long end = Instant.now().minusSeconds(5).toEpochMilli();
        long snapTime = Instant.now().toEpochMilli();

        Edge repr = new Edge("A", "B", new Label("L"), start, end);
        StreamingGraphTuple tuple = new StreamingGraphTuple(repr);

        graph.add(tuple);

        StreamingGraph result = AlgebraFunctions.Snapshot(graph, snapTime);

        assertTrue(result.getTuples().isEmpty());
    }

    @Test
    void testSnapshotStartInclusiveEndExclusive() {
        StreamingGraph graph = new StreamingGraph();

        long start = Instant.now().toEpochMilli();
        long end = Instant.now().plusSeconds(10).toEpochMilli();

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
                                snapTime.minusSeconds(10).toEpochMilli(),
                                snapTime.plusSeconds(10).toEpochMilli()));

        StreamingGraphTuple expired =
                new StreamingGraphTuple(
                        new Edge("B", "C", new Label("L2"),
                                snapTime.minusSeconds(20).toEpochMilli(),
                                snapTime.minusSeconds(5).toEpochMilli()));

        StreamingGraphTuple future =
                new StreamingGraphTuple(
                        new Edge("C", "D", new Label("L3"),
                                snapTime.plusSeconds(5).toEpochMilli(),
                                snapTime.plusSeconds(20).toEpochMilli()));

        graph.add(valid);
        graph.add(expired);
        graph.add(future);

        StreamingGraph result = AlgebraFunctions.Snapshot(graph, snapTime.toEpochMilli());

        assertEquals(1, result.getTuples().size());
        assertSame(valid, result.getTuples().getFirst());
    }
}
