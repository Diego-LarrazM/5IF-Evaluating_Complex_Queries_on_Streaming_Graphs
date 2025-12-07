package if5.datasystems.core.models.queries;

import java.sql.Time;

import if5.datasystems.core.models.aliases.State;
import lombok.Data;

@Data
public class IndexNode {
    private String name;
    private State state;
    private Time startTime;
    private Time expiricy;
    private IndexNode parent;

    public IndexNode(String name, State state) {
        this.name = name;
        this.state = state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IndexNode)) return false;
        IndexNode other = (IndexNode) o;
        return name.equals(other.name) && state == other.state;
    }

    @Override
    public int hashCode() {
        return 31 * name.hashCode() + state.hashCode();
    }
}