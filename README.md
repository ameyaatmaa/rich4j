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
