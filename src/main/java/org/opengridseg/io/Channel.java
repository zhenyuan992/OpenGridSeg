package org.opengridseg.io;
public enum Channel { BF(3, "BF"), GFP(1, "GFP"), MCHERRY(2, "mCherry");
    private final int wave; private final String label;
    Channel(int wave, String label) { this.wave = wave; this.label = label; }
    public int wave() { return wave; } public String label() { return label; }
    public static Channel fromWave(int wave) { for (Channel c : values()) if (c.wave == wave) return c; throw new IllegalArgumentException("Unknown wave " + wave); }
}
