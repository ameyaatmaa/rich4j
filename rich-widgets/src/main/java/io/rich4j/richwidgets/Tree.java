package io.rich4j.richwidgets;

import io.rich4j.richcore.*;
import java.util.*;

public final class Tree implements Renderable {

    private final String label;
    private final List<Tree> children = new ArrayList<>();

    public Tree(String label) {
        this.label = label;
    }

    public Tree addChild(String label) {
        Tree child = new Tree(label);
        children.add(child);
        return child;
    }

    @Override
    public List<List<Segment>> render(Measure measure, int maxWidth) {
        List<List<Segment>> lines = new ArrayList<>();
        lines.add(List.of(Segment.plain(label)));
        renderChildren(lines, "");
        return lines;
    }

    private void renderChildren(List<List<Segment>> lines, String prefix) {
        for (int i = 0; i < children.size(); i++) {
            boolean last = (i == children.size() - 1);
            Tree child = children.get(i);
            lines.add(List.of(Segment.plain(prefix + (last ? "└── " : "├── ") + child.label)));
            child.renderChildren(lines, prefix + (last ? "    " : "│   "));
        }
    }
}
