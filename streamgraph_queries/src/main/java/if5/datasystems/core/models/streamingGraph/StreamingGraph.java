package if5.datasystems.core.models.streamingGraph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import if5.datasystems.core.models.aliases.Label;
import lombok.Data;

@Data public class StreamingGraph {
  private Set<StreamingGraphTuple> tuples;

  public StreamingGraph(){
    this.tuples = new HashSet<>(); // We really need to think if its a hashset or a orderedSet for coalesce and ordering
  }

  public StreamingGraph(Set<StreamingGraphTuple> tuples){
    this.tuples = tuples; // We really need to think if its a hashset or a orderedSet for coalesce and ordering
  }

  public void add(StreamingGraphTuple tuple) {
    if (tuple == null) {return;}
    this.tuples.add(tuple); // Coalesce to implement with ordering of tupels by time
  }
}
