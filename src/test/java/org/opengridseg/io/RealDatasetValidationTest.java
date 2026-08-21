package org.opengridseg.io;

import java.nio.file.Paths;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class RealDatasetValidationTest {
    @Test void validatesRealFolderWhenProvided() throws Exception {
        String value=System.getProperty("opengridseg.realFolder");
        Assumptions.assumeTrue(value!=null && !value.isEmpty());
        java.util.List<FrameSet> sets=DatasetScanner.scan(Paths.get(value));
        DatasetValidator.validate(sets);
        System.out.printf("REAL_DATASET fovs=%d frames_per_fov=%d planes=%d%n",
            sets.size(),sets.get(0).getFrameCount(),sets.size()*sets.get(0).getFrameCount()*3);
    }
}
