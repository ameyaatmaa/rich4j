package io.rich4j.richtests;
import io.rich4j.richwidgets.ProgressBar;
import io.rich4j.richcore.Measure;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class ProgressBarTest {
    @Test
    public void renderProducesLines() {
        ProgressBar bar = new ProgressBar(10, 20);
        bar.step(3);
        var lines = bar.render(Measure.simple(), 80);
        assertNotNull(lines);
        assertTrue(lines.size()>=1);
    }
}
