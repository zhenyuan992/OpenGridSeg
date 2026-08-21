package org.opengridseg.fluorescence;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import org.opengridseg.ProgressListener;
import org.opengridseg.export.ExportBar;
import org.opengridseg.image.CropExtractor;
import org.opengridseg.image.FloatPlane;
import org.opengridseg.image.Interpolation;
import org.opengridseg.io.Channel;

public final class FluorescenceReviewService {
    private static final Channel[] CHANNELS={Channel.GFP,Channel.MCHERRY};
    private static final int MAX_THREADS=8;
    private FluorescenceReviewService(){}

    public static List<BarFluorescenceReview> score(List<ExportBar> bars,int frames,double angle,
            int cropWidth,int cropHeight,Interpolation interpolation,FluorescenceParameters parameters,
            FluorescencePlaneProvider provider)throws Exception{
        return score(bars,frames,angle,cropWidth,cropHeight,interpolation,parameters,provider,ProgressListener.NONE);
    }

    public static List<BarFluorescenceReview> score(List<ExportBar> bars,int frames,double angle,
            int cropWidth,int cropHeight,Interpolation interpolation,FluorescenceParameters parameters,
            FluorescencePlaneProvider provider,ProgressListener progress)throws Exception{
        FluorescenceScoreAccumulator[][] accumulators=new FluorescenceScoreAccumulator[2][bars.size()];
        for(int channel=0;channel<2;channel++)for(int bar=0;bar<bars.size();bar++)
            accumulators[channel][bar]=new FluorescenceScoreAccumulator(cropWidth,cropHeight,parameters);
        int total=CHANNELS.length*frames*bars.size();
        int threads=Math.max(1,Math.min(MAX_THREADS,Runtime.getRuntime().availableProcessors()));
        ForkJoinPool pool=new ForkJoinPool(threads);
        try{
            for(int channel=0;channel<CHANNELS.length;channel++)for(int time=1;time<=frames;time++){
                final int channelIndex=channel,timeIndex=time;
                final int base=(channel*frames+(time-1))*bars.size();
                final FloatPlane source=provider.read(CHANNELS[channel],time);
                final AtomicInteger processed=new AtomicInteger();
                pool.submit(()->IntStream.range(0,bars.size()).parallel().forEach(bar->{
                    ExportBar item=bars.get(bar);
                    FloatPlane crop=CropExtractor.extractHorizontal(source,item.originalX(),item.originalY(),angle,cropWidth,cropHeight,interpolation);
                    accumulators[channelIndex][bar].accept(crop);
                    synchronized(progress){
                        int done=processed.incrementAndGet();
                        String name=CHANNELS[channelIndex]==Channel.GFP?"GFP":"mCherry";
                        progress.update(base+done,total,"Scoring "+name+" frame "+timeIndex+" / "+frames+" — "+done+" / "+bars.size()+" bars processed");
                    }
                })).get();
            }
        }finally{pool.shutdown();}
        List<BarFluorescenceReview> reviews=new ArrayList<>();
        for(int bar=0;bar<bars.size();bar++)
            reviews.add(new BarFluorescenceReview(bars.get(bar),accumulators[0][bar].finish(),accumulators[1][bar].finish()));
        return reviews;
    }
}
