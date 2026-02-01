package if5.datasystems.core.models.queries;

import if5.datasystems.core.models.aliases.Label;
import if5.datasystems.core.models.aliases.Pair;
import if5.datasystems.core.models.aliases.State;
import lombok.Data;

@Data
public class IndexNode {
    private String name;
    private State state;
    private long startTime;
    private long expiricy;
    private Pair<String, State> parent;
    private Label fromLabel;

    public IndexNode(String name, State state) {
        this.name = name;
        this.state = state;
    }

    public IndexNode(String name, State state, long startTime, long expiricy) {
        this.name = name;
        this.state = state;
        this.startTime = startTime;
        this.expiricy = expiricy;
    }

    public Pair<String, State> getKey() {
        return new Pair<>(this.name, this.state);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IndexNode)) return false;
        IndexNode other = (IndexNode) o;
        return name.equals(other.name) && state.equals(other.state);
    }

    @Override
    public int hashCode() {
        return 31 * name.hashCode() + state.hashCode();
    }
}