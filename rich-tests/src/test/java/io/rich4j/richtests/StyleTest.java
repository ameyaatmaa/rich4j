package io.rich4j.richtests;

import io.rich4j.richcore.Style;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class StyleTest {

    @Test
    public void noneIsAllNull() {
        Style s = Style.none();
        assertNull(s.fg());
        assertNull(s.bg());
        assertNull(s.fgColorIndex());
        assertFalse(s.isBold());
        assertFalse(s.isItalic());
    }

    @Test
    public void chainedBuilders() {
        Style s = Style.none().withFg(0xFF0000).bold().italic();
        assertEquals(Integer.valueOf(0xFF0000), s.fg());
        assertTrue(s.isBold());
        assertTrue(s.isItalic());
    }

    @Test
    public void bgColor() {
        Style s = Style.none().withBg(0x00FF00);
        assertNull(s.fg());
        assertEquals(Integer.valueOf(0x00FF00), s.bg());
    }

    @Test
    public void colorIndex() {
        Style s = Style.none().colorIndex(42);
        assertEquals(Integer.valueOf(42), s.fgColorIndex());
        assertNull(s.fg());
    }

    @Test
    public void buildersAreImmutable() {
        Style base = Style.none();
        Style bold = base.bold();
        assertFalse(base.isBold());
        assertTrue(bold.isBold());
    }
}
