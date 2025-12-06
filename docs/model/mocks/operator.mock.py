#processors
def op1():
def WSCAN()
def PATTERN()
  PATH FILTER SELECT UNION INTERSECT
  

class operator:
  function : JOIN(PATH(JOIN(WSCAN(op1()),...)),...)
  def __call__():
    self.function(x)

Operator op = new Operator(WSCAN);
op.compose(PATH):
   this.function = this.function.andThen(PATH); // PATH(WSCAN)

op(x) // PATH(WSCAN(x))

f = lambda x: x+1
f = lambda y: f(y)*2  # f(x) = (x+1)*2

import java.util.function.Function;
 
public class Test {
    public static void main(String[] args) {
        Function<Integer, Integer> f = x -> x + 1;
 
        // reassign f to compose on top of itself
        f = f.andThen(y -> y * 2);   // f(x) = (x + 1) * 2
 
        System.out.println(f.apply(3));  // (3 + 1) * 2 = 8
    }
}


# We need a way to define congonction functions for JOIN operator �1 = (𝑡𝑟𝑔1 = 𝑡𝑟𝑔2 ∧ 𝑠𝑟𝑐1 = 𝑠𝑟𝑐3 ∧ 𝑠𝑟𝑐2 = 𝑡𝑟𝑔3),