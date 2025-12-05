Alias Label: str
Alias State: str
Alias Time: int
Alias SpanningTree: dict<IndexNode, list<IndexNode>>

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


class Automaton: 
  states : dict<[State,Label], State> 
  def isFinal(State)->bool
  def transition(State, Label)->State 


IndexPath: dist<IndexNode, SpanningTree>


class QueryProcessor # processes queries from input stream, called each push
# has functions to process


  

