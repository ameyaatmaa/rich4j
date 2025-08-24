package io.rich4j.richcore;
import java.util.List;
public interface Renderable {
    // produce lines of segments
    java.util.List<java.util.List<Segment>> render(Measure measure, int maxWidth);
}
