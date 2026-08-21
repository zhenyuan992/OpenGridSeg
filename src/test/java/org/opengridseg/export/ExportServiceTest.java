package org.opengridseg.export;

import static org.junit.jupiter.api.Assertions.*;
import ij.ImagePlus;
import ij.io.FileSaver;
import ij.process.ShortProcessor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.nio.charset.StandardCharsets;
import loci.formats.ImageReader;
import org.opengridseg.io.DatasetScanner;
import org.opengridseg.io.FrameSet;
import org.opengridseg.image.Interpolation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ExportServiceTest {
    @TempDir Path folder;

    @Test void exportsHorizontalXyctCropCsvAndReproducibleJson() throws Exception {
        Path input=folder.resolve("input"),output=folder.resolve("output");Files.createDirectories(input);
        for(int t=1;t<=2;t++) for(int wave=1;wave<=3;wave++) {
            short[] pixels=new short[48*48];
            for(int y=0;y<48;y++) for(int x=0;x<48;x++) pixels[y*48+x]=(short)(100+wave*100+t*10+x+y);
            String channel=wave==1?"CSU GFP":wave==2?"CSU mCherry":"Trans Cam";
            Path file=input.resolve("sample_Cell12_w"+wave+channel+"_t"+t+".TIF");
            assertTrue(new FileSaver(new ImagePlus("p",new ShortProcessor(48,48,pixels,null))).saveAsTiff(file.toString()));
        }
        FrameSet set=DatasetScanner.scan(input).get(0);
        ExportOptions options=new ExportOptions(output,13,9,Interpolation.BICUBIC);
        options.setFluorescenceRule("GFP OR mCherry");
        options.addFluorescenceDecision(new FluorescenceDecision("bar_0001",0,2,3,4,9.0,8.5,15.0,0.3,3,0,3.5,3.2,2.0,0.0,1,true,false));
        options.addFluorescenceDecision(new FluorescenceDecision("bar_0002",0,2,4,0,3.2,3.1,1.0,0.0,0,0,3.1,3.0,1.0,0.0,0,false,true));
        ExportBar bar=new ExportBar("bar_0001",0,2,3,"observed",24,24,24,24,false);
        java.util.List<String> progress=new java.util.ArrayList<>();
        ExportResult result=ExportService.export(set,Collections.singletonList(bar),31.0,options,(completed,total,message)->progress.add(completed+"/"+total+" "+message));
        assertEquals("2/2 Exported 1 / 1 bars",progress.get(progress.size()-1));
        assertTrue(Files.isRegularFile(result.omeTiff()));
        String csv=new String(Files.readAllBytes(result.csv()),StandardCharsets.UTF_8);
        assertTrue(csv.contains("source_filename"));
        assertTrue(csv.contains("gfp_persistent_frames"));
        assertTrue(csv.contains(",4,9.0,8.5,15.0,0.3,3,0,3.5,3.2,2.0,0.0,1,"));
        String json=new String(Files.readAllBytes(result.config()),StandardCharsets.UTF_8);
        assertTrue(json.contains("\"intensity_rescaled\": false"));
        assertTrue(json.contains("\"interpolation\": \"BICUBIC\""));
        assertTrue(json.contains("\"threshold_sigma\": 7.0"));
        assertTrue(json.contains("\"manual_edits\": []"));
        assertTrue(json.contains("\"fluorescence_selection_rule\": \"GFP OR mCherry\""));
        assertTrue(json.contains("\"bar_id\": \"bar_0002\""));
        assertTrue(json.contains("\"selected\": false"));
        ImageReader reader=new ImageReader();reader.setId(result.omeTiff().toString());
        assertEquals(1,reader.getSeriesCount());assertEquals(3,reader.getSizeC());assertEquals(2,reader.getSizeT());reader.close();
    }
}
