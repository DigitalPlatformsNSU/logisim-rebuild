package com.test;

import com.cburch.logisim.circuit.SimulatorEvent;
import com.cburch.logisim.circuit.SimulatorListener;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.InstanceDataSingleton;

public class MyVerifListener implements SimulatorListener {
    private final FreqTester tester;

    public MyVerifListener(FreqTester tester) {
        this.tester = tester;
    }

    @Override
    public void propagationCompleted(SimulatorEvent e) {
        tester.ticks++;
        InstanceDataSingleton obj = (InstanceDataSingleton) tester.proj.getCircuitState().getData(tester.led);
        Value val = (Value) obj.getValue();
        if (val.equals(Value.TRUE)) {
            tester.flagNotEnd = false;
        }
        tester.proj.getSimulator().setIsTicking(false);
        synchronized (tester.sync) {
            tester.sync.notify();
        }
    }

    @Override
    public void simulatorStateChanged(SimulatorEvent e) {}

    @Override
    public void tickCompleted(SimulatorEvent e) {}
}
