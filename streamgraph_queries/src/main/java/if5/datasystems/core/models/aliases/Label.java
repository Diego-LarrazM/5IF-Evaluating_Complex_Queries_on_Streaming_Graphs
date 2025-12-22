package if5.datasystems.core.models.aliases;

import lombok.Data;

public record Label(String l) {
  public Label {
      if (l == null) l = "";
  }
  public int length() { return l.length(); }
  public boolean isEmpty() { return l.isEmpty(); }
  @Override public String toString() { return l; }
}
