package org.opengridseg.ui;

import ij.ImagePlus;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.AbstractTableModel;
import org.opengridseg.fluorescence.*;

public final class FluorescenceReviewDialog extends JDialog {
    private final List<BarFluorescenceReview> reviews;
    private final FullFovReviewData fullFovData;
    private final double bfMin,bfMax,gfpMin,gfpMax,mCherryMin,mCherryMax;
    private final JComboBox<SignalLogic> logic=new JComboBox<>(SignalLogic.values());
    private final JCheckBox previewScaling=new JCheckBox("Preview with per-channel scaling",true);
    private final ReviewTableModel model;
    private final JTable table;
    private final JLabel summary=new JLabel();
    private ImagePlus preview;
    private JDialog previewWindow;
    private FullFovReviewDialog fullFovWindow;
    private boolean approved;

    public FluorescenceReviewDialog(Window owner,List<BarFluorescenceReview> reviews,FullFovReviewData fullFovData,
            double bfMin,double bfMax,double gfpMin,double gfpMax,double mCherryMin,double mCherryMax) {
        super(owner,"Review GFP / mCherry bars",ModalityType.APPLICATION_MODAL);
        this.reviews=reviews;this.fullFovData=fullFovData;this.bfMin=bfMin;this.bfMax=bfMax;this.gfpMin=gfpMin;this.gfpMax=gfpMax;this.mCherryMin=mCherryMin;this.mCherryMax=mCherryMax;
        this.model=new ReviewTableModel();this.table=new JTable(model);
        buildUi();updateSummary();pack();setMinimumSize(new Dimension(1180,650));setSize(new Dimension(1180,700));setLocationRelativeTo(owner);
        addWindowListener(new WindowAdapter(){public void windowClosed(WindowEvent e){closeAllPreviews();}});
    }

    public boolean approved(){return approved;}
    public SignalLogic signalLogic(){return (SignalLogic)logic.getSelectedItem();}
    public int selectedCount(){int count=0;for(BarFluorescenceReview review:reviews)if(review.selected(signalLogic()))count++;return count;}

    private void buildUi(){
        JPanel root=new JPanel(new BorderLayout(8,8));root.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        JLabel help=new JLabel("OpenGridSeg suggests which bars to keep by comparing each bar with its nearby background across time. You can change every choice.");
        root.add(help,BorderLayout.NORTH);
        table.setAutoCreateRowSorter(true);table.setFillsViewportHeight(true);table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);int[] widths={50,85,50,45,45,70,75,75,75,85,75,75,75,85,105};for(int i=0;i<widths.length;i++)table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        table.getSelectionModel().addListSelectionListener(this::selectionChanged);root.add(new JScrollPane(table),BorderLayout.CENTER);
        JPanel controls=new JPanel(new BorderLayout(8,8));
        JPanel actionRows=new JPanel(new GridLayout(2,1));
        JPanel selectionControls=new JPanel(new FlowLayout(FlowLayout.LEFT));selectionControls.add(new JLabel("Automatic rule:"));selectionControls.add(logic);
        JPanel previewControls=new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton reset=new JButton("Reset to automatic");JButton all=new JButton("Keep all");JButton none=new JButton("Remove all");JButton show=new JButton("Show selected BF / GFP / mCherry");JButton fullFov=new JButton("Show full FOV map");
        reset.addActionListener(e->{for(BarFluorescenceReview review:reviews)review.setManualSelection(null);changed();});
        all.addActionListener(e->{for(BarFluorescenceReview review:reviews)review.setManualSelection(Boolean.TRUE);changed();});
        none.addActionListener(e->{for(BarFluorescenceReview review:reviews)review.setManualSelection(Boolean.FALSE);changed();});
        show.addActionListener(e->showSelectedPreview());fullFov.addActionListener(e->showFullFov());logic.addActionListener(e->changed());previewScaling.addActionListener(e->{updateSummary();if(previewWindow!=null)showSelectedPreview();});
        selectionControls.add(reset);selectionControls.add(all);selectionControls.add(none);
        previewControls.add(show);previewControls.add(fullFov);previewControls.add(previewScaling);
        actionRows.add(selectionControls);actionRows.add(previewControls);controls.add(actionRows,BorderLayout.NORTH);controls.add(summary,BorderLayout.CENTER);
        JPanel buttons=new JPanel(new FlowLayout(FlowLayout.RIGHT));JButton cancel=new JButton("Cancel");JButton use=new JButton("Use Selection");
        cancel.addActionListener(e->dispose());use.addActionListener(e->{if(selectedCount()==0){JOptionPane.showMessageDialog(this,"Keep at least one bar.","No bars selected",JOptionPane.WARNING_MESSAGE);return;}approved=true;dispose();});buttons.add(cancel);buttons.add(use);controls.add(buttons,BorderLayout.SOUTH);root.add(controls,BorderLayout.SOUTH);setContentPane(root);
        Tooltips.apply("Open the rotated BF, GFP, and mCherry images for the whole field of view. Green marks mean keep; red marks mean reject. Click a visible mark to change that bar.",fullFov);
        Tooltips.apply("Makes each BF, GFP, and mCherry bar preview easier to see by adjusting that channel separately. This changes only the screen preview. Exported pixel values are never changed.",previewScaling);
        Tooltips.apply("Show the BF, GFP, and mCherry images for the highlighted bar. If no row is highlighted, OpenGridSeg shows the first kept bar.",show);
        Tooltips.apply("The GFP and mCh scores count how many signal checks passed: 0–1 looks like background; 2–3 looks like signal. Z90 describes repeated brightness above nearby background. A90 describes the repeated bright area size. Persist % tells how often a strong spot appears.",table);
        Tooltips.apply("OR keeps a bar when GFP or mCherry looks like signal. AND is stricter and requires both channels. A channel looks like signal when at least two of its three checks pass.",logic);
    }
    private void selectionChanged(ListSelectionEvent event){if(!event.getValueIsAdjusting())showSelectedPreview();}
    private void showFullFov(){if(fullFovWindow!=null&&fullFovWindow.isDisplayable()){fullFovWindow.refresh();fullFovWindow.setVisible(true);fullFovWindow.toFront();return;}fullFovWindow=new FullFovReviewDialog(this,fullFovData,reviews,this::signalLogic,this::fullFovBarChanged);fullFovWindow.setVisible(true);}
    private void showSelectedPreview(){
        int view=table.getSelectedRow();
        if(view<0){
            int row=PreviewRowChooser.choose(-1,reviews.size(),index->reviews.get(index).selected(signalLogic()));
            if(row<0){JOptionPane.showMessageDialog(this,"No kept bars are available to preview.","No kept bars",JOptionPane.INFORMATION_MESSAGE);return;}
            int firstView=table.convertRowIndexToView(row);table.setRowSelectionInterval(firstView,firstView);table.scrollRectToVisible(table.getCellRect(firstView,0,true));return;
        }
        int row=table.convertRowIndexToModel(view);closeBarPreview();preview=FluorescencePreviewRenderer.create(reviews.get(row),bfMin,bfMax,gfpMin,gfpMax,mCherryMin,mCherryMax,previewScaling.isSelected());previewWindow=new JDialog(this,"BF / GFP / mCherry preview — "+reviews.get(row).bar().id(),Dialog.ModalityType.MODELESS);previewWindow.setDefaultCloseOperation(DISPOSE_ON_CLOSE);previewWindow.setContentPane(new FluorescencePreviewPanel(preview));previewWindow.pack();previewWindow.setResizable(false);previewWindow.setLocationRelativeTo(this);previewWindow.setVisible(true);previewWindow.toFront();
    }
    private void closeBarPreview(){if(previewWindow!=null){previewWindow.dispose();previewWindow=null;}if(preview!=null){preview.close();preview=null;}}
    private void closeAllPreviews(){if(fullFovWindow!=null){fullFovWindow.dispose();fullFovWindow=null;}closeBarPreview();}
    private void fullFovBarChanged(int modelRow){int previous=table.getSelectedRow()<0?-1:table.convertRowIndexToModel(table.getSelectedRow());model.fireTableRowsUpdated(0,reviews.size()-1);updateSummary();ReviewRowSelection.selectModelRow(table,modelRow);if(previous==modelRow)showSelectedPreview();}
    private void changed(){model.fireTableDataChanged();updateSummary();if(fullFovWindow!=null)fullFovWindow.refresh();}
    private void updateSummary(){int selected=selectedCount(),manual=0;for(BarFluorescenceReview review:reviews)if(review.manuallyOverridden())manual++;String display=previewScaling.isSelected()?"Preview scales each channel separately for display only; export is unchanged.":"Preview uses full source ranges; export is unchanged.";summary.setText(selected+" of "+reviews.size()+" bars kept; "+manual+" manually changed. Score 2–3 means signal-like. "+display);}

    private final class ReviewTableModel extends AbstractTableModel {
        private final String[] names={"Keep","Bar","Array","Row","Col","Node","GFP score","GFP Z90","GFP A90","GFP persist %","mCh score","mCh Z90","mCh A90","mCh persist %","Choice"};
        public int getRowCount(){return reviews.size();}public int getColumnCount(){return names.length;}public String getColumnName(int c){return names[c];}
        public Class<?> getColumnClass(int c){if(c==0)return Boolean.class;if(c==2||c==3||c==4||c==6||c==9||c==10||c==13)return Integer.class;if(c==7||c==8||c==11||c==12)return Double.class;return String.class;}
        public boolean isCellEditable(int row,int col){return col==0;}
        public Object getValueAt(int row,int col){BarFluorescenceReview review=reviews.get(row);switch(col){case 0:return review.selected(signalLogic());case 1:return review.bar().id();case 2:return review.bar().arrayId();case 3:return review.bar().row();case 4:return review.bar().col();case 5:return review.bar().status();case 6:return review.gfp().criteriaPassed();case 7:return round(review.gfp().z90());case 8:return round(review.gfp().area90());case 9:return (int)Math.round(100*review.gfp().persistence());case 10:return review.mCherry().criteriaPassed();case 11:return round(review.mCherry().z90());case 12:return round(review.mCherry().area90());case 13:return (int)Math.round(100*review.mCherry().persistence());default:return review.manuallyOverridden()?"Manual":review.autoSelected(signalLogic())?"Auto signal":"Auto background";}}
        public void setValueAt(Object value,int row,int col){if(col==0){reviews.get(row).setManualSelection((Boolean)value);fireTableRowsUpdated(row,row);updateSummary();}}
        private double round(double value){return Math.round(value*100.0)/100.0;}
    }
}
