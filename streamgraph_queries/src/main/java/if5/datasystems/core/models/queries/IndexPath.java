package if5.datasystems.core.models.queries;

import if5.datasystems.core.models.aliases.Pair;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data public class IndexPath {
  private Map<IndexNode, SpanningTree> indexPath;

  public IndexPath() {
    this.indexPath = new HashMap<>();
  }

  public void createTree(IndexNode rootNode) {
    this.indexPath.put(rootNode, new SpanningTree(rootNode));
  }

  public void addEdgeTo(IndexNode rootNode, Pair<IndexNode, IndexNode> toAddEntry) {
    SpanningTree tree = this.indexPath.get(rootNode);
    if(tree != null && toAddEntry != null) {
      tree.addNode(toAddEntry.first(), toAddEntry.second());
    }
  }
  
}
