/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.gui.main;

import java.awt.Rectangle;

class CanvasPaintThread extends Thread {
    private static final int REPAINT_TIMESPAN = 50; // 50 ms between repaints

    private Canvas canvas;
    private Object lock;
    private boolean repaintRequested;
    private long nextRepaint;
    private boolean alive;
    private Rectangle repaintRectangle;

    public CanvasPaintThread(Canvas canvas) {
        this.canvas = canvas;
        lock = new Object();
        repaintRequested = false;
        alive = true;
        nextRepaint = System.currentTimeMillis();
    }

    public void requestStop() {
        synchronized (lock) {
            alive = false;
            lock.notifyAll();
        }
    }

    public void requentRepaint(Rectangle rect) {
        synchronized (lock) {
            if (repaintRequested) {
                if (repaintRectangle != null) {
                    repaintRectangle.add(rect);
                }
            } else {
                repaintRequested = true;
                repaintRectangle = rect;
                lock.notifyAll();
            }
        }
    }

    public void requestRepaint() {
        synchronized (lock) {
            if (!repaintRequested) {
                repaintRequested = true;
                repaintRectangle = null;
                lock.notifyAll();
            }
        }
    }

    @Override
    public void run() {
        while (alive) {
            long now = System.currentTimeMillis();
            synchronized (lock) {
                long wait = nextRepaint - now;
                while (alive && !(repaintRequested && wait <= 0)) {
                    try {
                        if (wait > 0) {
                            lock.wait(wait);
                        } else {
                            lock.wait();
                        }
                    } catch (InterruptedException e) {
                    }
                    now = System.currentTimeMillis();
                    wait = nextRepaint - now;
                }
                if (!alive) break;
                repaintRequested = false;
                nextRepaint = now + REPAINT_TIMESPAN;
            }
            canvas.repaint();
        }
    }
}
