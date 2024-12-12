package com.test;

import com.cburch.logisim.comp.Component;
import com.cburch.logisim.file.LoadFailedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.cburch.logisim.proj.*;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class SeparatorsTest {
    Project proj = null;
    static String resources = File.separator +
            "src" +
            File.separator +
            "test" +
            File.separator +
            "resources" +
            File.separator;
    Set<Component> comps = new HashSet<Component>();

    //Тест положителен, если имеются разветвители
    @Test
    void testSeparators() throws LoadFailedException {
        File f = new File(System.getProperty("user.dir") + resources + "Separator.circ");
        proj = ProjectActions.doOpen(null, f, new HashMap<File, File>());
        comps = proj.getCurrentCircuit().getNonWires();
        boolean haveSeparators = false;
        for (Component c : comps) {
            if (c.getAttributeSet().getAttributes().toString().equals("[facing, fanout, incoming, appear, bit0]")) {
              haveSeparators = true;
              break;
            }
        }
        Assertions.assertTrue(haveSeparators);
    }
}

