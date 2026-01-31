package if5.datasystems.core.models.queries;

import lombok.Data;
import java.util.Map;
import java.util.HashMap;

import if5.datasystems.core.models.aliases.Pair;
import if5.datasystems.core.models.aliases.State;

@Data
public class SpanningTree {
    
    private Map<Pair<String, State>, IndexNode> nodes;
    private IndexNode root;

    public SpanningTree(IndexNode rootNode) {
        this.root = rootNode;
        this.nodes = new HashMap<>();
        this.addNode(this.root);
    }

    public void addNode(IndexNode node) {
        if(node == null) {return;}
        Pair<String, State> nodeKey = new Pair<>(node.getName(),node.getState());
        this.nodes.put(nodeKey, node);
    }

    public IndexNode getNode(String name, State state) {
        if(name == null || state == null) {return null;}
        Pair<String, State> nodeKey = new Pair<>(name,state);
        return this.nodes.get(nodeKey);
    }

    public IndexNode getNode(Pair<String, State> nodeKey) {
        if(nodeKey == null) {return null;}
        return this.nodes.get(nodeKey);
    }

    public IndexNode getNode(IndexNode node) {
        if(node == null) {return null;}
        Pair<String, State> nodeKey = new Pair<>(node.getName(),node.getState());
        return this.nodes.get(nodeKey);
    }

    public boolean contains(String name, State state) {
        Pair<String, State> nodeKey = new Pair<>(name,state);
        return this.nodes.containsKey(nodeKey);
    }

    public boolean contains(Pair<String, State> nodeKey) {
        return this.nodes.containsKey(nodeKey);
    }
}   