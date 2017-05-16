/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.draw2d;

import com.google.common.collect.Lists;

import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.PointList;
import org.eclipse.wb.draw2d.geometry.Rectangle;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;

import java.util.List;

/**
 * The Graphics class allows you to draw to a surface. The drawXxx() methods that pertain to shapes
 * draw an outline of the shape, whereas the fillXxx() methods fill in the shape. Also provides for
 * drawing text, lines and images.
 *
 * @author lobas_av
 * @coverage gef.draw2d
 */
public class Graphics {
  public final GC gc;
  public int m_translateX;
  public int m_translateY;
  private final State m_appliedState = new State();
  private final State m_currentState = new State();
  private final List<State> m_stack = Lists.newArrayList();
  private int m_stackPointer = 0;
  private final Rectangle m_clipping;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public Graphics(GC gc) {
    this.gc = gc;
    this.gc.setLineWidth(1);
    // initializes clipping
    m_clipping = new Rectangle(gc.getClipping());
    // initializes all State information for currentState
    m_currentState.fill(gc, m_clipping);
    m_appliedState.fill(gc, m_clipping);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Graphics
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Draws the outline of an arc located at (<i>r.x</i>, <i>r.y</i>) with width <i>r.width</i> and
   * height <i>r.height</i>. The starting angle of the arc (specified in degrees) is <i>offset</i>
   * and <i>length</i> is the arc's angle (specified in degrees).
   */
  public final void drawArc(Rectangle r, int offset, int length) {
    drawArc(r.x, r.y, r.width, r.height, offset, length);
  }

  /**
   * Draws the outline of an arc located at (<i>x</i>,<i>y</i>) with width <i>w</i> and height
   * <i>h</i>. The starting angle of the arc (specified in degrees) is <i>offset</i> and
   * <i>length</i> is the arc's angle (specified in degrees).
   *
   * @param x
   *          the x coordinate
   * @param y
   *          the y coordinate
   * @param width
   *          the width
   * @param height
   *          the height
   * @param offset
   *          the start angle
   * @param length
   *          the length of the arc
   */
  public void drawArc(int x, int y, int width, int height, int offset, int length) {
    checkPaint();
    gc.drawArc(x + m_translateX, y + m_translateY, width, height, offset, length);
  }

  /**
   * Fills the interior of an arc located at (<i>x</i>,<i>y</i>) with width <i>w</i> and height
   * <i>h</i>. The starting angle of the arc (specified in degrees) is <i>offset</i> and
   * <i>length</i> is the arc's angle (specified in degrees).
   *
   * @param x
   *          the x coordinate
   * @param y
   *          the y coordinate
   * @param width
   *          the width
   * @param height
   *          the height
   * @param offset
   *          the start angle
   * @param length
   *          the length of the arc
   */
  public void fillArc(int x, int y, int width, int height, int offset, int length) {
    checkFill();
    gc.fillArc(x + m_translateX, y + m_translateY, width, height, offset, length);
  }

  /**
   * Fills the interior of an arc located at (<i>r.x</i>, <i>r.y</i>) with width <i>r.width</i> and
   * height <i>r.height</i>. The starting angle of the arc (specified in degrees) is <i>offset</i>
   * and <i>length</i> is the arc's angle (specified in degrees).
   */
  public final void fillArc(Rectangle r, int offset, int length) {
    fillArc(r.x, r.y, r.width, r.height, offset, length);
  }

  /**
   * Fills the the given rectangle with a gradient from the foreground color to the background
   * color. If <i>vertical</i> is <code>true</code>, the gradient will go from top to bottom.
   * Otherwise, it will go from left to right. background color.
   */
  public final void fillGradient(Rectangle r, boolean vertical) {
    fillGradient(r.x, r.y, r.width, r.height, vertical);
  }

  /**
   * Fills the the given rectangle with a gradient from the foreground color to the background
   * color. If <i>vertical</i> is <code>true</code>, the gradient will go from top to bottom.
   * Otherwise, it will go from left to right. background color.
   *
   * @param x
   *          the x coordinate
   * @param y
   *          the y coordinate
   * @param width
   *          the width
   * @param height
   *          the height
   * @param vertical
   *          whether the gradient should be vertical
   */
  public void fillGradient(int x, int y, int width, int height, boolean vertical) {
    checkFill();
    checkPaint();
    gc.fillGradientRectangle(x + m_translateX, y + m_translateY, width, height, vertical);
  }

  /**
   * Draws a focus rectangle.
   */
  public final void drawFocus(Rectangle r) {
    drawFocus(r.x, r.y, r.width, r.height);
  }

  /**
   * Draws a focus rectangle.
   *
   * @param x
   *          the x coordinate
   * @param y
   *          the y coordinate
   * @param w
   *          the width
   * @param h
   *          the height
   */
  public void drawFocus(int x, int y, int w, int h) {
    checkPaint();
    checkFill();
    gc.drawFocus(x + m_translateX, y + m_translateY, w + 1, h + 1);
  }

  /**
   * Draws the given Image at the location (p.x, p.y).
   */
  public final void drawImage(Image srcImage, Point p) {
    drawImage(srcImage, p.x, p.y);
  }

  /**
   * Draws the given Image at the location (x,y).
   *
   * @param srcImage
   *          the Image
   * @param x
   *          the x coordinate
   * @param y
   *          the y coordinate
   */
  public void drawImage(Image srcImage, int x, int y) {
    checkGC();
    gc.drawImage(srcImage, x + m_translateX, y + m_translateY);
  }

  /**
   * Draws a rectangular section of the given Image to the specified rectangular reagion on the
   * canvas. The section of the image bounded by the rectangle (src.x,src.y,src.w,src.h) is copied
   * to the section of the canvas bounded by the rectangle (dest.x,dest.y,dest.w,dest.h). If these
   * two sizes are different, scaling will occur.
   */
  public final void drawImage(Image srcImage, Rectangle src, Rectangle dest) {
    drawImage(
        srcImage,
        src.x,
        src.y,
        src.width,
        src.height,
        dest.x,
        dest.y,
        dest.width,
        dest.height);
  }

  /**
   * Draws a rectangular section of the given Image to the specified rectangular reagion on the
   * canvas. The section of the image bounded by the rectangle (x1,y1,w1,h1) is copied to the
   * section of the canvas bounded by the rectangle (x2,y2,w2,h2). If these two sizes are different,
   * scaling will occur.
   *
   * @param srcImage
   *          the image
   * @param x1
   *          the x coordinate of the source
   * @param y1
   *          the y coordinate of the source
   * @param w1
   *          the width of the source
   * @param h1
   *          the height of the source
   * @param x2
   *          the x coordinate of the destination
   * @param y2
   *          the y coordinate of the destination
   * @param w2
   *          the width of the destination
   * @param h2
   *          the height of the destination
   */
  public void drawImage(Image srcImage,
      int x1,
      int y1,
      int w1,
      int h1,
      int x2,
      int y2,
      int w2,
      int h2) {
    checkGC();
    gc.drawImage(srcImage, x1, y1, w1, h1, x2 + m_translateX, y2 + m_translateY, w2, h2);
  }

  /**
   * Draws a line between the points <code>(p1.x,p1.y)</code> and <code>(p2.x,p2.y)</code> using the
   * foreground color.
   */
  public final void drawLine(Point p1, Point p2) {
    drawLine(p1.x, p1.y, p2.x, p2.y);
  }

  /**
   * Draws a line between the points <code>(x1,y1)</code> and <code>(x2,y2)</code> using the
   * foreground color.
   *
   * @param x1
   *          the x coordinate for the first point
   * @param y1
   *          the y coordinate for the first point
   * @param x2
   *          the x coordinate for the second point
   * @param y2
   *          the y coordinate for the second point
   */
  public void drawLine(int x1, int y1, int x2, int y2) {
    checkPaint();
    gc.drawLine(x1 + m_translateX, y1 + m_translateY, x2 + m_translateX, y2 + m_translateY);
  }

  /**
   * Draws a pixel, using the foreground color, at the specified point (<code>x</code>,
   * <code>y</code>).
   * <p>
   * Note that the receiver's line attributes do not affect this operation.
   * </p>
   *
   * @param x
   *          the point's x coordinate
   * @param y
   *          the point's y coordinate
   */
  public void drawPoint(int x, int y) {
    checkPaint();
    gc.drawPoint(x + m_translateX, y + m_translateY);
  }

  /**
   * Draws the outline of an ellipse that fits inside the rectangle with the given properties using
   * the foreground color.
   */
  public final void drawOval(Rectangle r) {
    drawOval(r.x, r.y, r.width, r.height);
  }

  /**
   * Draws the outline of an ellipse that fits inside the rectangle with the given properties using
   * the foreground color.
   *
   * @param x
   *          the x coordinate
   * @param y
   *          the y coordinate
   * @param width
   *          the width
   * @param height
   *          the height
   */
  public void drawOval(int x, int y, int width, int height) {
    checkPaint();
    gc.drawOval(x + m_translateX, y + m_translateY, width, height);
  }

  /**
   * Fills an ellipse that fits inside the rectangle with the given properties using the background
   * color.
   */
  public final void fillOval(Rectangle r) {
    fillOval(r.x, r.y, r.width, r.height);
  }

  /**
   * Fills an ellipse that fits inside the rectangle with the given properties using the background
   * color.
   *
   * @param x
   *          the x coordinate
   * @param y
   *          the y coordinate
   * @param w
   *          the width
   * @param h
   *          the height
   */
  public void fillOval(int x, int y, int w, int h) {
    checkFill();
    gc.fillOval(x + m_translateX, y + m_translateY, w, h);
  }

  /**
   * Draws a closed polygon defined by the given <code>PointList</code> containing the vertices. The
   * first and last points in the list will be connected.
   *
   * @param points
   *          the vertices
   */
  public void drawPolygon(PointList points) {
    checkPaint();
    points.translate(m_translateX, m_translateY);
    gc.drawPolygon(points.toIntArray());
    points.translate(-m_translateX, -m_translateY);
  }

  /**
   * Fills a closed polygon defined by the given <code>PointList</code> containing the vertices. The
   * first and last points in the list will be connected.
   *
   * @param points
   *          the vertices
   */
  public void fillPolygon(PointList points) {
    checkFill();
    points.translate(m_translateX, m_translateY);
    gc.fillPolygon(points.toIntArray());
    points.translate(-m_translateX, -m_translateY);
  }

  /**
   * Draws a polyline defined by the given <code>PointList</code> containing the vertices. The first
   * and last points in the list will <b>not</b> be connected.
   *
   * @param points
   *          the vertices
   */
  public void drawPolyline(PointList points) {
    checkPaint();
    points.translate(m_translateX, m_translateY);
    int array[] = points.toIntArray();
    gc.drawPolyline(array);
    if (getLineWidth() == 1 && array.length >= 2) {
      int x = array[array.length - 2];
      int y = array[array.length - 1];
      gc.drawLine(x, y, x, y);
    }
    points.translate(-m_translateX, -m_translateY);
  }

  /**
   * Draws a rectangle whose top-left corner is located at the point (r.x,r.y) with the given
   * r.width and r.height.
   */
  public final void drawRectangle(Rectangle r) {
    drawRectangle(r.x, r.y, r.width, r.height);
  }

  /**
   * Draws a rectangle whose top-left corner is located at the point (x,y) with the given width and
   * height.
   *
   * @param x
   *          the x coordinate
   * @param y
   *          the y coordinate
   * @param width
   *          the width
   * @param height
   *          the height
   */
  public void drawRectangle(int x, int y, int width, int height) {
    checkPaint();
    gc.drawRectangle(x + m_translateX, y + m_translateY, width, height);
  }

  /**
   * Fills a rectangle whose top-left corner is located at the point (r.x,r.y) with the given
   * r.width and r.height.
   */
  public final void fillRectangle(Rectangle r) {
    fillRectangle(r.x, r.y, r.width, r.height);
  }

  /**
   * Fills a rectangle whose top-left corner is located at the point (x,y) with the given width and
   * height.
   *
   * @param x
   *          the x coordinate
   * @param y
   *          the y coordinate
   * @param width
   *          the width
   * @param height
   *          the height
   */
  public void fillRectangle(int x, int y, int width, int height) {
    checkFill();
    gc.fillRectangle(x + m_translateX, y + m_translateY, width, height);
  }

  /**
   * Draws a rectangle with rounded corners using the foreground color. <i>arcWidth</i> and
   * <i>arcHeight</i> represent the horizontal and vertical diameter of the corners.
   *
   * @param r
   *          the rectangle
   * @param arcWidth
   *          the arc width
   * @param arcHeight
   *          the arc height
   */
  public void drawRoundRectangle(Rectangle r, int arcWidth, int arcHeight) {
    checkPaint();
    gc.drawRoundRectangle(
        r.x + m_translateX,
        r.y + m_translateY,
        r.width,
        r.height,
        arcWidth,
        arcHeight);
  }

  /**
   * Fills a rectangle with rounded corners using the background color. <i>arcWidth</i> and
   * <i>arcHeight</i> represent the horizontal and vertical diameter of the corners.
   *
   * @param r
   *          the rectangle
   * @param arcWidth
   *          the arc width
   * @param arcHeight
   *          the arc height
   */
  public void fillRoundRectangle(Rectangle r, int arcWidth, int arcHeight) {
    checkFill();
    gc.fillRoundRectangle(
        r.x + m_translateX,
        r.y + m_translateY,
        r.width,
        r.height,
        arcWidth,
        arcHeight);
  }

  /**
   * Draws the given string using the current font and foreground color. Tab expansion and carriage
   * return processing are performed. The background of the text will be transparent.
   *
   * @param s
   *          the text
   * @param x
   *          the x coordinate
   * @param y
   *          the y coordinate
   */
  public void drawText(String s, int x, int y) {
    checkText();
    gc.drawText(s, x + m_translateX, y + m_translateY, true);
  }

  /**
   * Draws the given string using the current font and foreground color. No tab expansion or
   * carriage return processing will be performed. The background of the string will be transparent.
   *
   * @param s
   *          the string
   * @param x
   *          the x coordinate
   * @param y
   *          the y coordinate
   */
  public void drawString(String s, int x, int y) {
    checkText();
    gc.drawString(s, x + m_translateX, y + m_translateY, true);
  }

  /**
   * Draws the given string using the current font and foreground color. No tab expansion or
   * carriage return processing will be performed. The background of the string will be transparent.
   */
  public final void drawString(String s, Point p) {
    drawString(s, p.x, p.y);
  }

  /**
   * Draws the given string using the current font and foreground color. Tab expansion and carriage
   * return processing are performed. The background of the text will be transparent.
   */
  public final void drawText(String s, Point p) {
    drawText(s, p.x, p.y);
  }

  /**
   * Draws the given string using the current font and foreground color. No tab expansion or
   * carriage return processing will be performed. The background of the string will be filled with
   * the current background color.
   */
  public final void fillString(String s, Point p) {
    fillString(s, p.x, p.y);
  }

  /**
   * Draws the given string using the current font and foreground color. No tab expansion or
   * carriage return processing will be performed. The background of the string will be filled with
   * the current background color.
   *
   * @param s
   *          the string
   * @param x
   *          the x coordinate
   * @param y
   *          the y coordinate
   */
  public void fillString(String s, int x, int y) {
    checkText();
    gc.drawString(s, x + m_translateX, y + m_translateY, false);
  }

  /**
   * Draws the given string using the current font and foreground color. Tab expansion and carriage
   * return processing are performed. The background of the text will be filled with the current
   * background color.
   */
  public final void fillText(String s, Point p) {
    fillText(s, p.x, p.y);
  }

  /**
   * Draws the given string using the current font and foreground color. Tab expansion and carriage
   * return processing are performed. The background of the text will be filled with the current
   * background color.
   *
   * @param s
   *          the text
   * @param x
   *          the x coordinate
   * @param y
   *          the y coordinate
   */
  public void fillText(String s, int x, int y) {
    checkText();
    gc.drawText(s, x + m_translateX, y + m_translateY, false);
  }

  /**
   * Returns the extent of the given string. Tab expansion and carriage return processing are
   * performed.
   */
  public Dimension getTextExtent(String s) {
    checkText();
    org.eclipse.swt.graphics.Point extent = gc.textExtent(s);
    return new Dimension(extent.x, extent.y);
  }

  /**
   * Returns the background color used for filling.
   *
   * @return the background color
   */
  public Color getBackgroundColor() {
    return m_currentState.background;
  }

  /**
   * Returns the font used to draw and fill text.
   *
   * @return the font
   */
  public Font getFont() {
    return m_currentState.font;
  }

  /**
   * Returns the font metrics for the current font.
   *
   * @return the font metrics
   */
  public FontMetrics getFontMetrics() {
    checkText();
    return gc.getFontMetrics();
  }

  /**
   * Returns the foreground color used to draw lines and text.
   *
   * @return the foreground color
   */
  public Color getForegroundColor() {
    return m_currentState.foreground;
  }

  /**
   * Returns the line style.
   *
   * @return the line style
   */
  public int getLineStyle() {
    return m_currentState.lineStyle;
  }

  /**
   * Returns the current line width.
   *
   * @return the line width
   */
  public int getLineWidth() {
    return m_currentState.lineWidth;
  }

  /**
   * Returns <code>true</code> if this graphics object should use XOR mode with painting.
   *
   * @return whether XOR mode is turned on
   */
  public boolean getXORMode() {
    return m_currentState.xor;
  }

  /**
   * Sets the background color.
   *
   * @param color
   *          the new background color
   */
  public void setBackgroundColor(Color color) {
    if (!m_currentState.background.equals(color)) {
      m_currentState.background = color;
    }
  }

  /**
   * Modifies the given rectangle to match the clip region and returns that rectangle.
   *
   * @param rectangle
   *          the rectangle to hold the clip region
   * @return the clip rectangle
   */
  public Rectangle getClip() {
    return new Rectangle(m_clipping);
  }

  /**
   * Sets the clip region to the given rectangle. Anything outside this rectangle will not be drawn.
   */
  public void clipRect(Rectangle rectangle) {
    m_clipping.intersect(rectangle);
    m_currentState.clippingX = m_clipping.x + m_translateX;
    m_currentState.clippingY = m_clipping.y + m_translateY;
    m_currentState.clippingWidth = m_clipping.width;
    m_currentState.clippingHeight = m_clipping.height;
  }

  /**
   * Sets the font.
   *
   * @param font
   *          the new font
   */
  public void setFont(Font font) {
    if (m_currentState.font != font) {
      m_currentState.font = font;
    }
  }

  /**
   * Sets the foreground color.
   *
   * @param color
   *          the new foreground color
   */
  public void setForegroundColor(Color color) {
    if (!m_currentState.foreground.equals(color)) {
      m_currentState.foreground = color;
    }
  }

  /**
   * Sets the line style.
   *
   * @param style
   *          the new style
   */
  public void setLineStyle(int style) {
    if (m_currentState.lineStyle != style) {
      m_currentState.lineStyle = style;
    }
  }

  /**
   * Sets the line width.
   *
   * @param width
   *          the new width
   */
  public void setLineWidth(int width) {
    if (m_currentState.lineWidth != width) {
      m_currentState.lineWidth = width;
    }
  }

  /**
   * Sets the XOR mode.
   *
   * @param b
   *          the new XOR mode
   */
  public void setXORMode(boolean b) {
    if (m_currentState.xor != b) {
      m_currentState.xor = b;
    }
  }

  /**
   * Translates this graphics object so that its origin is offset horizontally by <i>p.x</i> and
   * vertically by <i>p.y</i>.
   */
  public final void translate(Point p) {
    translate(p.x, p.y);
  }

  /**
   * Translates this graphics object so that its origin is offset horizontally by <i>dx</i> and
   * vertically by <i>dy</i>.
   *
   * @param dx
   *          the horizontal offset
   * @param dy
   *          the vertical offset
   */
  public void translate(int dx, int dy) {
    setTranslation(m_translateX + dx, m_translateY + dy);
    m_clipping.x -= dx;
    m_clipping.y -= dy;
  }

  /**
   * Sets the translation values of this to the given values
   *
   * @param x
   *          The x value
   * @param y
   *          The y value
   */
  protected void setTranslation(int x, int y) {
    m_translateX = m_currentState.translateX = x;
    m_translateY = m_currentState.translateY = y;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Check state's
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * If the background color has changed, this change will be pushed to the GC. Also calls
   * {@link #checkGC()}.
   */
  protected final void checkFill() {
    if (!m_appliedState.background.equals(m_currentState.background)) {
      gc.setBackground(m_appliedState.background = m_currentState.background);
    }
    checkGC();
  }

  /**
   * If the XOR or the clip region has change, these changes will be pushed to the GC.
   */
  protected final void checkGC() {
    if (m_appliedState.xor != m_currentState.xor) {
      gc.setXORMode(m_appliedState.xor = m_currentState.xor);
    }
    if (m_appliedState.clippingX != m_currentState.clippingX
        || m_appliedState.clippingY != m_currentState.clippingY
        || m_appliedState.clippingWidth != m_currentState.clippingWidth
        || m_appliedState.clippingHeight != m_currentState.clippingHeight) {
      gc.setClipping(
          m_appliedState.clippingX = m_currentState.clippingX,
          m_appliedState.clippingY = m_currentState.clippingY,
          m_appliedState.clippingWidth = m_currentState.clippingWidth,
          m_appliedState.clippingHeight = m_currentState.clippingHeight);
    }
  }

  /**
   * If the line width, line style, foreground or background colors have changed, these changes will
   * be pushed to the GC. Also calls {@link #checkGC()}.
   */
  protected final void checkPaint() {
    checkGC();
    if (!m_appliedState.foreground.equals(m_currentState.foreground)) {
      gc.setForeground(m_appliedState.foreground = m_currentState.foreground);
    }
    if (m_appliedState.lineStyle != m_currentState.lineStyle) {
      gc.setLineStyle(m_appliedState.lineStyle = m_currentState.lineStyle);
    }
    if (m_appliedState.lineWidth != m_currentState.lineWidth) {
      gc.setLineWidth(m_appliedState.lineWidth = m_currentState.lineWidth);
    }
    if (!m_appliedState.background.equals(m_currentState.background)) {
      gc.setBackground(m_appliedState.background = m_currentState.background);
    }
  }

  /**
   * If the font has changed, this change will be pushed to the GC. Also calls {@link #checkPaint()}
   * and {@link #checkFill()}.
   */
  protected final void checkText() {
    checkPaint();
    checkFill();
    if (!m_appliedState.font.equals(m_currentState.font)) {
      gc.setFont(m_appliedState.font = m_currentState.font);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // State
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Pushes the current state of this graphics object onto a stack.
   */
  public void pushState() {
    if (m_stackPointer < m_stack.size()) {
      State state = m_stack.get(m_stackPointer);
      state.fill(m_currentState);
    } else {
      m_stack.add(m_currentState.copy());
    }
    m_stackPointer++;
  }

  /**
   * Pops the previous state of this graphics object off the stack (if {@link #pushState()} has
   * previously been called) and restores the current state to that popped state.
   */
  public void popState() {
    restoreState(--m_stackPointer);
  }

  /**
   * Restores the previous state of this graphics object.
   */
  public void restoreState() {
    restoreState(m_stackPointer - 1);
  }

  /**
   * Sets all State information to that of the given State, called by restoreState()
   *
   * @param s
   *          the State
   */
  private void restoreState(int index) {
    State state = m_stack.get(index);
    // restore graphics info
    state.fillTo(this);
    // restore translate
    m_translateX = m_currentState.translateX = state.translateX;
    m_translateY = m_currentState.translateY = state.translateY;
    // restore absolute clipping
    m_currentState.clippingX = state.clippingX;
    m_currentState.clippingY = state.clippingY;
    m_currentState.clippingWidth = state.clippingWidth;
    m_currentState.clippingHeight = state.clippingHeight;
    // restore clipping
    m_clipping.x = state.clippingX - m_translateX;
    m_clipping.y = state.clippingY - m_translateY;
    m_clipping.width = state.clippingWidth;
    m_clipping.height = state.clippingHeight;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // State inner class
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Contains the state variables of this SWTGraphics object
   */
  private static class State {
    /**
     * Background and foreground colors.
     */
    public Color background;
    public Color foreground;
    /**
     * Clip values
     */
    public int clippingX; // X and Y are absolute here.
    public int clippingY;
    public int clippingWidth;
    public int clippingHeight;
    /**
     * Font value.
     */
    public Font font;
    /**
     * Line values.
     */
    public int lineWidth;
    public int lineStyle;
    public int translateX;
    public int translateY;
    /**
     * XOR value.
     */
    public boolean xor;

    /**
     * A clone of this instance.
     */
    public State copy() {
      return new State().fill(this);
    }

    /**
     * Copies all state information from the given State to this State.
     */
    public State fill(State state) {
      background = state.background;
      foreground = state.foreground;
      clippingX = state.clippingX;
      clippingY = state.clippingY;
      clippingWidth = state.clippingWidth;
      clippingHeight = state.clippingHeight;
      lineStyle = state.lineStyle;
      lineWidth = state.lineWidth;
      translateX = state.translateX;
      translateY = state.translateY;
      font = state.font;
      xor = state.xor;
      return this;
    }

    /**
     * Copies all state information from the given SWT {@link GC} to this State.
     */
    public void fill(GC gc, Rectangle clipping) {
      background = gc.getBackground();
      foreground = gc.getForeground();
      clippingX = clipping.x;
      clippingY = clipping.y;
      clippingWidth = clipping.width;
      clippingHeight = clipping.height;
      font = gc.getFont();
      lineWidth = gc.getLineWidth();
      lineStyle = gc.getLineStyle();
      xor = gc.getXORMode();
    }

    public void fillTo(Graphics graphics) {
      graphics.setBackgroundColor(background);
      graphics.setForegroundColor(foreground);
      graphics.setLineStyle(lineStyle);
      graphics.setLineWidth(lineWidth);
      graphics.setFont(font);
      graphics.setXORMode(xor);
    }
  }
}