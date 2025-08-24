package io.rich4j.richcore;
public final class Style {
    private final Integer fg; // rgb 0xRRGGBB or null
    private final boolean bold;
    private final boolean italic;
    private Style(Integer fg, boolean bold, boolean italic){
        this.fg = fg; this.bold = bold; this.italic = italic;
    }
    public static Style none(){ return new Style(null,false,false); }
    public Style withFg(int rgb){ return new Style(rgb, bold, italic); }
    public Style bold(){ return new Style(fg, true, italic); }
    public Integer fg(){ return fg; }
    public boolean isBold(){ return bold; }
    public boolean isItalic(){ return italic; }
    @Override public String toString(){ return "Style[fg="+fg+",bold="+bold+",italic="+italic+"]"; }
}
