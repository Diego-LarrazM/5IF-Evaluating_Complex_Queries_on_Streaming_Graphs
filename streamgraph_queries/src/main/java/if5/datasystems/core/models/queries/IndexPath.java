package if5.datasystems.core.models.queries;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;

@Data public class IndexPath {
  private Map<IndexNode, SpanningTree> indexPath;

  public IndexPath() {
    this.indexPath = new HashMap<IndexNode, SpanningTree>();
  }

  public void createTree(IndexNode rootNode) {
    this.indexPath.put(rootNode, new SpanningTree(rootNode));
  }

  public void addTo(IndexNode rootNode, Map.Entry<IndexNode, IndexNode> toAddEntry) {
    SpanningTree tree = this.indexPath.get(rootNode);
    if(tree != null && toAddEntry != null) {
      tree.addNode(toAddEntry.getKey(), toAddEntry.getValue());
    }
  }
  
}
