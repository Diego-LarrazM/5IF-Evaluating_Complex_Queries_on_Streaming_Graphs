package if5.datasystems.core.models.automaton;

import lombok.Data;

import java.util.HashMap;
import java.util.HashSet;

import if5.datasystems.core.models.aliases.Label;
import if5.datasystems.core.models.aliases.Pair;
import if5.datasystems.core.models.aliases.State;

@Data
public class Automaton {

    private HashMap<Pair<State, Label>, State> transitions;   
    private HashSet<State> states;
    private HashSet<State> finalStates;
    private State initialState;

    public Automaton(String uniqueLabel){
        HashMap<Pair<State, Label>, State> transitions = new HashMap<>();
        HashSet<State> Fstates = new HashSet<>();
        this.initialState = new State("s0");
        this.states = new HashSet<>();
        this.states.add(this.initialState);

        int labelLength = uniqueLabel.length(); 
        char LastChar = uniqueLabel.charAt(labelLength-1);
        if(LastChar == '+'){ // label+: s0 -l-> s1 [ -l-> s1 ]
            State nextState = new State("s1");
            String label = uniqueLabel.substring(0, labelLength-1);
            transitions.put(new Pair<>(this.initialState,new Label(label)),nextState);
            Fstates.add(nextState);
            transitions.put(new Pair<>(nextState,new Label(label)),nextState);
            this.states.add(nextState);            
        }
        else if(LastChar == '*'){ // label*: s0 [ -l-> s0 ]
            String label = uniqueLabel.substring(0, labelLength-1);
            transitions.put(new Pair<>(this.initialState, new Label(label)), this.initialState);
            Fstates.add(this.initialState);
        }
        else{ // label:  s0 -l-> s1
            State nextState = new State("s1");
            transitions.put(new Pair<>(this.initialState, new Label(uniqueLabel)), nextState);
            Fstates.add(nextState);
            this.states.add(nextState);
        }
        this.transitions = transitions;
        this.finalStates = Fstates;
        
    }

    public boolean isFinal(State state) {
        return this.finalStates.contains(state);
    }

    public State transition(State state, Label label) {
        return this.transitions.get(new Pair<>(state, label));
    }
} 
