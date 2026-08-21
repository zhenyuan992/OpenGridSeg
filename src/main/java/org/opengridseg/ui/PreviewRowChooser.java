package org.opengridseg.ui;

import java.util.function.IntPredicate;

final class PreviewRowChooser {
    private PreviewRowChooser(){}
    static int choose(int selectedModelRow,int rowCount,IntPredicate kept){
        if(selectedModelRow>=0)return selectedModelRow;
        for(int row=0;row<rowCount;row++)if(kept.test(row))return row;
        return -1;
    }
}
