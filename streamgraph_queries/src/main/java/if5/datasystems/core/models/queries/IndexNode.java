package if5.datasystems.core.models.queries;

import java.sql.Time;

import if5.datasystems.core.models.aliases.Label;
import if5.datasystems.core.models.aliases.Pair;
import if5.datasystems.core.models.aliases.State;
import lombok.Data;

@Data
public class IndexNode {
    private String name;
    private State state;
    private Time startTime;
    private Time expiricy;
    private Pair<String, State> parent;
    private Label fromLabel;

    public IndexNode(String name, State state) {
        this.name = name;
        this.state = state;
    }

    public IndexNode(String name, State state, Time startTime, Time expiricy) {
        this.name = name;
        this.state = state;
        this.startTime = startTime;
        this.expiricy = expiricy;
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