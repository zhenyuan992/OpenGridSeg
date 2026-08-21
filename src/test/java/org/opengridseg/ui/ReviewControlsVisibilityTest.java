package org.opengridseg.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.Collections;
import javax.swing.JCheckBox;
import javax.swing.SwingUtilities;
import org.opengridseg.ProgressListener;
import org.opengridseg.export.ExportBar;
import org.opengridseg.fluorescence.BarFluorescenceReview;
import org.opengridseg.fluorescence.FluorescenceParameters;
import org.opengridseg.fluorescence.FluorescenceReviewService;
import org.opengridseg.image.FloatPlane;
import org.opengridseg.image.Interpolation;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

final class ReviewControlsVisibilityTest {
    @Test void previewScalingCheckBoxFitsInsideTheStep2Window()throws Exception{
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());
        SwingUtilities.invokeAndWait(()->{
            try{
                ExportBar bar=new ExportBar("bar",0,0,0,"observed",10,10,10,10,false);
                BarFluorescenceReview review=FluorescenceReviewService.score(Collections.singletonList(bar),1,0,12,8,Interpolation.NEAREST,new FluorescenceParameters(),(channel,time)->new FloatPlane(24,24),ProgressListener.NONE).get(0);review.setBfPreview(new FloatPlane(12,8));
                FovDisplayImage fov=new FovDisplayImage(new BufferedImage(100,100,BufferedImage.TYPE_INT_RGB),100,100);
                FluorescenceReviewDialog dialog=new FluorescenceReviewDialog(null,Collections.singletonList(review),new FullFovReviewData(fov,fov,fov),0,1,0,1,0,1);
                dialog.setSize(900,700);dialog.addNotify();layout(dialog.getContentPane());
                JCheckBox check=findCheckBox(dialog,"Preview with per-channel scaling");assertTrue(check!=null,"scaling checkbox is missing");assertTrue(check.isSelected(),"preview scaling should be on by default");assertEquals(3,check.getParent().getComponentCount(),"preview controls need their own row so the scaling checkbox stays visible");Rectangle parentVisible=new Rectangle(0,0,check.getParent().getWidth(),check.getParent().getHeight());assertTrue(parentVisible.contains(check.getBounds()),"scaling checkbox is clipped by its control row: "+check.getBounds()+" outside "+parentVisible);Rectangle bounds=SwingUtilities.convertRectangle(check.getParent(),check.getBounds(),dialog.getContentPane());Rectangle visible=new Rectangle(0,0,dialog.getContentPane().getWidth(),dialog.getContentPane().getHeight());
                assertTrue(visible.contains(bounds),"scaling checkbox is clipped: "+bounds+" outside "+visible);dialog.dispose();
            }catch(Exception error){throw new RuntimeException(error);}
        });
    }
    private static void layout(Container root){root.doLayout();for(Component child:root.getComponents())if(child instanceof Container)layout((Container)child);}
    private static JCheckBox findCheckBox(Container root,String text){for(Component child:root.getComponents()){if(child instanceof JCheckBox&&text.equals(((JCheckBox)child).getText()))return (JCheckBox)child;if(child instanceof Container){JCheckBox found=findCheckBox((Container)child,text);if(found!=null)return found;}}return null;}
}
