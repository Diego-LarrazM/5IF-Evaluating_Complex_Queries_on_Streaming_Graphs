package if5.datasystems.core.models.streamingGraph;

import java.util.ArrayList;

import lombok.Data;

@Data public class LabeledGraphStream {
  private Label label;
  private ArrayList<StreamingGraphTuple> edges;

  public LabeledGraphStream(Label label, ArrayList<StreamingGraphTuple> edges) {
    this.label = label;
    this.edges = edges;
  }

  public LabeledGraphStream(Label label) {
    this.label = label;
    this.edges = new ArrayList<StreamingGraphTuple>();
  }

  public void add(StreamingGraphTuple tuple) {
    if (tuple == null || tuple.getLabel() != this.label) {return;}
    this.edges.add(tuple);
  }
}
