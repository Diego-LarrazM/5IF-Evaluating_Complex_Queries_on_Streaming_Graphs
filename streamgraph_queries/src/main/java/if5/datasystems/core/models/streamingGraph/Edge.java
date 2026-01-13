package if5.datasystems.core.models.streamingGraph;

import java.io.Serializable;
import java.util.Date;

import if5.datasystems.core.models.aliases.Label;
import lombok.Data;

@Data public class Edge implements Serializable {

  public String source;
  public String target;
  public Label label;
  public long startTime;
  public long expiricy;

  public Edge ()
  {
      
  }

  public Edge(String source, String target, Label label, long startTime){
    this.source=source;
    this.target=target;
    this.label=label;
    this.startTime=startTime;
  }
  
  public Edge(String source, String target, Label label, long startTime, long expiricy){
    this.source=source;
    this.target=target;
    this.label=label;
    this.startTime=startTime;
    this.expiricy=expiricy;
  }

  @Override
  public boolean equals(Object obj){ // Value Equivalence
    if (this == obj) return true;
    if (!(obj instanceof Edge)) return false;
    Edge other = (Edge) obj;
    return this.source.equals(other.source) &&
           this.target.equals(other.target) &&
           this.label.equals(other.label);
  }

  @Override
  public int hashCode() {
    int result = source.hashCode();
    result = 31 * result + target.hashCode();
    result = 31 * result + label.hashCode();
    return result;
}

}
