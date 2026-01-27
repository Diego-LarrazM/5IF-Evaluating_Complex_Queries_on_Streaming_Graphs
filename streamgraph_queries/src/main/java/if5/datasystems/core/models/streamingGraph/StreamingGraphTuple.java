package if5.datasystems.core.models.streamingGraph;

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

  public boolean isPath(String[] nodes) {
    // Check if nodes array is compatible with the number of edges
    if (nodes == null || nodes.length < 2 || nodes.length - 1 != content.size()) {
        return false;
    }

    // Check each edge in content matches the consecutive nodes
    for (int i = 0; i < content.size(); i++) {
        Edge edge = content.get(i);
        if (!edge.source.equals(nodes[i]) || !edge.target.equals(nodes[i + 1])) {
            return false;
        }
    }

    return true;
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

  public long getStartTime(){
    return this.repr.getStartTime();
  }
  public long getExpiricy(){
    return this.repr.getExpiricy();
  }

  public StreamingGraphTuple mergeTuple(StreamingGraphTuple sgt) {
    Edge e = this.repr;
    e.setStartTime(
      Math.min(
        e.getStartTime(),
        sgt.getStartTime())
      );
    e.setExpiricy(
      Math.max(
        e.getExpiricy(),
        sgt.getExpiricy())
      );
      
    return new StreamingGraphTuple(e);
  }

  public static final Comparator<StreamingGraphTuple> BY_EXPIRICY = Comparator
        .comparing((StreamingGraphTuple t) -> t.getExpiricy())
        .thenComparing(t -> t.getRepr().toString())
        .thenComparing(t -> t.getContent().size()); // this can still give an error if same repr and same exp and same content size but diff content, rare case
}
