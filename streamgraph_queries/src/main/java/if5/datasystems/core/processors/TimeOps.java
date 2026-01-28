package if5.datasystems.core.processors;


public class TimeOps {

  public static long maxTime(long t1, long t2) {
    if (t1 == -1) return t2;
    if (t2 == -1) return t1;
    return t1 > t2 ? t1 : t2;
  }

  public static long minTime(long t1, long t2) {
    if (t1 == -1) return t2;
    if (t2 == -1) return t1;
    return t1 < t2 ? t1 : t2;
  }

}