package io.rich4j.richwidgets;

import io.rich4j.richcore.*;
import java.util.List;

public final class Spinner implements Renderable {
    private final String[] frames = new String[]{"|", "/", "-", "\\"};
    private int frame = 0;

    public void tick() {
        frame = (frame + 1) % frames.length;
    }

    @Override
    public List<List<Segment>> render(Measure measure, int maxWidth) {
        String s = frames[frame] + " working...";
        return List.of(List.of(Segment.plain(s)));
    }
}
