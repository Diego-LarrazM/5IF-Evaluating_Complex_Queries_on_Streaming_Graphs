package if5.datasystems.core.models.queries;

import if5.datasystems.core.models.aliases.Pair;
import if5.datasystems.core.models.aliases.State;

import java.sql.Time;
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
  
  public ArrayList<SpanningTree> expandableTrees(Pair<String,State> searchNodeKey, Time t){
    ArrayList<SpanningTree> treesWithSearchNode = new ArrayList<>();
    for(Map.Entry e : this.indexPath.entrySet()){
      SpanningTree Ts = (SpanningTree) e.getValue();
      IndexNode searchedNode = Ts.getNode(searchNodeKey);
      Time searchedTs = searchedNode.getStartTime();
      Time searchedExp = searchedNode.getExpiricy();
      if(searchedNode == null || (searchedTs.before(t) && searchedExp.after(t))){continue;} // Is exp open? In that case ouch, to change
      treesWithSearchNode.add(Ts);
    }
    return treesWithSearchNode;
  }
}
