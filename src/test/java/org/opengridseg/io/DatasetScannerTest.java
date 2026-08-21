package org.opengridseg.io;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DatasetScannerTest {
    @TempDir Path folder;

    @Test void groupsThreeChannelsAndTimeWithoutNdMetadata() throws Exception {
        for (int t = 1; t <= 3; t++) {
            Files.createFile(folder.resolve("sample_Cell12_w1CSU GFP_t" + t + ".TIF"));
            Files.createFile(folder.resolve("sample_Cell12_w2CSU mCherry_t" + t + ".TIF"));
            Files.createFile(folder.resolve("sample_Cell12_w3Trans Cam_t" + t + ".TIF"));
        }
        Files.createFile(folder.resolve("sample_Cell12.nd"));
        List<FrameSet> sets = DatasetScanner.scan(folder);
        assertEquals(1, sets.size());
        FrameSet set = sets.get(0);
        assertEquals("sample_Cell12", set.getFovId());
        assertEquals(3, set.getFrameCount());
        assertEquals("sample_Cell12_w3Trans Cam_t2.TIF", set.file(Channel.BF, 2).getFileName().toString());
    }

    @Test void rejectsMissingChannelFrame() throws Exception {
        Files.createFile(folder.resolve("sample_Cell12_w1CSU GFP_t1.TIF"));
        Files.createFile(folder.resolve("sample_Cell12_w2CSU mCherry_t1.TIF"));
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () -> DatasetScanner.scan(folder));
        assertTrue(error.getMessage().contains("BF"));
    }
}
