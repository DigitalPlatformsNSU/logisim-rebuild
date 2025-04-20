package com.test;

import com.cburch.logisim.circuit.SimulatorEvent;
import com.cburch.logisim.circuit.SimulatorListener;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.file.LoadFailedException;
import com.cburch.logisim.instance.InstanceDataSingleton;
import com.cburch.logisim.std.io.Led;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.cburch.logisim.proj.*;

import java.io.File;
import java.util.HashMap;

import static java.lang.Thread.sleep;

public class TestNesting {
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

    public class MyVerifListener implements SimulatorListener {
        MyVerifListener() {

        }
        @Override
        public void propagationCompleted(SimulatorEvent e) {
            ticks++;
            InstanceDataSingleton obj = (InstanceDataSingleton) proj.getCircuitState().getData(led);
            Value val = (Value) obj.getValue();
            if (val.equals(Value.TRUE)) {
                flagNotEnd = false;
            }
            proj.getSimulator().setIsTicking(false);
            synchronized (this) {
                this.notify();
            }
        }

        @Override
        public void simulatorStateChanged(SimulatorEvent e) {

        }

        @Override
        public void tickCompleted(SimulatorEvent e) {

        }
    }

    Project proj = null;
    Component led = null;
    boolean flagNotEnd = true;
    long ticks = 0;
    int countChecks = 100;
    static String resources = File.separator +
            "src" +
            File.separator +
            "test" +
            File.separator +
            "resources" +
            File.separator;

    long getRealFreq(long millis) {
        ticks = 0;
        proj.getSimulator().setIsTicking(true);

        try {
            sleep(millis);
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
        }

        proj.getSimulator().setIsTicking(false);
        return ticks;
    }

    double getVerifyFreq() {
        flagNotEnd = true;
        ticks = 0;
        for (Component comp : proj.getCurrentCircuit().getComponents(Location.create(10, 20))){
            if (comp.getFactory() instanceof Led) {
                led = comp;
                break;
            }
        }
        Assertions.assertNotNull(led);


        double time = 0;
        do {
            proj.getSimulator().setIsTicking(true);
            double start = System.currentTimeMillis();
            try {
                synchronized (this) {
                    wait();
                }
            } catch (InterruptedException e) {}
            time += (System.currentTimeMillis() - start);
        } while (flagNotEnd);
        time /= 1000;
        return ticks / time;
    }

    @Test
    void testFreqSmallNestingCircuit() throws LoadFailedException {
        File f = new File(System.getProperty("user.dir") + resources + "test_nesting.circ");
        proj = ProjectActions.doOpen(null, f, new HashMap<File, File>());
        proj.getSimulator().setTickFrequency(4096);
        proj.getSimulator().addSimulatorListener(new MyListener());
        long sum = 0;
        for (int i = 0; i < countChecks; i++) {
            sum += getRealFreq(1000);
        }
        double res = (double) sum / countChecks;
        System.out.println("Average ticks in small nesting circuit = " + res);
        Assertions.assertTrue(res >= 2048);
    }

}

