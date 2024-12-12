package com.test;

import com.cburch.logisim.file.LoadFailedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import com.cburch.logisim.proj.*;
import java.io.File;
import java.util.HashMap;

public class IncompatibleDataTest {
    Project proj = null;
    static String resources = File.separator +
            "src" +
            File.separator +
            "test" +
            File.separator +
            "resources" +
            File.separator;

    //Тест положителен, если имеются несовместимые данные
    @Test
    void testIncompatibleData() throws LoadFailedException {
        File f = new File(System.getProperty("user.dir") + resources + "IncompatibleData.circ");
        proj = ProjectActions.doOpen(null, f, new HashMap<File, File>());
        Assertions.assertTrue(proj.getCurrentCircuit().getWidthIncompatibilityData()!=null);
    }
}
