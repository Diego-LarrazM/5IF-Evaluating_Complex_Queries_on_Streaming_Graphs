package if5.datasystems.core.models.automaton;

import lombok.Data;

@Data public class State {
  private String state;

  public State(String state) {
    this.state = state;
  }
}