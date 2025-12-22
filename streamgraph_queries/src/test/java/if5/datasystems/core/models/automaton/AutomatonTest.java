package if5.datasystems.core.models.automaton;

import if5.datasystems.core.models.aliases.Label;
import if5.datasystems.core.models.aliases.Pair;
import if5.datasystems.core.models.aliases.State;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class AutomatonTest {

    // ---------- Simple label ----------
    @Test
    void testSimpleLabel() {
        Automaton automaton = new Automaton("aLabel");

        State s0 = new State("s0");
        State s1 = new State("s1");
        State s2 = new State("s2");

        Label labelA = new Label("aLabel");

        // State existance
        assertTrue(automaton.getStates().contains(s0));
        assertEquals(automaton.getStates(), new HashSet<State>() {{ add(s0); add(s1); }});
        // Final state
        assertEquals(automaton.getFinalStates(), new HashSet<State>() {{ add(s1); }});
        // isFinal
        assertTrue(automaton.isFinal(s1));
        assertFalse(automaton.isFinal(s0));
        
        // Transition
        assertEquals(s1, automaton.transition(s0, labelA));
        // No self-transition
        assertNull(automaton.transition(s1, labelA));
        // No other transitions
        assertNull(automaton.transition(s0, new Label("bLabel")));
        assertNull(automaton.transition(s2, labelA));
    }

    // ---------- Label+ ----------
    @Test
    void testLabelPlus() {
        Automaton automaton = new Automaton("bLabel+");

        State s0 = new State("s0");
        State s1 = new State("s1");
        State s2 = new State("s2");

        // States
        assertEquals(automaton.getStates(), new HashSet<State>() {{ add(s0); add(s1); }});
        // Final state
        assertEquals(automaton.getFinalStates(), new HashSet<State>() {{ add(s1); }});

        Label labelB = new Label("bLabel");

        // s0 -b-> s1
        assertEquals(s1, automaton.transition(s0, labelB));
        // s1 -b-> s1 (loop)
        assertEquals(s1, automaton.transition(s1, labelB));
        // isFinal
        assertTrue(automaton.isFinal(s1));
        assertFalse(automaton.isFinal(s0));
        // no other transitions
        assertNull(automaton.transition(s0, new Label("aLabel"))); 
        assertNull(automaton.transition(s1, new Label("aLabel")));
        assertNull(automaton.transition(s1, new Label("bLab")));
        assertNull(automaton.transition(s2, labelB));
    }

    // ---------- Label* ----------
    @Test
    void testLabelStar() {
        Automaton automaton = new Automaton("cLabel*");

        State s0 = new State("s0");
        State s1 = new State("s1");

        Label labelC = new Label("cLabel");

        // States
        assertEquals(1, automaton.getStates().size());
        assertTrue(automaton.getStates().contains(s0));

        // Final state is s0 itself
        assertEquals(automaton.getFinalStates(), new HashSet<State>() {{ add(s0); }});

        // s0 -c-> s0
        assertEquals(s0, automaton.transition(s0, labelC));
        // isFinal
        assertTrue(automaton.isFinal(s0));
        // no other transitions
        assertNull(automaton.transition(s0, new Label("a"))); 
        assertNull(automaton.transition(s0, new Label("cLabe"))); 
        assertNull(automaton.transition(s1, labelC));
    }

    // ---------- Edge cases ----------
    @Test
    void testEmptyLabel() {
        Automaton automaton = new Automaton("");

        State s0 = new State("s0");
        State s1 = new State("s1");

        Label emptyLabel = new Label("");
        Label label = new Label("a");

        assertNull(automaton.transition(s0, emptyLabel));
        assertNull(automaton.transition(s0, label));
        assertTrue(automaton.getFinalStates().isEmpty());
        assertFalse(automaton.isFinal(s0));
        assertTrue(automaton.getTransitions().isEmpty());
        assertTrue(automaton.getStates().contains(s0));
    }

    @Test
    void testUnknownTransitionReturnsNull() {
        Automaton automaton = new Automaton("x");

        State s0 = new State("s0");
        Label unknown = new Label("y");

        assertNull(automaton.transition(s0, unknown));
    }
}
