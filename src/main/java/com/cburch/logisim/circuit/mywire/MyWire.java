package com.cburch.logisim.circuit.mywire;

import java.awt.Graphics;
import java.util.*;

import com.cburch.logisim.circuit.CircuitState;
import com.cburch.logisim.circuit.mywire.Strings;
import com.cburch.logisim.circuit.mywire.WireFactory;
import com.cburch.logisim.circuit.mywire.WireIterator;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.comp.ComponentFactory;
import com.cburch.logisim.comp.ComponentDrawContext;
import com.cburch.logisim.comp.ComponentListener;
import com.cburch.logisim.comp.EndData;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeListener;
import com.cburch.logisim.data.AttributeOption;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Attributes;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.tools.CustomHandles;
import com.cburch.logisim.util.Cache;
import com.cburch.logisim.util.GraphicsUtil;

public final class MyWire implements Component, AttributeSet, CustomHandles, Iterable<Location> {
    public static final int WIDTH = 3;

    public static final AttributeOption VALUE_HORZ
            = new AttributeOption("horz", Strings.getter("wireDirectionHorzOption"));
    public static final AttributeOption VALUE_VERT
            = new AttributeOption("vert", Strings.getter("wireDirectionVertOption"));
    public static final Attribute<AttributeOption> dir_attr
            = Attributes.forOption("direction", Strings.getter("wireDirectionAttr"),
            new AttributeOption[]{VALUE_HORZ, VALUE_VERT});
    public static final Attribute<Integer> len_attr
            = Attributes.forInteger("length", Strings.getter("wireLengthAttr"));

    private static final List<Attribute<?>> ATTRIBUTES
            = Arrays.asList(new Attribute<?>[]{dir_attr, len_attr});
    private static final Cache cache = new Cache();

    private class EndList extends AbstractList<EndData> {
        @Override
        public EndData get(int i) {
            return getEnd(i);
        }

        @Override
        public int size() {
            return 2;
        }
    }
    private HashSet<Location> Exits = new HashSet<Location>();
    private HashSet<Wire> IncludedWires = new HashSet<Wire>();

    private Wire wExample;
    final Location e0;
    final Location e1;

    private MyWire(HashSet<Wire> wires) {
        this.IncludedWires = wires;
        for (Wire w : wires){
            this.Exits.add(w.e0);
            this.Exits.add(w.e1);
            this.wExample = w.clone();
        }
        this.e0 = this.wExample.e0;
        this.e1 = this.wExample.e1;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof MyWire)) return false;
        MyWire w = (MyWire) other;
        return w.Exits.equals(this.Exits);
    }

    @Override
    public int hashCode() {
        return e0.hashCode() * 31 + e1.hashCode();
    }

    public int getLength() {
        return (1);
    }

    @Override
    public String toString() {
        return "Wire[" + e0 + "-" + e1 + "]";
    }

    //
    // Component methods
    //
    // (Wire never issues ComponentEvents, so we don't need to track listeners)
    public void addComponentListener(ComponentListener e) {}

    public void removeComponentListener(ComponentListener e) {}

    public ComponentFactory getFactory() {
        return WireFactory.instance;
    }

    public AttributeSet getAttributeSet() {
        return this;
    }

    // location/extent methods
    public Location getLocation() {
        return null;
    }

    public Bounds getBounds() {
        int x0 = e0.getX();
        int y0 = e0.getY();
        return Bounds.create(x0 - 2, y0 - 2,
                e1.getX() - x0 + 5, e1.getY() - y0 + 5);
    }

    public Bounds getBounds(Graphics g) {
        return getBounds();
    }

    public boolean contains(Location q) {
        return true;
    }

    public boolean contains(Location pt, Graphics g) {
        return true;
    }

    //
    // propagation methods
    //
    public List<EndData> getEnds() {
        return new MyWire.EndList();
    }

    public EndData getEnd(int index) {
        Location loc = getEndLocation(index);
        return new EndData(loc, BitWidth.UNKNOWN,
                EndData.INPUT_OUTPUT);
    }

    public boolean endsAt(Location pt) {
        return Exits.contains(pt);
    }

    public void propagate(CircuitState state) {
        for (Location x : Exits){
            state.markPointAsDirty(x);
        }
    }

    //
    // user interface methods
    //
    public void expose(ComponentDrawContext context) {
        java.awt.Component dest = context.getDestination();
        int x0 = e0.getX();
        int y0 = e0.getY();
        dest.repaint(x0 - 5, y0 - 5,
                e1.getX() - x0 + 10, e1.getY() - y0 + 10);
    }

    public void draw(ComponentDrawContext context) {}

    public Object getFeature(Object key) {
        if (key == CustomHandles.class) return this;
        return null;
    }


    //
    // AttributeSet methods
    //
    // It makes some sense for a wire to be its own attribute, since
    // after all it is immutable.
    //
    @Override
    public Object clone() {
        return this;
    }

    public void addAttributeListener(AttributeListener l) {
    }

    public void removeAttributeListener(AttributeListener l) {
    }

    public List<Attribute<?>> getAttributes() {
        return ATTRIBUTES;
    }

    public boolean containsAttribute(Attribute<?> attr) {
        return ATTRIBUTES.contains(attr);
    }

    public Attribute<?> getAttribute(String name) {
        for (Attribute<?> attr : ATTRIBUTES) {
            if (name.equals(attr.getName())) return attr;
        }
        return null;
    }

    public boolean isReadOnly(Attribute<?> attr) {
        return true;
    }

    public void setReadOnly(Attribute<?> attr, boolean value) {
        throw new UnsupportedOperationException();
    }

    public boolean isToSave(Attribute<?> attr) {
        return false;
    }

    @SuppressWarnings("unchecked")
    public <V> V getValue(Attribute<V> attr) {
        if (attr == dir_attr) {
            return (V) (VALUE_VERT);
        } else if (attr == len_attr) {
            return (V) Integer.valueOf(getLength());
        } else {
            return null;
        }
    }

    public <V> void setValue(Attribute<V> attr, V value) {
        throw new IllegalArgumentException("read only attribute");
    }


    public Location getEndLocation(int index) {
        return index == 0 ? e0 : e1;
    }

    public Location getEnd0() {
        return e0;
    }

    public Location getEnd1() {
        return e1;
    }

    public HashSet <Location> getExits() {
        return Exits;
    }

    public Location getOtherEnd(Location loc) {
        return (loc.equals(e0) ? e1 : e0);
    }

    public boolean sharesEnd(MyWire other) {
        return this.e0.equals(other.e0) || this.e1.equals(other.e0)
                || this.e0.equals(other.e1) || this.e1.equals(other.e1);
    }

    public boolean overlaps(MyWire other, boolean includeEnds) {
        return overlaps(other.e0, other.e1, includeEnds);
    }

    private boolean overlaps(Location q0, Location q1, boolean includeEnds) {
        int y0 = q0.getY();
        if (y0 != q1.getY() || y0 != e0.getY()) return false;
        if (includeEnds) {
            return e1.getX() >= q0.getX() && e0.getX() <= q1.getX();
        } else {
            return e1.getX() > q0.getX() && e0.getX() < q1.getX();
        }
    }

    public Iterator<Location> iterator() {
        return new WireIterator(e0, e1);
    }

    public void drawHandles(ComponentDrawContext context) {
        context.drawHandle(e0);
        context.drawHandle(e1);
    }
}
