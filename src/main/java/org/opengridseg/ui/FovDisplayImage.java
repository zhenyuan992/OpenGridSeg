package org.opengridseg.ui;

import java.awt.image.BufferedImage;

public final class FovDisplayImage {
    private final BufferedImage image;
    private final int sourceWidth,sourceHeight;
    public FovDisplayImage(BufferedImage image,int sourceWidth,int sourceHeight){this.image=image;this.sourceWidth=sourceWidth;this.sourceHeight=sourceHeight;}
    BufferedImage image(){return image;}int sourceWidth(){return sourceWidth;}int sourceHeight(){return sourceHeight;}
    int displayX(double sourceX){return (int)Math.round(sourceX*(image.getWidth()-1)/Math.max(1,sourceWidth-1));}
    int displayY(double sourceY){return (int)Math.round(sourceY*(image.getHeight()-1)/Math.max(1,sourceHeight-1));}
}
