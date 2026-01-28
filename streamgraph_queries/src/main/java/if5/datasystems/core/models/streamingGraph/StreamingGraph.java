package if5.datasystems.core.models.streamingGraph;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;

import if5.datasystems.core.models.aliases.Label;
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
    for (Iterator<StreamingGraphTuple> iterator = this.tuples.iterator(); iterator.hasNext(); ) {
        StreamingGraphTuple existingTuple = iterator.next();

        if (existingTuple.equals(sgt)) {
            sgt = sgt.mergeTuple(existingTuple);
            iterator.remove(); // Supprimer temporairement pour réinsérer à la bonne position
            break;
        }
    }
    // Réinsérer le tuple fusionné ou insérer le nouveau tuple à la bonne position
    boolean inserted = false;
    int i  = this.tuples.size();
    while(i-- >0) {
        StreamingGraphTuple currentTuple = this.tuples.get(i);
        // Insérer dans la bonne position en fonction de la date d'expiration
        if (sgt.getRepr().getExpiricy() < currentTuple.getRepr().getExpiricy()) {
            this.tuples.add(i, sgt);
            inserted = true;
            break;
        }
    }
    // Si non inséré, ajouter à la fin
    if (!inserted) {
        this.tuples.add(sgt);
    }
}

}
