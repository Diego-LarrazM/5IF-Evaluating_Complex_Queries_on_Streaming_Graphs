package if5.datasystems.core.models.processors;

import if5.datasystems.core.processors.TimeOps;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;

class TimeOpsTest {

    @Test
    void testMaxTimeBothNonNull() {
        long t1 = 10*3600000L;
        long t2 = 12*3600000L;

        assertEquals(t2, TimeOps.maxTime(t1, t2));
        assertEquals(t2, TimeOps.maxTime(t2, t1));
    }

    @Test
    void testMinTimeBothNonNull() {
        long t1 = 10*3600000L;
        long t2 = 12*3600000L;

        assertEquals(t1, TimeOps.minTime(t1, t2));
        assertEquals(t1, TimeOps.minTime(t2, t1));
    }

    @Test
    void testMaxTimeWithNull() {
        long t = Instant.now().toEpochMilli();

        assertEquals(t, TimeOps.maxTime(-1, t));
        assertEquals(t, TimeOps.maxTime(t, -1));
    }

    @Test
    void testMinTimeWithNull() {
        long t = Instant.now().toEpochMilli();

        assertEquals(t, TimeOps.minTime(-1, t));
        assertEquals(t, TimeOps.minTime(t, -1));
    }


    @Test
    void testEquallongs() {
        long t = Instant.parse("2024-01-01T10:00:00Z").toEpochMilli();

        assertEquals(t, TimeOps.maxTime(t, t));
        assertEquals(t, TimeOps.minTime(t, t));
    }
}
