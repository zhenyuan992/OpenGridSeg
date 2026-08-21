package org.opengridseg.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.Window;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.Timer;
import org.opengridseg.OpenGridSegPlugin;
import org.opengridseg.export.ExportBar;
import org.opengridseg.fluorescence.BarFluorescenceReview;

public final class RealPluginProgressSmoke {
    private OpenGridSegWindow main;
    private boolean detectionStarted,reviewStarted,dialogSeen;

    public static void main(String[] args)throws Exception{
        if(args.length!=1)throw new IllegalArgumentException("dataset folder required");
        Files.deleteIfExists(Paths.get("/tmp/opengridseg-scoring-started"));
        Files.deleteIfExists(Paths.get("/tmp/opengridseg-review-opened"));
        System.setProperty("opengridseg.input",args[0]);
        new OpenGridSegPlugin().run();
        RealPluginProgressSmoke smoke=new RealPluginProgressSmoke();
        new Timer(250,e->{try{smoke.tick();}catch(Exception error){error.printStackTrace();System.exit(2);}}).start();
        new Timer(120000,e->{System.err.println("timeout");System.exit(3);}){{setRepeats(false);start();}};
    }

    private void tick()throws Exception{
        if(main==null){for(Window window:Window.getWindows())if(window instanceof OpenGridSegWindow){main=(OpenGridSegWindow)window;break;}if(main==null)return;}
        if(!detectionStarted){
            if(!"OpenGridSeg — Open Grid Segmentation".equals(main.getTitle()))throw new IllegalStateException("OpenGridSeg title missing");
            JLabel template=findLabel(main,"Template bar (length / width, px)");
            if(template==null)throw new IllegalStateException("Template bar control missing");
            BufferedImage screenshot=new BufferedImage(main.getWidth(),main.getHeight(),BufferedImage.TYPE_INT_RGB);Graphics2D graphics=screenshot.createGraphics();main.printAll(graphics);graphics.dispose();ImageIO.write(screenshot,"png",Paths.get("target/opengridseg-main-panel.png").toFile());
            detectionStarted=true;invoke(main,"detect");return;
        }
        Field result=OpenGridSegWindow.class.getDeclaredField("result");result.setAccessible(true);
        if(!reviewStarted&&result.get(main)!=null){reviewStarted=true;invoke(main,"reviewFluorescence");Files.write(Paths.get("/tmp/opengridseg-scoring-started"),"started".getBytes(StandardCharsets.UTF_8));return;}
        if(!reviewStarted||dialogSeen)return;
        main.toFront();main.requestFocus();
        for(Window window:Window.getWindows())if(window instanceof FluorescenceReviewDialog&&window.isShowing()){
            dialogSeen=true;FluorescenceReviewDialog dialog=(FluorescenceReviewDialog)window;
            JButton show=findButton(dialog,"Show selected BF / GFP / mCherry");JButton full=findButton(dialog,"Show full FOV map");JCheckBox scaling=findCheckBox(dialog,"Preview with per-channel scaling");
            if(show==null||full==null)throw new IllegalStateException("review buttons not found");
            if(scaling==null||!scaling.isSelected()||scaling.getToolTipText()==null||!scaling.getToolTipText().contains("Exported pixel values are never changed"))throw new IllegalStateException("default-on preview scaling control or export tooltip missing");
            show.doClick();full.doClick();
            new Timer(1000,e->{try{verify(dialog);}catch(Exception error){error.printStackTrace();System.exit(5);}}){{setRepeats(false);start();}};
        }
    }

    @SuppressWarnings("unchecked")
    private static void verify(FluorescenceReviewDialog dialog)throws Exception{
        Field previewField=FluorescenceReviewDialog.class.getDeclaredField("preview");previewField.setAccessible(true);ij.ImagePlus image=(ij.ImagePlus)previewField.get(dialog);
        if(image==null||image.getWidth()!=288||!image.getTitle().contains("per-channel preview scaling"))throw new IllegalStateException("three-channel scaled bar preview missing");
        new ij.io.FileSaver(image).saveAsPng("target/real-selected-bar-preview.png");

        Field fullField=FluorescenceReviewDialog.class.getDeclaredField("fullFovWindow");fullField.setAccessible(true);FullFovReviewDialog full=(FullFovReviewDialog)fullField.get(dialog);
        if(full==null||!full.isShowing())throw new IllegalStateException("full FOV selection map is not visible");
        Field panelsField=FullFovReviewDialog.class.getDeclaredField("panels");panelsField.setAccessible(true);List<FullFovSelectionPanel> panels=(List<FullFovSelectionPanel>)panelsField.get(full);
        if(panels.size()!=3)throw new IllegalStateException("full FOV map does not have three channels");
        FullFovSelectionPanel panel=panels.get(0);Field displayField=FullFovSelectionPanel.class.getDeclaredField("display");displayField.setAccessible(true);FovDisplayImage display=(FovDisplayImage)displayField.get(panel);
        Field barsField=FullFovSelectionPanel.class.getDeclaredField("bars");barsField.setAccessible(true);List<ExportBar> bars=(List<ExportBar>)barsField.get(panel);int clickedIndex=Math.min(10,bars.size()-1);ExportBar clicked=bars.get(clickedIndex);
        JCheckBox markers=findCheckBox(full,"Show bar markers");if(markers==null)throw new IllegalStateException("marker visibility toggle missing");markers.doClick();Field markersVisible=FullFovSelectionPanel.class.getDeclaredField("markersVisible");markersVisible.setAccessible(true);if(markersVisible.getBoolean(panel))throw new IllegalStateException("markers did not hide");markers.doClick();
        Field reviewsField=FluorescenceReviewDialog.class.getDeclaredField("reviews");reviewsField.setAccessible(true);List<BarFluorescenceReview> reviews=(List<BarFluorescenceReview>)reviewsField.get(dialog);boolean before=reviews.get(clickedIndex).selected(dialog.signalLogic());
        int x=display.displayX(clicked.rotatedX()),y=display.displayY(clicked.rotatedY());panel.dispatchEvent(new MouseEvent(panel,MouseEvent.MOUSE_CLICKED,0,0,x,y,1,false));
        if(reviews.get(clickedIndex).selected(dialog.signalLogic())==before||!reviews.get(clickedIndex).manuallyOverridden())throw new IllegalStateException("single click did not toggle exactly the clicked bar");
        image=(ij.ImagePlus)previewField.get(dialog);if(image==null||!image.getTitle().contains(clicked.id()))throw new IllegalStateException("single clicked full-FOV bar did not become the bar preview");

        JCheckBox bulk=findCheckBox(full,"Toggle in a 5×5 selection");if(bulk==null)throw new IllegalStateException("5x5 bulk toggle control missing");bulk.doClick();int bulkIndex=0,best=0;for(int index=0;index<bars.size();index++){int size=BarSelectionNeighborhood.fiveByFive(bars,index).size();if(size>best){best=size;bulkIndex=index;}}if(best<9)throw new IllegalStateException("no usable real-data bulk neighborhood");List<Integer> block=BarSelectionNeighborhood.fiveByFive(bars,bulkIndex);boolean[] stateBefore=new boolean[reviews.size()];for(int index=0;index<reviews.size();index++)stateBefore[index]=reviews.get(index).selected(dialog.signalLogic());boolean target=!stateBefore[bulkIndex];ExportBar bulkBar=bars.get(bulkIndex);
        x=display.displayX(bulkBar.rotatedX());y=display.displayY(bulkBar.rotatedY());panel.dispatchEvent(new MouseEvent(panel,MouseEvent.MOUSE_CLICKED,0,0,x,y,1,false));
        for(int index=0;index<reviews.size();index++){boolean selected=reviews.get(index).selected(dialog.signalLogic());if(block.contains(index)){if(selected!=target||!reviews.get(index).manuallyOverridden())throw new IllegalStateException("5x5 block was not set together");}else if(selected!=stateBefore[index])throw new IllegalStateException("5x5 click changed a bar outside its block");}
        image=(ij.ImagePlus)previewField.get(dialog);if(image==null||!image.getTitle().contains(bulkBar.id()))throw new IllegalStateException("bulk clicked full-FOV bar did not become the bar preview");

        BufferedImage screenshot=new BufferedImage(full.getWidth(),full.getHeight(),BufferedImage.TYPE_INT_RGB);Graphics2D graphics=screenshot.createGraphics();full.printAll(graphics);graphics.dispose();ImageIO.write(screenshot,"png",Paths.get("target/real-full-fov-selection-map.png").toFile());
        Field tableField=FluorescenceReviewDialog.class.getDeclaredField("table");tableField.setAccessible(true);JTable table=(JTable)tableField.get(dialog);if(table.getSelectedRow()<0||table.convertRowIndexToModel(table.getSelectedRow())!=bulkIndex)throw new IllegalStateException("bulk clicked full-FOV bar did not become the table selection");
        Files.write(Paths.get("/tmp/opengridseg-review-opened"),("single-and-5x5-verified block="+best).getBytes(StandardCharsets.UTF_8));
        new Timer(1500,e->{for(Window window:Window.getWindows())window.dispose();System.exit(0);}){{setRepeats(false);start();}};
    }

    private static JCheckBox findCheckBox(Container root,String text){for(Component component:root.getComponents()){if(component instanceof JCheckBox&&text.equals(((JCheckBox)component).getText()))return (JCheckBox)component;if(component instanceof Container){JCheckBox found=findCheckBox((Container)component,text);if(found!=null)return found;}}return null;}
    private static JLabel findLabel(Container root,String text){for(Component component:root.getComponents()){if(component instanceof JLabel&&text.equals(((JLabel)component).getText()))return (JLabel)component;if(component instanceof Container){JLabel found=findLabel((Container)component,text);if(found!=null)return found;}}return null;}
    private static JButton findButton(Container root,String text){for(Component component:root.getComponents()){if(component instanceof JButton&&text.equals(((JButton)component).getText()))return (JButton)component;if(component instanceof Container){JButton found=findButton((Container)component,text);if(found!=null)return found;}}return null;}
    private static void invoke(OpenGridSegWindow window,String methodName)throws Exception{Method method=OpenGridSegWindow.class.getDeclaredMethod(methodName);method.setAccessible(true);method.invoke(window);}
}
