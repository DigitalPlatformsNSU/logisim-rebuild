/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.instance;

import com.cburch.logisim.circuit.*;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.comp.EndData;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.circuit.CircuitWires;

class InstanceStateImpl implements InstanceState {
    private CircuitState circuitState;
    private Component component;

    public InstanceStateImpl(CircuitState circuitState, Component component) {
        this.circuitState = circuitState;
        this.component = component;
    }

    public void repurpose(CircuitState circuitState, Component component) {
        this.circuitState = circuitState;
        this.component = component;
    }

    CircuitState getCircuitState() {
        return circuitState;
    }

    public Project getProject() {
        return circuitState.getProject();
    }

    public Instance getInstance() {
        if (component instanceof InstanceComponent) {
            return ((InstanceComponent) component).getInstance();
        } else {
            return null;
        }
    }

    public InstanceFactory getFactory() {
        if (component instanceof InstanceComponent) {
            InstanceComponent comp = (InstanceComponent) component;
            return (InstanceFactory) comp.getFactory();
        } else {
            return null;
        }
    }

    public AttributeSet getAttributeSet() {
        return component.getAttributeSet();
    }

    public <E> E getAttributeValue(Attribute<E> attr) {
        return component.getAttributeSet().getValue(attr);
    }

    public Value getPort(int portIndex) {
        EndData data = component.getEnd(portIndex);
        if (data.getWire() == null) {
            return Value.createUnknown(data.getWidth());
        }
        return circuitState.getValue(data.getWire(), data.getWidth());
    }

    public boolean isPortConnected(int index) {
        Circuit circ = circuitState.getCircuit();
        WireBundle loc = component.getEnd(index).getWire();
        if (loc == null) {
            return false;
        }
        return circ.isConnected(loc, component);
    }

    public void setPort(int portIndex, Value value, int delay) {
        EndData end = component.getEnd(portIndex);
        if (end.wire == null) {
            end.wire = new WireBundle();
            end.wire.setWidth(end.getWidth(), end.getLocation());
            if (end.wire.isValid() && end.wire.threads != null) {
                for (int i = 0; i < end.wire.threads.length; i++) {
                    WireThread thr = end.wire.threads[i].find();
                    end.wire.threads[i] = thr;
                    thr.getBundles().add(new CircuitWires.ThreadBundle(i, end.wire));
                }
            }
            end.wire.points.add(end.getLocation());
            circuitState.setValueByWire(end.wire, value);
        }
        circuitState.setValue(end.getLocation(), value, component, delay, end.getWire());
    }

    public InstanceData getData() {
        return (InstanceData) circuitState.getData(component);
    }

    public void setData(InstanceData value) {
        circuitState.setData(component, value);
    }

    public void fireInvalidated() {
        if (component instanceof InstanceComponent) {
            ((InstanceComponent) component).fireInvalidated();
        }
    }

    public boolean isCircuitRoot() {
        return !circuitState.isSubstate();
    }

    public long getTickCount() {
        return circuitState.getPropagator().getTickCount();
    }
}
