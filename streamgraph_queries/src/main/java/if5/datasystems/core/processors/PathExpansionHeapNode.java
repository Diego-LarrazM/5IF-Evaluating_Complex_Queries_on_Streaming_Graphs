package if5.datasystems.core.processors;

import java.util.Comparator;

import if5.datasystems.core.models.aliases.Pair;
import if5.datasystems.core.models.aliases.State;
import if5.datasystems.core.models.streamingGraph.Edge;

public class PathExpansionHeapNode {
  public Pair<String, State> parentKey;
  public Pair<String, State> childKey;
  public Edge edge;
  public long dest_ts;

  public static final Comparator<PathExpansionHeapNode> BY_TS_DESC = 
                      Comparator.comparingLong((PathExpansionHeapNode node) -> node.dest_ts).reversed();
  public PathExpansionHeapNode(
    Pair<String, State> parent, 
    Pair<String, State> child, 
    Edge edge,
    long dest_ts
  ) {
      this.parentKey = parent;
      this.childKey = child;
      this.edge = edge;
      this.dest_ts = dest_ts;
  } 
} 
