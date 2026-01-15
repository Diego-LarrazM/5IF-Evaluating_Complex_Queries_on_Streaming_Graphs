package if5.datasystems.core.models.aliases;

import lombok.Data;

public record State(String s) {
  public State {
      if (s == null) s = "";
  }
  public int length() { return s.length(); }
  public boolean isEmpty() { return s.isEmpty(); }
  @Override public String toString() { return s; }
}