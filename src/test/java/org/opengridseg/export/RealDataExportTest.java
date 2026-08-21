package org.opengridseg.export;

import static org.junit.jupiter.api.Assertions.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import loci.formats.FormatTools;
import loci.formats.ImageReader;
import org.opengridseg.detect.*;
import org.opengridseg.image.Interpolation;
import org.opengridseg.image.CropExtractor;
import org.opengridseg.image.FloatPlane;
import org.opengridseg.io.*;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class RealDataExportTest {
    @Test void exportsOneRealBarAcrossThreeChannelsAndThirtyFrames() throws Exception {
        String folderValue=System.getProperty("opengridseg.realFolder");
        String outputValue=System.getProperty("opengridseg.realOutput");
        Assumptions.assumeTrue(folderValue!=null && outputValue!=null);
        Path folder=Paths.get(folderValue), output=Paths.get(outputValue);
        FrameSet set=DatasetScanner.scan(folder).stream()
            .filter(f->f.getFovId().endsWith("Cell14")).findFirst().orElseThrow(IllegalStateException::new);
        SourcePlane bf=TiffPlaneReader.read(set.file(Channel.BF,1));
        DetectionResult detection=DetectionEngine.detect(bf.data(),new DetectionParameters());
        Peak chosen=null;double[] sourceCenter=null;
        for(int index:detection.phase().inlierIndices()){
            Peak peak=detection.peaks().get(index);double[] source=detection.rotated().toOriginal(peak.x(),peak.y());
            if(source[0]>100&&source[1]>100&&source[0]<bf.data().width()-100&&source[1]<bf.data().height()-100){chosen=peak;sourceCenter=source;break;}
        }
        assertNotNull(chosen);
        Files.createDirectories(output);
        ExportBar bar=new ExportBar("bar_0001",0,0,0,"observed",sourceCenter[0],sourceCenter[1],chosen.x(),chosen.y(),true);
        ExportOptions options=new ExportOptions(output,96,48,Interpolation.BICUBIC);
        options.setFittedPitch(detection.pitch().x(),detection.pitch().y());
        ExportResult result=ExportService.export(set,Collections.singletonList(bar),detection.angleDegrees(),options);
        ImageReader reader=new ImageReader();reader.setId(result.omeTiff().toString());
        assertEquals(1,reader.getSeriesCount());assertEquals(3,reader.getSizeC());assertEquals(30,reader.getSizeT());assertEquals(1,reader.getSizeZ());assertEquals(FormatTools.FLOAT,reader.getPixelType());
        byte[] firstBytes=reader.openBytes(reader.getIndex(0,0,0));
        FloatBuffer floats=ByteBuffer.wrap(firstBytes).order(reader.isLittleEndian()?ByteOrder.LITTLE_ENDIAN:ByteOrder.BIG_ENDIAN).asFloatBuffer();
        float cropMin=Float.POSITIVE_INFINITY,cropMax=Float.NEGATIVE_INFINITY;
        while(floats.hasRemaining()){float v=floats.get();if(Float.isFinite(v)){cropMin=Math.min(cropMin,v);cropMax=Math.max(cropMax,v);}}
        assertTrue(cropMin>=bf.observedMin());assertTrue(cropMax<=bf.observedMax());assertTrue(cropMax>10f);
        Channel[] outputChannels={Channel.BF,Channel.GFP,Channel.MCHERRY};
        for(int time:new int[]{0,29})for(int channel=0;channel<3;channel++){
            SourcePlane source=TiffPlaneReader.read(set.file(outputChannels[channel],time+1));
            FloatPlane expected=CropExtractor.extractHorizontal(source.data(),sourceCenter[0],sourceCenter[1],detection.angleDegrees(),96,48,Interpolation.BICUBIC);
            byte[] bytes=reader.openBytes(reader.getIndex(0,channel,time));
            FloatBuffer actualBuffer=ByteBuffer.wrap(bytes).order(reader.isLittleEndian()?ByteOrder.LITTLE_ENDIAN:ByteOrder.BIG_ENDIAN).asFloatBuffer();
            float[] actual=new float[actualBuffer.remaining()];actualBuffer.get(actual);
            assertArrayEquals(expected.pixels(),actual,0f,"channel="+outputChannels[channel]+" time="+(time+1));
            System.out.printf("CHANNEL_CHECK channel=%s time=%d source=[%.3f,%.3f] crop=[%.3f,%.3f]%n",outputChannels[channel],time+1,source.observedMin(),source.observedMax(),expected.min(),expected.max());
        }
        reader.close();
        assertTrue(Files.size(result.omeTiff())>1_000_000);
        System.out.printf("REAL_EXPORT ome=%s bytes=%d csv=%s json=%s%n",result.omeTiff(),Files.size(result.omeTiff()),result.csv(),result.config());
    }
}
