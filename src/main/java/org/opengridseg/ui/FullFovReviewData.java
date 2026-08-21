package org.opengridseg.ui;

public final class FullFovReviewData {
    private final FovDisplayImage bf,gfp,mCherry;
    public FullFovReviewData(FovDisplayImage bf,FovDisplayImage gfp,FovDisplayImage mCherry){this.bf=bf;this.gfp=gfp;this.mCherry=mCherry;}
    FovDisplayImage bf(){return bf;}FovDisplayImage gfp(){return gfp;}FovDisplayImage mCherry(){return mCherry;}
}
