package org.opengridseg.detect;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.opengridseg.io.TiffPlaneReader;
import org.opengridseg.ui.ArraySelection;
import org.opengridseg.ui.PreviewRenderer;
import ij.io.FileSaver;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class RealDataDetectionTest {
    @Test void detectsRealCell14WhenPathIsProvided() {
        String value=System.getProperty("opengridseg.realData");
        Assumptions.assumeTrue(value!=null && !value.isEmpty());
        DetectionResult result=DetectionEngine.detect(TiffPlaneReader.read(Paths.get(value)).data(),new DetectionParameters());
        System.out.printf("REAL_DETECTION angle=%.3f pitchX=%.3f pitchY=%.3f peaks=%d phase=%d arrays=%d%n",
            result.angleDegrees(),result.pitch().x(),result.pitch().y(),result.peaks().size(),result.phase().inlierIndices().size(),result.arrays().size());
        String previewPath=System.getProperty("opengridseg.preview");
        if(previewPath!=null&&!previewPath.isEmpty()){
            List<ArraySelection> selections=new ArrayList<>();
            for(int i=0;i<result.arrays().size();i++)selections.add(new ArraySelection(i,result.arrays().get(i)));
            assertTrue(new FileSaver(PreviewRenderer.create("real-data",result,selections).flatten()).saveAsPng(previewPath));
        }
        assertTrue(Math.abs(result.angleDegrees())>35 && Math.abs(result.angleDegrees())<45);
        assertTrue(result.pitch().x()>=90 && result.pitch().x()<=130);
        assertTrue(result.pitch().y()>=90 && result.pitch().y()<=130);
        assertTrue(result.peaks().size()>100);
        assertFalse(result.arrays().isEmpty());
    }
}
