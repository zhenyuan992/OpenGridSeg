package org.opengridseg.image;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class ExpandedRotationTest {
    @Test void expandedCanvasMapsRotatedCoordinatesBackToOriginalPixels() {
        FloatPlane source = new FloatPlane(7, 5);
        for (int i = 0; i < source.pixels().length; i++) source.pixels()[i] = i;
        RotatedImage rotated = ExpandedRotation.rotate(source, 31.0, Interpolation.BILINEAR);
        double[] q = rotated.toRotated(5.0, 1.0);
        double[] p = rotated.toOriginal(q[0], q[1]);
        assertEquals(5.0, p[0], 1e-9);
        assertEquals(1.0, p[1], 1e-9);
        assertTrue(rotated.plane().width() > source.width());
        assertTrue(rotated.plane().height() > source.height());
        assertTrue(Float.isNaN(rotated.plane().get(0, 0)));
    }
}
