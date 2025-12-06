package if5.datasystems.core.models.streamingGraph;

import java.sql.Time;
import java.util.List;

import lombok.Data;

@Data public class StreamingGraphTuple {
  private String source;
  private String target;
  private Label label;
  private Time startTime;
  private Time expiricy;
  private List<Edge> content;

  @Override
  public boolean equals(Object obj){ // Value Equivalence
    if (this == obj) return true;
    if (!(obj instanceof StreamingGraphTuple)) return false;
    StreamingGraphTuple other = (StreamingGraphTuple) obj;
    return this.source.equals(other.source) &&
           this.target.equals(other.target) &&
           this.label.equals(other.label);
  }
}
