package if5.datasystems.core.processors;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.sql.Time;

import if5.datasystems.core.models.aliases.Label;
import if5.datasystems.core.models.aliases.State;
import if5.datasystems.core.models.aliases.Triple;
import if5.datasystems.core.models.automaton.Automaton;
import if5.datasystems.core.models.queries.IndexNode;
import if5.datasystems.core.models.queries.RegularQuery;
import if5.datasystems.core.models.queries.SpanningTree;
import if5.datasystems.core.models.streamingGraph.Edge;
import if5.datasystems.core.models.streamingGraph.StreamingGraph;
import if5.datasystems.core.models.streamingGraph.StreamingGraphTuple;


public class SPath implements Function<Triple<StreamingGraph, RegularQuery, Label>, StreamingGraph> { 

  private Set<StreamingGraphTuple> Expand(SpanningTree T, IndexNode parent, IndexNode child, Edge edge, Automaton automaton, StreamingGraph S) {
    HashSet<StreamingGraphTuple> results = new HashSet<>();
    child.setParent(parent);
    T.addNode(parent, child);
    child.setStartTime(TimeOps.maxTime(edge.getStartTime(),parent.getStartTime()));
    child.setExpiricy(TimeOps.minTime(edge.getExpiricy(), parent.getExpiricy()));
    if (automaton.isFinal(child.getState())) {
      StreamingGraphTuple pathResult = new StreamingGraphTuple(); // to be implemented with PATH operator PATH(T.root, child)
      results.add(pathResult);
    }

    for (Edge e: Snapshot(S, child.getStartTime()).getEdges()) {
        State q = automaton.transition(child.getState(), e.getLabel());
        if (q == null) continue;
        String w = e.getTarget();
        if(T.contains(w,q)){
          IndexNode possibleNewChild = T.getNode(w,q); //  To Implement, get Index Node of name w and state q
          if (possibleNewChild.getExpiricy().before(TimeOps.minTime(child.getExpiricy(), e.getExpiricy()))){
            results.addAll(Propagate(T, child, possibleNewChild, edge, automaton, S));
          }
        }
        else{results.addAll(Expand(T, child, new IndexNode(w,q), edge, automaton, S));}
        
    }

    return results;
  }

  private Set<StreamingGraphTuple> Propagate(SpanningTree T, IndexNode parent, IndexNode child, Edge edge, Automaton automaton, StreamingGraph S) {
    HashSet<StreamingGraphTuple> results = new HashSet<>();
    child.setParent(parent);
    child.setStartTime(TimeOps.minTime(child.getStartTime(),TimeOps.maxTime(edge.getStartTime(), parent.getStartTime())));
    child.setExpiricy(TimeOps.maxTime(child.getExpiricy(), TimeOps.minTime(edge.getExpiricy(), parent.getExpiricy())));
    
    if (automaton.isFinal(child.getState())) {
        StreamingGraphTuple pathResult = new StreamingGraphTuple();  // to be implemented with PATH operator PATH(T.root, child)
        results.add(pathResult);  
    }

    for (Edge e: Snapshot(S, child.getStartTime()).getEdges()) {
        State q = automaton.transition(child.getState(), e.getLabel());
        if (q == null) continue;
        String w = e.getTarget();
        IndexNode possibleNewChild = T.getNode(w,q); //  To Implement, get Index Node of name w and state q
        if (possibleNewChild.getExpiricy().before(TimeOps.minTime(child.getExpiricy(), e.getExpiricy()))){
          results.addAll(Propagate(T, child, possibleNewChild, edge, automaton, S));
        }
    }
    
    return results;
  }

  private StreamingGraph spath(StreamingGraph S, RegularQuery RQ, Label outputLabel) {
    Set<StreamingGraphTuple> results = new HashSet<>();
    StreamingGraph outputGraph = new StreamingGraph();

    return outputGraph ;
  }

  @Override
  public StreamingGraph apply(Triple<StreamingGraph, RegularQuery, Label> input){
    return spath(input.first(), input.second(), input.third());
  }


}

