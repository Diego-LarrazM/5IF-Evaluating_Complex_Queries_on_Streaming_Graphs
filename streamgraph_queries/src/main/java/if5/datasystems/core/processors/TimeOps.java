package if5.datasystems.core.processors;

import java.sql.Time;

public class TimeOps {

  public static Time maxTime(Time t1, Time t2) {
    if (t1 == null) return t2;
    if (t2 == null) return t1;
    return t1.after(t2) ? t1 : t2;
  }

  public static Time minTime(Time t1, Time t2) {
    if (t1 == null) return t2;
    if (t2 == null) return t1;
    return t1.before(t2) ? t1 : t2;
  }

}