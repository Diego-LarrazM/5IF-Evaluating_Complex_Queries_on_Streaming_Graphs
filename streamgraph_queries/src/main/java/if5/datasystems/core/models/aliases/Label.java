package if5.datasystems.core.models.aliases;

import java.io.Serializable;
public class Label implements Serializable {
    public String l;

    public Label() {}

    public Label(String l) {
        this.l = (l == null) ? "" : l;
    }

    public String l() {return l;}
    public int length() { return l.length(); }
    public boolean isEmpty() { return l.isEmpty(); }

    @Override
    public String toString() {return l;}
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Label)) return false;
        Label label = (Label) o;
        return l.equals(label.l);
    }
    @Override
    public int hashCode() {
        return l.hashCode();
    }
}
