package if5.datasystems.core.processors;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.ArrayList;

import if5.datasystems.core.models.aliases.Label;
import if5.datasystems.core.models.aliases.Pair;
import if5.datasystems.core.models.aliases.State;
import if5.datasystems.core.models.aliases.Tuple4;
import if5.datasystems.core.models.automaton.Automaton;
import if5.datasystems.core.models.queries.IndexNode;
import if5.datasystems.core.models.queries.IndexPath;
import if5.datasystems.core.models.queries.SpanningTree;
import if5.datasystems.core.models.streamingGraph.Edge;
import if5.datasystems.core.models.streamingGraph.StreamingGraph;
import if5.datasystems.core.models.streamingGraph.StreamingGraphTuple;

public class SPath implements Function<Tuple4<IndexPath, StreamingGraph, Label, Label>, StreamingGraph> { 

  private Set<StreamingGraphTuple> Expand(SpanningTree T, Pair<String, State> parentKey, Pair<String, State> childKey, Edge edge, Automaton automaton, StreamingGraph S, Label outputLabel) {
    HashSet<StreamingGraphTuple> results = new HashSet<>();
    IndexNode child = new IndexNode(childKey.first(),childKey.second());
    IndexNode parent = T.getNode(parentKey);
    child.setParent(parentKey);
    child.setFromLabel(edge.getLabel());
    T.addNode(child);
    child.setStartTime(TimeOps.maxTime(edge.getStartTime(), parent.getStartTime()));
    child.setExpiricy(TimeOps.minTime(edge.getExpiricy(), parent.getExpiricy()));
    
    if (automaton.isFinal(child.getState())) {
        StreamingGraph pathResult = AlgebraFunctions.PathTree(T, childKey, outputLabel);
        results.addAll(pathResult.getTuples());  
    }

    for (StreamingGraphTuple sgt: AlgebraFunctions.Snapshot(S, child.getStartTime()).getTuples()) {
      for(Edge e: sgt.getContent()){
        if(!e.getSource().equals(childKey.first())) continue;
        State q = automaton.transition(child.getState(), e.getLabel());
        if (q == null) continue;
        String w = e.getTarget();
        if(T.contains(w,q)){
          IndexNode possibleNewChild = T.getNode(w,q);
          if (possibleNewChild.getExpiricy() < TimeOps.minTime(child.getExpiricy(), e.getExpiricy())){
            results.addAll(Propagate(T, childKey, new Pair<>(w,q), e, automaton, S, outputLabel));
          }
        }
        else{results.addAll(Expand(T, childKey, new Pair<>(w,q), e, automaton, S, outputLabel));}
      }
    }

    return results;
  }

  private Set<StreamingGraphTuple> Propagate(SpanningTree T, Pair<String, State> parentKey, Pair<String, State> childKey, Edge edge, Automaton automaton, StreamingGraph S, Label outputLabel) {
    HashSet<StreamingGraphTuple> results = new HashSet<>();
    IndexNode child = T.getNode(childKey); // We obtain the current child in Tree to get its current validity interval [ts,exp]
    IndexNode parent = T.getNode(parentKey);
    child.setParent(parentKey);
    child.setFromLabel(edge.getLabel());
    child.setStartTime(TimeOps.minTime(child.getStartTime(),TimeOps.maxTime(edge.getStartTime(), parent.getStartTime())));
    child.setExpiricy(TimeOps.maxTime(child.getExpiricy(), TimeOps.minTime(edge.getExpiricy(), parent.getExpiricy())));
    
    if (automaton.isFinal(child.getState())) {
        StreamingGraph pathResult = AlgebraFunctions.PathTree(T, childKey, outputLabel);
        results.addAll(pathResult.getTuples());  
    }

    for (StreamingGraphTuple sgt: AlgebraFunctions.Snapshot(S, child.getStartTime()).getTuples()) {
        for(Edge e: sgt.getContent()){
          if(!e.getSource().equals(childKey.first())) continue;
          State q = automaton.transition(child.getState(), e.getLabel());
          if (q == null) continue;
          String w = e.getTarget();
          IndexNode possibleNewChild = T.getNode(w,q);
          if (possibleNewChild.getExpiricy() < TimeOps.minTime(child.getExpiricy(), e.getExpiricy())){
            results.addAll(Propagate(T, childKey, new Pair<>(w,q), e, automaton, S, outputLabel));
          }
        }
    }
    return results;
  }

  private StreamingGraph spath(IndexPath deltaPath, StreamingGraph S, Label pathLabel, Label outputLabel) {
    TreeSet<StreamingGraphTuple> results = new TreeSet<>(StreamingGraphTuple.BY_EXPIRICY);
    Automaton automaton = new Automaton(pathLabel); // TO implement automaton definition from regular query

    for (StreamingGraphTuple tuple : S.getTuples()) {
      Edge edge = tuple.getRepr();
      String u = edge.getSource();
      String v = edge.getTarget();
      Label l = edge.getLabel();
      long ts = edge.getStartTime();
      long exp = edge.getExpiricy();

      for (State s : automaton.getStates()) {
        State t = automaton.transition(s, l);
        if (t == null) continue;

        State so = automaton.getInitialState();
        Pair<String, State> parentKey = new Pair<>(u, s);
        Pair<String, State> childKey = new Pair<>(v, t);

        if (s.equals(so)) {
          if (!deltaPath.contains(u)) {
            IndexNode rootNode = new IndexNode(u, so, ts, exp);
            deltaPath.createTree(rootNode);
          }

          SpanningTree Tu = deltaPath.getTree(u);
          if (!Tu.contains(v, t)) {
            results.addAll(Expand(Tu, parentKey, childKey, edge, automaton, S, outputLabel));
          }
          else {
            IndexNode existingChildNode =  Tu.getNode(childKey);
            if (existingChildNode.getExpiricy() < edge.getExpiricy()) {
              results.addAll(Propagate(Tu, parentKey, childKey, edge, automaton, S, outputLabel));
            }
          }
        }

        ArrayList<SpanningTree> ExpandedTrees = deltaPath.expandableTrees(parentKey, ts);
        for (SpanningTree Tx : ExpandedTrees) {
          if (!Tx.contains(v, t)) {
            results.addAll(Expand(Tx, parentKey, childKey, edge, automaton, S, outputLabel));
          }
          else {
            IndexNode parentNode = Tx.getNode(u, s);
            IndexNode existingChild = Tx.getNode(v, t);
            long minExp = TimeOps.minTime(parentNode.getExpiricy(), edge.getExpiricy());
            if (existingChild.getExpiricy() < minExp) {
              results.addAll(Propagate(Tx, parentKey, childKey, edge, automaton, S, outputLabel));
            }
          }
        }
      }
    }
    return new StreamingGraph(new LinkedList<>(results));
  }

  @Override
  public StreamingGraph apply(Tuple4<IndexPath, StreamingGraph, Label, Label> input){
    return spath(input.first(), input.second(), input.third(), input.fourth());
  }


}

