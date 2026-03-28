package io.rich4j.richwidgets;

import io.rich4j.richcore.*;
import java.util.*;

public final class VBox implements Renderable {

    private final Renderable[] items;

    public VBox(Renderable... items) {
        this.items = items;
    }

    @Override
    public List<List<Segment>> render(Measure measure, int maxWidth) {
        List<List<Segment>> out = new ArrayList<>();
        for (Renderable r : items) {
            out.addAll(r.render(measure, maxWidth));
        }
        return out;
    }
}
