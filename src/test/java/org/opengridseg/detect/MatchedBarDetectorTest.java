package org.opengridseg.detect;

import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import org.opengridseg.image.FloatPlane;
import org.junit.jupiter.api.Test;

class MatchedBarDetectorTest {
    @Test void ignoresUnsupportedNanCorners() {
        FloatPlane image = new FloatPlane(260, 260);
        for (int y=0;y<260;y++) for(int x=0;x<260;x++) image.set(x,y,(x+y<60)?Float.NaN:1000f);
        for (int y=121;y<=139;y++) for(int x=100;x<=160;x++) image.set(x,y,200f);
        DetectionParameters p = new DetectionParameters(); p.thresholdSigma=4;
        List<Peak> peaks = MatchedBarDetector.detect(image,p);
        assertTrue(peaks.stream().anyMatch(q -> Math.hypot(q.x()-130,q.y()-130)<3));
        assertFalse(peaks.stream().anyMatch(q -> q.x()+q.y()<80));
    }

    @Test void findsHorizontalDarkBarsAndRejectsRoundDistractor() {
        FloatPlane image = new FloatPlane(360, 360);
        for (int i = 0; i < image.pixels().length; i++) image.pixels()[i] = 1000f;
        int[][] centres = {{90,90},{180,90},{270,90},{90,180},{180,180},{270,180},{90,270},{180,270},{270,270}};
        for (int[] centre : centres) for (int y = centre[1]-9; y <= centre[1]+9; y++) for (int x=centre[0]-30;x<=centre[0]+30;x++) image.set(x,y,200f);
        for (int y=150;y<=170;y++) for(int x=40;x<=60;x++) if(Math.hypot(x-50,y-160)<=10) image.set(x,y,100f);
        DetectionParameters p = new DetectionParameters();
        p.thresholdSigma = 5.0; p.barLength = 60; p.barWidth = 18; p.minPeakDistance = 45;
        List<Peak> peaks = MatchedBarDetector.detect(image, p);
        assertEquals(9, peaks.size());
        for (int[] centre : centres) assertTrue(peaks.stream().anyMatch(q -> Math.hypot(q.x()-centre[0], q.y()-centre[1]) < 3));
        assertFalse(peaks.stream().anyMatch(q -> Math.hypot(q.x()-50, q.y()-160) < 15));
    }
}
