package com.test;

import com.cburch.logisim.circuit.SimulatorListener;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.file.LoadFailedException;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.proj.ProjectActions;
import com.cburch.logisim.std.io.Led;
import org.junit.jupiter.api.Assertions;

import java.io.File;
import java.util.HashMap;

import static java.lang.Thread.sleep;

public class FreqTester {
    public Project proj = null;
    public Component led = null;
    public boolean flagNotEnd = true;
    public long ticks = 0;
    public final Object sync = new Object();
    public int countChecks = 100;

    public void init(File file, SimulatorListener listener) throws LoadFailedException {
        proj = ProjectActions.doOpen(null, file, new HashMap<>());
        proj.getSimulator().setTickFrequency(4096);
        proj.getSimulator().addSimulatorListener(listener);
    }

    public long getRealFreq(long millis) {
        ticks = 0;
        proj.getSimulator().setIsTicking(true);

        try {
            sleep(millis);
        } catch (InterruptedException e) { }

        proj.getSimulator().setIsTicking(false);
        return ticks;
    }

    public double getVerifyFreq() throws InterruptedException {
        flagNotEnd = true;
        ticks = 0;
        for (Component comp : proj.getCurrentCircuit().getComponents(Location.create(10, 20))) {
            if (comp.getFactory() instanceof Led) {
                led = comp;
                break;
            }
        }
        Assertions.assertNotNull(led);

        double time = 0;
        while (flagNotEnd) {
            proj.getSimulator().setIsTicking(true);
            double start = System.currentTimeMillis();
            synchronized (sync) {
                sync.wait();
            }
            time += (System.currentTimeMillis() - start);
        }
        time /= 1000;
        return ticks / time;
    }
}
