package if5.datasystems.core.models.processors;

import if5.datasystems.core.models.aliases.Label;
import if5.datasystems.core.models.streamingGraph.Edge;
import if5.datasystems.core.processors.EdgeStreamFormat;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.connector.file.src.reader.StreamFormat;
import org.apache.flink.core.fs.FSDataInputStream;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class EdgeStreamFormatTest {

    private static class TestFSDataInputStream extends FSDataInputStream {

        private final ByteArrayInputStream in;

        TestFSDataInputStream(String content) {
            this.in = new ByteArrayInputStream(content.getBytes());
        }

        @Override
        public int read() throws IOException {
            return in.read();
        }

        @Override
        public void seek(long desired) {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getPos() {
            throw new UnsupportedOperationException();
        }
    }

    @Test
    void testReadSingleEdge() throws Exception {
        long ts = 1_700_000_000_000L;
        String csv = "A,B,L1," + ts + "\n";

        EdgeStreamFormat format = new EdgeStreamFormat();
        FSDataInputStream stream = new TestFSDataInputStream(csv);

        StreamFormat.Reader<Edge> reader =
                format.createReader(new Configuration(), stream, csv.length(), csv.length());

        Edge edge = reader.read();

        assertNotNull(edge);
        assertEquals("A", edge.getSource());
        assertEquals("B", edge.getTarget());
        assertEquals(new Label("L1"), edge.getLabel());
        assertEquals(ts, edge.getStartTime());

        assertNull(reader.read()); // EOF
        reader.close();
    }

    @Test
    void testReadMultipleEdges() throws Exception {
        String csv =
                "A,B,L1,1000\n" +
                        "B,C,L2,2000\n";

        EdgeStreamFormat format = new EdgeStreamFormat();
        FSDataInputStream stream = new TestFSDataInputStream(csv);

        StreamFormat.Reader<Edge> reader =
                format.createReader(new Configuration(), stream, csv.length(), csv.length());

        Edge e1 = reader.read();
        Edge e2 = reader.read();
        Edge e3 = reader.read();

        assertNotNull(e1);
        assertNotNull(e2);
        assertNull(e3);

        assertEquals("A", e1.getSource());
        assertEquals("C", e2.getTarget());
        assertEquals(new Label("L2"), e2.getLabel());

        reader.close();
    }

    @Test
    void testWhitespaceIsTrimmed() throws Exception {
        String csv = "  A ,  B ,  L  ,  42  \n";

        EdgeStreamFormat format = new EdgeStreamFormat();
        FSDataInputStream stream = new TestFSDataInputStream(csv);

        StreamFormat.Reader<Edge> reader =
                format.createReader(new Configuration(), stream, csv.length(), csv.length());

        Edge edge = reader.read();

        assertEquals("A", edge.getSource());
        assertEquals("B", edge.getTarget());
        assertEquals(new Label("L"), edge.getLabel());
        assertEquals(42L, edge.getStartTime());

        reader.close();
    }

    @Test
    void testGetProducedType() {
        EdgeStreamFormat format = new EdgeStreamFormat();
        TypeInformation<Edge> typeInfo = format.getProducedType();

        assertEquals(TypeInformation.of(Edge.class), typeInfo);
    }

    @Test
    void testIsNotSplittable() {
        EdgeStreamFormat format = new EdgeStreamFormat();
        assertFalse(format.isSplittable());
    }
}
