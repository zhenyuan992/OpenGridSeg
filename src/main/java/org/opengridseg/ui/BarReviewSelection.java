package org.opengridseg.ui;

import java.util.Collections;
import java.util.List;
import org.opengridseg.export.ExportBar;
import org.opengridseg.fluorescence.BarFluorescenceReview;
import org.opengridseg.fluorescence.SignalLogic;

final class BarReviewSelection {
    private BarReviewSelection(){}
    static List<Integer> toggle(List<BarFluorescenceReview> reviews,SignalLogic logic,int clickedIndex,boolean fiveByFive){
        boolean target=!reviews.get(clickedIndex).selected(logic);List<Integer> indices;
        if(fiveByFive){java.util.ArrayList<ExportBar> bars=new java.util.ArrayList<>();for(BarFluorescenceReview review:reviews)bars.add(review.bar());indices=BarSelectionNeighborhood.fiveByFive(bars,clickedIndex);}else indices=Collections.singletonList(clickedIndex);
        for(int index:indices)reviews.get(index).setManualSelection(target);
        return indices;
    }
}
