/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.circuit.mywire;

import java.util.HashSet;

class WireThread {
    private WireThread parent;
    private HashSet<CircuitWires.ThreadBundle> bundles = new HashSet<CircuitWires.ThreadBundle>();

    WireThread() {
        parent = this;
    }

    HashSet<CircuitWires.ThreadBundle> getBundles() {
        return bundles;
    }

    void unite(WireThread other) {
        WireThread group = this.find();
        WireThread group2 = other.find();
        if (group != group2) group.parent = group2;
    }

    WireThread find() {
        WireThread ret = this;
        if (ret.parent != ret) {
            do ret = ret.parent;
            while (ret.parent != ret);
            this.parent = ret;
        }
        return ret;
    }
}
