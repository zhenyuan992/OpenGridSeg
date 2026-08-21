package org.opengridseg.fluorescence;
import org.opengridseg.image.FloatPlane;import org.opengridseg.io.Channel;
public interface FluorescencePlaneProvider { FloatPlane read(Channel channel,int timeIndex) throws Exception; }
