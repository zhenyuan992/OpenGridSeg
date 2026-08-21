package org.opengridseg.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import org.junit.jupiter.api.Test;

final class ReviewRowSelectionTest {
    @Test void selectsTheClickedModelRowEvenWhenTheTableIsSorted(){
        JTable table=new JTable(new DefaultTableModel(new Object[][]{{"a"},{"z"}},new Object[]{"Bar"}));table.setAutoCreateRowSorter(true);table.getRowSorter().toggleSortOrder(0);table.getRowSorter().toggleSortOrder(0);
        ReviewRowSelection.selectModelRow(table,0);
        assertEquals(0,table.convertRowIndexToModel(table.getSelectedRow()));
    }
}
