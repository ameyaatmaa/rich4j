package io.rich4j.richwidgets;

import io.rich4j.richcore.*;
import java.util.*;

public final class Table implements Renderable {

    private final List<String> headers = new ArrayList<>();
    private final List<Style>  headerStyles = new ArrayList<>();
    private final List<List<String>> rows = new ArrayList<>();

    public void addColumn(String header, Style style) {
        headers.add(header);
        headerStyles.add(style);
    }

    public void addColumn(String header) {
        addColumn(header, Style.none());
    }

    public void addRow(String... cells) {
        rows.add(Arrays.asList(cells));
    }

    @Override
    public List<List<Segment>> render(Measure measure, int maxWidth) {
        int cols = headers.size();
        if (cols == 0) return List.of();

        int[] widths = computeWidths(cols, measure);
        clampWidths(widths, maxWidth);

        List<List<Segment>> out = new ArrayList<>();
        out.add(List.of(Segment.plain(borderTop(widths))));
        out.add(buildRow(headers, widths, headerStyles, measure));
        out.add(List.of(Segment.plain(borderMid(widths))));
        for (List<String> row : rows) {
            out.add(buildRow(padRow(row, cols), widths, null, measure));
        }
        out.add(List.of(Segment.plain(borderBottom(widths))));
        return out;
    }

    private int[] computeWidths(int cols, Measure measure) {
        int[] w = new int[cols];
        for (int i = 0; i < cols; i++) w[i] = measure.width(headers.get(i));
        for (List<String> row : rows) {
            for (int i = 0; i < Math.min(cols, row.size()); i++) {
                w[i] = Math.max(w[i], measure.width(row.get(i)));
            }
        }
        return w;
    }

    private void clampWidths(int[] widths, int maxWidth) {
        // total = 1 (left border) + sum(w + 3) per column
        int total = 1;
        for (int w : widths) total += w + 3;
        while (total > maxWidth) {
            int maxIdx = 0;
            for (int i = 1; i < widths.length; i++) {
                if (widths[i] > widths[maxIdx]) maxIdx = i;
            }
            if (widths[maxIdx] <= 1) break;
            widths[maxIdx]--;
            total--;
        }
    }

    private List<String> padRow(List<String> row, int cols) {
        List<String> padded = new ArrayList<>(row);
        while (padded.size() < cols) padded.add("");
        return padded;
    }

    private String borderTop(int[] w) {
        StringBuilder sb = new StringBuilder("┌");
        for (int i = 0; i < w.length; i++) {
            sb.append("─".repeat(w[i] + 2)).append(i < w.length - 1 ? "┬" : "┐");
        }
        return sb.toString();
    }

    private String borderMid(int[] w) {
        StringBuilder sb = new StringBuilder("├");
        for (int i = 0; i < w.length; i++) {
            sb.append("─".repeat(w[i] + 2)).append(i < w.length - 1 ? "┼" : "┤");
        }
        return sb.toString();
    }

    private String borderBottom(int[] w) {
        StringBuilder sb = new StringBuilder("└");
        for (int i = 0; i < w.length; i++) {
            sb.append("─".repeat(w[i] + 2)).append(i < w.length - 1 ? "┴" : "┘");
        }
        return sb.toString();
    }

    private List<Segment> buildRow(List<String> cells, int[] widths, List<Style> styles, Measure measure) {
        List<Segment> segs = new ArrayList<>();
        segs.add(Segment.plain("│"));
        for (int i = 0; i < widths.length; i++) {
            String cell = i < cells.size() ? cells.get(i) : "";
            // Truncate cell content if it exceeds the allocated column width
            cell = truncate(cell, widths[i], measure);
            int pad = Math.max(0, widths[i] - measure.width(cell));
            Style st = (styles != null && i < styles.size()) ? styles.get(i) : Style.none();
            segs.add(new Segment(" " + cell + " ".repeat(pad) + " ", st));
            segs.add(Segment.plain("│"));
        }
        return segs;
    }

    private String truncate(String s, int maxLen, Measure measure) {
        if (measure.width(s) <= maxLen) return s;
        // Walk codepoints until we reach maxLen
        int[] cps = s.codePoints().toArray();
        StringBuilder sb = new StringBuilder();
        int w = 0;
        for (int cp : cps) {
            String ch = new String(Character.toChars(cp));
            int cw = measure.width(ch);
            if (w + cw > maxLen) break;
            sb.appendCodePoint(cp);
            w += cw;
        }
        return sb.toString();
    }
}
