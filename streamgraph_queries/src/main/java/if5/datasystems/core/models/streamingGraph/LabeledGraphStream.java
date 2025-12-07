package if5.datasystems.core.models.streamingGraph;

import java.util.ArrayList;

import if5.datasystems.core.models.aliases.Label;
import lombok.Data;

@Data public class LabeledGraphStream {
  private Label label;
  private ArrayList<StreamingGraphTuple> tuples;

  public LabeledGraphStream(Label label, ArrayList<StreamingGraphTuple> tuples) {
    this.label = label;
    this.tuples = tuples;
  }

  public LabeledGraphStream(Label label) {
    this.label = label;
    this.tuples = new ArrayList<StreamingGraphTuple>();
  }

  public void add(StreamingGraphTuple tuple) {
    if (tuple == null || tuple.getRepr().getLabel() != this.label) {return;}
    this.tuples.add(tuple);
  }
}
