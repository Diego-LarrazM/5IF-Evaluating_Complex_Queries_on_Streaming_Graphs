


class Predicate:
  class body:
    predicate: Predicate | function
    input_mapping # tells us how input from SGQ maps to predicate inputs well see later
  head: str
  bodies: list[body]
  def __call__ (): 
    res =  AND (self.body()) # well see how to implement AND


def SGQParser: