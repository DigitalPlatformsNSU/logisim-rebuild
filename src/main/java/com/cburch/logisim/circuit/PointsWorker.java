package com.cburch.logisim.circuit;

import com.cburch.logisim.comp.Component;
import com.cburch.logisim.data.Location;

import java.util.HashSet;
import java.util.concurrent.Callable;

public class PointsWorker implements Callable<HashSet<Component>> {
    private HashSet<Location> dirty;
    private CircuitState circuit;

    public PointsWorker(CircuitState circuit, HashSet<Location> dirty) {
        this.dirty = dirty;
        this.circuit = circuit;
    }

    @Override
    public HashSet<Component> call() throws Exception {
        return circuit.getCircuit().wires.propagate(circuit, dirty);
    }
}
