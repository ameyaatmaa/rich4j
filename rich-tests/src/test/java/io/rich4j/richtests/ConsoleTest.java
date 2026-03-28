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
