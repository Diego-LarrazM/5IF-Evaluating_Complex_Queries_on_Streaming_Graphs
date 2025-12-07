package if5.datasystems.core.models.aliases;

import lombok.Data;

public record Label(String l) {
  public int length() { return l.length(); }
  public boolean isEmpty() { return l.isEmpty(); }
  @Override public String toString() { return l; }
}
