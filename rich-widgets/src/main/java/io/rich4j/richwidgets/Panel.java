package io.rich4j.richwidgets;
import io.rich4j.richcore.*;
import java.util.List;
public final class Panel implements Renderable {
    private final Renderable inner;
    private final String title;
    public Panel(String title, Renderable inner){ this.title = title; this.inner = inner; }
    @Override public List<List<Segment>> render(Measure measure, int maxWidth){
        var lines = inner.render(measure, maxWidth-4);
        java.util.List<List<Segment>> out = new java.util.ArrayList<>();
        out.add(java.util.List.of(Segment.plain("+" + "-".repeat(Math.max(0, maxWidth-2)) + "+")));
        for (var ln : lines){
            StringBuilder sb = new StringBuilder();
            for (var seg : ln) sb.append(seg.text());
            String s = sb.toString();
            if (s.length() > maxWidth-4) s = s.substring(0, maxWidth-4);
            out.add(java.util.List.of(Segment.plain("| " + s + " ".repeat(Math.max(0, maxWidth-4 - s.length())) + " |")));
        }
        out.add(java.util.List.of(Segment.plain("+" + "-".repeat(Math.max(0, maxWidth-2)) + "+")));
        return out;
    }
}
