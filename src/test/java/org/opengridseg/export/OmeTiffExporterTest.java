package org.opengridseg.export;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.Arrays;
import loci.formats.FormatTools;
import loci.formats.ImageReader;
import org.opengridseg.image.FloatPlane;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class OmeTiffExporterTest {
    @TempDir Path folder;

    @Test void writesOneXyctSeriesPerBarAsFloat32() throws Exception {
        Path output = folder.resolve("bars.ome.tif");
        OmeTiffExporter.write(output, Arrays.asList("bar_0001", "bar_0002"), 5, 4, 3, 2,
            (bar, channel, time) -> {
                FloatPlane p = new FloatPlane(5, 4);
                float value = 1000 * bar + 100 * channel + 10 * time + 3.5f;
                Arrays.fill(p.pixels(), value);
                return p;
            });
        ImageReader reader = new ImageReader();
        reader.setId(output.toString());
        assertEquals(2, reader.getSeriesCount());
        reader.setSeries(1);
        assertEquals(5, reader.getSizeX());
        assertEquals(4, reader.getSizeY());
        assertEquals(3, reader.getSizeC());
        assertEquals(2, reader.getSizeT());
        assertEquals(FormatTools.FLOAT, reader.getPixelType());
        int index = reader.getIndex(0, 2, 1);
        byte[] bytes = reader.openBytes(index);
        float first = ByteBuffer.wrap(bytes).order(reader.isLittleEndian()?ByteOrder.LITTLE_ENDIAN:ByteOrder.BIG_ENDIAN).getFloat();
        assertEquals(1213.5f, first, 0f);
        reader.close();
    }
}
