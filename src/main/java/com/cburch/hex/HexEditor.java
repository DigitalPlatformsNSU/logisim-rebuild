/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.hex;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JComponent;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

public class HexEditor extends JComponent implements Scrollable {
    private class Listener implements HexModelListener {
        public void metainfoChanged(HexModel source) {
            measures.recompute();
            repaint();
        }

        public void bytesChanged(HexModel source, long start, long numBytes,
                                 int[] oldValues) {
            repaint(0, measures.toY(start),
                    getWidth(), measures.toY(start + numBytes) + measures.getCellHeight());
        }
    }

    private HexModel model;
    private Listener listener;
    private Measures measures;
    private Caret caret;
    private Highlighter highlighter;

    public HexEditor(HexModel model) {
        this.model = model;
        this.listener = new Listener();
        this.measures = new Measures(this);
        this.caret = new Caret(this);
        this.highlighter = new Highlighter(this);

        setOpaque(true);
        setBackground(Color.WHITE);
        if (model != null) model.addHexModelListener(listener);

        measures.recompute();
    }

    Measures getMeasures() {
        return measures;
    }

    Highlighter getHighlighter() {
        return highlighter;
    }

    public HexModel getModel() {
        return model;
    }

    public Caret getCaret() {
        return caret;
    }

    public Object addHighlight(int start, int end, Color color) {
        return highlighter.add(start, end, color);
    }

    public void removeHighlight(Object tag) {
        highlighter.remove(tag);
    }

    public void setModel(HexModel value) {
        if (model == value) return;
        if (model != null) model.removeHexModelListener(listener);
        model = value;
        highlighter.clear();
        caret.setDot(-1, false);
        if (model != null) model.addHexModelListener(listener);
        measures.recompute();
    }

    public void scrollAddressToVisible(int start, int end) {
        if (start < 0 || end < 0) return;
        int x0 = measures.toX(start);
        int x1 = measures.toX(end) + measures.getCellWidth();
        int y0 = measures.toY(start);
        int y1 = measures.toY(end);
        int h = measures.getCellHeight();
        if (y0 == y1) {
            scrollRectToVisible(new Rectangle(x0, y0, x1 - x0, h));
        } else {
            scrollRectToVisible(new Rectangle(x0, y0, x1 - x0, (y1 + h) - y0));
        }
    }

    @Override
    public void setFont(Font value) {
        super.setFont(value);
        measures.recompute();
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        super.setBounds(x, y, width, height);
        measures.widthChanged();
    }

    @Override
    protected void paintComponent(Graphics g) {
        measures.ensureComputed(g);

        Rectangle clip = g.getClipBounds();
        if (isOpaque()) {
            g.setColor(getBackground());
            g.fillRect(clip.x, clip.y, clip.width, clip.height);
        }

        long addr0 = model.getFirstOffset();
        long addr1 = model.getLastOffset();

        long xaddr0 = measures.toAddress(0, clip.y);
        if (xaddr0 == addr0) xaddr0 = measures.getBaseAddress(model);
        long xaddr1 = measures.toAddress(getWidth(), clip.y + clip.height) + 1;
        highlighter.paint(g, xaddr0, xaddr1);

        g.setColor(getForeground());
        Font baseFont = g.getFont();
        FontMetrics baseFm = g.getFontMetrics(baseFont);
        Font labelFont = baseFont.deriveFont(Font.ITALIC);
        FontMetrics labelFm = g.getFontMetrics(labelFont);
        int cols = measures.getColumnCount();
        int baseX = measures.getBaseX();
        int baseY = measures.toY(xaddr0) + baseFm.getAscent() + baseFm.getLeading() / 2;
        int dy = measures.getCellHeight();
        int labelWidth = measures.getLabelWidth();
        int labelChars = measures.getLabelChars();
        int cellWidth = measures.getCellWidth();
        int cellChars = measures.getCellChars();
        for (long a = xaddr0; a < xaddr1; a += cols, baseY += dy) {
            String label = toHex(a, labelChars);
            g.setFont(labelFont);
            g.drawString(label, baseX - labelWidth + (labelWidth - labelFm.stringWidth(label)) / 2, baseY);
            g.setFont(baseFont);
            long b = a;
            for (int j = 0; j < cols; j++, b++) {
                if (b >= addr0 && b <= addr1) {
                    String val = toHex(model.get(b), cellChars);
                    int x = measures.toX(b) + (cellWidth - baseFm.stringWidth(val)) / 2;
                    g.drawString(val, x, baseY);
                }
            }
        }

        caret.paintForeground(g, xaddr0, xaddr1);
    }

    private String toHex(long value, int chars) {
        String ret = Long.toHexString(value);
        int retLen = ret.length();
        if (retLen < chars) {
            ret = "0" + ret;
            for (int i = retLen + 1; i < chars; i++) {
                ret = "0" + ret;
            }
            return ret;
        } else if (retLen == chars) {
            return ret;
        } else {
            return ret.substring(retLen - chars);
        }
    }

    //
    // selection methods
    //
    public boolean selectionExists() {
        return caret.getMark() >= 0 && caret.getDot() >= 0;
    }

    public void selectAll() {
        caret.setDot(model.getLastOffset(), false);
        caret.setDot(0, true);
    }

    public void delete() {
        long p0 = caret.getMark();
        long p1 = caret.getDot();
        if (p0 < 0 || p1 < 0) return;
        if (p0 > p1) {
            long t = p0;
            p0 = p1;
            p1 = t;
        }
        model.fill(p0, p1 - p0 + 1, 0);
    }

    //
    // Scrollable methods
    //
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    public int getScrollableUnitIncrement(Rectangle vis,
                                          int orientation, int direction) {
        if (orientation == SwingConstants.VERTICAL) {
            int ret = measures.getCellHeight();
            if (ret < 1) {
                measures.recompute();
                ret = measures.getCellHeight();
                if (ret < 1) return 1;
            }
            return ret;
        } else {
            return Math.max(1, vis.width / 20);
        }
    }

    public int getScrollableBlockIncrement(Rectangle vis,
                                           int orientation, int direction) {
        if (orientation == SwingConstants.VERTICAL) {
            int height = measures.getCellHeight();
            if (height < 1) {
                measures.recompute();
                height = measures.getCellHeight();
                if (height < 1) return 19 * vis.height / 20;
            }
            int lines = Math.max(1, (vis.height / height) - 1);
            return lines * height;
        } else {
            return 19 * vis.width / 20;
        }
    }

    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
}
