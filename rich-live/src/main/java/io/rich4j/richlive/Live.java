package io.rich4j.richlive;
import io.rich4j.richcore.*;
import io.rich4j.richansi.AnsiRenderer;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

public final class Live implements AutoCloseable {
    private final PrintStream out;
    private final AnsiRenderer ansi = new AnsiRenderer();
    private List<String> last = new ArrayList<>();
    public Live(PrintStream out){ this.out = out; }
    public void set(Renderable r, Measure m, int width){
        var lines = r.render(m, width);
        List<String> now = new ArrayList<>();
        for (var ln : lines) now.add(ansi.toAnsiLine(ln));
        // simple: move cursor up and repaint all
        if (!last.isEmpty()){
            out.print("\u001B[" + last.size() + "F");
        }
        for (String l : now){
            out.print(l);
            out.print("\u001B[K\n");
        }
        last = now;
    }
    @Override public void close(){ out.print("\n"); out.flush(); }
}
