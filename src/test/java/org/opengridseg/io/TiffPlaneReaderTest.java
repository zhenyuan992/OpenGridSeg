package org.opengridseg.io;

import static org.junit.jupiter.api.Assertions.*;
import ij.ImagePlus;
import ij.io.FileSaver;
import ij.process.ShortProcessor;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TiffPlaneReaderTest {
    @TempDir Path folder;
    @Test void readsUint16ValuesWithoutRescaling() throws Exception {
        short[] pixels = new short[20];
        for (int i=0;i<pixels.length;i++) pixels[i]=(short)(10+i*50);
        Path file=folder.resolve("plane.tif");
        assertTrue(new FileSaver(new ImagePlus("plane",new ShortProcessor(5,4,pixels,null))).saveAsTiff(file.toString()));
        SourcePlane plane=TiffPlaneReader.read(file);
        assertEquals("uint16",plane.storageType());
        assertEquals(10f,plane.observedMin(),0f);
        assertEquals(960f,plane.observedMax(),0f);
        assertEquals(10f,plane.data().get(0,0),0f);
        assertEquals(960f,plane.data().get(4,3),0f);
    }
}
