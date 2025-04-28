package com.test;

import com.cburch.logisim.comp.Component;
import com.cburch.logisim.file.LoadFailedException;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.proj.ProjectActions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Set;

import static java.lang.Thread.sleep;

public class TestFreq {
    static final String RESOURCES = File.separator +
            "src" +
            File.separator +
            "test" +
            File.separator +
            "resources" +
            File.separator;

    double startSim(File circ) throws LoadFailedException {
        FreqTester tester = new FreqTester();
        tester.init(circ, new MyListener(tester));

        long sum = 0;
        for (int i = 0; i < tester.countChecks; i++) {
            sum += tester.getRealFreq(1000);
        }

        return (double) sum / tester.countChecks;
    }

    @Test
    void testFreqSmallCircuit() throws LoadFailedException {
        double res = startSim(new File(System.getProperty("user.dir") + RESOURCES + "test.circ"));
        System.out.println("Average ticks in small circuit = " + res);
        Assertions.assertTrue(res >= 2048);
    }

    @Test
    void testFreqBigCircuit() throws LoadFailedException {
        double res = startSim(new File(System.getProperty("user.dir") + RESOURCES + "big_test.circ"));
        System.out.println("Average ticks in big circuit = " + res);
        Assertions.assertTrue(res >= 10.0);
    }

    @Test
    void testFreqWithVerification() throws Exception {
        File circ = new File(System.getProperty("user.dir") + RESOURCES + "fibonacci.circ");
        FreqTester tester = new FreqTester();
        tester.init(circ, new MyVerifyListener(tester));

        double res = tester.getVerifyFreq();
        System.out.println("Average ticks in fibonacci circuit = " + res);
        Assertions.assertTrue(res >= 100.0);
    }

    @Test
    void testSeparators() throws LoadFailedException {
        File f = new File(System.getProperty("user.dir") + RESOURCES + "Separator.circ");
        Project proj = ProjectActions.doOpen(null, f, new HashMap<File, File>());
        boolean haveSeparators = false;
        Set<Component> comps = proj.getCurrentCircuit().getNonWires();
        for (Component c : comps) {
            if (c.getAttributeSet().getAttributes().toString().equals("[facing, fanout, incoming, appear, bit0]")) {
                haveSeparators = true;
                break;
            }
        }
        Assertions.assertTrue(haveSeparators);
    }

    @Test
    void testOscillating() throws LoadFailedException {
        File f = new File(System.getProperty("user.dir") + RESOURCES + "Oscillating.circ");
        Project proj = ProjectActions.doOpen(null, f, new HashMap<File, File>());
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

    @Test
    void testIncompatibleData() throws LoadFailedException {
        File f = new File(System.getProperty("user.dir") + RESOURCES + "IncompatibleData.circ");
        Project proj = ProjectActions.doOpen(null, f, new HashMap<File, File>());
        Assertions.assertTrue(proj.getCurrentCircuit().getWidthIncompatibilityData()!=null);
    }

    @Test
    void testFreqSmallNestingCircuit() throws LoadFailedException {
        double res = startSim(new File(System.getProperty("user.dir") + RESOURCES + "test_nesting.circ"));
        System.out.println("Average ticks in small circuit = " + res);
        Assertions.assertTrue(res >= 2048);
    }
}