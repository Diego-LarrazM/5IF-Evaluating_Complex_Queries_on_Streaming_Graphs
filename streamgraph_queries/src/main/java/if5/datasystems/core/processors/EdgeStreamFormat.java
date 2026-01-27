package if5.datasystems.core.processors;

import if5.datasystems.core.models.streamingGraph.Edge;
import if5.datasystems.core.models.aliases.Label;

import org.apache.flink.core.io.SimpleVersionedSerializer;
import org.apache.flink.connector.file.src.reader.StreamFormat;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.core.fs.FSDataInputStream;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class EdgeStreamFormat implements StreamFormat<Edge> {

    @Override
    public Reader<Edge> createReader(
            Configuration config,
            FSDataInputStream stream,
            long fileLen,
            long splitEnd
    ) throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

        return new Reader<Edge>() {

            String line;

            @Override
            public Edge read() throws IOException {
                line = reader.readLine();
                if (line == null) {
                    return null;
                }

                // CSV format: src,target,label,startTime
                String[] parts = line.split(",");

                return new Edge(
                    parts[0].trim(),
                    parts[1].trim(),
                    new Label(parts[2].trim()),
                    Long.parseLong(parts[3].trim())
                );
            }

            @Override
            public void close() throws IOException {
                reader.close();
            }
        };
    }

    @Override
    public TypeInformation<Edge> getProducedType() {
        return TypeInformation.of(Edge.class);
    }

    @Override
    public StreamFormat.Reader<Edge> restoreReader(
        Configuration config,
        FSDataInputStream stream,
        long offset,
        long length,
        long splitId
    ) throws IOException {
        // since isSplittable() == false, Flink will read the whole file in one go
        return new StreamFormat.Reader<>() {
            @Override
            public Edge read() throws IOException {
                // implement CSV reading here
                return null;
            }

            @Override
            public void close() throws IOException {
                stream.close();
            }
        };
    }

    @Override
    public boolean isSplittable() {
        return false;  // sequential reading cause restoreReader udnefined.
    }
}
