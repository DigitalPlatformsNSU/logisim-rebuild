/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.main;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;

import com.cburch.logisim.circuit.Circuit;
import com.cburch.logisim.circuit.CircuitEvent;
import com.cburch.logisim.circuit.CircuitListener;
import com.cburch.logisim.circuit.ReplacementMap;
import com.cburch.logisim.comp.Component;
import com.cburch.logisim.comp.ComponentDrawContext;
import com.cburch.logisim.data.AttributeSet;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Location;
import com.cburch.logisim.proj.Action;
import com.cburch.logisim.proj.Project;
import com.cburch.logisim.proj.ProjectEvent;
import com.cburch.logisim.proj.ProjectListener;
import com.cburch.logisim.tools.CustomHandles;

public class Selection extends SelectionBase {
    public static class Event {
        Object source;

        Event(Object source) {
            this.source = source;
        }

        public Object getSource() {
            return source;
        }
    }

    public static interface Listener {
        public void selectionChanged(Selection.Event event);
    }

    private class MyListener implements ProjectListener, CircuitListener {
        private WeakHashMap<Action, SelectionSave> savedSelections;

        MyListener() {
            savedSelections = new WeakHashMap<Action, SelectionSave>();
        }

        public void projectChanged(ProjectEvent event) {
            int type = event.getAction();
            if (type == ProjectEvent.ACTION_START) {
                SelectionSave save = SelectionSave.create(Selection.this);
                savedSelections.put((Action) event.getData(), save);
            } else if (type == ProjectEvent.ACTION_COMPLETE) {
                SelectionSave save = savedSelections.get(event.getData());
                if (save != null && save.isSame(Selection.this)) {
                    savedSelections.remove(event.getData());
                }
            } else if (type == ProjectEvent.ACTION_MERGE) {
                SelectionSave save = savedSelections.get(event.getOldData());
                savedSelections.put((Action) event.getData(), save);
            } else if (type == ProjectEvent.UNDO_COMPLETE) {
                Circuit circ = event.getProject().getCurrentCircuit();
                Action act = (Action) event.getData();
                SelectionSave save = savedSelections.get(act);
                if (save != null) {
                    lifted.clear();
                    selected.clear();
                    for (int i = 0; i < 2; i++) {
                        Component[] cs;
                        if (i == 0) cs = save.getFloatingComponents();
                        else cs = save.getAnchoredComponents();

                        if (cs != null) {
                            for (Component c : cs) {
                                if (circ.contains(c)) {
                                    selected.add(c);
                                } else {
                                    lifted.add(c);
                                }
                            }
                        }
                    }
                    fireSelectionChanged();
                }
            }
        }

        public void circuitChanged(CircuitEvent event) {
            if (event.getAction() == CircuitEvent.TRANSACTION_DONE) {
                Circuit circuit = event.getCircuit();
                ReplacementMap repl = event.getResult().getReplacementMap(circuit);
                boolean change = false;

                ArrayList<Component> oldAnchored;
                oldAnchored = new ArrayList<Component>(getComponents());
                for (Component comp : oldAnchored) {
                    Collection<Component> replacedBy = repl.get(comp);
                    if (replacedBy != null) {
                        change = true;
                        selected.remove(comp);
                        lifted.remove(comp);
                        for (Component add : replacedBy) {
                            if (circuit.contains(add)) {
                                selected.add(add);
                            } else {
                                lifted.add(add);
                            }
                        }
                    }
                }

                if (change) {
                    fireSelectionChanged();
                }
            }
        }
    }

    private MyListener myListener;
    private boolean isVisible = true;
    private SelectionAttributes attrs;

    public Selection(Project proj, Canvas canvas) {
        super(proj);

        myListener = new MyListener();
        attrs = new SelectionAttributes(canvas, this);
        proj.addProjectListener(myListener);
        proj.addCircuitListener(myListener);
    }

    //
    // query methods
    //
    public boolean isEmpty() {
        return selected.isEmpty() && lifted.isEmpty();
    }

    public AttributeSet getAttributeSet() {
        return attrs;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Selection)) return false;
        Selection otherSelection = (Selection) other;
        return this.selected.equals(otherSelection.selected)
                && this.lifted.equals(otherSelection.lifted);
    }

    public Set<Component> getComponents() {
        return unionSet;
    }

    public Collection<Component> getAnchoredComponents() {
        return selected;
    }

    public Collection<Component> getFloatingComponents() {
        return lifted;
    }

    public Collection<Component> getComponentsContaining(Location query) {
        HashSet<Component> ret = new HashSet<Component>();
        for (Component comp : unionSet) {
            if (comp.contains(query)) ret.add(comp);
        }
        return ret;
    }

    public Collection<Component> getComponentsContaining(Location query, Graphics g) {
        HashSet<Component> ret = new HashSet<Component>();
        for (Component comp : unionSet) {
            if (comp.contains(query, g)) ret.add(comp);
        }
        return ret;
    }

    public Collection<Component> getComponentsWithin(Bounds bds) {
        HashSet<Component> ret = new HashSet<Component>();
        for (Component comp : unionSet) {
            if (bds.contains(comp.getBounds())) ret.add(comp);
        }
        return ret;
    }

    public Collection<Component> getComponentsWithin(Bounds bds, Graphics g) {
        HashSet<Component> ret = new HashSet<Component>();
        for (Component comp : unionSet) {
            if (bds.contains(comp.getBounds(g))) ret.add(comp);
        }
        return ret;
    }

    public boolean contains(Component comp) {
        return unionSet.contains(comp);
    }

    //
    // graphics methods
    //
    public void draw(ComponentDrawContext context, Set<Component> hidden) {
        Graphics g = context.getGraphics();

        for (Component c : lifted) {
            if (!hidden.contains(c)) {
                Location loc = c.getLocation();

                Graphics g_new = g.create();
                context.setGraphics(g_new);
                c.getFactory().drawGhost(context, Color.GRAY,
                        loc.getX(), loc.getY(), c.getAttributeSet());
                g_new.dispose();
            }
        }

        for (Component comp : unionSet) {
            if (!suppressHandles.contains(comp) && !hidden.contains(comp)) {
                Graphics g_new = g.create();
                context.setGraphics(g_new);
                CustomHandles handler
                        = (CustomHandles) comp.getFeature(CustomHandles.class);
                if (handler == null) {
                    context.drawHandles(comp);
                } else {
                    handler.drawHandles(context);
                }
                g_new.dispose();
            }
        }

        context.setGraphics(g);
    }

    public void drawGhostsShifted(ComponentDrawContext context,
                                  int dx, int dy) {
        if (shouldSnap()) {
            dx = Canvas.snapXToGrid(dx);
            dy = Canvas.snapYToGrid(dy);
        }
        Graphics g = context.getGraphics();
        for (Component comp : unionSet) {
            AttributeSet attrs = comp.getAttributeSet();
            Location loc = comp.getLocation();
            int x = loc.getX() + dx;
            int y = loc.getY() + dy;
            context.setGraphics(g.create());
            comp.getFactory().drawGhost(context, Color.gray, x, y, attrs);
            context.getGraphics().dispose();
        }
        context.setGraphics(g);
    }

    @Override
    public void print() {
        System.err.println(" isVisible: " + isVisible); //OK
        super.print();
    }

}
