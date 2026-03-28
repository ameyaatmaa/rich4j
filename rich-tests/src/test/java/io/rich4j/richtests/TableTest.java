package io.rich4j.richtests;

import io.rich4j.richwidgets.Table;
import io.rich4j.richcore.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class TableTest {

    private String renderToString(Table t) {
        StringBuilder sb = new StringBuilder();
        for (var line : t.render(Measure.simple(), 80)) {
            for (var seg : line) sb.append(seg.text());
            sb.append("\n");
        }
        return sb.toString();
    }

    @Test
    public void borderCharactersPresent() {
        Table t = new Table();
        t.addColumn("Name");
        t.addColumn("Age");
        t.addRow("Alice", "30");
        String out = renderToString(t);
        assertTrue(out.contains("┌"), "top-left border");
        assertTrue(out.contains("┐"), "top-right border");
        assertTrue(out.contains("└"), "bottom-left border");
        assertTrue(out.contains("┘"), "bottom-right border");
        assertTrue(out.contains("│"), "vertical separator");
        assertTrue(out.contains("─"), "horizontal line");
    }

    @Test
    public void headerAndDataPresent() {
        Table t = new Table();
        t.addColumn("Name");
        t.addColumn("Age");
        t.addRow("Alice", "30");
        String out = renderToString(t);
        assertTrue(out.contains("Name"));
        assertTrue(out.contains("Alice"));
        assertTrue(out.contains("30"));
    }

    @Test
    public void cellPaddingPresent() {
        Table t = new Table();
        t.addColumn("X");
        t.addRow("Y");
        String out = renderToString(t);
        // Each cell has one space on each side: " Y "
        assertTrue(out.contains(" Y "), "cell padding missing");
    }

    @Test
    public void separatorBetweenHeaderAndData() {
        Table t = new Table();
        t.addColumn("Name");
        t.addRow("Alice");
        var lines = t.render(Measure.simple(), 80);
        // Structure: top-border, header-row, separator, data-row, bottom-border = 5 lines
        assertEquals(5, lines.size());
        StringBuilder sep = new StringBuilder();
        for (var seg : lines.get(2)) sep.append(seg.text());
        String s = sep.toString();
        assertTrue(s.contains("├") || s.contains("┼") || s.contains("┤"),
                   "mid-border character missing from separator row");
    }

    @Test
    public void columnClampingRespectsMaxWidth() {
        Table t = new Table();
        t.addColumn("VeryLongColumnHeaderName");
        t.addColumn("AnotherLongHeader");
        t.addRow("some data value here", "more data");
        // Render into a narrow 30-char terminal
        var lines = t.render(Measure.simple(), 30);
        for (var line : lines) {
            StringBuilder sb = new StringBuilder();
            for (var seg : line) sb.append(seg.text());
            assertTrue(sb.length() <= 30,
                "line exceeds maxWidth: [" + sb + "] length=" + sb.length());
        }
    }
}
