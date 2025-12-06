package if5.datasystems.core.models.algebra;
import java.util.function.Function;
import if5.datasystems.core.models.streaminggraph.StreamingGraph;

class Operator implements Function<StreamingGraph, StreamingGraph> {
    private Function<Integer, Integer> func;

    public Operator(Function<StreamingGraph, StreamingGraph> func) {
        this.func = func;
    }

    @Override
    public Integer apply(Integer x) {
        return func.apply(x);
    }

    // In-place (before o operator)
    public void composeAfter(Operator before) {
        this.func = x -> this.func.apply(before.apply(x));
    }

    // In-place (operator o after)
    public void composeBefore(Operator after) {
        this.func = x -> after.apply(this.func.apply(x));
    }
}

