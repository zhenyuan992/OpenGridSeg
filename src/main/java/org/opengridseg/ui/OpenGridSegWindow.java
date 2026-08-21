package org.opengridseg.ui;

import ij.IJ;
import ij.ImagePlus;
import ij.io.DirectoryChooser;
import java.awt.*;
import java.awt.event.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import org.opengridseg.detect.*;
import org.opengridseg.export.*;
import org.opengridseg.fluorescence.*;
import org.opengridseg.image.CropExtractor;
import org.opengridseg.image.ExpandedRotation;
import org.opengridseg.image.Interpolation;
import org.opengridseg.image.RotatedImage;
import org.opengridseg.io.*;

public final class OpenGridSegWindow extends JDialog {
    static final int DEFAULT_CROP_WIDTH=60;
    static final int DEFAULT_CROP_HEIGHT=30;
    private static final int REVIEW_CROP_WIDTH=96;
    private static final int REVIEW_CROP_HEIGHT=48;

    private final Path inputFolder;
    private final List<FrameSet> frameSets;
    private final JComboBox<String> fovBox;
    private final JSpinner detectionFrame = new JSpinner(new SpinnerNumberModel(1,1,999,1));
    private final JSpinner threshold = new JSpinner(new SpinnerNumberModel(7.0,0.5,30.0,0.5));
    private final JSpinner barLength = new JSpinner(new SpinnerNumberModel(60.0,5.0,300.0,1.0));
    private final JSpinner barWidth = new JSpinner(new SpinnerNumberModel(18.0,2.0,100.0,1.0));
    private final JSpinner minDistance = new JSpinner(new SpinnerNumberModel(45,1,500,1));
    private final JSpinner pitchX = new JSpinner(new SpinnerNumberModel(0.0,0.0,500.0,0.5));
    private final JSpinner pitchY = new JSpinner(new SpinnerNumberModel(0.0,0.0,500.0,0.5));
    private final JSpinner tolerance = new JSpinner(new SpinnerNumberModel(9.0,1.0,40.0,0.5));
    private final JSpinner cropWidth = new JSpinner(new SpinnerNumberModel(DEFAULT_CROP_WIDTH,3,1000,1));
    private final JSpinner cropHeight = new JSpinner(new SpinnerNumberModel(DEFAULT_CROP_HEIGHT,3,1000,1));
    private final JComboBox<Interpolation> interpolation = new JComboBox<>(Interpolation.values());
    private final JCheckBox includeInferred = new JCheckBox("Include interpolated and extrapolated grid nodes",true);
    private final JTextField outputFolder = new JTextField(36);
    private final ProgressDisplay progress = new ProgressDisplay();
    private final JTable arrayTable = new JTable();
    private List<ArraySelection> selections = new ArrayList<>();
    private DetectionResult result;
    private ImagePlus preview;
    private List<BarFluorescenceReview> fluorescenceReviews;
    private SignalLogic fluorescenceLogic=SignalLogic.EITHER_CHANNEL;
    private String fluorescenceFingerprint;
    private String detectionSettingsFingerprint;

    public OpenGridSegWindow(Path inputFolder,List<FrameSet> frameSets) {
        super((Frame)null,"OpenGridSeg — Open Grid Segmentation",false);
        this.inputFolder=inputFolder;this.frameSets=frameSets;
        String[] names=new String[frameSets.size()];for(int i=0;i<names.length;i++)names[i]=frameSets.get(i).getFovId();
        fovBox=new JComboBox<>(names);interpolation.setSelectedItem(Interpolation.BICUBIC);fovBox.addActionListener(e->{result=null;detectionSettingsFingerprint=null;fluorescenceReviews=null;fluorescenceFingerprint=null;selections=new ArrayList<>();arrayTable.setModel(new ArrayModel(selections));if(preview!=null){preview.close();preview=null;}progress.finish("FOV changed. Run step 1 again.");});
        outputFolder.setText(inputFolder.resolve("OpenGridSeg_output").toString());
        progress.finish("Choose settings, then click Detect / Update Preview.");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);buildUi();pack();setMinimumSize(new Dimension(760,680));setLocationByPlatform(true);
        addWindowListener(new WindowAdapter(){public void windowClosed(WindowEvent e){if(preview!=null)preview.close();}});
    }

    private void buildUi(){
        JPanel root=new JPanel(new BorderLayout(8,8));root.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        JPanel settings=new JPanel(new GridBagLayout());GridBagConstraints g=new GridBagConstraints();g.insets=new Insets(3,4,3,4);g.fill=GridBagConstraints.HORIZONTAL;
        int row=0;add(settings,g,row++,"FOV",fovBox,"<html>FOV means field of view.<br>Put all TIFF files directly in the same folder, not in subfolders.<br>Name one set like: sample_Cell1_w1GFP_t1.TIF, sample_Cell1_w2mCherry_t1.TIF, and sample_Cell1_w3BF_t1.TIF.<br>The text before _w becomes the FOV name. w1 = GFP, w2 = mCherry, and w3 = BF.<br>Use t1, t2, ... and include every time point for all three channels.</html>");
        add(settings,g,row++,"BF frame used for detection",detectionFrame,"Choose the BF time point used to find bar positions and show BF previews. Frame 1 is usually a good starting point. All matched frames are still exported.");
        add(settings,g,row++,"Detection sensitivity",threshold,"How strongly an image region must look like the bar template. Lower values find more possible bars, including more false matches. Higher values keep fewer, clearer matches.");
        add(settings,g,row++,"Template bar (length / width, px)",pair(barLength,barWidth),"Approximate size of one bar in the BF image. The first number is the bar length; the second is its thickness. This helps detection and does not change the export crop size.");
        add(settings,g,row++,"Minimum distance between bars (px)",minDistance,"Smallest allowed distance between detected bar centres. Increase it if the same bar is found twice. Decrease it if nearby real bars are missed.");
        add(settings,g,row++,"Bar spacing X / Y (px; 0 = automatic)",pair(pitchX,pitchY),"Centre-to-centre spacing between neighbouring bars. X is horizontal and Y is vertical in the rotated view. Leave both at 0 to let OpenGridSeg estimate them.");
        add(settings,g,row++,"Grid matching tolerance (px)",tolerance,"How far a detected bar centre may be from its expected grid position. Increase it for a less regular grid. Decrease it for stricter matching.");
        add(settings,g,row++,"Export crop width / height (px)",pair(cropWidth,cropHeight),"Size of each saved bar image. Width runs along the horizontal bar; height controls the area above and below it.");
        add(settings,g,row++,"Pixel interpolation",interpolation,"How new pixel values are calculated during rotation and cropping. Bicubic is the smooth default, bilinear is simpler, and nearest uses the closest source pixel. Source intensity units are preserved.");
        includeInferred.setText("Include estimated bars at missing grid positions");Tooltips.apply("When a grid position has no directly detected bar, include an estimated bar at that expected position. Turn this off to keep only bars found directly in the BF image.",includeInferred);
        g.gridx=0;g.gridy=row;g.gridwidth=3;settings.add(includeInferred,g);row++;
        JPanel out=new JPanel(new BorderLayout(4,0));out.add(outputFolder,BorderLayout.CENTER);JButton browse=new JButton("Browse");browse.addActionListener(e->chooseOutput());out.add(browse,BorderLayout.EAST);add(settings,g,row++,"Output folder",out,"Folder where OpenGridSeg saves the OME-TIFF bar movies, the CSV table, and the JSON settings record.");
        root.add(settings,BorderLayout.NORTH);
        arrayTable.setFillsViewportHeight(true);arrayTable.setToolTipText("Each row is one detected grid. Uncheck Use to ignore a grid. Change Row start or Column start only when the overlay begins at the wrong grid edge.");root.add(new JScrollPane(arrayTable),BorderLayout.CENTER);
        JPanel bottom=new JPanel(new BorderLayout(8,8));bottom.add(progress,BorderLayout.CENTER);JPanel buttons=new JPanel(new FlowLayout(FlowLayout.RIGHT));JButton detect=new JButton("1. Detect / Update Preview");JButton refresh=new JButton("Refresh Array Overlay");JButton review=new JButton("2. Review GFP / mCherry Bars");JButton export=new JButton("3. Export Selected Bars");JButton close=new JButton("Close");detect.addActionListener(e->detect());refresh.addActionListener(e->showPreview());review.addActionListener(e->reviewFluorescence());export.addActionListener(e->export());close.addActionListener(e->dispose());buttons.add(detect);buttons.add(refresh);buttons.add(review);buttons.add(export);buttons.add(close);bottom.add(buttons,BorderLayout.SOUTH);root.add(bottom,BorderLayout.SOUTH);setContentPane(root);
        Tooltips.apply("Open Step 2. OpenGridSeg checks GFP and mCherry over time and suggests which bars to keep. You can inspect and change every choice before export.",review);
    }
    private static JPanel pair(JComponent a,JComponent b){JPanel p=new JPanel(new GridLayout(1,2,4,0));p.add(a);p.add(b);return p;}
    private static void add(JPanel panel,GridBagConstraints g,int row,String label,JComponent component,String tip){g.gridy=row;g.gridwidth=1;g.weightx=0;g.gridx=0;JLabel l=new JLabel(label);panel.add(l,g);g.gridx=1;g.weightx=1;panel.add(component,g);g.gridx=2;g.weightx=0;JLabel h=new JLabel("ⓘ");panel.add(h,g);Tooltips.apply(tip,l,component,h);}
    private FrameSet selectedSet(){return frameSets.get(fovBox.getSelectedIndex());}
    private DetectionParameters parameters(){DetectionParameters p=new DetectionParameters();p.thresholdSigma=((Number)threshold.getValue()).doubleValue();p.barLength=((Number)barLength.getValue()).doubleValue();p.barWidth=((Number)barWidth.getValue()).doubleValue();p.minPeakDistance=((Number)minDistance.getValue()).intValue();p.gridTolerance=((Number)tolerance.getValue()).doubleValue();double px=((Number)pitchX.getValue()).doubleValue(),py=((Number)pitchY.getValue()).doubleValue();p.pitchX=px>0?px:Double.NaN;p.pitchY=py>0?py:Double.NaN;return p;}
    private void detect(){final FrameSet set=selectedSet();final int frame=((Number)detectionFrame.getValue()).intValue();if(frame<1||frame>set.getFrameCount()){error("Detection frame must be between 1 and "+set.getFrameCount());return;}final DetectionParameters p=parameters();progress.startIndeterminate("Detecting bars in "+set.getFovId()+" BF t"+frame+" …");setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));new SwingWorker<DetectionResult,Void>(){protected DetectionResult doInBackground(){return DetectionEngine.detect(TiffPlaneReader.read(set.file(Channel.BF,frame)).data(),p);}protected void done(){try{DetectionResult detected=get();if(!selectedSet().getFovId().equals(set.getFovId())){progress.finish("Detection discarded because the FOV changed.");return;}result=detected;fluorescenceReviews=null;fluorescenceFingerprint=null;selections=new ArrayList<>();for(int i=0;i<result.arrays().size();i++)selections.add(new ArraySelection(i,result.arrays().get(i)));arrayTable.setModel(new ArrayModel(selections));pitchX.setValue(result.pitch().x());pitchY.setValue(result.pitch().y());detectionSettingsFingerprint=currentDetectionFingerprint();progress.finish(String.format(Locale.ROOT,"Angle %.3f°, pitch %.2f × %.2f px, %d raw peaks, %d arrays",result.angleDegrees(),result.pitch().x(),result.pitch().y(),result.peaks().size(),result.arrays().size()));showPreview();}catch(Exception ex){error(cause(ex).getMessage());}finally{setCursor(Cursor.getDefaultCursor());}}}.execute();}
    private void showPreview(){if(result==null)return;if(preview!=null)preview.close();preview=PreviewRenderer.create(selectedSet().getFovId(),result,selections);preview.show();}

    private void reviewFluorescence(){
        if(result==null){error("Run detection first.");return;}
        if(!currentDetectionFingerprint().equals(detectionSettingsFingerprint)){error("Detection settings changed. Run step 1 again.");return;}
        final List<ExportBar> candidates=buildReviewBars();
        if(candidates.isEmpty()){error("No candidate bars have a complete crop inside the source image.");return;}
        final FrameSet set=selectedSet();final double reviewAngle=result.angleDegrees();final int width=REVIEW_CROP_WIDTH,height=REVIEW_CROP_HEIGHT;final int reviewBfFrame=((Number)detectionFrame.getValue()).intValue();final RotatedImage reviewBfRotated=result.rotated();final String candidateFingerprint=fingerprint(candidates);
        progress.update(0,2*set.getFrameCount()*candidates.size(),"Starting fluorescence scoring for "+candidates.size()+" bars …");setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<ReviewBundle,Void>(){
            protected ReviewBundle doInBackground()throws Exception{
                final double[] ranges={Double.POSITIVE_INFINITY,Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY,Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY,Double.NEGATIVE_INFINITY};
                SourcePlane bfSource=TiffPlaneReader.read(set.file(Channel.BF,reviewBfFrame));ranges[0]=bfSource.observedMin();ranges[1]=bfSource.observedMax();
                List<BarFluorescenceReview> reviews=FluorescenceReviewService.score(candidates,set.getFrameCount(),reviewAngle,width,height,Interpolation.BILINEAR,new FluorescenceParameters(),(channel,time)->{
                    SourcePlane source=TiffPlaneReader.read(set.file(channel,time));int base=channel==Channel.GFP?2:4;ranges[base]=Math.min(ranges[base],source.observedMin());ranges[base+1]=Math.max(ranges[base+1],source.observedMax());return source.data();},this::report);
                for(BarFluorescenceReview review:reviews){ExportBar bar=review.bar();review.setBfPreview(CropExtractor.extractHorizontal(bfSource.data(),bar.originalX(),bar.originalY(),reviewAngle,width,height,Interpolation.BILINEAR));}
                report(2*set.getFrameCount()*candidates.size(),2*set.getFrameCount()*candidates.size(),"Preparing rotated full-FOV review …");
                FullFovReviewData fullFovData=new FullFovReviewData(
                    FullFovDisplayRenderer.render(reviewBfRotated.plane(),360,560,FullFovDisplayRenderer.ChannelColor.GRAY),
                    renderFullFov(set.file(Channel.GFP,reviewBfFrame),reviewAngle,FullFovDisplayRenderer.ChannelColor.GREEN),
                    renderFullFov(set.file(Channel.MCHERRY,reviewBfFrame),reviewAngle,FullFovDisplayRenderer.ChannelColor.MAGENTA));
                return new ReviewBundle(reviews,ranges,fullFovData,candidateFingerprint);
            }
            private long lastUiUpdate;
            private synchronized void report(int completed,int total,String message){long now=System.nanoTime();if(completed==total||lastUiUpdate==0||now-lastUiUpdate>=200000000L){lastUiUpdate=now;SwingUtilities.invokeLater(()->progress.update(completed,total,message));}}
            protected void done(){try{ReviewBundle bundle=get();if(result==null||!selectedSet().getFovId().equals(set.getFovId())||!fingerprint(buildReviewBars()).equals(bundle.fingerprint)){progress.finish("Fluorescence scores discarded because bar settings changed. Run step 2 again.");return;}FluorescenceReviewDialog dialog=new FluorescenceReviewDialog(OpenGridSegWindow.this,bundle.reviews,bundle.fullFovData,bundle.ranges[0],bundle.ranges[1],bundle.ranges[2],bundle.ranges[3],bundle.ranges[4],bundle.ranges[5]);dialog.setVisible(true);if(dialog.approved()){fluorescenceReviews=bundle.reviews;fluorescenceLogic=dialog.signalLogic();fluorescenceFingerprint=bundle.fingerprint;progress.finish(dialog.selectedCount()+" of "+bundle.reviews.size()+" bars selected for export.");}else progress.finish("Fluorescence review cancelled; nothing changed.");}catch(Exception ex){error(cause(ex).getMessage());}finally{setCursor(Cursor.getDefaultCursor());}}
        }.execute();
    }

    private void export(){if(result==null){error("Run detection first.");return;}if(!currentDetectionFingerprint().equals(detectionSettingsFingerprint)){error("Detection settings changed. Run step 1 again.");return;}if(fluorescenceReviews==null){error("Run step 2: Review GFP / mCherry Bars before export.");return;}final List<ExportBar> current=buildReviewBars();if(!fingerprint(current).equals(fluorescenceFingerprint)){error("Bar settings changed after fluorescence review. Run step 2 again.");return;}final List<ExportBar> bars=new ArrayList<>();for(BarFluorescenceReview review:fluorescenceReviews)if(review.selected(fluorescenceLogic))bars.add(review.bar());if(bars.isEmpty()){error("No bars are selected for export.");return;}final FrameSet set=selectedSet();final ExportOptions options=new ExportOptions(Paths.get(outputFolder.getText()),((Number)cropWidth.getValue()).intValue(),((Number)cropHeight.getValue()).intValue(),(Interpolation)interpolation.getSelectedItem());copy(parameters(),options.detectionParameters());options.setDetectionFrame(((Number)detectionFrame.getValue()).intValue());options.setFittedPitch(result.pitch().x(),result.pitch().y());options.setFluorescenceRule(fluorescenceLogic.toString());options.addManualEdit("fluorescence first pass: local background; channel positive when at least 2 of Z90>=5, A90>=12, persistence>=20%; selected="+bars.size()+" of "+fluorescenceReviews.size());for(BarFluorescenceReview review:fluorescenceReviews)options.addFluorescenceDecision(new FluorescenceDecision(review.bar().id(),review.bar().arrayId(),review.bar().row(),review.bar().col(),review.gfp().persistentFrames(),review.gfp().maxPeakRobustZ(),review.gfp().z90(),review.gfp().area90(),review.gfp().persistence(),review.gfp().criteriaPassed(),review.mCherry().persistentFrames(),review.mCherry().maxPeakRobustZ(),review.mCherry().z90(),review.mCherry().area90(),review.mCherry().persistence(),review.mCherry().criteriaPassed(),review.selected(fluorescenceLogic),review.manuallyOverridden()));for(ArraySelection s:selections)if(s.edited())options.addManualEdit("array "+s.id()+" anchor changed from ("+s.source().rowStart()+","+s.source().colStart()+") to ("+s.rowStart()+","+s.colStart()+")");progress.update(0,2*bars.size(),"Starting export of "+bars.size()+" bars …");setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));new SwingWorker<ExportResult,Void>(){protected ExportResult doInBackground()throws Exception{return ExportService.export(set,bars,result.angleDegrees(),options,this::report);}private long lastUiUpdate;private synchronized void report(int completed,int total,String message){long now=System.nanoTime();if(completed==total||lastUiUpdate==0||now-lastUiUpdate>=200000000L){lastUiUpdate=now;SwingUtilities.invokeLater(()->progress.update(completed,total,message));}}protected void done(){try{ExportResult r=get();progress.finish("Export complete: "+r.omeTiff());IJ.showMessage("OpenGridSeg","Export complete:\n"+r.omeTiff()+"\n"+r.csv()+"\n"+r.config());}catch(Exception ex){error(cause(ex).getMessage());}finally{setCursor(Cursor.getDefaultCursor());}}}.execute();}

    private String currentDetectionFingerprint(){return selectedSet().getFovId()+":"+detectionFrame.getValue()+":"+threshold.getValue()+":"+barLength.getValue()+":"+barWidth.getValue()+":"+minDistance.getValue()+":"+pitchX.getValue()+":"+pitchY.getValue()+":"+tolerance.getValue();}
    private String fingerprint(List<ExportBar> bars){StringBuilder value=new StringBuilder().append(cropWidth.getValue()).append('x').append(cropHeight.getValue()).append(':').append(includeInferred.isSelected()).append(';');for(ExportBar bar:bars)value.append(bar.id()).append(':').append(bar.arrayId()).append(':').append(bar.row()).append(':').append(bar.col()).append(':').append(Double.doubleToLongBits(bar.originalX())).append(':').append(Double.doubleToLongBits(bar.originalY())).append(';');return value.toString();}
    private List<ExportBar> buildBars(){return buildBars(((Number)cropWidth.getValue()).intValue(),((Number)cropHeight.getValue()).intValue());}
    private List<ExportBar> buildReviewBars(){return buildBars(Math.max(REVIEW_CROP_WIDTH,((Number)cropWidth.getValue()).intValue()),Math.max(REVIEW_CROP_HEIGHT,((Number)cropHeight.getValue()).intValue()));}
    private List<ExportBar> buildBars(int cw,int ch){List<ExportBar> bars=new ArrayList<>();GridPhase phase=result.phase();Pitch pitch=result.pitch();Set<String> observed=new HashSet<>();for(int pi:phase.inlierIndices()){Peak p=result.peaks().get(pi);int gr=(int)Math.round((p.y()-phase.yOffset())/pitch.y()),gc=(int)Math.round((p.x()-phase.xOffset())/pitch.x());observed.add(gr+":"+gc);}int id=0;for(ArraySelection selection:selections){if(!selection.enabled())continue;boolean uncertain=selection.edited()||selection.source().observedCount()<200;for(int r=0;r<20;r++)for(int c=0;c<20;c++){int gr=selection.rowStart()+r,gc=selection.colStart()+c;boolean direct=observed.contains(gr+":"+gc);if(!direct&&!includeInferred.isSelected())continue;double rx=phase.xOffset()+gc*pitch.x(),ry=phase.yOffset()+gr*pitch.y();double[] original=result.rotated().toOriginal(rx,ry);if(!inside(original[0],original[1],result.angleDegrees(),cw,ch,result.rotated()))continue;String status=direct?"observed":"inferred";bars.add(new ExportBar(String.format(Locale.ROOT,"bar_%04d",++id),selection.id(),r,c,status,original[0],original[1],rx,ry,uncertain));}}return bars;}
    private static boolean inside(double cx,double cy,double angle,int width,int height,org.opengridseg.image.RotatedImage mapping){double a=Math.toRadians(angle),ca=Math.cos(a),sa=Math.sin(a),hw=(width-1)/2.0,hh=(height-1)/2.0;for(double u:new double[]{-hw,hw})for(double v:new double[]{-hh,hh}){double x=cx+ca*u-sa*v,y=cy+sa*u+ca*v;if(x<0||y<0||x>mapping.sourceWidth()-1||y>mapping.sourceHeight()-1)return false;}return true;}
    private static FovDisplayImage renderFullFov(Path file,double angle,FullFovDisplayRenderer.ChannelColor color)throws Exception{SourcePlane source=TiffPlaneReader.read(file);RotatedImage rotated=ExpandedRotation.rotate(source.data(),angle,Interpolation.BILINEAR);return FullFovDisplayRenderer.render(rotated.plane(),360,560,color);}
    private static void copy(DetectionParameters from,DetectionParameters to){to.barLength=from.barLength;to.barWidth=from.barWidth;to.thresholdSigma=from.thresholdSigma;to.minPeakDistance=from.minPeakDistance;to.gridTolerance=from.gridTolerance;to.pitchX=from.pitchX;to.pitchY=from.pitchY;}
    private void chooseOutput(){DirectoryChooser c=new DirectoryChooser("Choose OpenGridSeg output folder");String value=c.getDirectory();if(value!=null)outputFolder.setText(value);}
    private void error(String message){progress.finish("Error: "+message);IJ.showMessage("OpenGridSeg error",message==null?"Unknown error":message);}
    private static Throwable cause(Throwable e){Throwable c=e;while(c.getCause()!=null)c=c.getCause();return c;}

    private static final class ReviewBundle {private final List<BarFluorescenceReview> reviews;private final double[] ranges;private final FullFovReviewData fullFovData;private final String fingerprint;ReviewBundle(List<BarFluorescenceReview> reviews,double[] ranges,FullFovReviewData fullFovData,String fingerprint){this.reviews=reviews;this.ranges=ranges;this.fullFovData=fullFovData;this.fingerprint=fingerprint;}}

    private static final class ArrayModel extends AbstractTableModel {private final List<ArraySelection> rows;private final String[] columns={"Use","Array","Direct peaks","Row start","Column start"};ArrayModel(List<ArraySelection> rows){this.rows=rows;}public int getRowCount(){return rows.size();}public int getColumnCount(){return columns.length;}public String getColumnName(int c){return columns[c];}public Class<?> getColumnClass(int c){return c==0?Boolean.class:Integer.class;}public boolean isCellEditable(int r,int c){return c==0||c==3||c==4;}public Object getValueAt(int r,int c){ArraySelection s=rows.get(r);switch(c){case 0:return s.enabled();case 1:return s.id();case 2:return s.source().observedCount();case 3:return s.rowStart();default:return s.colStart();}}public void setValueAt(Object v,int r,int c){ArraySelection s=rows.get(r);if(c==0)s.setEnabled((Boolean)v);else if(c==3)s.setRowStart(((Number)v).intValue());else if(c==4)s.setColStart(((Number)v).intValue());fireTableRowsUpdated(r,r);}}
}
