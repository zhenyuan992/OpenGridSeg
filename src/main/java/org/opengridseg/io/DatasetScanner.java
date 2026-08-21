package org.opengridseg.io;
import java.io.IOException; import java.nio.file.*; import java.util.*; import java.util.regex.*; import java.util.stream.Stream;
public final class DatasetScanner {
    private static final Pattern TIFF=Pattern.compile("^(.*)_w([123])[^_]*_t(\\d+)\\.tiff?$",Pattern.CASE_INSENSITIVE);
    private DatasetScanner(){}
    public static List<FrameSet> scan(Path folder) throws IOException {
        if(!Files.isDirectory(folder)) throw new IllegalArgumentException("Not a folder: "+folder);
        Map<String,EnumMap<Channel,SortedMap<Integer,Path>>> grouped=new TreeMap<>();
        try(Stream<Path> stream=Files.list(folder)){
            Iterator<Path> it=stream.filter(Files::isRegularFile).iterator();
            while(it.hasNext()){
                Path path=it.next(); Matcher m=TIFF.matcher(path.getFileName().toString()); if(!m.matches())continue;
                String fov=m.group(1); Channel channel=Channel.fromWave(Integer.parseInt(m.group(2))); int frame=Integer.parseInt(m.group(3));
                EnumMap<Channel,SortedMap<Integer,Path>> channels=grouped.get(fov);
                if(channels==null){channels=new EnumMap<>(Channel.class); for(Channel c:Channel.values())channels.put(c,new TreeMap<Integer,Path>()); grouped.put(fov,channels);}
                if(channels.get(channel).put(frame,path)!=null) throw new IllegalArgumentException("Duplicate "+channel+" frame "+frame+" for "+fov);
            }
        }
        if(grouped.isEmpty()) throw new IllegalArgumentException("No TIFF files matching *_w[1-3]*_tN.TIF in "+folder);
        List<FrameSet> result=new ArrayList<>();
        for(Map.Entry<String,EnumMap<Channel,SortedMap<Integer,Path>>> entry:grouped.entrySet()){
            EnumMap<Channel,SortedMap<Integer,Path>> channels=entry.getValue(); int max=0;
            for(Channel c:Channel.values()) if(!channels.get(c).isEmpty()) max=Math.max(max,channels.get(c).lastKey());
            for(Channel c:Channel.values()) for(int t=1;t<=max;t++) if(!channels.get(c).containsKey(t)) throw new IllegalArgumentException("Missing "+c+" frame "+t+" for "+entry.getKey());
            result.add(new FrameSet(entry.getKey(),channels));
        }
        return result;
    }
}
