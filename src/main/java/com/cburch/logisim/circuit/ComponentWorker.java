package com.cburch.logisim.circuit;

import com.cburch.logisim.comp.Component;
import com.cburch.logisim.std.wiring.Pin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.Callable;

public class ComponentWorker implements Callable<HashSet<CircuitThreadPool.Struct>> {
    private Object[] toProcess;
    private CircuitState circuitState;

    public ComponentWorker(CircuitState state, Object[] objects) {
        this.toProcess = objects;
        this.circuitState = state;
    }

    @Override
    public HashSet<CircuitThreadPool.Struct> call() throws Exception {
        HashSet<CircuitThreadPool.Struct> ret = new HashSet<>();
        for (Object compObj : toProcess) {
            if (compObj instanceof Component) {
                HashSet<CircuitThreadPool.Struct> result = new HashSet<>();
                circuitState.threadLocal.set(result);
                Component comp = (Component) compObj;
                comp.propagate(circuitState);
                result = circuitState.threadLocal.get();
                ret.addAll(result);
                if (comp.getFactory() instanceof Pin && circuitState.getParentState() != null) {
                    // should be propagated in superstate
                    HashSet<CircuitThreadPool.Struct> resul = new HashSet<>();
                    circuitState.getParentState().threadLocal.set(resul);
                    circuitState.parentComp.propagate(circuitState.getParentState());
                    resul = circuitState.getParentState().threadLocal.get();
                    ret.addAll(resul);
                }

            }
        }

        return ret;
    }
}
