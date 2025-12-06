package if5.datasystems.core.models.automaton;

import if5.datasystems.core.models.streamingGraph.Label;

import lombok.Data;
import java.util.Map;
import java.util.Set;

@Data
public class Automaton {

    private Map<Map.Entry<State, Label>, State> states;
    private Set<State> finalStates;

    public boolean isFinal(State state) {
        return this.finalStates.contains(state);
    }

    public State transition(State state, Label label) {
        return this.states.get(Map.entry(state, label));
    }
} 
