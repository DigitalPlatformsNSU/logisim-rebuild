/* Copyright (c) 2010, Carl Burch. License information is located in the
 * com.cburch.logisim.Main source code and at www.cburch.com/logisim/. */

package com.cburch.logisim.util;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class GraphicsUtil {
    public static final int H_LEFT = -1;
    public static final int H_CENTER = 0;
    public static final int H_RIGHT = 1;
    public static final int V_TOP = -1;
    public static final int V_CENTER = 0;
    public static final int V_BASELINE = 1;
    public static final int V_BOTTOM = 2;
    public static final int V_CENTER_OVERALL = 3;

    public static void switchToWidth(Graphics g, int width) {
        if (g instanceof Graphics2D) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(new BasicStroke((float) width));
        }
    }

    public static void drawCenteredArc(Graphics g, int x, int y,
                                       int r, int start, int dist) {
        g.drawArc(x - r, y - r, 2 * r, 2 * r, start, dist);
    }

    public static Rectangle getTextBounds(Graphics g, Font font,
                                          String text, int x, int y, int halign, int valign) {
        if (g == null) return new Rectangle(x, y, 0, 0);
        Font oldfont = g.getFont();
        if (font != null) g.setFont(font);
        Rectangle ret = getTextBounds(g, text, x, y, halign, valign);
        if (font != null) g.setFont(oldfont);
        return ret;
    }

    public static Rectangle getTextBounds(Graphics g, String text,
                                          int x, int y, int halign, int valign) {
        if (g == null) return new Rectangle(x, y, 0, 0);
        FontMetrics mets = g.getFontMetrics();
        int width = mets.stringWidth(text);
        int ascent = mets.getAscent();
        int descent = mets.getDescent();
        int height = ascent + descent;

        Rectangle ret = new Rectangle(x, y, width, height);
        switch (halign) {
            case H_CENTER:
                ret.translate(-(width / 2), 0);
                break;
            case H_RIGHT:
                ret.translate(-width, 0);
                break;
            default:
        }
        switch (valign) {
            case V_TOP:
                break;
            case V_CENTER:
                ret.translate(0, -(ascent / 2));
                break;
            case V_CENTER_OVERALL:
                ret.translate(0, -(height / 2));
                break;
            case V_BASELINE:
                ret.translate(0, -ascent);
                break;
            case V_BOTTOM:
                ret.translate(0, -height);
                break;
            default:
        }
        return ret;
    }

    public static void drawText(Graphics g, Font font,
                                String text, int x, int y, int halign, int valign) {
        Font oldfont = g.getFont();
        if (font != null) g.setFont(font);
        drawText(g, text, x, y, halign, valign);
        if (font != null) g.setFont(oldfont);
    }

    public static void drawText(Graphics g, String text,
                                int x, int y, int halign, int valign) {
        if (text.isEmpty()) return;
        Rectangle bd = getTextBounds(g, text, x, y, halign, valign);
        g.drawString(text, bd.x, bd.y + g.getFontMetrics().getAscent());
    }

    public static void drawCenteredText(Graphics g, String text,
                                        int x, int y) {
        drawText(g, text, x, y, H_CENTER, V_CENTER);
    }

    public static void drawArrow(Graphics g, int x0, int y0, int x1, int y1,
                                 int headLength, int headAngle) {
        double offs = headAngle * Math.PI / 180.0;
        double angle = Math.atan2(y0 - y1, x0 - x1);
        int[] xs = {x1 + (int) (headLength * Math.cos(angle + offs)), x1,
                x1 + (int) (headLength * Math.cos(angle - offs))};
        int[] ys = {y1 + (int) (headLength * Math.sin(angle + offs)), y1,
                y1 + (int) (headLength * Math.sin(angle - offs))};
        g.drawLine(x0, y0, x1, y1);
        g.drawPolyline(xs, ys, 3);
    }
}
