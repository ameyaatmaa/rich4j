package io.rich4j.richcore;

import java.util.*;
import java.util.regex.*;

public final class Text implements Renderable {

    private static final Map<String, Integer> NAMED_COLORS = new HashMap<>();
    static {
        NAMED_COLORS.put("red",     0xFF0000);
        NAMED_COLORS.put("green",   0x00FF00);
        NAMED_COLORS.put("blue",    0x0000FF);
        NAMED_COLORS.put("yellow",  0xFFFF00);
        NAMED_COLORS.put("cyan",    0x00FFFF);
        NAMED_COLORS.put("magenta", 0xFF00FF);
        NAMED_COLORS.put("white",   0xFFFFFF);
        NAMED_COLORS.put("black",   0x000000);
    }

    private static final Pattern TAG = Pattern.compile("\\[([^\\[\\]]+)\\]");

    private final List<Segment> segments;

    public Text(String markup) {
        this.segments = parse(markup);
    }

    public List<Segment> segments() {
        return Collections.unmodifiableList(segments);
    }

    @Override
    public List<List<Segment>> render(Measure measure, int maxWidth) {
        List<List<Segment>> lines = new ArrayList<>();
        List<Segment> current = new ArrayList<>();
        for (Segment seg : segments) {
            String remaining = seg.text();
            int idx;
            while ((idx = remaining.indexOf('\n')) >= 0) {
                if (idx > 0) {
                    current.add(new Segment(remaining.substring(0, idx), seg.style()));
                }
                lines.add(new ArrayList<>(current));
                current.clear();
                remaining = remaining.substring(idx + 1);
            }
            if (!remaining.isEmpty()) {
                current.add(new Segment(remaining, seg.style()));
            }
        }
        if (!current.isEmpty()) lines.add(current);
        if (lines.isEmpty()) lines.add(new ArrayList<>());
        return lines;
    }

    private static List<Segment> parse(String markup) {
        List<Segment> result = new ArrayList<>();
        Deque<Style> stack = new ArrayDeque<>();
        stack.push(Style.none());

        Matcher m = TAG.matcher(markup);
        int last = 0;

        while (m.find()) {
            if (m.start() > last) {
                result.add(new Segment(markup.substring(last, m.start()), stack.peek()));
            }
            String tag = m.group(1).trim();
            if (tag.startsWith("/")) {
                if (stack.size() > 1) stack.pop();
            } else {
                stack.push(applyTokens(stack.peek(), tag));
            }
            last = m.end();
        }

        if (last < markup.length()) {
            result.add(new Segment(markup.substring(last), stack.peek()));
        }
        return result;
    }

    private static Style applyTokens(Style base, String tokens) {
        Style s = base;
        for (String token : tokens.split("\\s+")) {
            String t = token.toLowerCase(java.util.Locale.ROOT);
            switch (t) {
                case "bold"   -> s = s.bold();
                case "italic" -> s = s.italic();
                default -> {
                    if (NAMED_COLORS.containsKey(t)) {
                        s = s.withFg(NAMED_COLORS.get(t));
                    } else if (t.startsWith("#") && t.length() == 7) {
                        try { s = s.withFg(Integer.parseInt(t.substring(1), 16)); }
                        catch (NumberFormatException ignored) {}
                    } else if (t.startsWith("color(") && t.endsWith(")")) {
                        try { s = s.colorIndex(Integer.parseInt(t.substring(6, t.length() - 1))); }
                        catch (NumberFormatException ignored) {}
                    }
                }
            }
        }
        return s;
    }
}
