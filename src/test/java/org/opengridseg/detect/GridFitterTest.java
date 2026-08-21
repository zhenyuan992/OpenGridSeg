package org.opengridseg.detect;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class GridFitterTest {
    @Test void estimatesIndependentHorizontalAndVerticalPitch() {
        List<Peak> peaks = new ArrayList<>();
        for (int row = 0; row < 8; row++) for (int col = 0; col < 10; col++)
            peaks.add(new Peak(40 + row * 109.0, 30 + col * 114.0, 10));
        Pitch pitch = GridFitter.estimatePitch(peaks, 90, 130, 12);
        assertEquals(114.0, pitch.x(), 0.25);
        assertEquals(109.0, pitch.y(), 0.25);
    }

    @Test void fitsPhaseWithoutSplittingWhenPitchIsCorrect() {
        List<Peak> peaks = new ArrayList<>();
        for (int row = 0; row < 8; row++) for (int col = 0; col < 10; col++)
            peaks.add(new Peak(40 + row * 109.0, 30 + col * 114.0, 10));
        GridPhase phase = GridFitter.fitPhase(peaks, new Pitch(114, 109), 3);
        assertEquals(80, phase.inlierIndices().size());
        assertEquals(30.0, phase.xOffset(), 0.01);
        assertEquals(40.0, phase.yOffset(), 0.01);
    }
}
