package com.cburch.logisim.circuit;

import com.cburch.logisim.comp.Component;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.std.wiring.Pin;

import java.util.PriorityQueue;
import java.util.Vector;

public class Threads extends Thread {
    class Struct{
        Location pt;
        Value val;
        Component cause;
        int delay;

        Struct(Location pt, Value val, Component cause, int delay) {
            this.pt = pt;
            this.val = val;
            this.cause = cause;
            this.delay = delay;
        }
    }
    private Object[] toProcess;
    private Vector<Struct> result;
    private CircuitState parentState = null; // parent in tree of CircuitStates
    private Component parentComp = null; // subcircuit component containing this state
    private CircuitState state;
    private boolean done = false;

    Threads(CircuitState parentState, Component parentComp, CircuitState state, Object[] toProcess) {
        this.parentState = parentState;
        this.parentComp = parentComp;
        this.state = state;
        this.toProcess = toProcess;
    }

    public void setValue(Location pt, Value val, Component cause, int delay) {
        result.add(new Struct(pt, val, cause, delay));
    }

    Vector<Struct> getResult() {
        return result;
    }

    boolean isDone() {
        return done;
    }

    @Override
    public void run() {
        Object lock = state.getLock();
        for (Object compObj : toProcess) {
            if (compObj instanceof Component) {
                Component comp = (Component) compObj;
                comp.propagate(state, this);
                if (comp.getFactory() instanceof Pin && parentState != null) {
                    // should be propagated in superstate
                    parentComp.propagate(parentState, this);
                }
            }
        }
        done = true;
        synchronized (lock) {
            lock.notify();
        }
    }
}