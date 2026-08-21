package org.opengridseg.image;
import java.util.Arrays;
public final class FloatPlane {
    private final int width,height; private final float[] pixels;
    public FloatPlane(int width,int height){if(width<1||height<1)throw new IllegalArgumentException("Invalid plane size");this.width=width;this.height=height;this.pixels=new float[width*height];}
    public FloatPlane(int width,int height,float[] pixels){if(pixels.length!=width*height)throw new IllegalArgumentException("Pixel count mismatch");this.width=width;this.height=height;this.pixels=pixels;}
    public int width(){return width;} public int height(){return height;} public float[] pixels(){return pixels;}
    public float get(int x,int y){return pixels[y*width+x];} public void set(int x,int y,float v){pixels[y*width+x]=v;}
    public float min(){float v=Float.POSITIVE_INFINITY;for(float p:pixels)if(Float.isFinite(p)&&p<v)v=p;return v;}
    public float max(){float v=Float.NEGATIVE_INFINITY;for(float p:pixels)if(Float.isFinite(p)&&p>v)v=p;return v;}
    public FloatPlane copy(){return new FloatPlane(width,height,Arrays.copyOf(pixels,pixels.length));}
}
