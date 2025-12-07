package if5.datasystems.core.models.streamingGraph;

import java.util.Map;

import if5.datasystems.core.models.aliases.Label;
import lombok.Data;

@Data public class StreamingGraph {
  private Map<Label, LabeledGraphStream> graphs;

  public void add(StreamingGraphTuple tuple) {
    if(tuple == null){return;}

    LabeledGraphStream subgraph_l = this.graphs.get(tuple.getRepr().getLabel());
    if(subgraph_l == null){
      subgraph_l = new LabeledGraphStream(tuple.getRepr().getLabel());
      this.graphs.put(tuple.getRepr().getLabel(), subgraph_l);
    }
    subgraph_l.add(tuple);
  }
}
