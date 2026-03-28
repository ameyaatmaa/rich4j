# rich4j Full Build Design
**Date:** 2026-03-28
**Status:** Approved
**Approach:** Option B — layered bottom-up

---

## Pre-flight Fixes (before Layer 0)

1. **Version mismatch:** `rich-examples/pom.xml` declares parent version `0.1.0-SNAPSHOT` while all other modules use `1.0.0-SNAPSHOT`. Normalize to `1.0.0-SNAPSHOT`.
2. **Missing dependency:** `rich-examples/pom.xml` depends on `rich-widgets` and `rich-live` only. Since `Console` lives in `rich-ansi`, add an explicit `rich-ansi` dependency to `rich-examples/pom.xml` so the Demo does not rely on transitive resolution.

---

## Overview

Extend the rich4j Maven multi-module project (a Java port of Python's Rich library) with:
- New core primitives: `Text` (markup parser) and `Console` (main entry point)
- Improved `AnsiRenderer` (256-color, true color bg, reset fix, italic)
- New widgets: `Table` (box-drawing borders, auto-sized columns) and `Tree` (guide lines)
- Improved `Live` (clean line erasure, `refresh` API)
- A showcase `Demo` using all widgets at ~30fps
- Expanded tests (5 new test classes, all passing)
- Maven Central publishing structure (GPG signing, Nexus staging, source/javadoc jars)
- GitHub Actions CI
- Updated README with badges, install snippets, usage examples
- Local project documentation (not committed)

---

## Layer 0 — rich-core additions

### `Text` (`io.rich4j.richcore`)
- Parses inline markup: `[bold red]hello[/bold red] world`
- Parser: scan for `[...]` tags; maintain a style stack
- Open tag: push combined style onto stack (e.g. `bold red` → bold=true, fg=RED)
- Close tag (`[/bold red]` or `[/]`): pop style from stack
- Supported tokens: `bold`, `italic`, `red`, `green`, `blue`, `yellow`, `cyan`, `magenta`, `white`, `black`, hex `#RRGGBB`, `color(N)` for 256-color
- Output: `List<Segment>` accessible via `segments()`
- Implements `Renderable` — wraps segments into a single-line render result
- Unclosed tags: treated as plain text (graceful fallback)
- Newline characters (`\n`) within markup content produce additional lines (extra entries in the outer `List<List<Segment>>`), not literal `\n` characters in a segment

### `Console` (`io.rich4j.richansi`)
- Lives in the **`rich-ansi`** module (package `io.rich4j.richansi`), NOT `rich-core`.
  Placing it in `rich-core` would create a circular dependency: `rich-core` ← `rich-ansi` ← `rich-core`.
- Wraps a `PrintStream` (default: `System.out`)
- Auto-detects terminal width: `System.getenv("COLUMNS")` → parse as int, fallback 80
- Methods:
  - `print(Renderable r)` — renders and writes ANSI string
  - `print(String markup)` — wraps in `Text`, then renders
  - `println(Renderable r)` — same as `print` + newline
  - `println(String markup)` — same as `print(String)` + newline
- Internally holds an `AnsiRenderer` instance

---

## Layer 1 — rich-ansi improvements

### `Style` additions (add these FIRST — before writing any tests that use them)
- `italic()` builder method — returns new Style with italic=true (the `isItalic()` getter exists but the builder was missing)
- `withBg(int rgb)` — sets background color
- `bg()` — returns bg Integer (nullable)
- `colorIndex(int n)` — sets a 256-color index on fg
- `fgColorIndex()` — returns the 256-color index (nullable Integer)

### `AnsiRenderer` changes
- **Bug fix:** `RESET` was `"[0m"` — corrected to `"\u001B[0m"`
- **256-color fg:** when Style carries `colorIndex` (0–255), emit `\u001B[38;5;Nm`
- **True color bg:** emit `\u001B[48;2;R;G;Bm` when `Style.bg()` is non-null
- **Italic:** emit `\u001B[3m` when `Style.isItalic()` is true
- Reset emitted once after each segment that has any style applied


---

## Layer 2 — rich-widgets additions

### `Table` (`io.rich4j.richwidgets`)
```java
Table t = new Table();
t.addColumn("Name", Style.none().bold());
t.addColumn("Age",  Style.none().withFg(0x00FFFF));
t.addRow("Alice", "30");
t.addRow("Bob",   "25");
```
- Unicode box-drawing borders: `┌─┬─┐` / `├─┼─┤` / `└─┴─┘` / `│`
- Column widths: max(header width, widest cell content), clamped so total ≤ maxWidth
- Header row styled with per-column Style
- Implements `Renderable`

### `Tree` (`io.rich4j.richwidgets`)
```java
Tree t = new Tree("root");
Tree child = t.addChild("child1");
child.addChild("leaf");
t.addChild("child2");
```
- Guide characters: `├── `, `└── `, `│   `, `    `
- Recursive rendering: last child uses `└──`, others use `├──`
- Implements `Renderable`

---

## Layer 3 — rich-live improvements

### `Live` changes
- After `\u001B[NF` (move up N lines), clear each line with `\u001B[2K` before printing
- If new render has fewer lines than last, clear extra old lines explicitly
- `refresh(Renderable renderable, Measure measure, int width)` — new primary method name; `width` is the terminal column width used for rendering
- `set(...)` kept as deprecated alias
- `setRefreshRate(int fps)` — stored as metadata hint

---

## Layer 4 — Demo + Tests

### Demo (`io.rich4j.richexamples.Demo`)
Sequence (all via `Live` at ~30fps, 33ms sleep):
1. `Console.println("[bold cyan]rich4j demo[/bold cyan]")` — styled header
2. Live loop: `ProgressBar` on line 1 + `Spinner` on line 2 (two separate lines in the live region, rendered via a `VBox` compositor — see below)
3. Live display: `Panel` wrapping a `Table` (5 rows × 3 cols of sample data)
4. Static: `Tree` printed via `Console`
5. `Console.println("[bold green]Done![/bold green]")`

### `VBox` compositor (`io.rich4j.richwidgets`)
- A simple `Renderable` that stacks multiple `Renderable` items vertically
- `VBox(Renderable... items)` — concatenates the rendered lines of each item in order
- Used in the Demo to combine `ProgressBar` + `Spinner` into a single live region

### Tests (rich-tests)
Add explicit `<dependency>` for `rich-core` (compile scope, no `<scope>` element — consistent with other deps in this POM) to `rich-tests/pom.xml` — currently only present transitively, which is fragile.

| Class | What it covers |
|---|---|
| `TextTest` | Bold, color, combined tokens, plain passthrough, unclosed tag fallback |
| `TableTest` | Border characters present, column padding, header styling in output |
| `ConsoleTest` | Integration: ANSI codes present in ByteArrayOutputStream |
| `StyleTest` | withFg+bold+italic chain, bg field, colorIndex |
| `AnsiRendererTest` | Existing + reset bug fix verified, italic emission |
| `ProgressBarTest` | Existing, unchanged |

---

## Layer 5 — Infrastructure

### Maven Central (parent pom.xml)
- Required metadata: `<name>`, `<description>`, `<url>`, `<licenses>` (Apache 2.0), `<developers>`, `<scm>`
- `maven-source-plugin` — attach `-sources.jar`
- `maven-javadoc-plugin` — attach `-javadoc.jar`
- `maven-gpg-plugin` — sign all artifacts; activated in profile `<id>release</id>` only (use `mvn -P release deploy` to publish)
- `nexus-staging-maven-plugin` — deploy to `https://s01.oss.sonatype.org/`; `autoReleaseAfterClose=false`
- `LICENSE` file — Apache 2.0 full text at repo root

### GitHub Actions (`.github/workflows/ci.yml`)
- Triggers: push + pull_request on all branches
- Steps: checkout → Java 21 Temurin → `mvn -q verify`

### README
- Badges: build status, Maven Central (placeholder), license
- Maven + Gradle install snippets
- Usage examples for each class/widget
- Screenshots section (placeholder)
- Contributing guide

---

## Commit Strategy
A commit is made after each layer completes with a descriptive message, e.g.:
- `feat(core): add Text markup parser and Console entry point`
- `feat(ansi): fix reset bug, add 256-color, bg color, italic support`
- `feat(widgets): add Table with box-drawing borders and Tree widget`
- `feat(live): improve line clearing and add refresh API`
- `feat(demo): showcase all widgets with live 30fps updates`
- `test: add TextTest, TableTest, ConsoleTest, StyleTest`
- `build: add Maven Central publish config with GPG signing`
- `ci: add GitHub Actions workflow`
- `docs: update README with badges, examples, contributing guide`

---

## Local Documentation (not committed)
A single markdown file saved locally covering:
- What rich4j is and Python Rich inspiration
- Architecture decisions and module dependency graph
- How each module works internally
- Key concepts: ANSI escape codes, Renderable/Segment pattern, markup parsing algorithm, Unicode box-drawing, Live cursor repainting
