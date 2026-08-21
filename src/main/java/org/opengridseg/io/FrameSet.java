package org.opengridseg.io;
import java.nio.file.Path; import java.util.*;
public final class FrameSet {
    private final String fovId; private final EnumMap<Channel, SortedMap<Integer, Path>> files;
    FrameSet(String fovId, EnumMap<Channel, SortedMap<Integer, Path>> files) { this.fovId=fovId; this.files=files; }
    public String getFovId(){return fovId;} public int getFrameCount(){return files.get(Channel.BF).size();}
    public Path file(Channel channel,int frame){Path p=files.get(channel).get(frame); if(p==null)throw new IllegalArgumentException("Missing "+channel+" frame "+frame); return p;}
    public SortedMap<Integer,Path> files(Channel channel){return Collections.unmodifiableSortedMap(files.get(channel));}
}
