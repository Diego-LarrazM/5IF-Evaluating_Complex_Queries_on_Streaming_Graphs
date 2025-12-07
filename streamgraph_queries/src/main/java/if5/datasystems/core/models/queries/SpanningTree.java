package if5.datasystems.core.models.queries;

import lombok.Data;
import java.util.Map;

import if5.datasystems.core.models.aliases.State;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

@Data
public class SpanningTree {
    
    private Map<IndexNode, List<IndexNode>> tree; // à adapter pour pouvoir obtenir les nodes

    public SpanningTree() {
        this.tree = new HashMap<>();
    }

    public SpanningTree(IndexNode rootNode) {
        this.tree = new HashMap<>();
        this.tree.put(rootNode, new ArrayList<>());
    }

    public SpanningTree(IndexNode rootNode, List<IndexNode> children) {
        if (children == null) {
            children = new ArrayList<>();
        }
        this.tree = new HashMap<>();
        this.tree.put(rootNode, children);
    }

    public void addNode(IndexNode parent, IndexNode child) {
        if(parent == null || child == null) {return;}
        this.tree.computeIfAbsent(parent, k -> new ArrayList<>()).add(child);
    }

    public List<IndexNode> getChildren(IndexNode node) {
        return this.tree.get(node);
    }

    public boolean contains(String name, State state) {return this.tree.containsKey(new IndexNode(name,state));}
}   