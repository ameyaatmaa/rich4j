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
            live.setRefreshRate(30); // metadata hint only; actual pacing is via Thread.sleep below
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
            Thread.sleep(2000); // hold so user can read the table
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
