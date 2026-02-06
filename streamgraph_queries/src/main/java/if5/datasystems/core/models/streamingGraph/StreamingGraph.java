package if5.datasystems.core.models.streamingGraph;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;

import lombok.Data;

@Data public class StreamingGraph implements Serializable {
  private LinkedList<StreamingGraphTuple> tuples;

  public boolean containsPath(String... nodes) {
      if (nodes == null || nodes.length < 2) return false;

      for (StreamingGraphTuple tuple : tuples) {
          if (tuple.isPath(nodes)) {
              return true;
          }
      }

      return false;
  }

  public StreamingGraph(){
    this.tuples =  new LinkedList<>();
  }

  public StreamingGraph(LinkedList<StreamingGraphTuple> tuples){
    this.tuples = tuples; // We really need to think if its a hashset or a orderedSet for coalesce and ordering
  }

  public void add(StreamingGraphTuple tuple) {
    if (tuple == null) {return;}
    this.tuples.add(tuple); // Coalesce to implement with ordering of tupels by time
  }

  public void updateStreamingGraph(StreamingGraphTuple sgt) {
    // Check for an existing tuple that matches sgt
    for (Iterator<StreamingGraphTuple> iterator = this.tuples.iterator(); iterator.hasNext(); ) {
        StreamingGraphTuple existingTuple = iterator.next();
        if (existingTuple.equals(sgt)) {
            // Merge with existing tuple
            sgt = sgt.mergeTuple(existingTuple);
            iterator.remove(); // Remove old tuple before reinserting
            break;
        }
    }

    // Insert sgt in ascending order by expiration
    boolean inserted = false;
    for (int i = 0; i < this.tuples.size(); i++) {
        StreamingGraphTuple currentTuple = this.tuples.get(i);
        if (sgt.getRepr().getExpiricy() < currentTuple.getRepr().getExpiricy()) {
            this.tuples.add(i, sgt); // Insert before first tuple with larger expiration
            inserted = true;
            break;
        }
    }

    // If it is the largest expiration, add at the end
    if (!inserted) {
        this.tuples.add(sgt);
    }
 }

}
