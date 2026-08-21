package org.opengridseg.ui;

import java.util.List;
import org.opengridseg.export.ExportBar;

final class FullFovHitTest {
    private FullFovHitTest(){}
    static int nearest(List<ExportBar> bars,FovDisplayImage display,int x,int y,int tolerancePixels){
        int nearest=-1;double best=tolerancePixels*(double)tolerancePixels;
        for(int index=0;index<bars.size();index++){
            ExportBar bar=bars.get(index);double dx=x-display.displayX(bar.rotatedX()),dy=y-display.displayY(bar.rotatedY()),distance=dx*dx+dy*dy;
            if(distance<=best){best=distance;nearest=index;}
        }
        return nearest;
    }
}
