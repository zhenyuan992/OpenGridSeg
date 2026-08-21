package org.opengridseg.ui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import javax.swing.*;
import org.opengridseg.export.ExportBar;
import org.opengridseg.fluorescence.BarFluorescenceReview;
import org.opengridseg.fluorescence.SignalLogic;

final class FullFovReviewDialog extends JDialog {
    private final List<BarFluorescenceReview> reviews;private final Supplier<SignalLogic> logic;private final IntConsumer selectionChanged;private final List<FullFovSelectionPanel> panels=new ArrayList<>();private final JLabel summary=new JLabel();private final JCheckBox fiveByFive=new JCheckBox("Toggle in a 5×5 selection");
    FullFovReviewDialog(Dialog owner,FullFovReviewData data,List<BarFluorescenceReview> reviews,Supplier<SignalLogic> logic,IntConsumer selectionChanged){
        super(owner,"Full FOV selection map",ModalityType.MODELESS);this.reviews=reviews;this.logic=logic;this.selectionChanged=selectionChanged;
        List<ExportBar> bars=new ArrayList<>();for(BarFluorescenceReview review:reviews)bars.add(review.bar());
        JPanel images=new JPanel(new GridLayout(1,3,8,8));images.add(channel("BF",data.bf(),bars));images.add(channel("GFP",data.gfp(),bars));images.add(channel("mCherry",data.mCherry(),bars));
        JLabel help=new JLabel("Green circle = kept. Red X = rejected. Click any marker to toggle that bar and show its bar preview.");
        JPanel top=new JPanel(new BorderLayout());top.add(help,BorderLayout.NORTH);top.add(new JLabel("All three panels use the chosen detection frame and separate display-only 5th–95th percentile scaling. Export values are unchanged."),BorderLayout.SOUTH);
        JButton close=new JButton("Close");close.addActionListener(e->dispose());JCheckBox showMarkers=new JCheckBox("Show bar markers",true);showMarkers.setToolTipText("Hide markers to inspect the images without circles. Show them again to click and edit bars.");showMarkers.addActionListener(e->{for(FullFovSelectionPanel panel:panels)panel.setMarkersVisible(showMarkers.isSelected());});fiveByFive.setToolTipText("When enabled, clicking a red bar keeps the surrounding 5×5 block; clicking a green bar rejects that block. The block stays within the same array and clips at its edges.");JPanel markerControls=new JPanel(new FlowLayout(FlowLayout.LEFT));markerControls.add(showMarkers);markerControls.add(fiveByFive);JPanel bottom=new JPanel(new BorderLayout());bottom.add(markerControls,BorderLayout.WEST);bottom.add(summary,BorderLayout.CENTER);bottom.add(close,BorderLayout.EAST);
        JPanel root=new JPanel(new BorderLayout(8,8));root.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));root.add(top,BorderLayout.NORTH);root.add(images,BorderLayout.CENTER);root.add(bottom,BorderLayout.SOUTH);setContentPane(root);pack();setLocationRelativeTo(owner);setDefaultCloseOperation(DISPOSE_ON_CLOSE);refresh();
    }
    private JPanel channel(String name,FovDisplayImage image,List<ExportBar> bars){FullFovSelectionPanel panel=new FullFovSelectionPanel(image,bars,index->reviews.get(index).selected(logic.get()),this::toggle);panels.add(panel);JPanel wrapper=new JPanel(new BorderLayout(2,2));JLabel title=new JLabel(name,SwingConstants.CENTER);title.setFont(title.getFont().deriveFont(Font.BOLD));wrapper.add(title,BorderLayout.NORTH);wrapper.add(panel,BorderLayout.CENTER);return wrapper;}
    private void toggle(int index){BarReviewSelection.toggle(reviews,logic.get(),index,fiveByFive.isSelected());selectionChanged.accept(index);refresh();}
    void refresh(){int kept=0,manual=0;for(BarFluorescenceReview review:reviews){if(review.selected(logic.get()))kept++;if(review.manuallyOverridden())manual++;}summary.setText(kept+" of "+reviews.size()+" bars kept; "+manual+" manually changed.");for(FullFovSelectionPanel panel:panels)panel.repaint();}
}
