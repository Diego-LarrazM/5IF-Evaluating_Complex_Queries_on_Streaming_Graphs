package if5.datasystems.core.models.streamingGraph;

import lombok.Data;

@Data public class Edge {

  private String source;
  private String target;
  private Label label;

}
