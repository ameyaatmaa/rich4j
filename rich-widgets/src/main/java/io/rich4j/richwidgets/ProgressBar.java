package io.rich4j.richwidgets;
import io.rich4j.richcore.*;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.List;
public final class ProgressBar implements Renderable {
    private final long total;
    private long current;
    private final Instant start;
    private final int width;
    public ProgressBar(long total, int width){ this.total = total; this.width = width; this.start = Instant.now(); this.current=0; }
    public void step(){ step(1); }
    public void step(long n){ current = Math.min(total, current + n); }
    @Override public List<List<Segment>> render(Measure measure, int maxWidth){
        int barW = Math.min(width, maxWidth-30);
        double p = total==0?1.0:((double)current)/total;
        int fill = (int)Math.round(p*barW);
        String bar = "█".repeat(Math.max(0, fill)) + " ".repeat(Math.max(0, barW-fill));
        long elapsed = Duration.between(start, Instant.now()).getSeconds();
        long eta = current==0?0: (long)((total-current) * (elapsed / (double)Math.max(1,current)));
        String txt = String.format("[%s] %6.2f%% %d/%d ET A:%ds", bar, p*100.0, current, total, eta);
        return java.util.List.of(java.util.List.of(Segment.plain(txt)));
    }
}
