# rich4j - Rich-like Console UI toolkit for Java (MVP skeleton)

This repository is a multi-module Maven skeleton for a Rich-inspired Java console UI toolkit.

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
