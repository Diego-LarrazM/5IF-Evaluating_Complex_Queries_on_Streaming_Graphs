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
        Automaton automaton = new Automaton(new Label("aLabel"));

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
        Automaton automaton = new Automaton(new Label("bLabel+"));

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
        Automaton automaton = new Automaton(new Label("cLabel*"));

        State s0 = new State("s0");
        State s1 = new State("s1");

        Label labelC = new Label("cLabel");

        // States
        assertEquals(2, automaton.getStates().size());
        assertTrue(automaton.getStates().contains(s0));
        assertTrue(automaton.getStates().contains(s1));

        // Final state is s0 itself
        assertEquals(automaton.getFinalStates(), new HashSet<State>() {{ add(s0); add(s1); }});

        // s0 -c-> s1
        assertEquals(s1, automaton.transition(s0, labelC));
        // s1 -c-> s1 (loop)
        assertEquals(s1, automaton.transition(s1, labelC));
        // isFinal
        assertTrue(automaton.isFinal(s0));
        assertTrue(automaton.isFinal(s1));
        // no other transitions
        assertNull(automaton.transition(s0, new Label("a"))); 
        assertNull(automaton.transition(s0, new Label("cLabe"))); 
    }

    // ---------- Edge cases ----------
    @Test
    void testEmptyLabel() {
        Automaton automaton = new Automaton(new Label(""));

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
        Automaton automaton = new Automaton(new Label("x"));

        State s0 = new State("s0");
        Label unknown = new Label("y");

        assertNull(automaton.transition(s0, unknown));
    }

    // ---------- Complex sequence 1 ----------
    @Test
    void testSequenceStarPlusPlain() {
        // Sequence: a*, b+, c
        Automaton automaton = new Automaton(new Label("a*,b+,c"));

        State s0 = new State("s0");
        State s1 = new State("s1");
        State s2 = new State("s2");
        State s3 = new State("s3");

        Label a = new Label("a");
        Label b = new Label("b");
        Label c = new Label("c");

        // States
        assertEquals(new HashSet<State>() {{
            add(s0);
            add(s1);
            add(s2);
            add(s3);
        }}, automaton.getStates());

        // Final states
        assertEquals(new HashSet<State>() {{
            add(s3);
        }}, automaton.getFinalStates());

        // Transitions for a* (loop on s1)
        assertEquals(s1, automaton.transition(s0, a));
        assertEquals(s1, automaton.transition(s1, a));

        // Transitions for b+ (s1 -> s2, loop on s2)
        assertEquals(s2, automaton.transition(s1, b));
        assertEquals(s2, automaton.transition(s2, b));

        // Transition for c (s2 -> s3)
        assertEquals(s3, automaton.transition(s2, c));

        // s0 -> c should not exist
        assertNull(automaton.transition(s0, c));

        // No extra transitions
        assertNull(automaton.transition(s3, a));
        assertNull(automaton.transition(s3, b));

        // Final states check
        assertFalse(automaton.isFinal(s0));
        assertFalse(automaton.isFinal(s1));
        assertFalse(automaton.isFinal(s2));
        assertTrue(automaton.isFinal(s3));
    }

    // ---------- Complex sequence 2 ----------
    @Test
    void testComplexSequence() {
        // Sequence: a*, b+, c, a*
        Automaton automaton = new Automaton(new Label("a*,b+,c,a*"));

        State s0 = new State("s0");
        State s1 = new State("s1");
        State s2 = new State("s2");
        State s3 = new State("s3");
        State s4 = new State("s4");

        Label a = new Label("a");
        Label b = new Label("b");
        Label c = new Label("c");

        // States
        assertEquals(new HashSet<State>() {{
            add(s0);
            add(s1);
            add(s2);
            add(s3);
            add(s4);
        }}, automaton.getStates());

        // Final states
        assertEquals(new HashSet<State>() {{
            add(s3);
            add(s4);
        }}, automaton.getFinalStates());

        // s0->a*s1 loop on s1
        assertEquals(s1, automaton.transition(s0, a));
        assertEquals(s1, automaton.transition(s1, a));

        // b+ transition (s0 -> s2, s1->s2, loop on s2)
        assertEquals(s2, automaton.transition(s0, b));
        assertEquals(s2, automaton.transition(s1, b));
        assertEquals(s2, automaton.transition(s2, b));

        // c transition (s2 -> s3)
        assertNull(automaton.transition(s0, c));
        assertNull(automaton.transition(s1, c));
        assertEquals(s3, automaton.transition(s2, c));

        // a* loop on s3 (s3 -> s4, loop on s4)
        assertEquals(s4, automaton.transition(s3, a));
        assertEquals(s4, automaton.transition(s4, a));

        // Check final states
        assertTrue(automaton.isFinal(s3));
        assertTrue(automaton.isFinal(s4));
    }

    // ---------- Full stars ----------
    @Test
    void fullStars() {
        // Sequence: a*, b*, c*, d*
        Automaton automaton = new Automaton(new Label("a*,b*,c*,d*"));

        State s0 = new State("s0");
        State s1 = new State("s1");
        State s2 = new State("s2");
        State s3 = new State("s3");
        State s4 = new State("s4");

        Label a = new Label("a");
        Label b = new Label("b");
        Label c = new Label("c");
        Label d = new Label("d");

        // States
        assertEquals(new HashSet<State>() {{
            add(s0);
            add(s1);
            add(s2);
            add(s3);
            add(s4);
        }}, automaton.getStates());

        // Final states
        assertEquals(new HashSet<State>() {{
            add(s0);
            add(s1);
            add(s2);
            add(s3);
            add(s4);
        }}, automaton.getFinalStates());

        // s0->a*s1 loop on s1
        assertEquals(s1, automaton.transition(s0, a));
        assertEquals(s1, automaton.transition(s1, a));

        // b* transitions (s0 -> s2, s1->s2, loop on s2)
        assertEquals(s2, automaton.transition(s0, b));
        assertEquals(s2, automaton.transition(s1, b));
        assertEquals(s2, automaton.transition(s2, b));

        // c* transition (s0 -> s3, s1->s3, s2->s3, loop on s3)
        assertEquals(s3, automaton.transition(s0, c));
        assertEquals(s3, automaton.transition(s1, c));
        assertEquals(s3, automaton.transition(s2, c));
        assertEquals(s3, automaton.transition(s3, c));

        // d* transition (s0 -> s4, s1->s4, s2->s4, s3->s4, loop on s4)
        assertEquals(s4, automaton.transition(s0, d));
        assertEquals(s4, automaton.transition(s1, d));
        assertEquals(s4, automaton.transition(s2, d));
        assertEquals(s4, automaton.transition(s3, d));
        assertEquals(s4, automaton.transition(s4, d));

        // Check final states
        assertTrue(automaton.isFinal(s0));
        assertTrue(automaton.isFinal(s1));
        assertTrue(automaton.isFinal(s2));
        assertTrue(automaton.isFinal(s3));
        assertTrue(automaton.isFinal(s4));
    }

    // ---------- StarSandwich ----------
    @Test
    void starSandwich() {
        // Sequence: a*, b*, c, d*, e*
        Automaton automaton = new Automaton(new Label("a*,b*,c,d*,e*"));

        State s0 = new State("s0");
        State s1 = new State("s1");
        State s2 = new State("s2");
        State s3 = new State("s3");
        State s4 = new State("s4");
        State s5 = new State("s5");

        Label a = new Label("a");
        Label b = new Label("b");
        Label c = new Label("c");
        Label d = new Label("d");
        Label e = new Label("e");

        // States
        assertEquals(new HashSet<State>() {{
            add(s0);
            add(s1);
            add(s2);
            add(s3);
            add(s4);
            add(s5);
        }}, automaton.getStates());

        // Final states
        assertEquals(new HashSet<State>() {{
            add(s3);
            add(s4);
            add(s5);
        }}, automaton.getFinalStates());

        // s0->a*s1 loop on s1
        assertEquals(s1, automaton.transition(s0, a));
        assertEquals(s1, automaton.transition(s1, a));

        // b* transitions (s0 -> s2, s1->s2, loop on s2)
        assertEquals(s2, automaton.transition(s0, b));
        assertEquals(s2, automaton.transition(s1, b));
        assertEquals(s2, automaton.transition(s2, b));

        // c transition (s0 -> s3, s1->s3, s2->s3)
        assertEquals(s3, automaton.transition(s0, c));
        assertEquals(s3, automaton.transition(s1, c));
        assertEquals(s3, automaton.transition(s2, c));

        // d* transition (s3 -> s4, loop on s4)
        assertEquals(s4, automaton.transition(s3, d));
        assertEquals(s4, automaton.transition(s4, d));

        // e* transition (s3 -> s5, s4->s5, loop on s5)
        assertEquals(s5, automaton.transition(s3, e));
        assertEquals(s5, automaton.transition(s4, e));
        assertEquals(s5, automaton.transition(s5, e));

        // Check final states
        assertTrue(automaton.isFinal(s3));
        assertTrue(automaton.isFinal(s4));
        assertTrue(automaton.isFinal(s5));
    }

    // ---------- StarSandwich2 ----------
    @Test
    void starSandwich2() {
        // Sequence: a*, b, c+, d*, e*
        Automaton automaton = new Automaton(new Label("a*,b,c+,d*,e*"));

        State s0 = new State("s0");
        State s1 = new State("s1");
        State s2 = new State("s2");
        State s3 = new State("s3");
        State s4 = new State("s4");
        State s5 = new State("s5");

        Label a = new Label("a");
        Label b = new Label("b");
        Label c = new Label("c");
        Label d = new Label("d");
        Label e = new Label("e");

        // States
        assertEquals(new HashSet<State>() {{
            add(s0);
            add(s1);
            add(s2);
            add(s3);
            add(s4);
            add(s5);
        }}, automaton.getStates());

        // Final states
        assertEquals(new HashSet<State>() {{
            add(s3);
            add(s4);
            add(s5);
        }}, automaton.getFinalStates());

        // s0->a*s1 loop on s1
        assertEquals(s1, automaton.transition(s0, a));
        assertEquals(s1, automaton.transition(s1, a));

        // b transition (s0 -> s2, s1->s2)
        assertEquals(s2, automaton.transition(s0, b));
        assertEquals(s2, automaton.transition(s1, b));

        // c+ transition (s2 -> s3, loop on s3)
        assertEquals(s3, automaton.transition(s2, c));
        assertEquals(s3, automaton.transition(s3, c));

        // d* transition (s3 -> s4, loop on s4)
        assertEquals(s4, automaton.transition(s3, d));
        assertEquals(s4, automaton.transition(s4, d));

        // e* transition (s3->s5, s4 -> s5, loop on s5)
        assertEquals(s5, automaton.transition(s3, e));
        assertEquals(s5, automaton.transition(s4, e));
        assertEquals(s5, automaton.transition(s5, e));

        // Check final states
        assertTrue(automaton.isFinal(s3));
        assertTrue(automaton.isFinal(s4));
        assertTrue(automaton.isFinal(s5));
    }

    // ---------- FinalBoss ----------
    @Test
    void FinalBoss() {
        // Sequence: a+, p, b*, c*, d, d*, f
        Automaton automaton = new Automaton(new Label("a+,p,b*,c*,d,d*,f"));

        State s0 = new State("s0");
        State s1 = new State("s1");
        State s2 = new State("s2");
        State s3 = new State("s3");
        State s4 = new State("s4");
        State s5 = new State("s5");
        State s6 = new State("s6");
        State s7 = new State("s7");

        Label a = new Label("a");
        Label p = new Label("p");
        Label b = new Label("b");
        Label c = new Label("c");
        Label d = new Label("d");
        Label f = new Label("f");

        // States
        assertEquals(new HashSet<State>() {{
            add(s0);
            add(s1);
            add(s2);
            add(s3);
            add(s4);
            add(s5);
            add(s6);
            add(s7);
        }}, automaton.getStates());

        // Final states
        assertEquals(new HashSet<State>() {{
            add(s7);
        }}, automaton.getFinalStates());

        // a+ transition (s0 -> s1, loop on s1)
        assertEquals(s1, automaton.transition(s0, a));
        assertEquals(s1, automaton.transition(s1, a));

        // p transition (s1 -> s2)
        assertEquals(s2, automaton.transition(s1, p));

        // b* transition (s2 -> s3, loop on s3)
        assertEquals(s3, automaton.transition(s2, b));
        assertEquals(s3, automaton.transition(s3, b));

        // c* transition (s2 -> s4, s3->s4, loop on s4)
        assertEquals(s4, automaton.transition(s2, c));
        assertEquals(s4, automaton.transition(s3, c));
        assertEquals(s4, automaton.transition(s4, c));

        // d transition (s2->s5, s3 -> s5, s4->s5)
        assertEquals(s5, automaton.transition(s2, d));
        assertEquals(s5, automaton.transition(s3, d));
        assertEquals(s5, automaton.transition(s4, d));

        // d* transition (s5 -> s6, loop on s6)
        assertEquals(s6, automaton.transition(s5, d));
        assertEquals(s6, automaton.transition(s6, d));

        // f transition (s5 -> s7, s6->s7)
        assertEquals(s7, automaton.transition(s5, f));
        assertEquals(s7, automaton.transition(s6, f));

        // Check final states
        assertTrue(automaton.isFinal(s7));
    }
}
