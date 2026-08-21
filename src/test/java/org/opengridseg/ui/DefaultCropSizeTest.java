package org.opengridseg.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

final class DefaultCropSizeTest {
    @Test void defaultsToSixtyByThirtyPixels(){
        assertEquals(60,OpenGridSegWindow.DEFAULT_CROP_WIDTH);
        assertEquals(30,OpenGridSegWindow.DEFAULT_CROP_HEIGHT);
    }
}
