package org.opengridseg.detect;
import java.util.*;
public final class MacroLayoutFitter {
    private MacroLayoutFitter(){}
    public static List<MacroArray> fit(List<Peak> peaks,GridPhase phase,Pitch pitch,int arraySize,int period,int minPeaks){
        List<Integer> inliers=phase.inlierIndices();int bestR=0,bestC=0,bestCount=-1;double bestStrength=-1;
        for(int ro=0;ro<period;ro++)for(int co=0;co<period;co++){int count=0;double strength=0;for(int pi:inliers){Peak p=peaks.get(pi);int row=(int)Math.round((p.y()-phase.yOffset())/pitch.y()),col=(int)Math.round((p.x()-phase.xOffset())/pitch.x());if(mod(row-ro,period)<arraySize&&mod(col-co,period)<arraySize){count++;strength+=p.response();}}if(count>bestCount||(count==bestCount&&strength>bestStrength)){bestCount=count;bestStrength=strength;bestR=ro;bestC=co;}}
        Map<String,List<Integer>> groups=new HashMap<>();Map<String,int[]> starts=new HashMap<>();
        for(int pi:inliers){Peak p=peaks.get(pi);int row=(int)Math.round((p.y()-phase.yOffset())/pitch.y()),col=(int)Math.round((p.x()-phase.xOffset())/pitch.x());if(mod(row-bestR,period)>=arraySize||mod(col-bestC,period)>=arraySize)continue;int rs=bestR+Math.floorDiv(row-bestR,period)*period,cs=bestC+Math.floorDiv(col-bestC,period)*period;String key=rs+":"+cs;groups.computeIfAbsent(key,k->new ArrayList<Integer>()).add(pi);starts.put(key,new int[]{rs,cs});}
        List<MacroArray> arrays=new ArrayList<>();
        for(Map.Entry<String,List<Integer>> entry:groups.entrySet()){if(entry.getValue().size()<minPeaks)continue;int[] start=starts.get(entry.getKey());Map<String,Integer> observed=new HashMap<>();for(int pi:entry.getValue()){Peak p=peaks.get(pi);int row=(int)Math.round((p.y()-phase.yOffset())/pitch.y()),col=(int)Math.round((p.x()-phase.xOffset())/pitch.x());observed.put(row+":"+col,pi);}List<BarNode> nodes=new ArrayList<>();for(int r=0;r<arraySize;r++)for(int c=0;c<arraySize;c++){int gr=start[0]+r,gc=start[1]+c;Integer pi=observed.get(gr+":"+gc);nodes.add(new BarNode(r,c,phase.xOffset()+gc*pitch.x(),phase.yOffset()+gr*pitch.y(),pi!=null,pi==null?-1:pi));}arrays.add(new MacroArray(start[0],start[1],nodes,entry.getValue().size()));}
        arrays.sort(new Comparator<MacroArray>(){public int compare(MacroArray a,MacroArray b){int r=Integer.compare(a.rowStart(),b.rowStart());return r!=0?r:Integer.compare(a.colStart(),b.colStart());}});return arrays;
    }
    private static int mod(int value,int period){int r=value%period;return r<0?r+period:r;}
}
