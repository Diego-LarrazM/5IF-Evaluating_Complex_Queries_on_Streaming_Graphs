Alias Label: str
Alias State: str
Alias Time: int
Alias SpanningTree: dict<IndexNode, list<IndexNode>>
Alias IndexPath: dist<IndexNode, SpanningTree>

# processors
def label(...polymorphism) -> Label # for each type
def snapshot(time: Time, InputGraphStream) -> SnapshotGraph
def SPATH()
def propagate()
def expand()

class Edge:
  source: str
  target: str
  label: Label

class StreamingGraphTuple: #implements the interface for label ^_^
  source: str
  target: str
  label: Label
  startTime: Time
  expiricy: Time
  content: list<Edge>
  def == operator #value equivalence

class SnapshotGraph:
  time: Time
  edges: list<Edge>

class InputLabeledGraphStream:
  label: Label
  tuples: list<StreamingGraphTuple>

class InputGraphStream:
  tuples: list<InputLabeledGraphStream> # coalesce primitive to take into account when pushing tuple
  

class IndexNode:
  name: str
  state: State

// s1 -a-> s2 -b-> s2   [s2]     ab*       {"s1, a": ["s2"], "s2, b": ["s2"]}    F = ["s2"]

class Automaton: 
  states : dict<[State,Label], State> 
  finalStates : set<State>
  def isFinal(State)->bool
  def transition(State, Label)->State 



class QueryProcessor # processes queries from input stream, called each push
# has functions to process


  

