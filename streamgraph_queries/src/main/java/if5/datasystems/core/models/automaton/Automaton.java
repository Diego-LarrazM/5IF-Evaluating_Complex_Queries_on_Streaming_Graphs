package if5.datasystems.core.models.automaton;

import lombok.Data;
import java.util.Map;
import java.util.Set;

import if5.datasystems.core.models.aliases.Label;
import if5.datasystems.core.models.aliases.Pair;
import if5.datasystems.core.models.aliases.State;

@Data
public class Automaton {

    private Map<Pair<State, Label>, State> states;   
    private Set<State> finalStates;

    public boolean isFinal(State state) {
        return this.finalStates.contains(state);
    }

    public State transition(State state, Label label) {
        return this.states.get(new Pair<>(state, label));
    }
} 
