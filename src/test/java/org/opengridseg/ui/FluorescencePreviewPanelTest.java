package org.opengridseg.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import ij.ImagePlus;
import ij.process.ColorProcessor;
import org.junit.jupiter.api.Test;

final class FluorescencePreviewPanelTest {
    @Test void embedsAReadableFourTimesPreview(){
        ImagePlus image=new ImagePlus("bar preview",new ColorProcessor(20,10));
        FluorescencePreviewPanel panel=new FluorescencePreviewPanel(image);
        assertEquals(80,panel.previewWidth());
        assertEquals(40,panel.previewHeight());
    }
}
