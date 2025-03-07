/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.draw.tools;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

import com.cburch.draw.actions.ModelAddAction;
import com.cburch.draw.canvas.Canvas;
import com.cburch.draw.model.CanvasModel;
import com.cburch.draw.model.CanvasObject;
import com.cburch.logisim.data.Bounds;
import com.cburch.logisim.data.Location;

abstract class RectangularTool extends AbstractTool {
    private boolean active;
    private Location dragStart;
    private int lastMouseX;
    private int lastMouseY;
    private Bounds currentBounds;

    public RectangularTool() {
        active = false;
        currentBounds = Bounds.EMPTY_BOUNDS;
    }

    public abstract CanvasObject createShape(int x, int y, int w, int h);

    public abstract void drawShape(Graphics g, int x, int y, int w, int h);

    public abstract void fillShape(Graphics g, int x, int y, int w, int h);

    @Override
    public Cursor getCursor(Canvas canvas) {
        return Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR);
    }

    @Override
    public void toolDeselected(Canvas canvas) {
        Bounds bds = currentBounds;
        active = false;
        repaintArea(canvas, bds);
    }

    @Override
    public void mousePressed(Canvas canvas, MouseEvent e) {
        Location loc = Location.create(e.getX(), e.getY());
        Bounds bds = Bounds.create(loc);
        dragStart = loc;
        lastMouseX = loc.getX();
        lastMouseY = loc.getY();
        active = canvas.getModel() != null;
        repaintArea(canvas, bds);
    }

    @Override
    public void mouseDragged(Canvas canvas, MouseEvent e) {
        updateMouse(canvas, e.getX(), e.getY(), e.getModifiersEx());
    }

    @Override
    public void mouseReleased(Canvas canvas, MouseEvent e) {
        if (active) {
            Bounds oldBounds = currentBounds;
            Bounds bds = computeBounds(canvas, e.getX(), e.getY(), e.getModifiersEx());
            currentBounds = Bounds.EMPTY_BOUNDS;
            active = false;
            CanvasObject add = null;
            if (bds.getWidth() != 0 && bds.getHeight() != 0) {
                CanvasModel model = canvas.getModel();
                add = createShape(bds.getX(), bds.getY(),
                        bds.getWidth(), bds.getHeight());
                canvas.doAction(new ModelAddAction(model, add));
                repaintArea(canvas, oldBounds.add(bds));
            }
            canvas.toolGestureComplete(this, add);
        }
    }

    @Override
    public void keyPressed(Canvas canvas, KeyEvent e) {
        int code = e.getKeyCode();
        if (active && (code == KeyEvent.VK_SHIFT || code == KeyEvent.VK_ALT
                || code == KeyEvent.VK_CONTROL)) {
            updateMouse(canvas, lastMouseX, lastMouseY, e.getModifiersEx());
        }
    }

    @Override
    public void keyReleased(Canvas canvas, KeyEvent e) {
        keyPressed(canvas, e);
    }

    private void updateMouse(Canvas canvas, int mx, int my, int mods) {
        Bounds oldBounds = currentBounds;
        Bounds bds = computeBounds(canvas, mx, my, mods);
        if (!bds.equals(oldBounds)) {
            currentBounds = bds;
            repaintArea(canvas, oldBounds.add(bds));
        }
    }

    private Bounds computeBounds(Canvas canvas, int mx, int my, int mods) {
        lastMouseX = mx;
        lastMouseY = my;
        if (!active) {
            return Bounds.EMPTY_BOUNDS;
        } else {
            Location start = dragStart;
            int x0 = start.getX();
            int y0 = start.getY();
            int x1 = mx;
            int y1 = my;
            if (x0 == x1 && y0 == y1) {
                return Bounds.EMPTY_BOUNDS;
            }

            boolean ctrlDown = (mods & MouseEvent.CTRL_DOWN_MASK) != 0;
            if (ctrlDown) {
                x0 = canvas.snapX(x0);
                y0 = canvas.snapY(y0);
                x1 = canvas.snapX(x1);
                y1 = canvas.snapY(y1);
            }

            boolean altDown = (mods & MouseEvent.ALT_DOWN_MASK) != 0;
            boolean shiftDown = (mods & MouseEvent.SHIFT_DOWN_MASK) != 0;
            if (altDown) {
                if (shiftDown) {
                    int r = Math.min(Math.abs(x0 - x1), Math.abs(y0 - y1));
                    x1 = x0 + r;
                    y1 = y0 + r;
                    x0 -= r;
                    y0 -= r;
                } else {
                    x0 = x0 - (x1 - x0);
                    y0 = y0 - (y1 - y0);
                }
            } else {
                if (shiftDown) {
                    int r = Math.min(Math.abs(x0 - x1), Math.abs(y0 - y1));
                    y1 = y1 < y0 ? y0 - r : y0 + r;
                    x1 = x1 < x0 ? x0 - r : x0 + r;
                }
            }

            int x = x0;
            int y = y0;
            int w = x1 - x0;
            int h = y1 - y0;
            if (w < 0) {
                x = x1;
                w = -w;
            }
            if (h < 0) {
                y = y1;
                h = -h;
            }
            return Bounds.create(x, y, w, h);
        }
    }

    private void repaintArea(Canvas canvas, Bounds bds) {
        canvas.repaint();
        /* The below doesn't work because Java doesn't deal correctly with stroke
         * widths that go outside the clip area
        canvas.repaintCanvasCoords(bds.getX() - 10, bds.getY() - 10,
                bds.getWidth() + 20, bds.getHeight() + 20);
         */
    }

    @Override
    public void draw(Canvas canvas, Graphics g) {
        Bounds bds = currentBounds;
        if (active && bds != null && bds != Bounds.EMPTY_BOUNDS) {
            g.setColor(Color.GRAY);
            drawShape(g, bds.getX(), bds.getY(), bds.getWidth(), bds.getHeight());
        }
    }

}
