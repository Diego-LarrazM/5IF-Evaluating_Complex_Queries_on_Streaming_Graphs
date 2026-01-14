package if5.datasystems.core.processors;

import java.time.Instant;

import if5.datasystems.core.models.aliases.Label;
import if5.datasystems.core.models.streamingGraph.Edge;

public class EdgeEventFormat {
    public Edge edge;
    public long timestamp;

    public EdgeEventFormat(String s){
        // CSV format: src,target,label,startTime
        String[] parts = s.split(";");
        this.timestamp = Long.parseLong(parts[3].trim());

        this.edge = new Edge(
            parts[0].trim(),
            parts[1].trim(),
            new Label(parts[2].trim()),
            Instant.ofEpochMilli(this.timestamp)
        );
    }
}
