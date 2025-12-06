package if5.datasystems.core.models.queries;

import if5.datasystems.core.models.automaton.State;
import lombok.Data;

@Data
public class IndexNode {
    private String name;
    private State state;

    public IndexNode(String name, String state) {
        this.name = name;
        this.state = new State(state);
    }
}