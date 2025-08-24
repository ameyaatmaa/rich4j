# rich4j - Rich-like Console UI toolkit for Java (MVP skeleton)

This repository is a multi-module Maven skeleton for a Rich-inspired Java console UI toolkit.

Project Structure:
.
├── .gitignore
├── README.md
├── pom.xml
├── rich-ansi
│   └── pom.xml
│   └── src/main/java/io/rich4j/richansi/AnsiRenderer.java
├── rich-core
│   └── pom.xml
│   └── src/main/java/io/rich4j/richcore/
│       ├── Measure.java
│       ├── Renderable.java
│       ├── Segment.java
│       └── Style.java
├── rich-examples
│   └── pom.xml
│   └── src/main/java/io/rich4j/richexamples/Demo.java
├── rich-live
│   └── pom.xml
│   └── src/main/java/io/rich4j/richlive/Live.java
├── rich-tests
│   └── pom.xml
│   └── src/test/java/io/rich4j/richtests/
│       ├── AnsiRendererTest.java
│       └── ProgressBarTest.java
└── rich-widgets
    └── pom.xml
    └── src/main/java/io/rich4j/richwidgets/
        ├── Panel.java
        ├── ProgressBar.java
        └── Spinner.java



Modules:
- rich-core: core models (Style, Segment, Renderable)
- rich-ansi: ANSI conversion
- rich-widgets: simple widgets (ProgressBar, Spinner, Panel)
- rich-live: live region renderer (naive repaint)
- rich-examples: demo application
- rich-tests: unit tests (JUnit 5)

Build & test:
  mvn -q -DskipTests=false test

Run demo:
  mvn -pl rich-examples exec:java -Dexec.mainClass=io.rich4j.richexamples.Demo

Roadmap:
- Unicode width/grapheme support
- Smart diffing (only changed lines)
- Markdown rendering and syntax highlighting
- Logging integration (SLF4J appender)
- Packaging to Maven Central

License: Apache-2.0 (add LICENSE file before publishing)
