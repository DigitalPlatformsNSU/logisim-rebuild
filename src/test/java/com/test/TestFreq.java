package com.test;

import com.cburch.logisim.circuit.SimulatorEvent;
import com.cburch.logisim.circuit.SimulatorListener;
import com.cburch.logisim.file.LoadFailedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.cburch.logisim.proj.*;

import java.lang.Math;
import java.io.File;
import java.util.HashMap;

import static java.lang.Math.abs;
import static java.lang.Thread.sleep;

public class TestFreq {
    public class MyListener implements SimulatorListener {
        @Override
        public void propagationCompleted(SimulatorEvent e) {
            ticks++;
        }

        @Override
        public void simulatorStateChanged(SimulatorEvent e) {

        }

        @Override
        public void tickCompleted(SimulatorEvent e) {

        }
    }
    Project proj;
    long ticks = 0;
    int countChecks = 100;

    long getRealFreq() {
        ticks = 0;
        proj.getSimulator().setIsTicking(true);
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
        }

        proj.getSimulator().setIsTicking(false);
        return ticks;
    }

    @Test
    void testFreqSmallCircuit() throws LoadFailedException {
        File f = new File(System.getProperty("user.dir") + "\\src\\test\\resources\\test.circ");
        proj = ProjectActions.doOpen(null, f, new HashMap<File, File>());
        proj.getSimulator().setTickFrequency(4096);
        proj.getSimulator().addSimulatorListener(new MyListener());
        long sum = 0;
        for (int i = 0; i < countChecks; i++) {
            System.out.println(i);
            sum += getRealFreq();
        }
        double res = (double) sum / countChecks;
        System.out.println("Average ticks in small circuit = " + res);
        Assertions.assertTrue(abs(res - 4096.0) <= 2000);
    }

    @Test
    void testFreqBigCircuit() throws LoadFailedException {
        File f = new File(System.getProperty("user.dir") + "\\src\\test\\resources\\big_test.circ");
        HashMap<File, File> substitutions = new HashMap<File, File>();
        proj = ProjectActions.doOpen(null, f, substitutions);
        proj.getSimulator().setTickFrequency(4096);
        proj.getSimulator().addSimulatorListener(new MyListener());
        long sum = 0;
        for (int i = 0; i < countChecks; i++) {
            System.out.println(i);
            sum += getRealFreq();
        }
        double res = (double) sum / countChecks;
        System.out.println("Average ticks in big circuit = " + res);
        Assertions.assertTrue(abs(res - 10.0) <= 0.0001);
    }
}

