package org.opengridseg.export;

public final class FluorescenceDecision {
    private final String barId;
    private final int arrayId,row,col,gfpSignalFrames,mCherrySignalFrames,gfpCriteriaPassed,mCherryCriteriaPassed;
    private final double gfpMaxRobustZ,mCherryMaxRobustZ,gfpZ90,gfpArea90,gfpPersistence,mCherryZ90,mCherryArea90,mCherryPersistence;
    private final boolean selected,manualOverride;

    public FluorescenceDecision(String barId,int arrayId,int row,int col,
            int gfpSignalFrames,double gfpMaxRobustZ,double gfpZ90,double gfpArea90,double gfpPersistence,int gfpCriteriaPassed,
            int mCherrySignalFrames,double mCherryMaxRobustZ,double mCherryZ90,double mCherryArea90,double mCherryPersistence,int mCherryCriteriaPassed,
            boolean selected,boolean manualOverride){
        this.barId=barId;this.arrayId=arrayId;this.row=row;this.col=col;
        this.gfpSignalFrames=gfpSignalFrames;this.gfpMaxRobustZ=gfpMaxRobustZ;this.gfpZ90=gfpZ90;this.gfpArea90=gfpArea90;this.gfpPersistence=gfpPersistence;this.gfpCriteriaPassed=gfpCriteriaPassed;
        this.mCherrySignalFrames=mCherrySignalFrames;this.mCherryMaxRobustZ=mCherryMaxRobustZ;this.mCherryZ90=mCherryZ90;this.mCherryArea90=mCherryArea90;this.mCherryPersistence=mCherryPersistence;this.mCherryCriteriaPassed=mCherryCriteriaPassed;
        this.selected=selected;this.manualOverride=manualOverride;
    }
    public FluorescenceDecision(String barId,int arrayId,int row,int col,int gfpSignalFrames,double gfpMaxRobustZ,int mCherrySignalFrames,double mCherryMaxRobustZ,boolean selected,boolean manualOverride){this(barId,arrayId,row,col,gfpSignalFrames,gfpMaxRobustZ,gfpMaxRobustZ,0,0,selected?2:0,mCherrySignalFrames,mCherryMaxRobustZ,mCherryMaxRobustZ,0,0,selected?2:0,selected,manualOverride);}
    public String barId(){return barId;}public int arrayId(){return arrayId;}public int row(){return row;}public int col(){return col;}
    public int gfpSignalFrames(){return gfpSignalFrames;}public double gfpMaxRobustZ(){return gfpMaxRobustZ;}public double gfpZ90(){return gfpZ90;}public double gfpArea90(){return gfpArea90;}public double gfpPersistence(){return gfpPersistence;}public int gfpCriteriaPassed(){return gfpCriteriaPassed;}
    public int mCherrySignalFrames(){return mCherrySignalFrames;}public double mCherryMaxRobustZ(){return mCherryMaxRobustZ;}public double mCherryZ90(){return mCherryZ90;}public double mCherryArea90(){return mCherryArea90;}public double mCherryPersistence(){return mCherryPersistence;}public int mCherryCriteriaPassed(){return mCherryCriteriaPassed;}
    public boolean selected(){return selected;}public boolean manualOverride(){return manualOverride;}
}
