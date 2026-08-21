package org.opengridseg.detect;

import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class MacroLayoutFitterTest {
    @Test void separatesAdjacentTwentyByTwentyArraysUsingThirtySlotPeriod() {
        List<Peak> peaks = new ArrayList<>();
        Pitch pitch = new Pitch(114, 109);
        double xPhase = 30, yPhase = 40;
        for (int row = 0; row < 20; row++) for (int col = 0; col < 20; col++)
            if ((row + col) % 3 != 0) peaks.add(new Peak(yPhase + (3 + row) * pitch.y(), xPhase + (4 + col) * pitch.x(), 10));
        for (int row = 0; row < 12; row++) for (int col = 0; col < 6; col++)
            peaks.add(new Peak(yPhase + (3 + row) * pitch.y(), xPhase + (34 + col) * pitch.x(), 8));
        GridPhase phase = GridFitter.fitPhase(peaks, pitch, 3);
        List<MacroArray> arrays = MacroLayoutFitter.fit(peaks, phase, pitch, 20, 30, 5);
        assertEquals(2, arrays.size());
        assertEquals(3, arrays.get(0).rowStart());
        assertEquals(4, arrays.get(0).colStart());
        assertEquals(3, arrays.get(1).rowStart());
        assertEquals(34, arrays.get(1).colStart());
        assertEquals(400, arrays.get(0).nodes().size());
    }
}
