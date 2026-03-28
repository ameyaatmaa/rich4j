# rich4j Full Build Implementation Plan

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Extend rich4j from a skeleton into a fully-featured Java console UI toolkit with Text markup, Table, Tree, improved ANSI rendering, live repainting, tests, Maven Central publishing config, CI, and updated docs.

**Architecture:** Layered bottom-up — fix pre-flight issues first, then extend core primitives (Style, Text), improve ANSI rendering (AnsiRenderer, Console), add widgets (Table, Tree, VBox), improve Live, build the Demo, then add infrastructure. Each layer compiles and tests green before the next starts.

**Tech Stack:** Java 21, Maven multi-module, JUnit 5 Jupiter, Unicode box-drawing chars, ANSI escape sequences, GitHub Actions, nexus-staging-maven-plugin, maven-gpg-plugin

**Spec:** `docs/superpowers/specs/2026-03-28-rich4j-full-build-design.md`

---

## File Map

### Modified files
- `rich-examples/pom.xml` — fix version `0.1.0-SNAPSHOT` → `1.0.0-SNAPSHOT`, add `rich-ansi` dep
- `rich-core/src/main/java/io/rich4j/richcore/Style.java` — add `italic()`, `withBg()`, `bg()`, `colorIndex()`, `fgColorIndex()`
- `rich-ansi/src/main/java/io/rich4j/richansi/AnsiRenderer.java` — fix RESET bug, add 256-color, bg color, italic
- `rich-live/src/main/java/io/rich4j/richlive/Live.java` — fix extra-line erasure, add `refresh()`, deprecate `set()`
- `rich-examples/src/main/java/io/rich4j/richexamples/Demo.java` — full showcase demo
- `rich-tests/pom.xml` — add explicit `rich-core` dependency
- `rich-tests/src/test/java/io/rich4j/richtests/AnsiRendererTest.java` — add reset bug + italic tests
- `pom.xml` (parent) — add metadata + plugins for Maven Central
- `README.md` — badges, install, usage examples, contributing

### Pre-existing files (already in skeleton — no implementation needed)
- `rich-widgets/src/main/java/io/rich4j/richwidgets/ProgressBar.java` — exists, unchanged
- `rich-widgets/src/main/java/io/rich4j/richwidgets/Spinner.java` — exists, unchanged
- `rich-widgets/src/main/java/io/rich4j/richwidgets/Panel.java` — exists, unchanged
- `rich-tests/src/test/java/io/rich4j/richtests/ProgressBarTest.java` — exists, unchanged

### Created files
- `rich-core/src/main/java/io/rich4j/richcore/Text.java`
- `rich-ansi/src/main/java/io/rich4j/richansi/Console.java`
- `rich-widgets/src/main/java/io/rich4j/richwidgets/Table.java`
- `rich-widgets/src/main/java/io/rich4j/richwidgets/Tree.java`
- `rich-widgets/src/main/java/io/rich4j/richwidgets/VBox.java`
- `rich-tests/src/test/java/io/rich4j/richtests/StyleTest.java`
- `rich-tests/src/test/java/io/rich4j/richtests/TextTest.java`
- `rich-tests/src/test/java/io/rich4j/richtests/TableTest.java`
- `rich-tests/src/test/java/io/rich4j/richtests/ConsoleTest.java`
- `LICENSE`
- `.github/workflows/ci.yml`
- `docs/rich4j-project-documentation.md` (**NOT committed/pushed**)

---

## Chunk 1: Pre-flight + Style + AnsiRenderer

### Task 1: Fix rich-examples pom version and dependency

**Files:**
- Modify: `rich-examples/pom.xml`

- [ ] **Step 1: Fix the POM**

Replace the entire content of `rich-examples/pom.xml` with:

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>io.rich4j</groupId>
        <artifactId>rich4j</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </parent>

    <artifactId>rich-examples</artifactId>
    <name>rich-examples</name>

    <dependencies>
        <dependency>
            <groupId>io.rich4j</groupId>
            <artifactId>rich-ansi</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>io.rich4j</groupId>
            <artifactId>rich-widgets</artifactId>
            <version>${project.version}</version>
        </dependency>
        <dependency>
            <groupId>io.rich4j</groupId>
            <artifactId>rich-live</artifactId>
            <version>${project.version}</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>exec-maven-plugin</artifactId>
                <version>3.1.0</version>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 2: Verify compile**

```bash
mvn compile -q
```

Expected: BUILD SUCCESS with no errors.

- [ ] **Step 3: Commit**

```bash
git add rich-examples/pom.xml
git commit -m "fix(examples): correct parent version and add rich-ansi dependency"
```

---

### Task 2: Extend Style with new builder methods (TDD)

**Files:**
- Create: `rich-tests/src/test/java/io/rich4j/richtests/StyleTest.java`
- Modify: `rich-core/src/main/java/io/rich4j/richcore/Style.java`
- Modify: `rich-tests/pom.xml`

- [ ] **Step 1: Add explicit rich-core dependency to rich-tests/pom.xml**

Inside the `<dependencies>` block of `rich-tests/pom.xml`, add before the JUnit dependencies:

```xml
        <dependency>
            <groupId>io.rich4j</groupId>
            <artifactId>rich-core</artifactId>
            <version>${project.version}</version>
        </dependency>
```

- [ ] **Step 2: Write the failing test**

Create `rich-tests/src/test/java/io/rich4j/richtests/StyleTest.java`:

```java
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
```

- [ ] **Step 3: Run test to verify it fails**

```bash
mvn -pl rich-tests test -Dtest=StyleTest -q 2>&1 | tail -20
```

Expected: COMPILATION ERROR — `italic()`, `withBg()`, `bg()`, `colorIndex()`, `fgColorIndex()` do not exist yet.

- [ ] **Step 4: Implement Style additions**

Replace the entire content of `rich-core/src/main/java/io/rich4j/richcore/Style.java`:

```java
package io.rich4j.richcore;

public final class Style {
    private final Integer fg;
    private final Integer bg;
    private final Integer fgIndex;
    private final boolean bold;
    private final boolean italic;

    private Style(Integer fg, Integer bg, Integer fgIndex, boolean bold, boolean italic) {
        this.fg = fg;
        this.bg = bg;
        this.fgIndex = fgIndex;
        this.bold = bold;
        this.italic = italic;
    }

    public static Style none() {
        return new Style(null, null, null, false, false);
    }

    public Style withFg(int rgb)   { return new Style(rgb,    bg,     fgIndex, bold,  italic); }
    public Style withBg(int rgb)   { return new Style(fg,     rgb,    fgIndex, bold,  italic); }
    public Style bold()            { return new Style(fg,     bg,     fgIndex, true,  italic); }
    public Style italic()          { return new Style(fg,     bg,     fgIndex, bold,  true);   }
    public Style colorIndex(int n) { return new Style(fg,     bg,     n,       bold,  italic); }

    public Integer fg()           { return fg; }
    public Integer bg()           { return bg; }
    public Integer fgColorIndex() { return fgIndex; }
    public boolean isBold()       { return bold; }
    public boolean isItalic()     { return italic; }

    @Override
    public String toString() {
        return "Style[fg=" + fg + ",bg=" + bg + ",idx=" + fgIndex
             + ",bold=" + bold + ",italic=" + italic + "]";
    }
}
```

- [ ] **Step 5: Run test to verify it passes**

```bash
mvn -pl rich-core install -q && mvn -pl rich-tests test -Dtest=StyleTest -q 2>&1 | tail -10
```

Expected: Tests run: 5, Failures: 0, Errors: 0.

- [ ] **Step 6: Compile full project**

```bash
mvn compile -q
```

Expected: BUILD SUCCESS.

- [ ] **Step 7: Commit**

```bash
git add rich-core/src/main/java/io/rich4j/richcore/Style.java \
        rich-tests/src/test/java/io/rich4j/richtests/StyleTest.java \
        rich-tests/pom.xml
git commit -m "feat(core): extend Style with italic, withBg, bg, colorIndex, fgColorIndex"
```

---

### Task 3: Fix and improve AnsiRenderer (TDD)

**Files:**
- Modify: `rich-tests/src/test/java/io/rich4j/richtests/AnsiRendererTest.java`
- Modify: `rich-ansi/src/main/java/io/rich4j/richansi/AnsiRenderer.java`

- [ ] **Step 1: Add new test cases for the reset bug and italic**

Replace entire `rich-tests/src/test/java/io/rich4j/richtests/AnsiRendererTest.java`:

```java
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
        // The reset sequence must be \u001B[0m (ESC + [0m), not just [0m
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
```

- [ ] **Step 2: Run tests to verify failures**

```bash
mvn -pl rich-core install -q && mvn -pl rich-ansi install -q && \
mvn -pl rich-tests test -Dtest=AnsiRendererTest -q 2>&1 | tail -20
```

Expected: Several failures — `resetHasEscapePrefix`, `italicApplied`, `color256Applied`, `bgColorApplied` fail; `colorApplied` may pass.

- [ ] **Step 3: Implement AnsiRenderer improvements**

Replace entire `rich-ansi/src/main/java/io/rich4j/richansi/AnsiRenderer.java`:

```java
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
```

- [ ] **Step 4: Run all tests to verify they pass**

```bash
mvn -pl rich-core install -q && mvn -pl rich-ansi install -q && mvn -pl rich-tests test -Dtest=AnsiRendererTest -q 2>&1 | tail -10
```

Expected: Tests run: 6, Failures: 0, Errors: 0.

- [ ] **Step 5: Commit**

```bash
git add rich-ansi/src/main/java/io/rich4j/richansi/AnsiRenderer.java \
        rich-tests/src/test/java/io/rich4j/richtests/AnsiRendererTest.java
git commit -m "feat(ansi): fix reset bug, add 256-color, bg color, and italic support"
```

---

## Chunk 2: Text + Console + Widgets + Live

### Task 4: Add Text markup parser (TDD)

**Files:**
- Create: `rich-tests/src/test/java/io/rich4j/richtests/TextTest.java`
- Create: `rich-core/src/main/java/io/rich4j/richcore/Text.java`

- [ ] **Step 1: Write the failing test**

Create `rich-tests/src/test/java/io/rich4j/richtests/TextTest.java`:

```java
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
```

- [ ] **Step 2: Run test to verify compilation failure**

```bash
mvn -pl rich-tests test -Dtest=TextTest -q 2>&1 | tail -10
```

Expected: COMPILATION ERROR — `Text` does not exist.

- [ ] **Step 3: Implement Text**

Create `rich-core/src/main/java/io/rich4j/richcore/Text.java`:

```java
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
```

- [ ] **Step 4: Run tests to verify they pass**

```bash
mvn -pl rich-core install -q && mvn -pl rich-ansi install -q && \
mvn -pl rich-tests test -Dtest=TextTest -q 2>&1 | tail -10
```

Expected: Tests run: 8, Failures: 0, Errors: 0.

- [ ] **Step 5: Commit**

```bash
git add rich-core/src/main/java/io/rich4j/richcore/Text.java \
        rich-tests/src/test/java/io/rich4j/richtests/TextTest.java
git commit -m "feat(core): add Text markup parser with style stack and newline support"
```

---

### Task 5: Add Console (TDD)

**Files:**
- Create: `rich-tests/src/test/java/io/rich4j/richtests/ConsoleTest.java`
- Create: `rich-ansi/src/main/java/io/rich4j/richansi/Console.java`

- [ ] **Step 1: Write the failing test**

Create `rich-tests/src/test/java/io/rich4j/richtests/ConsoleTest.java`:

```java
package io.rich4j.richtests;

import io.rich4j.richansi.Console;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class ConsoleTest {

    @Test
    public void styledOutputContainsAnsiCodes() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Console c = new Console(new PrintStream(baos));
        c.print("[bold red]hello[/bold red]");
        String out = baos.toString();
        assertTrue(out.contains("\u001B["), "ANSI escape missing");
        assertTrue(out.contains("hello"), "text content missing");
    }

    @Test
    public void printlnEndsWithNewline() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Console c = new Console(new PrintStream(baos));
        c.println("text");
        String out = baos.toString();
        assertTrue(out.endsWith("\n") || out.endsWith(System.lineSeparator()),
                   "println did not end with newline");
    }

    @Test
    public void plainTextAppearsInOutput() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Console c = new Console(new PrintStream(baos));
        c.print("hello world");
        assertTrue(baos.toString().contains("hello world"));
    }
}
```

- [ ] **Step 2: Run to verify compilation failure**

```bash
mvn -pl rich-tests test -Dtest=ConsoleTest -q 2>&1 | tail -10
```

Expected: COMPILATION ERROR — `Console` does not exist.

- [ ] **Step 3: Implement Console**

Create `rich-ansi/src/main/java/io/rich4j/richansi/Console.java`:

```java
package io.rich4j.richansi;

import io.rich4j.richcore.*;
import java.io.PrintStream;
import java.util.List;

public final class Console {

    private final PrintStream out;
    private final int width;
    private final AnsiRenderer renderer = new AnsiRenderer();
    private final Measure measure = Measure.simple();

    public Console(PrintStream out) {
        this.out = out;
        this.width = detectWidth();
    }

    public Console() {
        this(System.out);
    }

    public int width() { return width; }

    public void print(Renderable r) {
        List<List<Segment>> lines = r.render(measure, width);
        for (int i = 0; i < lines.size(); i++) {
            out.print(renderer.toAnsiLine(lines.get(i)));
            if (i < lines.size() - 1) out.print("\n");
        }
        out.flush();
    }

    public void print(String markup) {
        print(new Text(markup));
    }

    public void println(Renderable r) {
        for (List<Segment> line : r.render(measure, width)) {
            out.println(renderer.toAnsiLine(line));
        }
        out.flush();
    }

    public void println(String markup) {
        println(new Text(markup));
    }

    private static int detectWidth() {
        String cols = System.getenv("COLUMNS");
        if (cols != null) {
            try { return Integer.parseInt(cols.trim()); }
            catch (NumberFormatException ignored) {}
        }
        return 80;
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

```bash
mvn -pl rich-core install -q && mvn -pl rich-ansi install -q && \
mvn -pl rich-tests test -Dtest=ConsoleTest -q 2>&1 | tail -10
```

Expected: Tests run: 3, Failures: 0, Errors: 0.

- [ ] **Step 5: Compile full project**

```bash
mvn compile -q
```

Expected: BUILD SUCCESS.

- [ ] **Step 6: Commit**

```bash
git add rich-ansi/src/main/java/io/rich4j/richansi/Console.java \
        rich-tests/src/test/java/io/rich4j/richtests/ConsoleTest.java
git commit -m "feat(ansi): add Console entry point with markup printing and terminal width detection"
```

---

### Task 6: Add Table widget (TDD)

**Files:**
- Create: `rich-tests/src/test/java/io/rich4j/richtests/TableTest.java`
- Create: `rich-widgets/src/main/java/io/rich4j/richwidgets/Table.java`

- [ ] **Step 1: Write the failing test**

Create `rich-tests/src/test/java/io/rich4j/richtests/TableTest.java`:

```java
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
```

- [ ] **Step 2: Run to verify compilation failure**

```bash
mvn -pl rich-tests test -Dtest=TableTest -q 2>&1 | tail -10
```

Expected: COMPILATION ERROR — `Table` does not exist.

- [ ] **Step 3: Implement Table**

Create `rich-widgets/src/main/java/io/rich4j/richwidgets/Table.java`:

```java
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
            int pad = Math.max(0, widths[i] - measure.width(cell));
            Style st = (styles != null && i < styles.size()) ? styles.get(i) : Style.none();
            segs.add(new Segment(" " + cell + " ".repeat(pad) + " ", st));
            segs.add(Segment.plain("│"));
        }
        return segs;
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

```bash
mvn -pl rich-core install -q && mvn -pl rich-ansi install -q && \
mvn -pl rich-widgets install -q && \
mvn -pl rich-tests test -Dtest=TableTest -q 2>&1 | tail -10
```

Expected: Tests run: 4, Failures: 0, Errors: 0.

- [ ] **Step 5: Commit**

```bash
git add rich-widgets/src/main/java/io/rich4j/richwidgets/Table.java \
        rich-tests/src/test/java/io/rich4j/richtests/TableTest.java
git commit -m "feat(widgets): add Table with Unicode box-drawing borders and auto-sized columns"
```

---

### Task 7: Add Tree and VBox widgets

**Files:**
- Create: `rich-widgets/src/main/java/io/rich4j/richwidgets/Tree.java`
- Create: `rich-widgets/src/main/java/io/rich4j/richwidgets/VBox.java`

(No separate test class for Tree/VBox — the Demo serves as integration test. Covered by `mvn verify` at end.)

- [ ] **Step 1: Implement Tree**

Create `rich-widgets/src/main/java/io/rich4j/richwidgets/Tree.java`:

```java
package io.rich4j.richwidgets;

import io.rich4j.richcore.*;
import java.util.*;

public final class Tree implements Renderable {

    private final String label;
    private final List<Tree> children = new ArrayList<>();

    public Tree(String label) {
        this.label = label;
    }

    public Tree addChild(String label) {
        Tree child = new Tree(label);
        children.add(child);
        return child;
    }

    @Override
    public List<List<Segment>> render(Measure measure, int maxWidth) {
        List<List<Segment>> lines = new ArrayList<>();
        lines.add(List.of(Segment.plain(label)));
        renderChildren(lines, "");
        return lines;
    }

    private void renderChildren(List<List<Segment>> lines, String prefix) {
        for (int i = 0; i < children.size(); i++) {
            boolean last = (i == children.size() - 1);
            Tree child = children.get(i);
            lines.add(List.of(Segment.plain(prefix + (last ? "└── " : "├── ") + child.label)));
            child.renderChildren(lines, prefix + (last ? "    " : "│   "));
        }
    }
}
```

- [ ] **Step 2: Implement VBox**

Create `rich-widgets/src/main/java/io/rich4j/richwidgets/VBox.java`:

```java
package io.rich4j.richwidgets;

import io.rich4j.richcore.*;
import java.util.*;

public final class VBox implements Renderable {

    private final Renderable[] items;

    public VBox(Renderable... items) {
        this.items = items;
    }

    @Override
    public List<List<Segment>> render(Measure measure, int maxWidth) {
        List<List<Segment>> out = new ArrayList<>();
        for (Renderable r : items) {
            out.addAll(r.render(measure, maxWidth));
        }
        return out;
    }
}
```

- [ ] **Step 3: Compile**

```bash
mvn compile -q
```

Expected: BUILD SUCCESS.

- [ ] **Step 4: Commit**

```bash
git add rich-widgets/src/main/java/io/rich4j/richwidgets/Tree.java \
        rich-widgets/src/main/java/io/rich4j/richwidgets/VBox.java
git commit -m "feat(widgets): add Tree with guide lines and VBox compositor"
```

---

### Task 8: Improve Live rendering

**Files:**
- Modify: `rich-live/src/main/java/io/rich4j/richlive/Live.java`

- [ ] **Step 1: Implement improved Live**

Replace entire `rich-live/src/main/java/io/rich4j/richlive/Live.java`:

```java
package io.rich4j.richlive;

import io.rich4j.richcore.*;
import io.rich4j.richansi.AnsiRenderer;
import java.io.PrintStream;
import java.util.*;

public final class Live implements AutoCloseable {

    private final PrintStream out;
    private final AnsiRenderer ansi = new AnsiRenderer();
    private List<String> last = new ArrayList<>();
    private int refreshRate = 30;

    public Live(PrintStream out) {
        this.out = out;
    }

    /** Metadata hint — callers control actual sleep interval. */
    public void setRefreshRate(int fps) {
        this.refreshRate = fps;
    }

    /**
     * Repaints the live region with the rendered output of {@code renderable}.
     *
     * @param renderable the widget to render
     * @param measure    text-width measurement strategy
     * @param width      terminal column width
     */
    public void refresh(Renderable renderable, Measure measure, int width) {
        List<List<Segment>> lines = renderable.render(measure, width);
        List<String> now = new ArrayList<>();
        for (List<Segment> ln : lines) now.add(ansi.toAnsiLine(ln));

        // Move cursor to start of previous render
        if (!last.isEmpty()) {
            out.print("\u001B[" + last.size() + "F");
        }

        int i = 0;
        for (; i < now.size(); i++) {
            out.print("\u001B[2K");   // clear line
            out.print(now.get(i));
            out.print("\n");
        }
        // Clear any extra lines left over from a longer previous render
        for (; i < last.size(); i++) {
            out.print("\u001B[2K\n");
        }
        // Reposition cursor: we are now (last.size() - now.size()) lines too far down
        if (last.size() > now.size()) {
            out.print("\u001B[" + (last.size() - now.size()) + "F");
        }

        out.flush();
        last = now;
    }

    /** @deprecated Use {@link #refresh(Renderable, Measure, int)} */
    @Deprecated
    public void set(Renderable r, Measure m, int width) {
        refresh(r, m, width);
    }

    @Override
    public void close() {
        out.print("\n");
        out.flush();
    }
}
```

- [ ] **Step 2: Compile**

```bash
mvn compile -q
```

Expected: BUILD SUCCESS.

- [ ] **Step 3: Commit**

```bash
git add rich-live/src/main/java/io/rich4j/richlive/Live.java
git commit -m "feat(live): fix extra-line erasure, add refresh() API, deprecate set()"
```

---

## Chunk 3: Demo + Full Tests + Infrastructure + Local Docs

### Task 9: Update Demo to showcase all widgets

> **Note:** `ProgressBar`, `Spinner`, and `Panel` already exist in `rich-widgets` from the project skeleton — no implementation steps needed for them. The Demo simply uses them.

**Files:**
- Modify: `rich-examples/src/main/java/io/rich4j/richexamples/Demo.java`

- [ ] **Step 1: Replace Demo.java**

```java
package io.rich4j.richexamples;

import io.rich4j.richansi.Console;
import io.rich4j.richcore.*;
import io.rich4j.richwidgets.*;
import io.rich4j.richlive.Live;

public class Demo {

    public static void main(String[] args) throws Exception {
        Console console = new Console();

        // ── Header ──────────────────────────────────────────────────────
        console.println("[bold cyan]╔══════════════════════════════╗[/bold cyan]");
        console.println("[bold cyan]║       rich4j  Demo           ║[/bold cyan]");
        console.println("[bold cyan]╚══════════════════════════════╝[/bold cyan]");
        console.println("");

        // ── Live section 1: ProgressBar + Spinner ───────────────────────
        console.println("[bold white]1. Progress + Spinner[/bold white]");
        ProgressBar bar = new ProgressBar(100, 38);
        Spinner spinner = new Spinner();
        VBox live1 = new VBox(bar, spinner);

        try (Live live = new Live(System.out)) {
            live.setRefreshRate(30);
            for (int i = 0; i < 100; i++) {
                bar.step(1);
                spinner.tick();
                live.refresh(live1, Measure.simple(), console.width());
                Thread.sleep(33);
            }
        }
        console.println("");

        // ── Live section 2: Panel with Table ────────────────────────────
        console.println("[bold white]2. Table inside a Panel[/bold white]");
        Table table = new Table();
        table.addColumn("Library",  Style.none().bold());
        table.addColumn("Language", Style.none().withFg(0x00FFFF));
        table.addColumn("Stars",    Style.none().withFg(0xFFFF00));
        table.addRow("Python Rich",     "Python", "47k ★");
        table.addRow("rich4j",          "Java",   "∞  ★");
        table.addRow("Charm Lip Gloss", "Go",     "7k  ★");
        table.addRow("Pastel",          "Rust",   "4k  ★");
        table.addRow("Spectre.Console", "C#",     "10k ★");
        Panel panel = new Panel("Console UI Libraries", table);

        try (Live live = new Live(System.out)) {
            live.refresh(panel, Measure.simple(), console.width());
            Thread.sleep(2000);
        }
        console.println("");

        // ── Static section: Tree ─────────────────────────────────────────
        console.println("[bold white]3. Project Tree[/bold white]");
        Tree tree = new Tree("rich4j");
        Tree core    = tree.addChild("rich-core");
        core.addChild("Style  — immutable text styling");
        core.addChild("Segment — styled text chunk");
        core.addChild("Text   — inline markup parser");
        Tree ansi    = tree.addChild("rich-ansi");
        ansi.addChild("AnsiRenderer — segments → ANSI strings");
        ansi.addChild("Console      — main entry point");
        Tree widgets = tree.addChild("rich-widgets");
        widgets.addChild("ProgressBar");
        widgets.addChild("Spinner");
        widgets.addChild("Panel");
        widgets.addChild("Table");
        widgets.addChild("Tree");
        widgets.addChild("VBox");
        tree.addChild("rich-live").addChild("Live — cursor-based repainting");
        console.println(tree);
        console.println("");

        // ── Done ──────────────────────────────────────────────────────────
        console.println("[bold green]✓  All widgets rendered successfully.[/bold green]");
    }
}
```

- [ ] **Step 2: Run Demo to confirm it works**

```bash
mvn install -q -DskipTests && \
mvn -pl rich-examples exec:java -Dexec.mainClass=io.rich4j.richexamples.Demo
```

Expected: Animated progress bar + spinner, then table in panel, then project tree, then green "done" message.

- [ ] **Step 3: Commit**

```bash
git add rich-examples/src/main/java/io/rich4j/richexamples/Demo.java
git commit -m "feat(demo): showcase all widgets — progress, spinner, table, panel, tree"
```

---

### Task 10: Run all tests and verify green

> **Note:** `ProgressBarTest` already exists in `rich-tests` from the skeleton. It is included in the expected passing suite below.

- [ ] **Step 1: Run full test suite**

```bash
mvn install -q && mvn -pl rich-tests test 2>&1 | tail -30
```

Expected output includes lines like:
```
Tests run: X, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

All of: `StyleTest`, `AnsiRendererTest`, `TextTest`, `TableTest`, `ConsoleTest`, `ProgressBarTest` must pass.

- [ ] **Step 2: Confirm no uncommitted changes remain**

All test files and `rich-tests/pom.xml` were committed in their individual tasks (1–5). This step is verification-only:

```bash
git status
```

Expected: `nothing to commit, working tree clean`. If any test file shows as untracked or modified, stage and commit it now before proceeding.

---

### Task 11: Maven Central publishing config

**Files:**
- Modify: `pom.xml` (parent)
- Create: `LICENSE`

- [ ] **Step 1: Replace parent pom.xml**

Replace entire `pom.xml`:

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>io.rich4j</groupId>
    <artifactId>rich4j</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>pom</packaging>

    <name>rich4j</name>
    <description>A Java port of Python's Rich library for beautiful terminal console UI</description>
    <url>https://github.com/yourusername/rich4j</url>

    <licenses>
        <license>
            <name>Apache License, Version 2.0</name>
            <url>https://www.apache.org/licenses/LICENSE-2.0.txt</url>
            <distribution>repo</distribution>
        </license>
    </licenses>

    <developers>
        <developer>
            <id>yourusername</id>
            <name>Your Name</name>
            <email>you@example.com</email>
        </developer>
    </developers>

    <scm>
        <connection>scm:git:git://github.com/yourusername/rich4j.git</connection>
        <developerConnection>scm:git:ssh://github.com/yourusername/rich4j.git</developerConnection>
        <url>https://github.com/yourusername/rich4j/tree/master</url>
    </scm>

    <modules>
        <module>rich-core</module>
        <module>rich-ansi</module>
        <module>rich-widgets</module>
        <module>rich-live</module>
        <module>rich-examples</module>
        <module>rich-tests</module>
    </modules>

    <properties>
        <maven.compiler.source>21</maven.compiler.source>
        <maven.compiler.target>21</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-source-plugin</artifactId>
                <version>3.3.0</version>
                <executions>
                    <execution>
                        <id>attach-sources</id>
                        <goals><goal>jar-no-fork</goal></goals>
                    </execution>
                </executions>
            </plugin>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-javadoc-plugin</artifactId>
                <version>3.6.3</version>
                <executions>
                    <execution>
                        <id>attach-javadocs</id>
                        <goals><goal>jar</goal></goals>
                    </execution>
                </executions>
                <configuration>
                    <failOnError>false</failOnError>
                </configuration>
            </plugin>
        </plugins>
    </build>

    <profiles>
        <profile>
            <id>release</id>
            <build>
                <plugins>
                    <plugin>
                        <groupId>org.apache.maven.plugins</groupId>
                        <artifactId>maven-gpg-plugin</artifactId>
                        <version>3.1.0</version>
                        <executions>
                            <execution>
                                <id>sign-artifacts</id>
                                <phase>verify</phase>
                                <goals><goal>sign</goal></goals>
                            </execution>
                        </executions>
                    </plugin>
                    <plugin>
                        <groupId>org.sonatype.plugins</groupId>
                        <artifactId>nexus-staging-maven-plugin</artifactId>
                        <version>1.6.13</version>
                        <extensions>true</extensions>
                        <configuration>
                            <serverId>ossrh</serverId>
                            <nexusUrl>https://s01.oss.sonatype.org/</nexusUrl>
                            <autoReleaseAfterClose>false</autoReleaseAfterClose>
                        </configuration>
                    </plugin>
                </plugins>
            </build>
            <distributionManagement>
                <snapshotRepository>
                    <id>ossrh</id>
                    <url>https://s01.oss.sonatype.org/content/repositories/snapshots</url>
                </snapshotRepository>
                <repository>
                    <id>ossrh</id>
                    <url>https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/</url>
                </repository>
            </distributionManagement>
        </profile>
    </profiles>
</project>
```

- [ ] **Step 2: Create LICENSE file**

Create `LICENSE` at the repo root with the Apache 2.0 license text:

```
                                 Apache License
                           Version 2.0, January 2004
                        http://www.apache.org/licenses/

   TERMS AND CONDITIONS FOR USE, REPRODUCTION, AND DISTRIBUTION

   1. Definitions.

      "License" shall mean the terms and conditions for use, reproduction,
      and distribution as defined by Sections 1 through 9 of this document.

      "Licensor" shall mean the copyright owner or entity authorized by
      the copyright owner that is granting the License.

      "Legal Entity" shall mean the union of the acting entity and all
      other entities that control, are controlled by, or are under common
      control with that entity. For the purposes of this definition,
      "control" means (i) the power, direct or indirect, to cause the
      direction or management of such entity, whether by contract or
      otherwise, or (ii) ownership of fifty percent (50%) or more of the
      outstanding shares, or (iii) beneficial ownership of such entity.

      "You" (or "Your") shall mean an individual or Legal Entity
      exercising permissions granted by this License.

      "Source" form shall mean the preferred form for making modifications,
      including but not limited to software source code, documentation
      source, and configuration files.

      "Object" form shall mean any form resulting from mechanical
      transformation or translation of a Source form, including but
      not limited to compiled object code, generated documentation,
      and conversions to other media types.

      "Work" shall mean the work of authorship made available under
      the License, as indicated by a copyright notice that is included in
      or attached to the work (an example is provided in the Appendix below).

      "Derivative Works" shall mean any work, whether in Source or Object
      form, that is based on (or derived from) the Work and for which the
      editorial revisions, annotations, elaborations, or other transformations
      represent, as a whole, an original work of authorship. For the purposes
      of this License, Derivative Works shall not include works that remain
      separable from, or merely link (or bind by name) to the interfaces of,
      the Work and Derivative Works thereof.

      "Contribution" shall mean, as submitted to the Licensor for inclusion
      in the Work by the copyright owner or by an individual or Legal Entity
      authorized to submit on behalf of the copyright owner. For the purposes
      of this definition, "submitted" means any form of electronic, verbal,
      or written communication sent to the Licensor or its representatives,
      including but not limited to communication on electronic mailing lists,
      source code control systems, and issue tracking systems that are managed
      by, or on behalf of, the Licensor for the purpose of developing and
      improving the Work, but excluding communication that is conspicuously
      marked or designated in writing by the copyright owner as "Not a
      Contribution."

      "Contributor" shall mean Licensor and any Legal Entity on behalf of
      whom a Contribution has been received by the Licensor and included
      within the Work.

   2. Grant of Copyright License. Subject to the terms and conditions of
      this License, each Contributor hereby grants to You a perpetual,
      worldwide, non-exclusive, no-charge, royalty-free, irrevocable
      copyright license to reproduce, prepare Derivative Works of,
      publicly display, publicly perform, sublicense, and distribute the
      Work and such Derivative Works in Source or Object form.

   3. Grant of Patent License. Subject to the terms and conditions of
      this License, each Contributor hereby grants to You a perpetual,
      worldwide, non-exclusive, no-charge, royalty-free, irrevocable
      (except as stated in this section) patent license to make, have made,
      use, offer to sell, sell, import, and otherwise transfer the Work,
      where such license applies only to those patent claims licensable
      by such Contributor that are necessarily infringed by their
      Contribution(s) alone or by the combination of their Contribution(s)
      with the Work to which such Contribution(s) was submitted. If You
      institute patent litigation against any entity (including a cross-claim
      or counterclaim in a lawsuit) alleging that the Work or any
      Contribution embodied within the Work constitutes direct or contributory
      patent infringement, then any patent licenses granted to You under
      this License for that Work shall terminate as of the date such
      litigation is filed.

   4. Redistribution. You may reproduce and distribute copies of the
      Work or Derivative Works thereof in any medium, with or without
      modifications, and in Source or Object form, provided that You
      meet the following conditions:

      (a) You must give any other recipients of the Work or Derivative
          Works a copy of this License; and

      (b) You must cause any modified files to carry prominent notices
          stating that You changed the files; and

      (c) You must retain, in the Source form of any Derivative Works
          that You distribute, all copyright, patent, trademark, and
          attribution notices from the Source form of the Work,
          excluding those notices that do not pertain to any part of
          the Derivative Works; and

      (d) If the Work includes a "NOTICE" text file as part of its
          distribution, You must include a readable copy of the
          attribution notices contained within such NOTICE file, in
          at least one of the following places: within a NOTICE text
          file distributed as part of the Derivative Works; within
          the Source form or documentation, if provided along with the
          Derivative Works; or, within a display generated by the
          Derivative Works, if and wherever such third-party notices
          normally appear. The contents of the NOTICE file are for
          informational purposes only and do not modify the License.
          You may add Your own attribution notices within Derivative
          Works that You distribute, alongside or as an addendum to
          the NOTICE text from the Work, provided that such additional
          attribution notices cannot be construed as modifying the
          License.

      You may add Your own license statement for Your modifications and
      may provide additional grant of rights to use, copy, modify, merge,
      publish, distribute, sublicense, and/or sell copies of the Work,
      to any person to whom the Work is furnished, provided the Work is
      furnished under the terms of this License.

   5. Submission of Contributions. Unless You explicitly state otherwise,
      any Contribution intentionally submitted for inclusion in the Work
      by You to the Licensor shall be under the terms and conditions of
      this License, without any additional terms or conditions.

   6. Trademarks. This License does not grant permission to use the trade
      names, trademarks, service marks, or product names of the Licensor,
      except as required for reasonable and customary use in describing the
      origin of the Work and reproducing the content of the NOTICE file.

   7. Disclaimer of Warranty. Unless required by applicable law or
      agreed to in writing, Licensor provides the Work (and each
      Contributor provides its Contributions) on an "AS IS" BASIS,
      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
      implied, including, without limitation, any warranties or conditions
      of TITLE, NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A
      PARTICULAR PURPOSE. You are solely responsible for determining the
      appropriateness of using or reproducing the Work and assume any
      risks associated with Your exercise of permissions under this License.

   8. Limitation of Liability. In no event and under no legal theory,
      whether in tort (including negligence), contract, or otherwise,
      unless required by applicable law (such as deliberate and grossly
      negligent acts) or agreed to in writing, shall any Contributor be
      liable to You for damages, including any direct, indirect, special,
      incidental, or exemplary damages of any character arising as a
      result of this License or out of the use or inability to use the
      Work (including but not limited to damages for loss of goodwill,
      work stoppage, computer failure or malfunction, or all other
      commercial damages or losses), even if such Contributor has been
      advised of the possibility of such damages.

   9. Accepting Warranty or Liability. While redistributing the Work or
      Derivative Works thereof, You may choose to offer, and charge a fee
      for, acceptance of support, warranty, indemnity, or other liability
      obligations and/or rights consistent with this License. However, in
      accepting such obligations, You may offer such obligations only on
      Your own behalf and on Your sole responsibility, not on behalf of
      any other Contributor, and only if You agree to indemnify, defend,
      and hold each Contributor harmless for any liability incurred by,
      or claims asserted against, such Contributor by reason of your
      accepting any such warranty or additional liability.

   END OF TERMS AND CONDITIONS

   Copyright 2026 rich4j contributors

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```

- [ ] **Step 3: Compile with new parent pom**

```bash
mvn compile -q
```

Expected: BUILD SUCCESS.

- [ ] **Step 4: Commit**

```bash
git add pom.xml LICENSE
git commit -m "build: add Maven Central publish config — source/javadoc jars, GPG signing (release profile), Nexus staging"
```

---

### Task 12: GitHub Actions CI

**Files:**
- Create: `.github/workflows/ci.yml`

- [ ] **Step 1: Create workflow file**

```bash
mkdir -p .github/workflows
```

Create `.github/workflows/ci.yml`:

```yaml
name: CI

on:
  push:
    branches: [ "**" ]
  pull_request:
    branches: [ "**" ]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Set up Java 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: 'maven'

      - name: Build and test
        run: mvn -q verify
```

- [ ] **Step 2: Commit**

```bash
git add .github/workflows/ci.yml
git commit -m "ci: add GitHub Actions workflow — Java 21 Temurin, mvn verify on all branches"
```

---

### Task 13: Update README

**Files:**
- Modify: `README.md`

- [ ] **Step 1: Replace README.md**

```markdown
# rich4j

[![CI](https://github.com/yourusername/rich4j/actions/workflows/ci.yml/badge.svg)](https://github.com/yourusername/rich4j/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.rich4j/rich4j.svg)](https://central.sonatype.com/artifact/io.rich4j/rich4j)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)

**rich4j** is a Java port of Python's [Rich](https://github.com/Textualize/rich) library for building beautiful terminal console UIs — styled text, animated progress bars, spinners, tables, trees, and live-updating dashboards, all with ANSI escape codes.

---

## Installation

### Maven
```xml
<dependency>
    <groupId>io.rich4j</groupId>
    <artifactId>rich-core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
<!-- Add rich-ansi, rich-widgets, rich-live as needed -->
```

### Gradle
```groovy
implementation 'io.rich4j:rich-core:1.0.0-SNAPSHOT'
// Add rich-ansi, rich-widgets, rich-live as needed
```

---

## Quick Start

### Styled text
```java
Console console = new Console();
console.println("[bold cyan]Hello[/bold cyan] [red]World[/red]!");
```

### Progress bar
```java
ProgressBar bar = new ProgressBar(100, 40);
try (Live live = new Live(System.out)) {
    for (int i = 0; i < 100; i++) {
        bar.step(1);
        live.refresh(bar, Measure.simple(), 80);
        Thread.sleep(50);
    }
}
```

### Table
```java
Table table = new Table();
table.addColumn("Name", Style.none().bold());
table.addColumn("Version");
table.addRow("rich4j", "1.0.0");
table.addRow("Java",   "21");
new Console().println(table);
```

### Tree
```java
Tree tree = new Tree("project");
Tree src = tree.addChild("src");
src.addChild("Main.java");
src.addChild("Utils.java");
tree.addChild("pom.xml");
new Console().println(tree);
```

### Spinner
```java
Spinner spinner = new Spinner();
try (Live live = new Live(System.out)) {
    for (int i = 0; i < 40; i++) {
        spinner.tick();
        live.refresh(spinner, Measure.simple(), 80);
        Thread.sleep(100);
    }
}
```

---

## Modules

| Module | Description |
|---|---|
| `rich-core` | `Style`, `Segment`, `Text`, `Renderable`, `Measure` |
| `rich-ansi` | `AnsiRenderer`, `Console` |
| `rich-widgets` | `ProgressBar`, `Spinner`, `Panel`, `Table`, `Tree`, `VBox` |
| `rich-live` | `Live` — cursor-based live repainting |
| `rich-examples` | Demo application |
| `rich-tests` | JUnit 5 test suite |

---

## Build & Test

```bash
# Build and run all tests
mvn verify

# Run the demo
mvn -pl rich-examples exec:java -Dexec.mainClass=io.rich4j.richexamples.Demo
```

---

## Screenshots

_Screenshots coming soon_

---

## Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feat/my-feature`
3. Write tests first (TDD)
4. Implement your changes
5. Run `mvn verify` — all tests must pass
6. Open a pull request

Code style: Java 21, no external runtime dependencies in `rich-core` / `rich-ansi`.

---

## License

Apache 2.0 — see [LICENSE](LICENSE).
```

- [ ] **Step 2: Commit**

```bash
git add README.md
git commit -m "docs: update README with badges, install snippets, usage examples, and contributing guide"
```

---

### Task 14: Write local project documentation (DO NOT COMMIT)

**Files:**
- Create: `docs/rich4j-project-documentation.md` (local only — add to .gitignore)

- [ ] **Step 1: Create the documentation**

Create `docs/rich4j-project-documentation.md` with the following content:

```markdown
# rich4j — Project Documentation
_Local reference. Not published._

## What is rich4j?

rich4j is a Java library for creating beautiful terminal console UIs, inspired by
Python's [Rich](https://github.com/Textualize/rich) library by Will McGugan.
Where Rich leverages Python's duck-typing and dynamic features, rich4j achieves
the same goals using Java 21 features — records (future), sealed types (future),
switch expressions, and text blocks.

The central idea: **everything is a Renderable**. A progress bar, a table, a tree,
a styled string — they all implement the same interface and can be composed together.
This mirrors Rich's design where any object can be "rich renderable".

---

## Inspiration: Python Rich

Python Rich works by:
1. Accepting any Python object that has a `__rich_console__` method (or built-ins)
2. Calling it with a `Console` and `ConsoleOptions` to get `RenderableType` output
3. Converting `Segment` objects (text + style) to ANSI escape sequences
4. Writing to a file-like object (stdout by default)

rich4j mirrors this pipeline exactly:
- `Renderable` → `__rich_console__`
- `Segment` → Python `Segment`
- `AnsiRenderer` → Python's `Console._render_segment`
- `Live` → Python `Live`
- `Text` → Python `Text` with markup

---

## Architecture

```
rich-core (no dependencies)
    ├── Style         — immutable styling (fg, bg, bold, italic, 256-color)
    ├── Segment       — a text string with an associated Style
    ├── Renderable    — interface: render(Measure, int) → List<List<Segment>>
    ├── Measure       — interface: width(String) → int
    └── Text          — markup parser → List<Segment>

rich-ansi (depends on rich-core)
    ├── AnsiRenderer  — List<Segment> → ANSI escape string
    └── Console       — main entry point; wraps PrintStream

rich-widgets (depends on rich-core, rich-ansi)
    ├── ProgressBar   — animated [█████    ] bar
    ├── Spinner       — cycling frame animation
    ├── Panel         — box border around any Renderable
    ├── Table         — Unicode box-drawing table with column auto-sizing
    ├── Tree          — hierarchical tree with guide lines
    └── VBox          — vertical compositor (stacks Renderables)

rich-live (depends on rich-core, rich-ansi)
    └── Live          — cursor-based region repainting at ~30fps

rich-examples (depends on rich-ansi, rich-widgets, rich-live)
    └── Demo          — full showcase

rich-tests (depends on rich-widgets, rich-ansi, rich-core)
    └── JUnit 5 tests for all modules
```

Module dependency graph (arrows = "depends on"):

```
rich-core ←── rich-ansi ←── rich-widgets ←── rich-examples
    ↑               ↑                              ↑
    └───────────────┘                         rich-live
    (rich-live also depends on both)
```

---

## Key Concepts

### 1. ANSI Escape Codes

ANSI escape sequences are special byte sequences that terminals interpret as
commands rather than text. They begin with ESC (U+001B, `\u001B` in Java) followed
by `[` (making the CSI — Control Sequence Introducer).

| Sequence | Effect |
|---|---|
| `\u001B[1m` | Bold |
| `\u001B[3m` | Italic |
| `\u001B[0m` | Reset all attributes |
| `\u001B[38;2;R;G;Bm` | Set fg color (24-bit true color) |
| `\u001B[48;2;R;G;Bm` | Set bg color (24-bit true color) |
| `\u001B[38;5;Nm` | Set fg color (256-color palette) |
| `\u001B[2K` | Erase current line |
| `\u001B[NF` | Move cursor up N lines to beginning of line |

**Common bug:** Missing the ESC prefix. `"[0m"` is just text. `"\u001B[0m"` is the reset code.

### 2. The Renderable / Segment Pattern

Every widget in rich4j implements:

```java
List<List<Segment>> render(Measure measure, int maxWidth)
```

- Outer list = lines
- Inner list = segments within a line (each segment has text + style)
- This separation lets AnsiRenderer handle the ANSI conversion separately from layout

**Why two levels?** A single line can contain multiple styled spans:
`Hello ` (plain) + `world` (bold red) + `!` (plain) — three segments, one line.

### 3. Text Markup Parsing

The parser uses a regex `\[([^\[\]]+)\]` to find tags. A style stack tracks current style:

```
"[bold red]hello[/bold red] world"
     ↓
push(bold+red) → emit "hello" with bold+red → pop() → emit " world" with none
```

Close tags pop the stack regardless of what they contain (so `[/bold red]` and `[/]` and `[/bold]` all just pop). This is intentional — it makes nesting forgiving.

### 4. Unicode Box-Drawing Characters

Table borders use Unicode box-drawing chars from the U+2500 block:

```
┌─┬─┐   ← U+250C, U+2500, U+252C, U+2500, U+2510
│ │ │   ← U+2502
├─┼─┤   ← U+251C, U+2500, U+253C, U+2500, U+2524
└─┴─┘   ← U+2514, U+2500, U+2534, U+2500, U+2518
```

These render as single-pixel-width lines in most monospace fonts and are part of
Unicode since 1.0, so they're universally available.

### 5. Live Cursor Repainting

`Live` achieves flicker-free updates by:
1. Tracking how many lines were printed last frame (`last.size()`)
2. Using `\u001B[NF` to move the cursor back up N lines to where the render started
3. Overwriting each line with `\u001B[2K` (clear line) + new content
4. If new frame has fewer lines, clearing the extra old lines and repositioning

This avoids full-screen clears (which cause flicker) by reusing the same terminal
region. The `AutoCloseable` interface lets `Live` be used in try-with-resources.

### 6. Style Immutability

`Style` is fully immutable. All builder methods (`.bold()`, `.withFg()`, etc.) return
a new `Style` instance. This is safe for the `Text` parser's style stack — popping the
stack restores the exact previous state without any defensive copying.

---

## Design Decisions

**Why put Console in rich-ansi, not rich-core?**
`Console` needs `AnsiRenderer` to function. `AnsiRenderer` lives in `rich-ansi` which
already depends on `rich-core`. Putting `Console` in `rich-core` would create a cycle:
`rich-core` ← `rich-ansi` ← `rich-core`. Maven does not allow circular dependencies.

**Why not a single module?**
Separation lets consumers pick only what they need. A library that only uses the
`Text` parser doesn't need to pull in `rich-widgets`. It also keeps compilation
units small and makes the dependency story clear.

**Why JUnit 5 in a separate rich-tests module?**
Test dependencies (JUnit) don't leak into the library JARs. The separate module
can depend on all library modules for integration testing without those transitive
deps appearing in consumers' classpaths.

**Why is `VBox` a widget, not a utility?**
It implements `Renderable`, lives in `rich-widgets`, and participates in the same
rendering pipeline as everything else. Putting it in a "util" package would
violate the layered architecture and add a dependency direction that doesn't exist.

---

## Publishing to Maven Central (checklist)

1. Register at https://central.sonatype.com
2. Claim group ID `io.rich4j` (requires DNS TXT record or GitHub proof)
3. Generate a GPG key: `gpg --gen-key`
4. Export to keyserver: `gpg --keyserver keyserver.ubuntu.com --send-keys <KEYID>`
5. Add to `~/.m2/settings.xml`:
   ```xml
   <servers>
     <server>
       <id>ossrh</id>
       <username>your-sonatype-username</username>
       <password>your-sonatype-token</password>
     </server>
   </servers>
   ```
6. Deploy: `mvn -P release clean deploy`
7. Log in to https://s01.oss.sonatype.org and "Close" then "Release" the staging repo
```

- [ ] **Step 2: Add docs/rich4j-project-documentation.md to .gitignore**

Append this line to `.gitignore` (create the file if it doesn't exist — do NOT overwrite existing content):

```
docs/rich4j-project-documentation.md
```

- [ ] **Step 3: Commit only the .gitignore update**

```bash
git add .gitignore
git commit -m "chore: ignore local project documentation file"
```

---

### Task 15: Final verification

- [ ] **Step 1: Run full build and tests**

```bash
mvn clean verify -q 2>&1 | tail -20
```

Expected: BUILD SUCCESS, all tests pass.

- [ ] **Step 2: Run Demo end-to-end**

```bash
mvn -pl rich-examples exec:java -Dexec.mainClass=io.rich4j.richexamples.Demo
```

Expected: Animated header, progress bar + spinner live update, table in panel, project tree, green done message.

- [ ] **Step 3: Push to GitHub**

```bash
git push origin master
```

Expected: CI workflow triggers. Check at `https://github.com/yourusername/rich4j/actions`.

---
