package org.opengridseg.export;

import java.nio.file.Path;
import java.util.List;
import loci.common.DataTools;
import loci.common.services.ServiceFactory;
import loci.formats.ome.OMEXMLMetadata;
import loci.formats.out.OMETiffWriter;
import loci.formats.services.OMEXMLService;
import ome.xml.model.enums.DimensionOrder;
import ome.xml.model.enums.PixelType;
import ome.xml.model.primitives.PositiveInteger;
import org.opengridseg.ProgressListener;
import org.opengridseg.image.FloatPlane;

public final class OmeTiffExporter {
    private OmeTiffExporter(){}
    public static void write(Path output,List<String> barNames,int width,int height,int channels,int times,CropPlaneProvider provider)throws Exception{
        write(output,barNames,width,height,channels,times,provider,ProgressListener.NONE);
    }
    public static void write(Path output,List<String> barNames,int width,int height,int channels,int times,CropPlaneProvider provider,ProgressListener progress)throws Exception{
        if(barNames.isEmpty())throw new IllegalArgumentException("No bars to export");
        OMEXMLService service=new ServiceFactory().getInstance(OMEXMLService.class);OMEXMLMetadata meta=service.createOMEXMLMetadata();
        String[] channelNames={"BF","GFP","mCherry"};
        for(int s=0;s<barNames.size();s++){
            meta.setImageID("Image:"+s,s);meta.setImageName(barNames.get(s),s);meta.setPixelsID("Pixels:"+s,s);
            meta.setPixelsDimensionOrder(DimensionOrder.XYCTZ,s);meta.setPixelsType(PixelType.FLOAT,s);
            meta.setPixelsSizeX(new PositiveInteger(width),s);meta.setPixelsSizeY(new PositiveInteger(height),s);
            meta.setPixelsSizeZ(new PositiveInteger(1),s);meta.setPixelsSizeC(new PositiveInteger(channels),s);meta.setPixelsSizeT(new PositiveInteger(times),s);meta.setPixelsBigEndian(Boolean.FALSE,s);
            for(int c=0;c<channels;c++){meta.setChannelID("Channel:"+s+":"+c,s,c);meta.setChannelName(c<channelNames.length?channelNames[c]:"Channel "+c,s,c);meta.setChannelSamplesPerPixel(new PositiveInteger(1),s,c);}
        }
        OMETiffWriter writer=new OMETiffWriter();writer.setMetadataRetrieve(meta);writer.setBigTiff(true);writer.setInterleaved(false);writer.setId(output.toString());
        try{
            for(int s=0;s<barNames.size();s++){
                writer.setSeries(s);
                for(int t=0;t<times;t++)for(int c=0;c<channels;c++){
                    FloatPlane plane=provider.plane(s,c,t);
                    if(plane.width()!=width||plane.height()!=height)throw new IllegalArgumentException("Crop dimensions changed at bar "+s+", channel "+c+", time "+t);
                    writer.saveBytes(t*channels+c,DataTools.floatsToBytes(plane.pixels(),true));
                }
                progress.update(s+1,barNames.size(),"Exported "+(s+1)+" / "+barNames.size()+" bars");
            }
        }finally{writer.close();}
    }
}
