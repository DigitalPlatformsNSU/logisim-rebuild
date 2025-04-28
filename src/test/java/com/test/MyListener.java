package com.test;

import com.cburch.logisim.circuit.SimulatorEvent;
import com.cburch.logisim.circuit.SimulatorListener;

public class MyListener implements SimulatorListener {
    private final FreqTester tester;

    public MyListener(FreqTester tester) {
        this.tester = tester;
    }

    @Override
    public void propagationCompleted(SimulatorEvent e) {
        tester.ticks++;
    }

    @Override
    public void simulatorStateChanged(SimulatorEvent e) {}

    @Override
    public void tickCompleted(SimulatorEvent e) {}
}
