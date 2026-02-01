package if5.datasystems.core.processors;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.ArrayList;
import java.util.HashMap;

import if5.datasystems.core.models.aliases.Label;
import if5.datasystems.core.models.aliases.NodeKey;
import if5.datasystems.core.models.aliases.Pair;
import if5.datasystems.core.models.aliases.State;
import if5.datasystems.core.models.automaton.Automaton;
import if5.datasystems.core.models.queries.IndexNode;
import if5.datasystems.core.models.queries.IndexPath;
import if5.datasystems.core.models.queries.SpanningTree;
import if5.datasystems.core.models.streamingGraph.Edge;

public class SPathProcessor implements Function<Pair<HashMap<String, HashSet<Edge>>, Edge>, TreeMap<Long, Set<NodeKey>>>  { 

  private IndexPath deltaPath;
  private Automaton automaton;
  private Label queryLabel;
  private Label outputLabel;
  private TreeMap<Long, Set<NodeKey>> resultTimestamps;
  private HashMap<NodeKey, Long> lookup;

  public IndexPath getDeltaPath() {
    return this.deltaPath;
  }
  public Label getOutLabel() {
    return this.outputLabel;
  }
  public Label getQueryLabel() {
    return this.queryLabel;
  }
  public HashMap<NodeKey, Long> getLookup(){
    return this.lookup;
  }
  public TreeMap<Long, Set<NodeKey>> getResultTimestamps(){
    return this.resultTimestamps;
  }

  public SPathProcessor(Label pathLabel, Label outputLabel) {
    this.queryLabel = pathLabel;
    this.automaton = new Automaton(pathLabel);
    this.outputLabel = outputLabel;
    this.deltaPath = new IndexPath();
    this.resultTimestamps = new TreeMap<>();
    this.lookup = new HashMap<>();
  }

  private void move_node_to_by(IndexNode childNode, IndexNode parentNode, Edge edgeExpansion) {
    childNode.setParent(parentNode.getKey());
    childNode.setFromLabel(edgeExpansion.getLabel());
    childNode.setStartTime(TimeOps.minTime(edgeExpansion.getStartTime(), parentNode.getStartTime()));
    childNode.setExpiricy(TimeOps.minTime(edgeExpansion.getExpiricy(), parentNode.getExpiricy()));
  }

  private void update_result_map(long newTs, NodeKey pathKey) {
    // Case 1: path exists but new timestamp is better, remove it to make it perdure more with nerwer timestamp
    Long oldTs = lookup.get(pathKey);
    if (oldTs != null && oldTs < newTs) { 
      Set<NodeKey> oldSet = resultTimestamps.get(oldTs);
      oldSet.remove(pathKey);
      if (oldSet.isEmpty()) {
        resultTimestamps.remove(oldTs);
      }
      // update both to new timestamp
      lookup.put(pathKey, newTs); 
      resultTimestamps.computeIfAbsent(newTs, k -> new HashSet<>()).add(pathKey);
    }
    // Case 2: path does not exist yet
    else if (oldTs == null) { 
      lookup.put(pathKey, newTs);
      if(!resultTimestamps.containsKey(newTs)) {
        resultTimestamps.put(newTs, new HashSet<>());
      }
      resultTimestamps.get(newTs).add(pathKey);
    }
    // Case 3: oldTs >= newTs -> prune
    else {
      return; 
    }
  }
  
  private TreeMap<Long, Set<NodeKey>> spath(HashMap<String, HashSet<Edge>> vertexEdgesSnapshot, Edge newEdge) {
      String src = newEdge.getSource();
      String trg = newEdge.getTarget();
      Label l = newEdge.getLabel();
      long ts = newEdge.getStartTime();
      State so = automaton.getInitialState();

      for (State s_start : automaton.getStates()) { //  (src,s_start) --l--> (trg, s_dest)
        State s_dest = automaton.transition(s_start, l);
        if (s_dest == null) continue;

        // Spanning Tree Node Keys
        Pair<String, State> parentKey = new Pair<>(src, s_start);
        Pair<String, State> childKey = new Pair<>(trg, s_dest);

        // Initial Spanning Tree Creation
        if (s_start.equals(so) && !deltaPath.contains(src)) {
          // Ensure root node accepts all, meaning -inf ts and +inf exp
          IndexNode rootNode = new IndexNode(src, so, Long.MAX_VALUE, Long.MAX_VALUE);
          deltaPath.createTree(rootNode);
        }

        // Search for expansions in all trees where (src,s_start) exists
        PriorityQueue<PathExpansionHeapNode> path_expansions_heap = new PriorityQueue<>(PathExpansionHeapNode.BY_TS_DESC);
        ArrayList<SpanningTree> ExpandedTrees = deltaPath.expandableTreesNoTConstraint(parentKey);
        for (SpanningTree Tx : ExpandedTrees) { 
          path_expansions_heap.add(new PathExpansionHeapNode(parentKey, childKey, newEdge, ts));
          while(!path_expansions_heap.isEmpty()){
            PathExpansionHeapNode current_expansion = path_expansions_heap.poll();
            Pair<String, State> parentKey_i = current_expansion.parentKey;
            Pair<String, State> childKey_j = current_expansion.childKey;
            IndexNode parentNode_i = Tx.getNode(parentKey_i);
            IndexNode childNode_i = Tx.getNode(childKey_j);
            Edge edgeExpansion = current_expansion.edge;

            if(childNode_i == null){ // Tree does not contain child yet => Expand
              childNode_i = new IndexNode(childKey_j.first(),childKey_j.second());
              Tx.addNode(childNode_i);
              move_node_to_by(childNode_i, parentNode_i, edgeExpansion);
            }
            else if(childNode_i.getStartTime() < TimeOps.minTime(parentNode_i.getStartTime(), edgeExpansion.getStartTime())) { // Propagate existing child for better start time
              move_node_to_by(childNode_i, parentNode_i, edgeExpansion);
            }
            else{
              continue;
            }

            IndexNode root = Tx.getRoot();
            if(root.getState() == so && automaton.isFinal(childNode_i.getState())){
              update_result_map(childNode_i.getStartTime(), new NodeKey(root.getName(), childKey_j));
            }
            HashSet<Edge> snapshotEdgesFromChild = vertexEdgesSnapshot.get(childNode_i.getName());
            if (snapshotEdgesFromChild == null) {continue;}
            for (Edge e: snapshotEdgesFromChild) {
              State q = automaton.transition(childNode_i.getState(), e.getLabel());
              if (q == null) continue;
              String w = e.getTarget();
              Pair<String, State> possibleNewChildKey = new Pair<>(w,q);
              path_expansions_heap.add(new PathExpansionHeapNode(childKey_j, possibleNewChildKey, e, e.getStartTime()));
            }
        }
      }

    
    }
    return resultTimestamps;
  }

  @Override
  public TreeMap<Long, Set<NodeKey>> apply(Pair<HashMap<String, HashSet<Edge>>, Edge> input){
    return spath(input.first(), input.second());
  }


}

