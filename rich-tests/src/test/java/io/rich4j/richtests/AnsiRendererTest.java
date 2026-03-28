package io.rich4j.richtests;

import io.rich4j.richansi.AnsiRenderer;
import io.rich4j.richcore.Segment;
import io.rich4j.richcore.Style;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class AnsiRendererTest {

    @Test
    public void colorApplied() {
        AnsiRenderer r = new AnsiRenderer();
        var seg = List.of(new Segment("hi", Style.none().withFg(0xFF0000)));
        var out = r.toAnsiLine(seg);
        assertTrue(out.contains("\u001B[38;2;"), "true-color fg escape missing");
    }

    @Test
    public void resetHasEscapePrefix() {
        AnsiRenderer r = new AnsiRenderer();
        var seg = List.of(new Segment("hi", Style.none().bold()));
        var out = r.toAnsiLine(seg);
        assertTrue(out.contains("\u001B[0m"), "reset escape missing ESC prefix");
    }

    @Test
    public void italicApplied() {
        AnsiRenderer r = new AnsiRenderer();
        var seg = List.of(new Segment("slant", Style.none().italic()));
        var out = r.toAnsiLine(seg);
        assertTrue(out.contains("\u001B[3m"), "italic escape missing");
    }

    @Test
    public void color256Applied() {
        AnsiRenderer r = new AnsiRenderer();
        var seg = List.of(new Segment("hi", Style.none().colorIndex(196)));
        var out = r.toAnsiLine(seg);
        assertTrue(out.contains("\u001B[38;5;196m"), "256-color fg escape missing");
    }

    @Test
    public void bgColorApplied() {
        AnsiRenderer r = new AnsiRenderer();
        var seg = List.of(new Segment("hi", Style.none().withBg(0x0000FF)));
        var out = r.toAnsiLine(seg);
        assertTrue(out.contains("\u001B[48;2;0;0;255m"), "bg color escape missing");
    }

    @Test
    public void plainSegmentNoEscapes() {
        AnsiRenderer r = new AnsiRenderer();
        var seg = List.of(Segment.plain("hello"));
        var out = r.toAnsiLine(seg);
        assertEquals("hello", out);
    }
}
