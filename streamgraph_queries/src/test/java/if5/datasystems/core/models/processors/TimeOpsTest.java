package if5.datasystems.core.models.processors;

import if5.datasystems.core.processors.TimeOps;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class TimeOpsTest {

    @Test
    void testMaxTimeBothNonNull() {
        Instant t1 = Instant.parse("2024-01-01T10:00:00Z");
        Instant t2 = Instant.parse("2024-01-01T12:00:00Z");

        assertEquals(t2, TimeOps.maxTime(t1, t2));
        assertEquals(t2, TimeOps.maxTime(t2, t1));
    }

    @Test
    void testMinTimeBothNonNull() {
        Instant t1 = Instant.parse("2024-01-01T10:00:00Z");
        Instant t2 = Instant.parse("2024-01-01T12:00:00Z");

        assertEquals(t1, TimeOps.minTime(t1, t2));
        assertEquals(t1, TimeOps.minTime(t2, t1));
    }

    @Test
    void testMaxTimeWithNull() {
        Instant t = Instant.now();

        assertEquals(t, TimeOps.maxTime(null, t));
        assertEquals(t, TimeOps.maxTime(t, null));
    }

    @Test
    void testMinTimeWithNull() {
        Instant t = Instant.now();

        assertEquals(t, TimeOps.minTime(null, t));
        assertEquals(t, TimeOps.minTime(t, null));
    }


    @Test
    void testEqualInstants() {
        Instant t = Instant.parse("2024-01-01T10:00:00Z");

        assertEquals(t, TimeOps.maxTime(t, t));
        assertEquals(t, TimeOps.minTime(t, t));
    }
}
