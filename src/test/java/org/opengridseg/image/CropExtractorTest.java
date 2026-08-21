package org.opengridseg.image;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class CropExtractorTest {
    @Test void bicubicCropStaysInOriginalDigitalNumberRange() {
        FloatPlane source = new FloatPlane(9, 9);
        for (int y = 0; y < 9; y++) for (int x = 0; x < 9; x++) source.set(x, y, (x + y) % 2 == 0 ? 100f : 900f);
        FloatPlane crop = CropExtractor.extractHorizontal(source, 4.0, 4.0, 31.0, 7, 7, Interpolation.BICUBIC);
        assertTrue(crop.min() >= 100f);
        assertTrue(crop.max() <= 900f);
        for (float value : crop.pixels()) if (Float.isFinite(value)) assertTrue(value >= 100f && value <= 900f);
    }

    @Test void rotatedLineBecomesHorizontal() {
        FloatPlane source = new FloatPlane(41, 41);
        for (int i = -12; i <= 12; i++) source.set(20 + i, 20 + i, 1000f);
        FloatPlane crop = CropExtractor.extractHorizontal(source, 20.0, 20.0, 45.0, 25, 9, Interpolation.BILINEAR);
        double middle = 0, off = 0;
        for (int x = 0; x < crop.width(); x++) middle += crop.get(x, 4);
        for (int x = 0; x < crop.width(); x++) off += crop.get(x, 1);
        assertTrue(middle > 4.0 * off, "the source diagonal should become a horizontal centre line");
    }
}
