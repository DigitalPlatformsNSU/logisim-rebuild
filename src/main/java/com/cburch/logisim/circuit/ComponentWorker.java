package com.cburch.logisim.circuit;

import com.cburch.logisim.comp.Component;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.std.wiring.Pin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.Callable;

import static com.cburch.logisim.instance.Instance.getInstanceFor;

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
                circuitState.threadLocal.set(ret);
                Component comp = (Component) compObj;
                Instance ic = getInstanceFor(comp);
                if (ic != null){
                    ic.setThreadColor(CircuitThreadPool.THREAD_COLOR.get());
                }
                comp.propagate(circuitState);
                ret = circuitState.threadLocal.get();
                if (comp.getFactory() instanceof Pin && circuitState.getParentState() != null) {
                    // should be propagated in superstate
                    circuitState.getParentState().threadLocal.set(ret);
                    circuitState.parentComp.propagate(circuitState.getParentState());
                    circuitState.getParentState().threadLocal.remove();
                }
                circuitState.threadLocal.remove();
            }
        }

        return ret;
    }
}
