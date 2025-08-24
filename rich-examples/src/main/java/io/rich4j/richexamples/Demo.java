package io.rich4j.richexamples;
import io.rich4j.richwidgets.ProgressBar;
import io.rich4j.richlive.Live;
import io.rich4j.richcore.Measure;
import java.lang.Thread;
public class Demo {
    public static void main(String[] args) throws Exception {
        ProgressBar bar = new ProgressBar(200, 40);
        try (Live live = new Live(System.out)){
            for (int i=0;i<200;i++){
                bar.step(1);
                live.set(bar, Measure.simple(), 80);
                Thread.sleep(20);
            }
        }
    }
}
