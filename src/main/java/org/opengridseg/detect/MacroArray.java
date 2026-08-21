package org.opengridseg.detect;
import java.util.*;
public final class MacroArray { private final int rowStart,colStart; private final List<BarNode> nodes; private final int observedCount; MacroArray(int rowStart,int colStart,List<BarNode> nodes,int observedCount){this.rowStart=rowStart;this.colStart=colStart;this.nodes=Collections.unmodifiableList(nodes);this.observedCount=observedCount;} public int rowStart(){return rowStart;}public int colStart(){return colStart;}public List<BarNode> nodes(){return nodes;}public int observedCount(){return observedCount;} }
