package org.opengridseg.export;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.opengridseg.detect.DetectionParameters;
import org.opengridseg.image.Interpolation;

public final class ExportOptions {
    private final Path outputFolder;
    private final int width, height;
    private final Interpolation interpolation;
    private final DetectionParameters detectionParameters = new DetectionParameters();
    private final List<String> manualEdits = new ArrayList<>();
    private final List<FluorescenceDecision> fluorescenceDecisions = new ArrayList<>();
    private String fluorescenceRule = "not_run";
    private int detectionFrame = 1;
    private double fittedPitchX = Double.NaN, fittedPitchY = Double.NaN;

    public ExportOptions(Path outputFolder, int width, int height, Interpolation interpolation) {
        this.outputFolder=outputFolder; this.width=width; this.height=height; this.interpolation=interpolation;
    }
    public Path outputFolder(){return outputFolder;} public int width(){return width;} public int height(){return height;}
    public Interpolation interpolation(){return interpolation;} public DetectionParameters detectionParameters(){return detectionParameters;}
    public int detectionFrame(){return detectionFrame;} public void setDetectionFrame(int value){detectionFrame=value;}
    public double fittedPitchX(){return fittedPitchX;} public double fittedPitchY(){return fittedPitchY;}
    public void setFittedPitch(double x,double y){fittedPitchX=x;fittedPitchY=y;}
    public void addManualEdit(String edit){manualEdits.add(edit);} public List<String> manualEdits(){return Collections.unmodifiableList(manualEdits);}
    public void setFluorescenceRule(String rule){fluorescenceRule=rule;} public String fluorescenceRule(){return fluorescenceRule;}
    public void addFluorescenceDecision(FluorescenceDecision decision){fluorescenceDecisions.add(decision);} public List<FluorescenceDecision> fluorescenceDecisions(){return Collections.unmodifiableList(fluorescenceDecisions);}
}
