package if5.datasystems.core.models.algebra;

import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OperatorTest {

    @Test
    void testApply() {
        Operator<Integer, Integer> op =
                new Operator<>(x -> x * 2);

        Integer result = op.apply(5);

        assertEquals(10, result);
    }

    @Test
    void testComposeBefore() {
        Operator<Integer, Integer> multiplyBy2 =
                new Operator<>(x -> x * 2);

        Operator<String, Integer> parseInt =
                new Operator<>(Integer::parseInt);

        Operator<String, Integer> composed =
                multiplyBy2.composeBefore(parseInt);

        Integer result = composed.apply("5");

        // parseInt("5") -> 5
        // multiplyBy2(5) -> 10
        assertEquals(10, result);
    }

    @Test
    void testComposeAfter() {
        Operator<Integer, Integer> multiplyBy2 =
                new Operator<>(x -> x * 2);

        Operator<Integer, String> toString =
                new Operator<>(Object::toString);

        Operator<Integer, String> composed =
                multiplyBy2.composeAfter(toString);

        String result = composed.apply(5);

        // multiplyBy2(5) -> 10
        // toString(10) -> "10"
        assertEquals("10", result);
    }

    @Test
    void testComposeBeforeAndAfterChain() {
        Operator<Integer, Integer> square =
                new Operator<>(x -> x * x);

        Operator<String, Integer> parseInt =
                new Operator<>(Integer::parseInt);

        Operator<Integer, String> toString =
                new Operator<>(Object::toString);

        Operator<String, String> pipeline =
                square.composeBefore(parseInt).composeAfter(toString);

        String result = pipeline.apply("4");

        // parseInt("4") -> 4
        // square(4) -> 16
        // toString(16) -> "16"
        assertEquals("16", result);
    }

    @Test
    void testMultiChain() {
        Operator<Integer, String> composed =
                new Operator<Integer, Integer>(x -> x * 2)
                .composeAfter(
                        new Operator<>(Object::toString)
                ).composeAfter(
                        new Operator<>(s -> "Value: " + s)
                );

        String result = composed.apply(5);

        // multiplyBy2(5) -> 10
        // toString(10) -> "10"
        // lambda(10) -> "Value: 10"
        assertEquals("Value: 10", result);
    }

    @Test
    void testIdentityComposition() {
        Operator<Integer, Integer> identity =
                new Operator<>(Function.identity());

        Operator<Integer, Integer> plusOne =
                new Operator<>(x -> x + 1);

        Operator<Integer, Integer> composed =
                plusOne.composeBefore(identity);

        assertEquals(6, composed.apply(5));
    }

    /////////////////////////////////////////////////////////////////
    // Check it behaves well as a Function
    /////////////////////////////////////////////////////////////////
    
    @Test
    void testFunctionAndThenOnOperator() {
        Operator<Integer, Integer> multiplyBy2 =
                new Operator<>(x -> x * 2);

        Function<Integer, Integer> plusOne =
                x -> x + 1;

        Function<Integer, Integer> composed =
                multiplyBy2.andThen(plusOne);

        Integer result = composed.apply(5);

        // multiplyBy2(5) -> 10
        // plusOne(10) -> 11
        assertEquals(11, result);
    }

    @Test
    void testFunctionComposeOnOperator() {
        Operator<Integer, Integer> multiplyBy2 =
                new Operator<>(x -> x * 2);

        Function<Integer, Integer> plusOne =
                x -> x + 1;

        Function<Integer, Integer> composed =
                multiplyBy2.compose(plusOne);

        Integer result = composed.apply(5);

        // plusOne(5) -> 6
        // multiplyBy2(6) -> 12
        assertEquals(12, result);
    }

    @Test
    void testOperatorUsedAsFunctionInChain() {
        Operator<Integer, Integer> square =
                new Operator<>(x -> x * x);

        Operator<Integer, Integer> plusOne =
                new Operator<>(x -> x + 1);

        Function<Integer, Integer> pipeline =
                square.andThen(plusOne).andThen(square);

        Integer result = pipeline.apply(2);

        // square(2) -> 4
        // plusOne(4) -> 5
        // square(5) -> 25
        assertEquals(25, result);
    }

    @Test
    void testMixedOperatorAndFunctionCompose() {
        Operator<String, Integer> parseInt =
                new Operator<>(Integer::parseInt);

        Function<Integer, Integer> times10 =
                x -> x * 10;

        Function<String, Integer> composed =
                times10.compose(parseInt);

        Integer result = composed.apply("7");

        // parseInt("7") -> 7
        // times10(7) -> 70
        assertEquals(70, result);
    }
}
