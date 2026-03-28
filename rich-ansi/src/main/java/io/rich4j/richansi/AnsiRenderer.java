package io.rich4j.richansi;

import io.rich4j.richcore.Segment;
import io.rich4j.richcore.Style;
import java.util.List;

public final class AnsiRenderer {

    private static final String RESET = "\u001B[0m";

    public String toAnsiLine(List<Segment> segments) {
        StringBuilder sb = new StringBuilder();
        for (Segment s : segments) {
            Style st = s.style();
            boolean styled = false;
            if (st != null) {
                if (st.fgColorIndex() != null) {
                    sb.append(String.format("\u001B[38;5;%dm", st.fgColorIndex()));
                    styled = true;
                } else if (st.fg() != null) {
                    int rgb = st.fg();
                    sb.append(String.format("\u001B[38;2;%d;%d;%dm",
                            (rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF));
                    styled = true;
                }
                if (st.bg() != null) {
                    int rgb = st.bg();
                    sb.append(String.format("\u001B[48;2;%d;%d;%dm",
                            (rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF));
                    styled = true;
                }
                if (st.isBold())   { sb.append("\u001B[1m"); styled = true; }
                if (st.isItalic()) { sb.append("\u001B[3m"); styled = true; }
            }
            sb.append(s.text());
            if (styled) sb.append(RESET);
        }
        return sb.toString();
    }
}
