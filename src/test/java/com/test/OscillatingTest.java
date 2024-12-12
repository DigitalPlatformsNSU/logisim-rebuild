package com.test;

import com.cburch.logisim.file.LoadFailedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.cburch.logisim.proj.*;
import java.io.File;
import java.util.HashMap;

import static java.lang.Thread.sleep;

public class OscillatingTest {
    Project proj = null;
    static String resources = File.separator +
            "src" +
            File.separator +
            "test" +
            File.separator +
            "resources" +
            File.separator;

    //Тест положителен, если имеется зацикливание(возбуждение)
    @Test
    void testOscillating() throws LoadFailedException {
        File f = new File(System.getProperty("user.dir") + resources + "Oscillating.circ");
        proj = ProjectActions.doOpen(null, f, new HashMap<File, File>());
        proj.getSimulator().setTickFrequency(10);
        boolean isOsc = false;
        proj.getSimulator().setIsTicking(true);
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
        }
        if (proj.getSimulator().isOscillating()){
            isOsc = true;
        }
        proj.getSimulator().setIsTicking(false);
        Assertions.assertTrue(isOsc);
    }
}
