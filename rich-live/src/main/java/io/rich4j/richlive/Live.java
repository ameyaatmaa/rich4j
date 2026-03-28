package io.rich4j.richlive;

import io.rich4j.richcore.*;
import io.rich4j.richansi.AnsiRenderer;
import java.io.PrintStream;
import java.util.*;

public final class Live implements AutoCloseable {

    private final PrintStream out;
    private final AnsiRenderer ansi = new AnsiRenderer();
    private List<String> last = new ArrayList<>();
    private int refreshRate = 30;

    public Live(PrintStream out) {
        this.out = out;
    }

    /** Metadata hint — callers control actual sleep interval. */
    public void setRefreshRate(int fps) {
        this.refreshRate = fps;
    }

    /**
     * Repaints the live region with the rendered output of {@code renderable}.
     *
     * @param renderable the widget to render
     * @param measure    text-width measurement strategy
     * @param width      terminal column width
     */
    public void refresh(Renderable renderable, Measure measure, int width) {
        List<List<Segment>> lines = renderable.render(measure, width);
        List<String> now = new ArrayList<>();
        for (List<Segment> ln : lines) now.add(ansi.toAnsiLine(ln));

        // Move cursor to start of previous render
        if (!last.isEmpty()) {
            out.print("\u001B[" + last.size() + "F");
        }

        int i = 0;
        for (; i < now.size(); i++) {
            out.print("\u001B[2K");   // clear line
            out.print(now.get(i));
            out.print("\n");
        }
        // Clear any extra lines left over from a longer previous render
        for (; i < last.size(); i++) {
            out.print("\u001B[2K\n");
        }
        // Reposition cursor: we are now (last.size() - now.size()) lines too far down
        if (last.size() > now.size()) {
            out.print("\u001B[" + (last.size() - now.size()) + "F");
        }

        out.flush();
        last = now;
    }

    /** @deprecated Use {@link #refresh(Renderable, Measure, int)} */
    @Deprecated
    public void set(Renderable r, Measure m, int width) {
        refresh(r, m, width);
    }

    @Override
    public void close() {
        out.print("\n");
        out.flush();
    }
}
