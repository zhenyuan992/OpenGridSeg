package org.opengridseg.fluorescence;

import org.opengridseg.export.ExportBar;
import org.opengridseg.image.FloatPlane;

public final class BarFluorescenceReview {
    private final ExportBar bar;
    private final FluorescenceBarScore gfp,mCherry;
    private FloatPlane bfPreview;
    private Boolean manualSelection;
    BarFluorescenceReview(ExportBar bar,FluorescenceBarScore gfp,FluorescenceBarScore mCherry){this.bar=bar;this.gfp=gfp;this.mCherry=mCherry;}
    public ExportBar bar(){return bar;}public FluorescenceBarScore gfp(){return gfp;}public FluorescenceBarScore mCherry(){return mCherry;}
    public FloatPlane bfPreview(){return bfPreview;}public void setBfPreview(FloatPlane preview){this.bfPreview=preview;}
    public boolean autoSelected(SignalLogic logic){return logic==SignalLogic.BOTH_CHANNELS?gfp.signalLike()&&mCherry.signalLike():gfp.signalLike()||mCherry.signalLike();}
    public boolean selected(SignalLogic logic){return manualSelection==null?autoSelected(logic):manualSelection.booleanValue();}
    public boolean manuallyOverridden(){return manualSelection!=null;}public void setManualSelection(Boolean selected){manualSelection=selected;}
}
