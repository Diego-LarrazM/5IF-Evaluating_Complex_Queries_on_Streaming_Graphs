package if5.datasystems.core.models.streamingGraph;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data public class StreamingGraphTuple {
  private Edge repr;
  private List<Edge> content;

  public StreamingGraphTuple(){
    this.content = new ArrayList<>();
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

}
