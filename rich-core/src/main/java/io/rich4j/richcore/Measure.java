package io.rich4j.richcore;
public interface Measure {
    int width(String s);
    static Measure simple(){ return s -> s.codePointCount(0, s.length()); }
}
