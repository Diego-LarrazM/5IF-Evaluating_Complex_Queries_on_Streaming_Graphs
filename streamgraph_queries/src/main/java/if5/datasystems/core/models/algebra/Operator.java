package if5.datasystems.core.models.algebra;
import java.util.function.Function;
import if5.datasystems.core.models.streamingGraph.StreamingGraph;

class Operator implements Function<StreamingGraph, StreamingGraph> {
    private Function<StreamingGraph, StreamingGraph> func;

    public Operator(Function<StreamingGraph, StreamingGraph> func) {
        this.func = func;
    }

    @Override
    public StreamingGraph apply(StreamingGraph s) {
        return func.apply(s);
    }

    // In-place (before o operator)
    public void composeAfter(Operator before) {
        this.func = this.func.compose(before);
    }

    // In-place (operator o after)
    public void composeBefore(Operator after) {
        this.func = this.func.andThen(after);
    }
}

