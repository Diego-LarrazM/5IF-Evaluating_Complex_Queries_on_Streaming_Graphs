package if5.datasystems.core.models.aliases;

import java.io.Serializable;

public class State implements Serializable {
    public String s;

    public State() {}

    public State(String s) {
        this.s = (s == null) ? "" : s;
    }

    public String s() {return s;}
    public int length() { return s.length(); }
    public boolean isEmpty() { return s.isEmpty(); }

    @Override
    public String toString() {return s;}
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof State)) return false;
        State state = (State) o;
        return s.equals(state.s);
    }
    @Override
    public int hashCode() {
        return s.hashCode();
    }
}