package if5.datasystems.core.models.automaton;

import lombok.Data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import if5.datasystems.core.models.aliases.Label;
import if5.datasystems.core.models.aliases.Pair;
import if5.datasystems.core.models.aliases.State;

@Data
public class Automaton {

    private HashMap<Pair<State, Label>, State> transitions;   
    private HashSet<State> states;
    private HashSet<State> finalStates;
    private State initialState;

    /*public Automaton(Label uniqueLabel){
        HashMap<Pair<State, Label>, State> transitions = new HashMap<>();
        HashSet<State> Fstates = new HashSet<>();
        this.initialState = new State("s0");
        this.states = new HashSet<>();
        this.states.add(this.initialState);
        if(uniqueLabel == null || uniqueLabel.isEmpty()){
            this.transitions = transitions;
            this.finalStates = Fstates;
            return;
        }

        int labelLength = uniqueLabel.length(); 
        char LastChar = uniqueLabel.l.charAt(labelLength-1);
        if(LastChar == '+'){ // label+: s0 -l-> s1 [ -l-> s1 ]
            State nextState = new State("s1");
            String label = uniqueLabel.l.substring(0, labelLength-1);
            transitions.put(new Pair<>(this.initialState,new Label(label)),nextState);
            Fstates.add(nextState);
            transitions.put(new Pair<>(nextState,new Label(label)),nextState);
            this.states.add(nextState);            
        }
        else if(LastChar == '*'){ // label*: s0 [ -l-> s0 ]
            String label = uniqueLabel.l.substring(0, labelLength-1);
            transitions.put(new Pair<>(this.initialState, new Label(label)), this.initialState);
            Fstates.add(this.initialState);
        }
        else{ // label:  s0 -l-> s1
            State nextState = new State("s1");
            transitions.put(new Pair<>(this.initialState, uniqueLabel), nextState);
            Fstates.add(nextState);
            this.states.add(nextState);
        }
        this.transitions = transitions;
        this.finalStates = Fstates;
        
    }*/

    public Automaton(Label labels) {
        this.transitions = new HashMap<>();
        this.states = new HashSet<>();
        this.finalStates = new HashSet<>();
        this.initialState = new State("s0");
        this.states.add(this.initialState);

        if (labels == null || labels.isEmpty()) {
            return;
        }

        Set<State> possibleStarts = new HashSet<>();
        possibleStarts.add(this.initialState);
        Set<State> lastNonStarStarts = null;
        int stateCounter = 1;

        // Split labels by comma
        String[] tokens = labels.l().split(",");

        for (String token : tokens) {
            token = token.trim();
            if (token.isEmpty()) continue;

            char lastChar = token.charAt(token.length() - 1);
            String baseLabel = token;
            boolean isStar = false;
            boolean isPlus = false;

            if (lastChar == '*') {
                isStar = true;
                baseLabel = token.substring(0, token.length() - 1);
            } else if (lastChar == '+') {
                isPlus = true;
                baseLabel = token.substring(0, token.length() - 1);
            }

            State newState = new State("s" + stateCounter++);
            this.states.add(newState);

            // Add transitions from all possibleStarts to newState on baseLabel
            for (State startState : possibleStarts) {
                this.transitions.put(new Pair<>(startState, new Label(baseLabel)), newState);
            }

            if (isStar || isPlus) {
                // Add loop transition for star or plus
                this.transitions.put(new Pair<>(newState, new Label(baseLabel)), newState);
                if (isStar) {
                    possibleStarts.add(newState); // For star, add newState to possibleStarts
                } else { // isPlus
                    possibleStarts = new HashSet<>(); // Reset possibleStarts for non star
                    possibleStarts.add(newState);
                    lastNonStarStarts = new HashSet<>(possibleStarts); // Update lastNonStarStarts
                }
            } else {
                possibleStarts = new HashSet<>(); // Reset possibleStarts for non star
                possibleStarts.add(newState);
                lastNonStarStarts = new HashSet<>(possibleStarts); // Update lastNonStarStarts
            }
        }
        // Mark final states
        this.finalStates.addAll(possibleStarts);
    }

    public boolean isFinal(State state) {
        return this.finalStates.contains(state);
    }

    public State transition(State state, Label label) {
        return this.transitions.get(new Pair<>(state, label));
    }
} 
