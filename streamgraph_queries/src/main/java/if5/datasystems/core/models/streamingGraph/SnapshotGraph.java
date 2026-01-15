package if5.datasystems.core.models.streamingGraph;

import java.time.Instant;
import java.util.List;

import lombok.Data;

@Data public class SnapshotGraph {
  private List<Edge> edges;
  private Instant timestamp;
}
