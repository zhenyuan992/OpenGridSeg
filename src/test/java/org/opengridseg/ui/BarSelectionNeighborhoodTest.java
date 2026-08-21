package org.opengridseg.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import java.util.ArrayList;
import java.util.List;
import org.opengridseg.export.ExportBar;
import org.junit.jupiter.api.Test;

final class BarSelectionNeighborhoodTest {
    @Test void centerClickReturnsFiveByFiveFromTheSameArray(){
        List<ExportBar> bars=new ArrayList<>();
        for(int row=0;row<6;row++)for(int col=0;col<6;col++)bars.add(bar(1,row,col));
        bars.add(bar(2,2,2));
        List<Integer> indices=BarSelectionNeighborhood.fiveByFive(bars,14);
        assertEquals(25,indices.size());
        for(int index:indices)assertEquals(1,bars.get(index).arrayId());
    }
    @Test void edgeClickClipsToExistingBars(){
        List<ExportBar> bars=new ArrayList<>();
        for(int row=0;row<6;row++)for(int col=0;col<6;col++)bars.add(bar(1,row,col));
        List<Integer> indices=BarSelectionNeighborhood.fiveByFive(bars,0);
        assertEquals(9,indices.size());
        assertFalse(indices.contains(21));
    }
    private static ExportBar bar(int array,int row,int col){return new ExportBar("a"+array+"-"+row+"-"+col,array,row,col,"observed",0,0,col,row,false);}
}
