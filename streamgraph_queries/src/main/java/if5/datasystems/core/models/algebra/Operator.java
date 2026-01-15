package if5.datasystems.core.models.algebra;
import java.util.function.Function;
import if5.datasystems.core.models.streamingGraph.StreamingGraph;


class Operator<I,O> implements Function<I, O> {
    private Function<I, O> func;

    public Operator(Function<I, O> func) {
        this.func = func;
    }

    @Override
    public O apply(I s) {
        return func.apply(s);
    }

    // In-place x-> this.func(before(x))
    public <T> Operator<T,O> composeBefore(Operator<T,I> before) {
        return new Operator<>(before.andThen(this.func));
    }

    // In-place x-> after(this.func(x))
    public <T> Operator<I,T> composeAfter(Operator<O, T> after) {
        return new Operator<>(this.func.andThen(after));
    }
}

