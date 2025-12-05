Alias Label: str
Alias State: str

def label(...polymorphism) -> Label # for each type

class Edge:
  source: str
  target: str
  label: Label

class StreamingGraphTuple: #implements the interface for label ^_^
  source: str
  target: str
  label: Label
  startTime: int
  expiricy: int
  content: list<Edge>


class IndexNode:
  name: str
  state: State


class Automaton: 
  states : dict<[State,Label], State> 
  def isFinal(State)->bool
  def transition(State, Label)->state 

IndexPath: dist<[IndexNode,Label], [IndexNode,Label]>