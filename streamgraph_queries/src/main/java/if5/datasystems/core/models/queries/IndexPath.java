package if5.datasystems.core.models.queries;

import if5.datasystems.core.models.aliases.Pair;
import if5.datasystems.core.models.aliases.State;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

@Data public class IndexPath {
  private Map<String, SpanningTree> indexPath;

  public IndexPath() {
    this.indexPath = new HashMap<>();
  }

  public void createTree(IndexNode rootNode) {
    this.indexPath.put(rootNode.getName(), new SpanningTree(rootNode));
  }

  public SpanningTree getTree(String rootName){
    return this.indexPath.get(rootName);
  }

  public boolean contains(String rootName){
    return this.indexPath.containsKey(rootName);
  }

  public ArrayList<SpanningTree> expandableTrees(Pair<String, State> searchNodeKey, Instant t) {
    ArrayList<SpanningTree> result = new ArrayList<>();
    for (SpanningTree tree : indexPath.values()) {
      IndexNode node = tree.getNode(searchNodeKey);

      if (node == null) {
        continue;
      }

      Instant start = node.getStartTime();
      Instant exp = node.getExpiricy();

      if (start != null && t.isBefore(start)) {
        continue;
      }
      if (exp != null && !t.isBefore(exp)) {
        continue;
      }

      result.add(tree);
    }
    return result;
  }

}
