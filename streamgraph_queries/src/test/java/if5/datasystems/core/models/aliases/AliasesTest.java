package if5.datasystems.core.models.aliases;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AliasesTest {

    // ---------- Label Tests ----------
    @Test
    void testLabelBasics() {
        Label label = new Label("hello");

        assertEquals("hello", label.l());
        assertEquals(5, label.length());
        assertFalse(label.isEmpty());
        assertEquals("hello", label.toString());
    }

    @Test
    void testLabelEmpty() {
        Label label = new Label("");

        assertEquals("", label.l());
        assertEquals(0, label.length());
        assertTrue(label.isEmpty());
        assertEquals("", label.toString());
    }
    @Test
    void testLabelNullBecomesEmpty() {
        Label label = new Label(null);

        assertEquals("", label.l());
        assertEquals(0, label.length());
        assertTrue(label.isEmpty());
        assertEquals("", label.toString());
    }

    // ---------- State Tests ----------
    @Test
    void testStateBasics() {
        State state = new State("running");

        assertEquals("running", state.s());
        assertEquals(7, state.length());
        assertFalse(state.isEmpty());
        assertEquals("running", state.toString());
    }

    @Test
    void testStateEmpty() {
        State state = new State("");

        assertEquals("", state.s());
        assertEquals(0, state.length());
        assertTrue(state.isEmpty());
        assertEquals("", state.toString());
    }

    @Test
    void testStateNullBecomesEmpty() {
        State state = new State(null);

        assertEquals("", state.s());
        assertEquals(0, state.length());
        assertTrue(state.isEmpty());
        assertEquals("", state.toString());
    }

    // ---------- Pair Tests ----------
    @Test
    void testPairGenerics() {
        Pair<Integer, String> pair = new Pair<>(42, "answer");

        assertEquals(42, pair.first());
        assertEquals("answer", pair.second());
    }

    @Test
    void testPairWithObjects() {
        Pair<Label, State> pair = new Pair<>(new Label("L"), new State("S"));

        assertEquals("L", pair.first().l());
        assertEquals("S", pair.second().s());
    }

    // ---------- Triple Tests ----------
    @Test
    void testTripleGenerics() {
        Triple<Integer, String, Double> triple = new Triple<>(1, "two", 3.0);

        assertEquals(1, triple.first());
        assertEquals("two", triple.second());
        assertEquals(3.0, triple.third());
    }

    @Test
    void testTripleWithAliases() {
        Triple<Label, State, Pair<String, Integer>> triple =
                new Triple<>(new Label("L"), new State("S"), new Pair<>("P", 99));

        assertEquals("L", triple.first().l());
        assertEquals("S", triple.second().s());
        assertEquals("P", triple.third().first());
        assertEquals(99, triple.third().second());
    }
}
