package org.opengridseg.ui;

import java.util.ArrayList;
import java.util.List;
import org.opengridseg.export.ExportBar;

final class BarSelectionNeighborhood {
    private BarSelectionNeighborhood(){}
    static List<Integer> fiveByFive(List<ExportBar> bars,int clickedIndex){
        ExportBar clicked=bars.get(clickedIndex);List<Integer> result=new ArrayList<>();
        for(int index=0;index<bars.size();index++){ExportBar bar=bars.get(index);if(bar.arrayId()==clicked.arrayId()&&Math.abs(bar.row()-clicked.row())<=2&&Math.abs(bar.col()-clicked.col())<=2)result.add(index);}
        return result;
    }
}
