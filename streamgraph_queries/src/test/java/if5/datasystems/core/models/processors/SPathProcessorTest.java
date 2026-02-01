package if5.datasystems.core.models.processors;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import if5.datasystems.core.models.aliases.State;
import if5.datasystems.core.models.aliases.Label;
import if5.datasystems.core.models.aliases.Pair;
import if5.datasystems.core.models.queries.IndexNode;
import if5.datasystems.core.models.queries.IndexPath;
import if5.datasystems.core.models.queries.SpanningTree;
import if5.datasystems.core.models.streamingGraph.Edge;
import if5.datasystems.core.processors.SPathProcessor;

import org.junit.jupiter.api.Test;

class SPathProcessorTest {
/* 
    private Edge edge(String src, String tgt, String label, long start, long exp) {
        return new Edge(src, tgt, new Label(label), start, exp);
    }

    // Helpers
    private HashMap<String, HashSet<Edge>> vertexEdges(Edge... edges) {
        HashMap<String, HashSet<Edge>> map = new HashMap<>();
        for (Edge e : edges) {
            map.computeIfAbsent(e.getSource(), k -> new HashSet<>()).add(e);
        }
        return map;
    }


    // ───────────── Tests ─────────────

    @Test
    void spath_multiple_labels_propagation_story() {
        SPathProcessor spath = new SPathProcessor(new Label("a,b*"), new Label("out"));

        // Step 1
      Edge e1 = edge("x","y","a",0,1000);
        Edge e2 = edge("y","z","a",1,1001);
        Edge eNo = edge("y","asdasd","s",1,1001);
        HashMap<String, HashSet<Edge>> vEdges = vertexEdges(e1,e2,eNo);

        spath.apply(new Pair<>(vEdges,e1));
        spath.apply(new Pair<>(vEdges,e2));
        spath.apply(new Pair<>(vEdges,eNo));

        IndexPath d1 = spath.getDeltaPath();
        assertTrue(containsPath(d1,"x","y"));
        assertTrue(containsPath(d1,"y","z"));
        assertFalse(containsPath(d1,"y","asdasd"));

        // Step 2
        Edge e3 = edge("y","l","a",2,1002);
        vEdges = vertexEdges(e1,e2,eNo,e3);
        spath.apply(new Pair<>(vEdges,e3));

        IndexPath d2 = spath.getDeltaPath();
        assertTrue(containsPath(d2,"y","l"));
        assertTrue(containsPath(d2,"x","y","l"));
        assertTrue(containsPath(d2,"y","z","l"));

        // Step 3
        Edge e4 = edge("z","l","b",3,1003);
        Edge e5 = edge("l","m","b",4,1004);
        vEdges = vertexEdges(e1,e2,eNo,e3,e4,e5);
        spath.apply(new Pair<>(vEdges,e4));
        spath.apply(new Pair<>(vEdges,e5));

        IndexPath d3 = spath.getDeltaPath();
        assertTrue(containsPath(d3,"z","l"));
        assertTrue(containsPath(d3,"l","m"));
        assertTrue(containsPath(d3,"y","z","l"));
        assertTrue(containsPath(d3,"x","y","z","l"));
        assertTrue(containsPath(d3,"y","z","l","m"));
        assertTrue(containsPath(d3,"x","y","z","l","m"));
        assertEquals(1,countPaths(d3,"x","l")); // only one path per src→tgt
    }

    @Test
    void emptyGraph_returnsEmptyDelta() {
        SPathProcessor spath = new SPathProcessor(new Label("a"), new Label("out"));
        HashMap<String, HashSet<Edge>> vEdges = new HashMap<>();
        assertThrows(NullPointerException.class,() -> {spath.apply(new Pair<>(vEdges, null));});
    }

    @Test
    void singleEdge_matchingLabel_producesPath() {
        SPathProcessor spath = new SPathProcessor(new Label("a"), new Label("p"));
        Edge e = edge("A","B","a",0,10);
        HashMap<String, HashSet<Edge>> vEdges = vertexEdges(e);
        spath.apply(new Pair<>(vEdges,e));

        IndexPath delta = spath.getDeltaPath();
        assertTrue(containsPath(delta,"A","B"));
    }

    @Test
    void singleEdge_wrongLabel_producesNothing() {
        SPathProcessor spath = new SPathProcessor(new Label("a"), new Label("p"));
        Edge e = edge("A","B","b",0,10);
        HashMap<String, HashSet<Edge>> vEdges = vertexEdges(e);
        spath.apply(new Pair<>(vEdges,e));

        IndexPath delta = spath.getDeltaPath();
        assertTrue(delta.isEmpty());
    }

    @Test
    void twoEdgePath_producesAllValidPathsWithPlusAutomaton() {
        SPathProcessor spath = new SPathProcessor(new Label("a+"), new Label("p"));
        Edge e1 = edge("A","B","a",0,10);
        Edge e2 = edge("B","C","a",0,10);
        HashMap<String, HashSet<Edge>> vEdges = vertexEdges(e1,e2);

        spath.apply(new Pair<>(vEdges,e1));
        spath.apply(new Pair<>(vEdges,e2));

        IndexPath delta = spath.getDeltaPath();
        assertTrue(containsPath(delta,"A","B"));
        assertTrue(containsPath(delta,"B","C"));
        assertTrue(containsPath(delta,"A","C")); // combined path
    }

    @Test
    void expiredEdges_doNotFormPathDueToSnapshot() {
        SPathProcessor spath = new SPathProcessor(new Label("a+"), new Label("p"));
        Edge e1 = edge("A","B","a",0,5);
        Edge e2 = edge("B","C","a",6,10);
        HashMap<String, HashSet<Edge>> vEdges = vertexEdges(e1,e2);

        spath.apply(new Pair<>(vEdges,e1));
        spath.apply(new Pair<>(vEdges,e2));

        IndexPath delta = spath.getDeltaPath();
        assertFalse(containsPath(delta,"A","C"));
    }

    @Test
    void atMostOnePathPerVertexStateIsMaintained() {
        SPathProcessor spath = new SPathProcessor(new Label("a+"), new Label("p"));
        Edge e1 = edge("A","B","a",0,10);
        Edge e2 = edge("B","C","a",0,10);
        Edge e3 = edge("A","D","a",0,10);
        Edge e4 = edge("D","C","a",0,10);
        HashMap<String, HashSet<Edge>> vEdges = vertexEdges(e1,e2, e3, e4);

        spath.apply(new Pair<>(vEdges, e1));
        spath.apply(new Pair<>(vEdges, e2));
        spath.apply(new Pair<>(vEdges, e3));
        spath.apply(new Pair<>(vEdges, e4));

        IndexPath delta = spath.getDeltaPath();
        long countAC = countPaths(delta,"A","C");
        assertEquals(1,countAC);
    }

    @Test
    void cyclesAreHandledUnderArbitraryPathSemantics() {
        SPathProcessor spath = new SPathProcessor(new Label("a+"), new Label("p"));
        Edge e1 = edge("A","B","a",0,10);
        Edge e2 = edge("B","A","a",0,10);
        HashMap<String, HashSet<Edge>> vEdges = vertexEdges(e1,e2);

        spath.apply(new Pair<>(vEdges, e1));
        spath.apply(new Pair<>(vEdges, e2));

        IndexPath delta = spath.getDeltaPath();
        assertFalse(delta.isEmpty());
    }*/
}
