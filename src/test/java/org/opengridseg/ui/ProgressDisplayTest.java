package org.opengridseg.ui;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

final class ProgressDisplayTest {
    @Test void showsCountAndPercent(){
        ProgressDisplay display=new ProgressDisplay();
        display.update(5,300,"Scoring bars");
        assertEquals(5,display.value());
        assertEquals(300,display.maximum());
        assertEquals("Scoring bars",display.text());
        assertTrue(display.visibleProgress());
    }

    @Test void hidesProgressWhenFinished(){
        ProgressDisplay display=new ProgressDisplay();
        display.update(5,300,"Scoring bars");
        display.finish("Done");
        assertEquals("Done",display.text());
        assertFalse(display.visibleProgress());
    }
}
