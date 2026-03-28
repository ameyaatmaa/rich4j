package io.rich4j.richansi;

import io.rich4j.richcore.*;
import java.io.PrintStream;
import java.util.List;

public final class Console {

    private final PrintStream out;
    private final int width;
    private final AnsiRenderer renderer = new AnsiRenderer();
    private final Measure measure = Measure.simple();

    public Console(PrintStream out) {
        this.out = out;
        this.width = detectWidth();
    }

    public Console() {
        this(System.out);
    }

    public int width() { return width; }

    public void print(Renderable r) {
        List<List<Segment>> lines = r.render(measure, width);
        for (int i = 0; i < lines.size(); i++) {
            out.print(renderer.toAnsiLine(lines.get(i)));
            if (i < lines.size() - 1) out.print("\n");
        }
        out.flush();
    }

    public void print(String markup) {
        print(new Text(markup));
    }

    public void println(Renderable r) {
        for (List<Segment> line : r.render(measure, width)) {
            out.println(renderer.toAnsiLine(line));
        }
        out.flush();
    }

    public void println(String markup) {
        println(new Text(markup));
    }

    private static int detectWidth() {
        String cols = System.getenv("COLUMNS");
        if (cols != null) {
            try { return Integer.parseInt(cols.trim()); }
            catch (NumberFormatException ignored) {}
        }
        return 80;
    }
}
