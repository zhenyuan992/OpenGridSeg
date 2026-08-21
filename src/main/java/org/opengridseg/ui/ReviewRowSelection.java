package org.opengridseg.ui;

import javax.swing.JTable;

final class ReviewRowSelection {
    private ReviewRowSelection(){}
    static void selectModelRow(JTable table,int modelRow){int viewRow=table.convertRowIndexToView(modelRow);if(viewRow<0)return;table.setRowSelectionInterval(viewRow,viewRow);table.scrollRectToVisible(table.getCellRect(viewRow,0,true));}
}
