package org.opengridseg.io;

import ij.io.FileInfo;
import ij.io.TiffDecoder;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public final class DatasetValidator {
    private DatasetValidator() {}

    public static void validate(List<FrameSet> sets) {
        for (FrameSet set : sets) validate(set);
    }

    public static void validate(FrameSet set) {
        int expectedWidth=-1, expectedHeight=-1;
        for (int time=1; time<=set.getFrameCount(); time++) {
            for (Channel channel : Channel.values()) {
                Path path=set.file(channel,time);
                FileInfo info=readHeader(path);
                if (expectedWidth < 0) {
                    expectedWidth=info.width;
                    expectedHeight=info.height;
                } else if (info.width!=expectedWidth || info.height!=expectedHeight) {
                    throw new IllegalArgumentException(
                        "TIFF dimensions differ in "+set.getFovId()+": "+path.getFileName()+
                        " is "+info.width+"x"+info.height+", expected "+expectedWidth+"x"+expectedHeight);
                }
            }
        }
    }

    private static FileInfo readHeader(Path path) {
        try {
            Path parent=path.toAbsolutePath().getParent();
            TiffDecoder decoder=new TiffDecoder(parent.toString()+java.io.File.separator,path.getFileName().toString());
            FileInfo[] infos=decoder.getTiffInfo();
            if (infos==null || infos.length!=1) {
                throw new IllegalArgumentException("Expected one readable TIFF plane: "+path);
            }
            return infos[0];
        } catch (IOException error) {
            throw new IllegalArgumentException("Cannot read TIFF header: "+path,error);
        }
    }
}
