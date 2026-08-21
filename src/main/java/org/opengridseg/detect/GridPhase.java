package org.opengridseg.detect;
import java.util.*;
public final class GridPhase { private final double xOffset,yOffset; private final List<Integer> inliers; GridPhase(double x,double y,List<Integer> inliers){this.xOffset=x;this.yOffset=y;this.inliers=Collections.unmodifiableList(new ArrayList<>(inliers));} public double xOffset(){return xOffset;}public double yOffset(){return yOffset;}public List<Integer> inlierIndices(){return inliers;} }
