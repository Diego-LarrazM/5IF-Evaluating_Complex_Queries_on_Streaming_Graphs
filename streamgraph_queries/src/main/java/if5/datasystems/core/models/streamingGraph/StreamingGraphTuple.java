package if5.datasystems.core.models.streamingGraph;

import java.sql.Time;
import java.util.List;

import lombok.Data;

@Data public class StreamingGraphTuple {
  private Edge repr;
  private List<Edge> content;

  @Override
  public boolean equals(Object obj){ // Value Equivalence
    if (this == obj) return true;
    if (!(obj instanceof StreamingGraphTuple)) return false;
    StreamingGraphTuple other = (StreamingGraphTuple) obj;
    return this.repr.equals(other.repr);
  }

  @Override
  public int hashCode() {
      return repr.hashCode();
  }

}
