package if5.datasystems.core.models.streamingGraph;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;

import lombok.Data;

@Data public class StreamingGraphTuple {
  private Edge repr;
  private List<Edge> content;

  public StreamingGraphTuple(){
    this.content = new ArrayList<>();
  }

  public StreamingGraphTuple(Edge repr){
    this.repr = repr;
    this.content = new ArrayList<>();
    this.content.add(repr);
  }

  public void add(Edge e){
    if(!this.content.isEmpty()){
      
    }

    content.add(e);
  }

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

  public long getStartTime_ms(){
    return this.repr.getStartTime_ms();
  }
  public long getExpiricy_ms(){
    return this.repr.getExpiricy_ms();
  }

  public StreamingGraphTuple mergeTuple(StreamingGraphTuple sgt) {
    Edge e = this.repr;
    e.setStartTime(
      Instant.ofEpochMilli(Math.min(
        e.getStartTime_ms(),
        sgt.getStartTime_ms())));
    e.setExpiricy(
      Instant.ofEpochMilli(Math.max(
        e.getExpiricy_ms(),
        sgt.getExpiricy_ms())));
      
    return new StreamingGraphTuple(e);
  }

  public static final Comparator<StreamingGraphTuple> BY_EXPIRICY = Comparator
        .comparing((StreamingGraphTuple t) -> t.getExpiricy_ms())
        .thenComparing(t -> t.getRepr().toString());
}
