package io.rich4j.richtests;
import io.rich4j.richansi.AnsiRenderer;
import io.rich4j.richcore.Segment;
import io.rich4j.richcore.Style;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class AnsiRendererTest {
    @Test
    public void colorApplied() {
        AnsiRenderer r = new AnsiRenderer();
        var seg = java.util.List.of(new Segment("hi", Style.none().withFg(0xFF0000)));
        var out = r.toAnsiLine(seg);
        assertTrue(out.contains("\u001B[38;2;"));
    }
}
