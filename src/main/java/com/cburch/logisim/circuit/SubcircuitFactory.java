/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.circuit;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Map;

import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;

import com.cburch.logisim.comp.Component;
import com.cburch.logisim.data.Attribute;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.BitWidth;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Direction;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.data.Value;
import com.cburch.logisim.instance.Instance;
import com.cburch.logisim.instance.InstanceFactory;
import com.cburch.logisim.instance.InstancePainter;
import com.cburch.logisim.instance.InstanceState;
import com.cburch.logisim.instance.Port;
import com.cburch.logisim.instance.StdAttr;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.std.wiring.Pin;
import com.cburch.logisim.tools.MenuExtender;
import com.cburch.logisim.util.GraphicsUtil;
import com.cburch.logisim.util.StringGetter;
import com.cburch.logisim.util.StringUtil;

public class SubcircuitFactory extends InstanceFactory {
    private class CircuitFeature implements StringGetter, MenuExtender, ActionListener {
        private Instance instance;
        private Project proj;

        public CircuitFeature(Instance instance) {
            this.instance = instance;
        }

        public String get() {
            return source.getName();
        }

        public void configureMenu(JPopupMenu menu, Project proj) {
            this.proj = proj;
            String name = instance.getFactory().getDisplayName();
            String text = Strings.get("subcircuitViewItem", name);
            JMenuItem item = new JMenuItem(text);
            item.addActionListener(this);
            menu.add(item);
        }

        public void actionPerformed(ActionEvent e) {
            CircuitState superState = proj.getCircuitState();
            if (superState == null) return;

            CircuitState subState = getSubstate(superState, instance);
            if (subState == null) return;
            proj.setCircuitState(subState);
        }
    }

    private Circuit source;

    public SubcircuitFactory(Circuit source) {
        super("", null);
        this.source = source;
        setFacingAttribute(StdAttr.FACING);
        setDefaultToolTip(new CircuitFeature(null));
        setInstancePoker(SubcircuitPoker.class);
    }

    public Circuit getSubcircuit() {
        return source;
    }

    @Override
    public String getName() {
        return source.getName();
    }

    @Override
    public StringGetter getDisplayGetter() {
        return StringUtil.constantGetter(source.getName());
    }

    @Override
    public Bounds getOffsetBounds(AttributeSet attrs) {
        Direction facing = attrs.getValue(StdAttr.FACING);
        Direction defaultFacing = source.getAppearance().getFacing();
        Bounds bds = source.getAppearance().getOffsetBounds();
        return bds.rotate(defaultFacing, facing, 0, 0);
    }

    @Override
    public AttributeSet createAttributeSet() {
        return new CircuitAttributes(source);
    }

    //
    // methods for configuring instances
    //
    @Override
    public void configureNewInstance(Instance instance) {
        CircuitAttributes attrs = (CircuitAttributes) instance.getAttributeSet();
        attrs.setSubcircuit(instance);

        instance.addAttributeListener();
        computePorts(instance);
        // configureLabel(instance); already done in computePorts
    }

    @Override
    public void instanceAttributeChanged(Instance instance, Attribute<?> attr) {
        if (attr == StdAttr.FACING) {
            computePorts(instance);
        } else if (attr == CircuitAttributes.LABEL_LOCATION_ATTR) {
            configureLabel(instance);
        }
    }

    @Override
    public Object getInstanceFeature(Instance instance, Object key) {
        if (key == MenuExtender.class) return new CircuitFeature(instance);
        return super.getInstanceFeature(instance, key);
    }

    void computePorts(Instance instance) {
        Direction facing = instance.getAttributeValue(StdAttr.FACING);
        Map<Location, Instance> portLocs = source.getAppearance().getPortOffsets(facing);
        Port[] ports = new Port[portLocs.size()];
        Instance[] pins = new Instance[portLocs.size()];
        int i = -1;
        for (Map.Entry<Location, Instance> portLoc : portLocs.entrySet()) {
            i++;
            Location loc = portLoc.getKey();
            Instance pin = portLoc.getValue();
            String type = Pin.FACTORY.isInputPin(pin) ? Port.INPUT : Port.OUTPUT;
            BitWidth width = pin.getAttributeValue(StdAttr.WIDTH);
            ports[i] = new Port(loc.getX(), loc.getY(), type, width);
            pins[i] = pin;

            String label = pin.getAttributeValue(StdAttr.LABEL);
            if (label != null && label.length() > 0) {
                ports[i].setToolTip(StringUtil.constantGetter(label));
            }
        }

        CircuitAttributes attrs = (CircuitAttributes) instance.getAttributeSet();
        attrs.setPinInstances(pins);
        instance.setPorts(ports);
        instance.recomputeBounds();
        configureLabel(instance); // since this affects the circuit's bounds
    }

    private void configureLabel(Instance instance) {
        Bounds bds = instance.getBounds();
        Direction loc = instance.getAttributeValue(CircuitAttributes.LABEL_LOCATION_ATTR);

        int x = bds.getX() + bds.getWidth() / 2;
        int y = bds.getY() + bds.getHeight() / 2;
        int ha = GraphicsUtil.H_CENTER;
        int va = GraphicsUtil.V_CENTER;
        if (loc == Direction.EAST) {
            x = bds.getX() + bds.getWidth() + 2;
            ha = GraphicsUtil.H_LEFT;
        } else if (loc == Direction.WEST) {
            x = bds.getX() - 2;
            ha = GraphicsUtil.H_RIGHT;
        } else if (loc == Direction.SOUTH) {
            y = bds.getY() + bds.getHeight() + 2;
            va = GraphicsUtil.V_TOP;
        } else {
            y = bds.getY() - 2;
            va = GraphicsUtil.V_BASELINE;
        }
        instance.setTextField(StdAttr.LABEL, StdAttr.LABEL_FONT, x, y, ha, va);
    }

    //
    // propagation-oriented methods
    //
    public CircuitState getSubstate(CircuitState superState, Instance instance) {
        return getSubstate(createInstanceState(superState, instance));
    }

    public CircuitState getSubstate(CircuitState superState, Component comp) {
        return getSubstate(createInstanceState(superState, comp));
    }

    private CircuitState getSubstate(InstanceState instanceState) {
        CircuitState subState = (CircuitState) instanceState.getData();
        if (subState == null) {
            subState = new CircuitState(instanceState.getProject(), source);
            instanceState.setData(subState);
            instanceState.fireInvalidated();
        }
        return subState;
    }

    @Override
    public void propagate(InstanceState superState) {
        CircuitState subState = getSubstate(superState);

        CircuitAttributes attrs = (CircuitAttributes) superState.getAttributeSet();
        Instance[] pins = attrs.getPinInstances();
        for (int i = 0; i < pins.length; i++) {
            Instance pin = pins[i];
            InstanceState pinState = subState.getInstanceState(pin);
            if (Pin.FACTORY.isInputPin(pin)) {
                Value newVal = superState.getPort(i);
                Value oldVal = Pin.FACTORY.getValue(pinState);
                if (!newVal.equals(oldVal)) {
                    HashSet<CircuitThreadPool.Struct> s = superState.getCircuitState().threadLocal.get();
                    if (s != null) {
                        pinState.getCircuitState().threadLocal.set(new HashSet<>());
                    }
                    Pin.FACTORY.setValue(pinState, newVal);
                    Pin.FACTORY.propagate(pinState);
                    if (s != null) {
                        s.addAll(pinState.getCircuitState().threadLocal.get());
                        superState.getCircuitState().threadLocal.set(s);
                    }

                }
            } else { // it is output-only
                Value val = pinState.getPort(0);
                superState.setPort(i, val, 1);
            }
        }
    }

    //
    // user interface features
    //
    @Override
    public void paintGhost(InstancePainter painter) {
        Graphics g = painter.getGraphics();
        Color fg = g.getColor();
        int v = fg.getRed() + fg.getGreen() + fg.getBlue();
        Composite oldComposite = null;
        if (g instanceof Graphics2D && v > 50) {
            oldComposite = ((Graphics2D) g).getComposite();
            Composite c = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
            ((Graphics2D) g).setComposite(c);
        }
        paintBase(painter, g);
        if (oldComposite != null) {
            ((Graphics2D) g).setComposite(oldComposite);
        }
    }

    @Override
    public void paintInstance(InstancePainter painter) {
        paintBase(painter, painter.getGraphics());
        painter.drawPorts();
    }

    private void paintBase(InstancePainter painter, Graphics g) {
        CircuitAttributes attrs = (CircuitAttributes) painter.getAttributeSet();
        Direction facing = attrs.getFacing();
        Direction defaultFacing = source.getAppearance().getFacing();
        Location loc = painter.getLocation();
        g.translate(loc.getX(), loc.getY());
        source.getAppearance().paintSubcircuit(g, facing);
        drawCircuitLabel(painter, getOffsetBounds(attrs), facing, defaultFacing);
        g.translate(-loc.getX(), -loc.getY());
        painter.drawLabel();
    }

    private void drawCircuitLabel(InstancePainter painter, Bounds bds,
                                  Direction facing, Direction defaultFacing) {
        AttributeSet staticAttrs = source.getStaticAttributes();
        String label = staticAttrs.getValue(CircuitAttributes.CIRCUIT_LABEL_ATTR);
        if (label != null && !label.equals("")) {
            Direction up = staticAttrs.getValue(CircuitAttributes.CIRCUIT_LABEL_FACING_ATTR);
            Font font = staticAttrs.getValue(CircuitAttributes.CIRCUIT_LABEL_FONT_ATTR);

            int back = label.indexOf('\\');
            int lines = 1;
            boolean backs = false;
            while (back >= 0 && back <= label.length() - 2) {
                char c = label.charAt(back + 1);
                if (c == 'n') lines++;
                else if (c == '\\') backs = true;
                back = label.indexOf('\\', back + 2);
            }

            int x = bds.getX() + bds.getWidth() / 2;
            int y = bds.getY() + bds.getHeight() / 2;
            Graphics g = painter.getGraphics().create();
            double angle = Math.PI / 2 - (up.toRadians() - defaultFacing.toRadians()) - facing.toRadians();
            if (g instanceof Graphics2D && Math.abs(angle) > 0.01) {
                Graphics2D g2 = (Graphics2D) g;
                g2.rotate(angle, x, y);
            }
            g.setFont(font);
            if (lines == 1 && !backs) {
                GraphicsUtil.drawCenteredText(g, label, x, y);
            } else {
                FontMetrics fm = g.getFontMetrics();
                int height = fm.getHeight();
                y = y - (height * lines - fm.getLeading()) / 2 + fm.getAscent();
                back = label.indexOf('\\');
                while (back >= 0 && back <= label.length() - 2) {
                    char c = label.charAt(back + 1);
                    if (c == 'n') {
                        String line = label.substring(0, back);
                        GraphicsUtil.drawText(g, line, x, y,
                                GraphicsUtil.H_CENTER, GraphicsUtil.V_BASELINE);
                        y += height;
                        label = label.substring(back + 2);
                        back = label.indexOf('\\');
                    } else if (c == '\\') {
                        label = label.substring(0, back) + label.substring(back + 1);
                        back = label.indexOf('\\', back + 1);
                    } else {
                        back = label.indexOf('\\', back + 2);
                    }
                }
                GraphicsUtil.drawText(g, label, x, y,
                        GraphicsUtil.H_CENTER, GraphicsUtil.V_BASELINE);
            }
            g.dispose();
        }
    }

    /* TODO
    public String getToolTip(ComponentUserEvent e) {
        return StringUtil.format(Strings.get("subcircuitCircuitTip"), source.getDisplayName());
    } */
}
