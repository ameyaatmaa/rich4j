package io.rich4j.richcore;

public final class Style {
    private final Integer fg;
    private final Integer bg;
    private final Integer fgIndex;
    private final boolean bold;
    private final boolean italic;

    private Style(Integer fg, Integer bg, Integer fgIndex, boolean bold, boolean italic) {
        this.fg = fg;
        this.bg = bg;
        this.fgIndex = fgIndex;
        this.bold = bold;
        this.italic = italic;
    }

    public static Style none() {
        return new Style(null, null, null, false, false);
    }

    public Style withFg(int rgb)   { return new Style(rgb,    bg,     fgIndex, bold,  italic); }
    public Style withBg(int rgb)   { return new Style(fg,     rgb,    fgIndex, bold,  italic); }
    public Style bold()            { return new Style(fg,     bg,     fgIndex, true,  italic); }
    public Style italic()          { return new Style(fg,     bg,     fgIndex, bold,  true);   }
    public Style colorIndex(int n) { return new Style(fg,     bg,     n,       bold,  italic); }

    public Integer fg()           { return fg; }
    public Integer bg()           { return bg; }
    public Integer fgColorIndex() { return fgIndex; }
    public boolean isBold()       { return bold; }
    public boolean isItalic()     { return italic; }

    @Override
    public String toString() {
        return "Style[fg=" + fg + ",bg=" + bg + ",idx=" + fgIndex
             + ",bold=" + bold + ",italic=" + italic + "]";
    }
}
