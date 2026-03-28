# rich4j

[![CI](https://github.com/ameyaatmaa/rich4j/actions/workflows/ci.yml/badge.svg)](https://github.com/ameyaatmaa/rich4j/actions/workflows/ci.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.ameyaatmaa/rich-core.svg)](https://central.sonatype.com/artifact/io.github.ameyaatmaa/rich-core)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)

**rich4j** is a Java port of Python's [Rich](https://github.com/Textualize/rich) library for building beautiful terminal console UIs — styled text, animated progress bars, spinners, tables, trees, and live-updating dashboards, all with ANSI escape codes.

> **Published on Maven Central** — no local build needed, just add the dependency and go.

---

## Installation

### Maven

Add the modules you need to your `pom.xml`:

```xml
<!-- Styled text + Console (required) -->
<dependency>
    <groupId>io.github.ameyaatmaa</groupId>
    <artifactId>rich-ansi</artifactId>
    <version>1.0.0</version>
</dependency>

<!-- Table, Tree, ProgressBar, Spinner, Panel, VBox -->
<dependency>
    <groupId>io.github.ameyaatmaa</groupId>
    <artifactId>rich-widgets</artifactId>
    <version>1.0.0</version>
</dependency>

<!-- Live cursor-based repainting -->
<dependency>
    <groupId>io.github.ameyaatmaa</groupId>
    <artifactId>rich-live</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Gradle

```groovy
implementation 'io.github.ameyaatmaa:rich-ansi:1.0.0'
implementation 'io.github.ameyaatmaa:rich-widgets:1.0.0'
implementation 'io.github.ameyaatmaa:rich-live:1.0.0'
```

---

## Quick Start

### Styled text

```java
import io.rich4j.richansi.Console;

Console console = new Console();
console.println("[bold cyan]Hello[/bold cyan] [red]World[/red]!");
console.println("[bold green]OK:[/bold green] Deployment successful");
console.println("[bold red]ERROR:[/bold red] Connection refused");
```

Supported tags: `bold`, `italic`, `red`, `green`, `blue`, `yellow`, `cyan`, `magenta`, `white`, `black`, `#RRGGBB` hex colors, `color(N)` for 256-color.

---

### Table

```java
import io.rich4j.richansi.Console;
import io.rich4j.richwidgets.Table;
import io.rich4j.richcore.Style;

Table table = new Table();
table.addColumn("Name", Style.none().bold());
table.addColumn("Status");
table.addColumn("Duration");
table.addRow("DatabaseTest", "PASS", "1.2s");
table.addRow("ApiTest",      "PASS", "0.8s");
table.addRow("AuthTest",     "FAIL", "0.1s");
new Console().println(table);
```

Output:
```
┌─────────────┬────────┬──────────┐
│ Name        │ Status │ Duration │
├─────────────┼────────┼──────────┤
│ DatabaseTest│ PASS   │ 1.2s     │
│ ApiTest     │ PASS   │ 0.8s     │
│ AuthTest    │ FAIL   │ 0.1s     │
└─────────────┴────────┴──────────┘
```

---

### Tree

```java
import io.rich4j.richansi.Console;
import io.rich4j.richwidgets.Tree;

Tree tree = new Tree("my-app");
Tree src = tree.addChild("src");
src.addChild("Main.java");
src.addChild("Utils.java");
tree.addChild("pom.xml");
new Console().println(tree);
```

Output:
```
my-app
├── src
│   ├── Main.java
│   └── Utils.java
└── pom.xml
```

---

### Progress bar

```java
import io.rich4j.richansi.Console;
import io.rich4j.richwidgets.ProgressBar;
import io.rich4j.richlive.Live;
import io.rich4j.richcore.Measure;

ProgressBar bar = new ProgressBar(100, 40);
try (Live live = new Live(System.out)) {
    for (int i = 0; i < 100; i++) {
        bar.step(1);
        live.refresh(bar, Measure.simple(), 80);
        Thread.sleep(50);
    }
}
```

---

### Spinner

```java
import io.rich4j.richwidgets.Spinner;
import io.rich4j.richlive.Live;
import io.rich4j.richcore.Measure;

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

### Live dashboard (Progress + Spinner together)

```java
import io.rich4j.richwidgets.*;
import io.rich4j.richlive.Live;
import io.rich4j.richcore.Measure;

ProgressBar bar = new ProgressBar(50, 40);
Spinner spinner = new Spinner();
VBox dashboard = new VBox(bar, spinner);

try (Live live = new Live(System.out)) {
    live.setRefreshRate(30);
    for (int i = 0; i < 50; i++) {
        bar.step(1);
        spinner.tick();
        live.refresh(dashboard, Measure.simple(), 80);
        Thread.sleep(33);
    }
}
```

---

## Modules

| Module | GroupId | ArtifactId | Description |
|---|---|---|---|
| Core | `io.github.ameyaatmaa` | `rich-core` | `Style`, `Segment`, `Text`, `Renderable`, `Measure` |
| ANSI | `io.github.ameyaatmaa` | `rich-ansi` | `AnsiRenderer`, `Console` |
| Widgets | `io.github.ameyaatmaa` | `rich-widgets` | `ProgressBar`, `Spinner`, `Panel`, `Table`, `Tree`, `VBox` |
| Live | `io.github.ameyaatmaa` | `rich-live` | `Live` — cursor-based live repainting |

---

## Build & Test locally

```bash
git clone https://github.com/ameyaatmaa/rich4j.git
cd rich4j

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
