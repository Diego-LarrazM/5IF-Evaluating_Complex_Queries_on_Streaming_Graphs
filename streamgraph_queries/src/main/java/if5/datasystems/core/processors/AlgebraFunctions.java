package if5.datasystems.core.processors;

import java.util.ArrayList;

import if5.datasystems.core.models.aliases.Label;
import if5.datasystems.core.models.aliases.Pair;
import if5.datasystems.core.models.aliases.State;
import if5.datasystems.core.models.queries.IndexNode;
import if5.datasystems.core.models.queries.SpanningTree;
import if5.datasystems.core.models.streamingGraph.Edge;
import if5.datasystems.core.models.streamingGraph.StreamingGraph;
import if5.datasystems.core.models.streamingGraph.StreamingGraphTuple;

public class AlgebraFunctions {
    public static StreamingGraph Snapshot(StreamingGraph S, long snapTime){
        StreamingGraph resultGraph = new StreamingGraph();
        
        for (StreamingGraphTuple tuple : S.getTuples())
        {
            long ts = tuple.getStartTime();
            long exp = tuple.getExpiricy();

            if (ts <= snapTime && snapTime < exp)
            {
                resultGraph.add(tuple);
            }
        }
        return resultGraph;
    }

    private static StreamingGraphTuple buildSGT(ArrayList<Edge> edges,Label outputLabel) {
        StreamingGraphTuple sgt = new StreamingGraphTuple();

        if (edges.isEmpty()) {
            sgt.setContent(new ArrayList<>());
            return sgt;
        }

        Edge startE = edges.get(0);
        Edge endE = edges.getLast();
        
        sgt.setRepr(new Edge(startE.getSource(), endE.getTarget(), outputLabel, startE.getStartTime(), startE.getExpiricy())); 
        sgt.setContent(new ArrayList<>(edges));
        return sgt;
    }

    public static StreamingGraph PathTree(SpanningTree T, Pair<String, State> targetNodeKey, Label outputLabel){
        StreamingGraph resultGraph = new StreamingGraph();

        IndexNode currentNode = T.getNode(targetNodeKey);
        
        if (currentNode==null) return resultGraph;
        ArrayList<Edge> edges = new ArrayList<>();
        Label currentLabel = null;

        while (!currentNode.equals(T.getRoot()))
        {
            Pair<String,State> parentKey = currentNode.getParent();
            IndexNode parent = T.getNode(parentKey);
            Edge e = new Edge(
                parent.getName(),
                currentNode.getName(),
                currentNode.getFromLabel(),
                currentNode.getStartTime(),
                currentNode.getExpiricy()
            );
            edges.addFirst(e);

            currentNode = parent;
        }

        //add the last sgt of the grp
        resultGraph.add(buildSGT(edges, outputLabel));
        
        return resultGraph;
    }
}
