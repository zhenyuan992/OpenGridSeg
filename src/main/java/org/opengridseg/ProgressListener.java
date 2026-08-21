package org.opengridseg;

@FunctionalInterface
public interface ProgressListener {
    ProgressListener NONE=(completed,total,message)->{};
    void update(int completed,int total,String message);
}
