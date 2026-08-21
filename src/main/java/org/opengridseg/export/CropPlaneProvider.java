package org.opengridseg.export;
import org.opengridseg.image.FloatPlane;
public interface CropPlaneProvider { FloatPlane plane(int barIndex,int channelIndex,int timeIndex) throws Exception; }
