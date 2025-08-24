package io.rich4j.richcore;
public final class Segment {
    private final String text;
    private final Style style;
    public Segment(String text, Style style){ this.text = text; this.style = style; }
    public String text(){ return text; }
    public Style style(){ return style; }
    public static Segment plain(String s){ return new Segment(s, Style.none()); }
    @Override public String toString(){ return "Segment{"+text+"}"; }
}
