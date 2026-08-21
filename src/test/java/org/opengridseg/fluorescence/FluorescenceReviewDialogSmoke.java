package org.opengridseg.fluorescence;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.opengridseg.export.ExportBar;
import org.opengridseg.image.FloatPlane;
import org.opengridseg.ui.FluorescenceReviewDialog;
import org.opengridseg.ui.FovDisplayImage;
import org.opengridseg.ui.FullFovReviewData;

public final class FluorescenceReviewDialogSmoke {
    public static void main(String[] args){
        SwingUtilities.invokeLater(()->{
            List<BarFluorescenceReview> reviews=new ArrayList<>();
            for(int i=0;i<24;i++){
                float[] pixels=new float[96*48];for(int p=0;p<pixels.length;p++)pixels[p]=(float)(200+20*Math.sin(p*.03+i));
                FloatPlane projection=new FloatPlane(96,48,pixels);
                FluorescenceBarScore g=new FluorescenceBarScore(30,12,8.2,6.4,18.0,.4,3,true,projection);
                FluorescenceBarScore m=new FluorescenceBarScore(30,2,4.1,4.0,5.0,.067,1,false,projection);
                ExportBar bar=new ExportBar(String.format("bar_%04d",i+1),0,i/20,i%20,i%3==0?"observed":"inferred",100+i,200+i,50+i,60+i,false);
                BarFluorescenceReview review=new BarFluorescenceReview(bar,g,m);review.setBfPreview(projection);reviews.add(review);
            }
            FovDisplayImage fov=new FovDisplayImage(new BufferedImage(300,300,BufferedImage.TYPE_INT_RGB),400,400);
            FullFovReviewData fullFov=new FullFovReviewData(fov,fov,fov);
            FluorescenceReviewDialog dialog=new FluorescenceReviewDialog(null,reviews,fullFov,0,4095,80,3500,90,4200);
            new Timer(1000,e->{try{BufferedImage image=new BufferedImage(dialog.getWidth(),dialog.getHeight(),BufferedImage.TYPE_INT_RGB);java.awt.Graphics2D graphics=image.createGraphics();dialog.printAll(graphics);graphics.dispose();javax.imageio.ImageIO.write(image,"png",new java.io.File("target/review-controls-two-rows.png"));dialog.dispose();System.exit(0);}catch(Exception error){error.printStackTrace();System.exit(2);}}){{setRepeats(false);start();}};
            dialog.setVisible(true);
        });
    }
}
