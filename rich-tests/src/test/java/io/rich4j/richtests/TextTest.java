package io.rich4j.richtests;

import io.rich4j.richcore.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class TextTest {

    @Test
    public void plainTextPassthrough() {
        Text t = new Text("hello world");
        List<Segment> segs = t.segments();
        assertEquals(1, segs.size());
        assertEquals("hello world", segs.get(0).text());
        assertFalse(segs.get(0).style().isBold());
    }

    @Test
    public void boldTag() {
        Text t = new Text("[bold]hello[/bold]");
        List<Segment> segs = t.segments();
        assertEquals(1, segs.size());
        assertTrue(segs.get(0).style().isBold());
        assertEquals("hello", segs.get(0).text());
    }

    @Test
    public void colorTag() {
        Text t = new Text("[red]error[/red]");
        List<Segment> segs = t.segments();
        assertEquals(1, segs.size());
        assertEquals(Integer.valueOf(0xFF0000), segs.get(0).style().fg());
    }

    @Test
    public void combinedTokens() {
        Text t = new Text("[bold red]warning[/bold red]");
        List<Segment> segs = t.segments();
        assertEquals(1, segs.size());
        Style s = segs.get(0).style();
        assertTrue(s.isBold());
        assertEquals(Integer.valueOf(0xFF0000), s.fg());
    }

    @Test
    public void mixedContent() {
        Text t = new Text("hello [bold]world[/bold] foo");
        List<Segment> segs = t.segments();
        assertEquals(3, segs.size());
        assertEquals("hello ", segs.get(0).text());
        assertFalse(segs.get(0).style().isBold());
        assertEquals("world", segs.get(1).text());
        assertTrue(segs.get(1).style().isBold());
        assertEquals(" foo", segs.get(2).text());
    }

    @Test
    public void unclosedTagFallback() {
        // Must not throw; remaining text gets the open style
        Text t = new Text("[bold]hello");
        assertFalse(t.segments().isEmpty());
        assertEquals("hello", t.segments().get(0).text());
        assertTrue(t.segments().get(0).style().isBold());
    }

    @Test
    public void newlinesProduceMultipleLines() {
        Text t = new Text("line1\nline2");
        List<List<Segment>> lines = t.render(Measure.simple(), 80);
        assertEquals(2, lines.size());
    }

    @Test
    public void hexColor() {
        Text t = new Text("[#FF8800]orange[/#FF8800]");
        List<Segment> segs = t.segments();
        assertEquals(Integer.valueOf(0xFF8800), segs.get(0).style().fg());
    }

    @Test
    public void italicTag() {
        Text t = new Text("[italic]slanted[/italic]");
        List<Segment> segs = t.segments();
        assertEquals(1, segs.size());
        assertTrue(segs.get(0).style().isItalic());
    }

    @Test
    public void color256Token() {
        Text t = new Text("[color(42)]indexed[/color(42)]");
        List<Segment> segs = t.segments();
        assertEquals(1, segs.size());
        assertEquals(Integer.valueOf(42), segs.get(0).style().fgColorIndex());
    }
}
