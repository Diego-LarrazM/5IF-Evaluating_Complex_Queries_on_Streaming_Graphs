package if5.datasystems.core.processors;

import java.time.Instant;

import if5.datasystems.core.models.aliases.Label;
import if5.datasystems.core.models.streamingGraph.Edge;

public class MyEvent {
    public String f1;
    public long f2;

    public MyEvent(String s){
        // CSV format: src,target,label,startTime
        String[] parts = s.split(";");
        this.f1 = parts[0];
        this.f2 = Long.parseLong(parts[1]);
    }

    @Override
    public String toString() {
        return f1 + " | " + f2;
    }
}
