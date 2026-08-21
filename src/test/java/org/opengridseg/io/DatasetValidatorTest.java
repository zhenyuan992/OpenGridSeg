package org.opengridseg.io;

import static org.junit.jupiter.api.Assertions.*;
import ij.ImagePlus;
import ij.io.FileSaver;
import ij.process.ShortProcessor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DatasetValidatorTest {
    @TempDir Path folder;

    @Test void rejectsAChannelWithDifferentDimensions() throws Exception {
        for (int wave=1; wave<=3; wave++) {
            int width=wave==2?11:10;
            Path file=folder.resolve("sample_Cell12_w"+wave+"channel_t1.TIF");
            assertTrue(new FileSaver(new ImagePlus("p",new ShortProcessor(width,8))).saveAsTiff(file.toString()));
        }
        List<FrameSet> sets=DatasetScanner.scan(folder);
        IllegalArgumentException error=assertThrows(IllegalArgumentException.class,()->DatasetValidator.validate(sets));
        assertTrue(error.getMessage().contains("dimensions"));
    }
}
