/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.circuit;

import java.util.HashMap;
import java.util.HashSet;

import com.cburch.gray.Components;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.data.Value;

public class WireBundle {
    private BitWidth width = BitWidth.UNKNOWN;
    private Value pullValue = Value.UNKNOWN;
    private WireBundle parent;
    private Location widthDeterminant = null;
    public WireThread[] threads = null;
    public HashSet<Location> points = new HashSet<Location>(); // points bundle hits
    HashSet<Component> comps = new HashSet<Component>();
    HashMap<Location, Component> compslocs = new HashMap<Location, Component>();
    private WidthIncompatibilityData incompatibilityData = null;

    public WireBundle() {
        parent = this;
    }

    public boolean isValid() {
        return incompatibilityData == null;
    }

    public void setWidth(BitWidth width, Location det) {
        if (width == BitWidth.UNKNOWN) return;
        if (incompatibilityData != null) {
            incompatibilityData.add(det, width);
            return;
        }
        if (this.width != BitWidth.UNKNOWN) {
            if (width.equals(this.width)) {
                return; // the widths match, and the bundle is already set; nothing to do
            } else {    // the widths are broken: Create incompatibilityData holding this info
                incompatibilityData = new WidthIncompatibilityData();
                incompatibilityData.add(widthDeterminant, this.width);
                incompatibilityData.add(det, width);
                return;
            }
        }
        this.width = width;
        this.widthDeterminant = det;
        this.threads = new WireThread[width.getWidth()];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new WireThread();
        }
    }

    public BitWidth getWidth() {
        if (incompatibilityData != null) {
            return BitWidth.UNKNOWN;
        } else {
            return width;
        }
    }

    Location getWidthDeterminant() {
        if (incompatibilityData != null) {
            return null;
        } else {
            return widthDeterminant;
        }
    }

    WidthIncompatibilityData getWidthIncompatibilityData() {
        return incompatibilityData;
    }

    void isolate() {
        parent = this;
    }

    void unite(WireBundle other) {
        WireBundle group = this.find();
        WireBundle group2 = other.find();
        if (group != group2) group.parent = group2;
    }

    WireBundle find() {
        WireBundle ret = this;
        if (ret.parent != ret) {
            do ret = ret.parent;
            while (ret.parent != ret);
            this.parent = ret;
        }
        return ret;
    }

    void addPullValue(Value val) {
        pullValue = pullValue.combine(val);
    }

    Value getPullValue() {
        return pullValue;
    }
}
