package org.opengridseg.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

final class PreviewRowChooserTest {
    @Test void choosesFirstKeptBarWhenNoTableRowIsSelected(){
        assertEquals(2,PreviewRowChooser.choose(-1,5,row->row==2||row==4));
    }
    @Test void keepsTheCurrentTableRow(){
        assertEquals(3,PreviewRowChooser.choose(3,5,row->false));
    }
    @Test void reportsNoRowWhenNothingIsKept(){
        assertEquals(-1,PreviewRowChooser.choose(-1,5,row->false));
    }
}
