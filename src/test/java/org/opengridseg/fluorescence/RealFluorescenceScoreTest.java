package org.opengridseg.fluorescence;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import org.opengridseg.detect.*;
import org.opengridseg.export.ExportBar;
import org.opengridseg.image.Interpolation;
import org.opengridseg.io.*;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class RealFluorescenceScoreTest {
    @Test void scoresRealCell14BarsWhenFolderIsProvided() throws Exception {
        String folderValue=System.getProperty("opengridseg.realFolder");
        String csvValue=System.getProperty("opengridseg.scoreCsv");
        Assumptions.assumeTrue(folderValue!=null&&csvValue!=null);
        FrameSet set=DatasetScanner.scan(Paths.get(folderValue)).stream().filter(f->f.getFovId().endsWith("Cell14")).findFirst().orElseThrow(IllegalStateException::new);
        DetectionResult detection=DetectionEngine.detect(TiffPlaneReader.read(set.file(Channel.BF,1)).data(),new DetectionParameters());
        List<ExportBar> bars=new ArrayList<>();int id=0;
        for(int array=0;array<detection.arrays().size();array++)for(BarNode node:detection.arrays().get(array).nodes()){
            double[] source=detection.rotated().toOriginal(node.x(),node.y());
            if(!inside(source[0],source[1],detection.angleDegrees(),96,48,detection.rotated().sourceWidth(),detection.rotated().sourceHeight()))continue;
            bars.add(new ExportBar(String.format(Locale.ROOT,"bar_%04d",++id),array,node.row(),node.col(),node.observed()?"observed":"inferred",source[0],source[1],node.x(),node.y(),true));
        }
        List<BarFluorescenceReview> reviews=FluorescenceReviewService.score(bars,30,detection.angleDegrees(),96,48,Interpolation.BILINEAR,new FluorescenceParameters(),(channel,time)->TiffPlaneReader.read(set.file(channel,time)).data());
        int either=0,both=0;StringBuilder csv=new StringBuilder("bar_id,array,row,col,status,gfp_persistent_frames,gfp_z90,gfp_area90,gfp_persistence,gfp_criteria,mcherry_persistent_frames,mcherry_z90,mcherry_area90,mcherry_persistence,mcherry_criteria,either,both\n");
        for(BarFluorescenceReview review:reviews){if(review.autoSelected(SignalLogic.EITHER_CHANNEL))either++;if(review.autoSelected(SignalLogic.BOTH_CHANNELS))both++;ExportBar b=review.bar();csv.append(b.id()).append(',').append(b.arrayId()).append(',').append(b.row()).append(',').append(b.col()).append(',').append(b.status()).append(',').append(review.gfp().persistentFrames()).append(',').append(review.gfp().z90()).append(',').append(review.gfp().area90()).append(',').append(review.gfp().persistence()).append(',').append(review.gfp().criteriaPassed()).append(',').append(review.mCherry().persistentFrames()).append(',').append(review.mCherry().z90()).append(',').append(review.mCherry().area90()).append(',').append(review.mCherry().persistence()).append(',').append(review.mCherry().criteriaPassed()).append(',').append(review.autoSelected(SignalLogic.EITHER_CHANNEL)).append(',').append(review.autoSelected(SignalLogic.BOTH_CHANNELS)).append('\n');}
        Files.write(Paths.get(csvValue),csv.toString().getBytes(StandardCharsets.UTF_8));
        System.out.printf("REAL_FLUORESCENCE bars=%d either=%d both=%d csv=%s%n",reviews.size(),either,both,csvValue);
        assertTrue(either>0);assertTrue(either<reviews.size());
    }
    private static boolean inside(double cx,double cy,double angle,int width,int height,int sourceWidth,int sourceHeight){double a=Math.toRadians(angle),ca=Math.cos(a),sa=Math.sin(a),hw=(width-1)/2.0,hh=(height-1)/2.0;for(double u:new double[]{-hw,hw})for(double v:new double[]{-hh,hh}){double x=cx+ca*u-sa*v,y=cy+sa*u+ca*v;if(x<0||y<0||x>sourceWidth-1||y>sourceHeight-1)return false;}return true;}
}
