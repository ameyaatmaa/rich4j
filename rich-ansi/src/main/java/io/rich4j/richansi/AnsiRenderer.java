package io.rich4j.richansi;
import io.rich4j.richcore.Segment;
import io.rich4j.richcore.Style;
import java.util.List;
public final class AnsiRenderer {
    private static final String RESET = "[0m";
    public String toAnsiLine(List<Segment> segments){
        StringBuilder sb = new StringBuilder();
        for (Segment s : segments){
            Style st = s.style();
            if (st!=null && st.fg()!=null){
                int rgb = st.fg();
                sb.append(String.format("\u001B[38;2;%d;%d;%dm", (rgb>>16)&0xFF, (rgb>>8)&0xFF, rgb&0xFF));
            }
            if (st!=null && st.isBold()) sb.append("\u001B[1m");
            sb.append(s.text());
            sb.append(RESET);
        }
        return sb.toString();
    }
}
