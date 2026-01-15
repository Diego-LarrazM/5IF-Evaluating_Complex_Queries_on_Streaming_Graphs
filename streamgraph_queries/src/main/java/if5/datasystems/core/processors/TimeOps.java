package if5.datasystems.core.processors;

import java.time.Instant;

public class TimeOps {

  public static Instant maxTime(Instant t1, Instant t2) {
    if (t1 == null) return t2;
    if (t2 == null) return t1;
    return t1.isAfter(t2) ? t1 : t2;
  }

  public static Instant minTime(Instant t1, Instant t2) {
    if (t1 == null) return t2;
    if (t2 == null) return t1;
    return t1.isBefore(t2) ? t1 : t2;
  }

}