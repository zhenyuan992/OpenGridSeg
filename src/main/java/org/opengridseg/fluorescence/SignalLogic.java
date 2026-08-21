package org.opengridseg.fluorescence;
public enum SignalLogic { EITHER_CHANNEL("GFP OR mCherry"),BOTH_CHANNELS("GFP AND mCherry");private final String label;SignalLogic(String label){this.label=label;}public String toString(){return label;}}
