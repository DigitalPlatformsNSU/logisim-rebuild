package com.test;

import com.cburch.logisim.file.LoadFailedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;

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
}