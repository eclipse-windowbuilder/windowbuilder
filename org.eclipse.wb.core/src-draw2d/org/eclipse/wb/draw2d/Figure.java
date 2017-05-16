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

import org.eclipse.wb.draw2d.border.Border;
import org.eclipse.wb.draw2d.events.IAncestorListener;
import org.eclipse.wb.draw2d.events.IFigureListener;
import org.eclipse.wb.draw2d.events.IMouseListener;
import org.eclipse.wb.draw2d.events.IMouseMoveListener;
import org.eclipse.wb.draw2d.events.IMouseTrackListener;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.draw2d.FigureCanvas;
import org.eclipse.wb.internal.draw2d.FigureVisitor;
import org.eclipse.wb.internal.draw2d.ICustomTooltipProvider;
import org.eclipse.wb.internal.draw2d.events.AncestorEventTable;
import org.eclipse.wb.internal.draw2d.events.EventTable;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;

import java.util.Collections;
import java.util.List;

/**
 * A lightweight graphical representation. Figures are rendered to a {@link Graphics} object.
 * Figures can be composed to create complex renderings.
 *
 * @author lobas_av
 * @coverage gef.draw2d
 */
public class Figure {
  private EventTable m_eventTable;
  private AncestorEventTable m_ancestorEventTable;
  private Figure m_parent;
  private final Rectangle m_bounds = new Rectangle();
  private List<Figure> m_children;
  private Border m_border;
  private Color m_background;
  private Color m_foreground;
  private Font m_font = Display.getCurrent().getSystemFont();
  private Cursor m_cursor;
  private boolean m_opaque;
  private boolean m_visible = true;
  private Object m_data;
  private String m_toolTipText;
  private ICustomTooltipProvider m_customTooltipProvider;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Events: mouse
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Registers the given listener as a {@link IMouseListener} of this {@link Figure}.
   */
  public void addMouseListener(IMouseListener listener) {
    getEnsureEventTable().addListener(IMouseListener.class, listener);
  }

  /**
   * Unregisters the given listener, so that it will no longer receive notification of mouse events.
   */
  public void removeMouseListener(IMouseListener listener) {
    getEnsureEventTable().removeListener(IMouseListener.class, listener);
  }

  /**
   * Registers the given listener as a {@link IMouseMoveListener} of this {@link Figure}.
   */
  public void addMouseMoveListener(IMouseMoveListener listener) {
    getEnsureEventTable().addListener(IMouseMoveListener.class, listener);
  }

  /**
   * Unregisters the given listener, so that it will no longer receive notification of mouse move
   * events.
   */
  public void removeMouseMoveListener(IMouseMoveListener listener) {
    getEnsureEventTable().removeListener(IMouseMoveListener.class, listener);
  }

  /**
   * Registers the given listener as a {@link IMouseTrackListener} of this {@link Figure}.
   */
  public void addMouseTrackListener(IMouseTrackListener listener) {
    getEnsureEventTable().addListener(IMouseTrackListener.class, listener);
  }

  /**
   * Unregisters the given listener, so that it will no longer receive notification of mouse move
   * events.
   */
  public void removeMouseTrackListener(IMouseTrackListener listener) {
    getEnsureEventTable().removeListener(IMouseTrackListener.class, listener);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Events: ancestor
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Registers the given listener as an {@link IAncestorListener} of this {@link Figure}.
   */
  public void addAncestorListener(IAncestorListener listener) {
    if (m_ancestorEventTable == null) {
      m_ancestorEventTable = new AncestorEventTable(this);
    }
    //
    m_ancestorEventTable.addAncestorListener(listener);
  }

  /**
   * Unregisters the given listener, so that it will no longer receive notification of ancestor
   * events.
   */
  public void removeAncestorListener(IAncestorListener listener) {
    if (m_ancestorEventTable != null) {
      m_ancestorEventTable.removeAncestorListener(listener);
      //
      if (m_ancestorEventTable.isEmpty()) {
        m_ancestorEventTable.unhookFigure();
        m_ancestorEventTable = null;
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Events: figure
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Registers the given listener as a {@link IFigureListener} of this {@link Figure}.
   */
  public void addFigureListener(IFigureListener listener) {
    getEnsureEventTable().addListener(IFigureListener.class, listener);
  }

  /**
   * Unregisters the given listener, so that it will no longer receive notification of
   * {@link Figure} events.
   */
  public void removeFigureListener(IFigureListener listener) {
    getEnsureEventTable().removeListener(IFigureListener.class, listener);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Events support
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Return all registers listeners for given class or <code>null</code>.
   */
  public <T extends Object> List<T> getListeners(Class<T> listenerClass) {
    return m_eventTable == null ? null : m_eventTable.getListeners(listenerClass);
  }

  /**
   * Access to <code>{@link EventTable}</code> use lazy creation mechanism.
   */
  private EventTable getEnsureEventTable() {
    if (m_eventTable == null) {
      m_eventTable = new EventTable();
    }
    return m_eventTable;
  }

  /**
   * Notifies any {@link IFigureListener IFigureListeners} listening to this {@link Figure} that it
   * has moved.
   */
  private void fireMoved() {
    List<IFigureListener> listeners = getListeners(IFigureListener.class);
    if (listeners != null) {
      for (IFigureListener figureListener : listeners) {
        figureListener.figureMoved(this);
      }
    }
  }

  /**
   * Notifies any {@link IFigureListener IFigureListeners} listening to this {@link Figure} that it
   * has set new parent.
   */
  private void fireReparent(Figure oldParent, Figure newParent) {
    List<IFigureListener> listeners = getListeners(IFigureListener.class);
    if (listeners != null) {
      for (IFigureListener figureListener : listeners) {
        figureListener.figureReparent(this, oldParent, newParent);
      }
    }
  }

  /**
   * Called after the receiver's parent has been set and it has been added to its parent.
   */
  protected void addNotify() {
    for (Figure childFigure : getChildren()) {
      childFigure.addNotify();
    }
  }

  /**
   * Called prior to this figure's removal from its parent.
   */
  protected void removeNotify() {
    for (Figure childFigure : getChildren()) {
      childFigure.removeNotify();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visiting
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Visits this {@link Figure} and its children using given {@link FigureVisitor}.
   */
  public final void accept(FigureVisitor visitor, boolean forward) {
    if (visitor.visit(this)) {
      List<Figure> children = getChildren();
      int size = children.size();
      //
      if (forward) {
        for (int i = 0; i < size; i++) {
          Figure childFigure = children.get(i);
          childFigure.accept(visitor, forward);
        }
      } else {
        for (int i = size - 1; i >= 0; i--) {
          Figure childFigure = children.get(i);
          childFigure.accept(visitor, forward);
        }
      }
      visitor.endVisit(this);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parent/Children
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds the given Figure as a child of this Figure.
   */
  public void add(Figure childFigure) {
    add(childFigure, null, -1);
  }

  /**
   * Adds the given Figure as a child of this Figure at the given index.
   */
  public void add(Figure childFigure, int index) {
    add(childFigure, null, index);
  }

  /**
   * Adds the given Figure as a child of this Figure with the given bounds.
   */
  public void add(Figure childFigure, Rectangle bounds) {
    add(childFigure, bounds, -1);
  }

  /**
   * Adds the child Figure using the specified index and bounds.
   */
  public void add(Figure childFigure, Rectangle bounds, int index) {
    // check figure parent
    if (childFigure.getParent() != null) {
      throw new IllegalArgumentException("Figure.add(...) Figure already added to parent");
    }
    // check figure
    for (Figure f = this; f != null; f = f.getParent()) {
      if (childFigure == f) {
        throw new IllegalArgumentException("IWAG0002E Figure.add(...) Cycle created in figure heirarchy");
      }
    }
    // check container
    if (m_children == null) {
      m_children = Lists.newArrayList();
    }
    // check index
    if (index < -1 || index > m_children.size()) {
      throw new IndexOutOfBoundsException("IWAG0001E Figure.add(...) invalid index");
    }
    // add to child list
    if (index == -1) {
      m_children.add(childFigure);
    } else {
      m_children.add(index, childFigure);
    }
    // set parent
    childFigure.setParent(this);
    // notify child of change
    childFigure.addNotify();
    // set bounds
    if (bounds != null) {
      childFigure.setBounds(bounds);
    }
    // notify of change
    resetState(childFigure);
  }

  /**
   * @return the {@link List} of children {@link Figure}'s.
   */
  public List<Figure> getChildren() {
    return m_children == null ? Collections.<Figure>emptyList() : m_children;
  }

  /**
   * Removes the given child Figure from this Figure's hierarchy.
   */
  public void remove(Figure childFigure) {
    // check child
    if (m_children == null || m_children.isEmpty()) {
      throw new IllegalArgumentException("This parent is empty");
    }
    if (childFigure.getParent() != this || !m_children.contains(childFigure)) {
      throw new IllegalArgumentException("IWAG0003E Figure is not a child of this parent");
    }
    // notify child of change
    childFigure.removeNotify();
    // remove child
    m_children.remove(childFigure);
    childFigure.setParent(null);
    // notify of change
    resetState(childFigure);
  }

  /**
   * Removes all children from this Figure.
   */
  public void removeAll() {
    // remove all children
    for (Figure childFigure : getChildren()) {
      childFigure.removeNotify();
      childFigure.setParent(null);
    }
    // notify of change
    if (m_children != null && !m_children.isEmpty()) {
      resetState();
    }
    // reset container
    m_children = null;
  }

  /**
   * @return the {@link FigureCanvas} that contains this {@link Figure}.
   * @noreference @nooverride
   */
  public FigureCanvas getFigureCanvas() {
    return m_parent.getFigureCanvas();
  }

  /**
   * Returns the Figure that is the current parent of this Figure or <code>null</code> if there is
   * no parent.
   */
  public Figure getParent() {
    return m_parent;
  }

  /**
   * Sets this Figure's parent.
   */
  public void setParent(Figure parent) {
    Figure oldParent = m_parent;
    m_parent = parent;
    // send reparent event
    fireReparent(oldParent, m_parent);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Figure
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Updates the cursor.
   */
  protected void updateCursor() {
    Figure parent = getParent();
    if (parent != null && isVisible()) {
      parent.updateCursor();
    }
  }

  /**
   * If the argument is <code>true</code>, causes the receiver to have all mouse events delivered to
   * it until the method is called with <code>false</code> as the argument.
   */
  protected final void setCapture(boolean capture) {
    if (capture) {
      setCapture(this);
    } else {
      setCapture(null);
    }
  }

  /**
   * Sets capture to the given figure.
   *
   * @noreference @nooverride
   */
  public void setCapture(Figure figure) {
    Figure parent = getParent();
    if (parent != null) {
      parent.setCapture(figure);
    }
  }

  /**
   * Request of change bounds and repaint this Figure. Use when bounds change is indirectly (change
   * visible, font, etc.).
   */
  public final void resetState() {
    resetState(getBounds());
  }

  /**
   * Request of change bounds and repaints the rectangular area within this Figure. Use into
   * <code>setBounds()</code> where <code>area</code> is union of <i>old</i> and <i>new</i> bounds.
   */
  public final void resetState(Rectangle area) {
    if (isVisible()) {
      repaint(true, area.x, area.y, area.width, area.height);
    }
  }

  /**
   * Request of change bounds and repaints the rectangular area within this Figure. Rectangular area
   * <code>childFigure</code>'s bounds. Use into {@link #add(Figure, Rectangle, int)} and
   * {@link #remove(Figure)}.
   */
  protected final void resetState(Figure childFigure) {
    if (isVisible() && childFigure.isVisible()) {
      Rectangle bounds = getBounds();
      Insets insets = getInsets();
      Rectangle childBounds = childFigure.getBounds();
      repaint(
          true,
          bounds.x + insets.left + childBounds.x,
          bounds.y + insets.top + childBounds.y,
          childBounds.width,
          childBounds.height);
    }
  }

  /**
   * Repaints this Figure.
   */
  public final void repaint() {
    if (isVisible()) {
      Rectangle bounds = getBounds();
      repaint(false, bounds.x, bounds.y, bounds.width, bounds.height);
    }
  }

  /**
   * Repaints the rectangular area within this Figure whose upper-left corner is located at the
   * point <code>(x,y)</code> and whose width and height are <code>w</code> and <code>h</code>,
   * respectively. If parameter <code>reset</code> is <code>true</code> then request of change
   * bounds and reconfigure scrolling.
   */
  protected void repaint(boolean reset, int x, int y, int width, int height) {
    Figure parent = getParent();
    if (parent != null) {
      Rectangle bounds = parent.getBounds();
      Insets insets = parent.getInsets();
      parent.repaint(reset, bounds.x + insets.left + x, bounds.y + insets.top + y, width, height);
    }
  }

  /**
   * Paints this Figure and its children.
   *
   * @noreference @nooverride
   */
  public final void paint(Graphics graphics) {
    // set figure state
    if (m_background != null) {
      graphics.setBackgroundColor(m_background);
    }
    if (m_foreground != null) {
      graphics.setForegroundColor(m_foreground);
    }
    if (m_font != null) {
      graphics.setFont(m_font);
    }
    graphics.pushState();
    //
    try {
      // paint figure
      paintFigure(graphics);
      // paint all children
      paintChildren(graphics);
      // paint border
      paintBorder(graphics);
    } finally {
      graphics.popState();
    }
  }

  private void paintFigure(Graphics graphics) {
    // fill all figure before any painting clientArea, clilds, and border.
    if (m_opaque) {
      Rectangle bounds = getBounds();
      graphics.fillRectangle(0, 0, bounds.width, bounds.height);
    }
    //
    Insets insets = getInsets();
    graphics.translate(insets.left, insets.top);
    paintClientArea(graphics);
    graphics.restoreState();
  }

  private void paintChildren(Graphics graphics) {
    List<Figure> children = getChildren();
    if (children.isEmpty()) {
      return;
    }
    // set children state
    Insets insets = getInsets();
    graphics.translate(insets.left, insets.top);
    graphics.pushState();
    // paint all
    for (Figure childFigure : children) {
      if (childFigure.isVisible() && childFigure.intersects(graphics.getClip())) {
        Rectangle childBounds = childFigure.getBounds();
        graphics.clipRect(childBounds);
        graphics.translate(childBounds.x, childBounds.y);
        childFigure.paint(graphics);
        graphics.restoreState();
      }
    }
    // reset state
    graphics.popState();
    graphics.restoreState();
  }

  /**
   * Paints this Figure's primary representation.
   */
  protected void paintClientArea(Graphics graphics) {
  }

  /**
   * Paints the border associated with this Figure, if one exists.
   */
  protected void paintBorder(Graphics graphics) {
    if (m_border != null) {
      m_border.paint(this, graphics);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Bounds
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns the current location.
   */
  public Point getLocation() {
    return m_bounds.getLocation();
  }

  /**
   * Returns the current size.
   */
  public Dimension getSize() {
    return m_bounds.getSize();
  }

  /**
   * Returns the smallest rectangle completely enclosing the figure. Returns Reactangle by
   * reference. DO NOT Modify returned value.
   */
  public Rectangle getBounds() {
    return m_bounds;
  }

  /**
   * Sets the location of this Figure.
   */
  public void setLocation(int x, int y) {
    if (m_bounds.x != x || m_bounds.y != y) {
      setBounds(new Rectangle(getBounds()).setLocation(x, y));
    }
  }

  /**
   * Sets the location of this Figure.
   */
  public void setLocation(Point location) {
    setLocation(location.x, location.y);
  }

  /**
   * Sets this Figure's size.
   */
  public void setSize(int width, int height) {
    if (m_bounds.width != width || m_bounds.height != height) {
      setBounds(new Rectangle(getBounds()).setSize(width, height));
    }
  }

  /**
   * Sets this Figure's size.
   */
  public void setSize(Dimension size) {
    setSize(size.width, size.height);
  }

  /**
   * Sets the bounds of this Figure to the Rectangle <i>rect</i>.
   */
  public void setBounds(Rectangle bounds) {
    if (!m_bounds.equals(bounds)) {
      // calc repaint rectangle
      Rectangle dirtyArea = m_bounds.getUnion(bounds);
      // change bounds
      m_bounds.setBounds(bounds);
      // send move event
      fireMoved();
      // reset state
      resetState(dirtyArea);
    }
  }

  /**
   * Returns the border's Insets if the border is set. Otherwise returns ZERO_INSETS, an instance of
   * Insets with all 0s. Returns Insets by reference. DO NOT Modify returned value.
   */
  public Insets getInsets() {
    if (m_border == null) {
      return Insets.ZERO_INSETS;
    }
    return m_border.getInsets();
  }

  /**
   * Returns the rectangular area within this Figure's bounds in which children will be placed and
   * the painting of children will be clipped.
   */
  public Rectangle getClientArea() {
    return getClientArea(new Rectangle());
  }

  /**
   * Copies the client area into the specified {@link Rectangle}, and returns that rectangle for
   * convenience.
   */
  public Rectangle getClientArea(Rectangle rectangle) {
    rectangle.setBounds(getBounds());
    rectangle.crop(getInsets());
    rectangle.setLocation(0, 0);
    return rectangle;
  }

  /**
   * Returns <code>true</code> if this Figure's bounds intersect with the given Rectangle. Figure is
   * asked so that non-rectangular Figures can reduce the frequency of paints.
   */
  public boolean intersects(Rectangle rectangle) {
    return getBounds().intersects(rectangle);
  }

  /**
   * Returns <code>true</code> if the {@link Point} <code>p</code> is contained within this
   * {@link Figure}'s bounds.
   */
  public boolean containsPoint(Point point) {
    return containsPoint(point.x, point.y);
  }

  /**
   * Returns <code>true</code> if the point <code>(x, y)</code> is contained within this
   * {@link Figure}'s bounds.
   */
  public boolean containsPoint(int x, int y) {
    return getBounds().contains(x, y);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Get user define data.
   */
  public Object getData() {
    return m_data;
  }

  /**
   * Set user define data.
   */
  public void setData(Object data) {
    m_data = data;
  }

  /**
   * Returns the current border by reference.
   */
  public Border getBorder() {
    return m_border;
  }

  /**
   * Sets the border.
   */
  public void setBorder(Border border) {
    if (m_border != border) {
      m_border = border;
      resetState();
    }
  }

  /**
   * Returns the background Color of this Figure.
   */
  public Color getBackground() {
    return m_background;
  }

  /**
   * Sets the background color.
   */
  public void setBackground(Color background) {
    if (m_background != background) {
      m_background = background;
      repaint();
    }
  }

  /**
   * Returns the local foreground Color of this Figure.
   */
  public Color getForeground() {
    return m_foreground;
  }

  /**
   * Sets the foreground color.
   */
  public void setForeground(Color foreground) {
    if (m_foreground != foreground) {
      m_foreground = foreground;
      repaint();
    }
  }

  /**
   * Returns the current Font by reference.
   */
  public Font getFont() {
    return m_font;
  }

  /**
   * Sets the font.
   */
  public void setFont(Font font) {
    if (m_font != font) {
      m_font = font;
      resetState();
    }
  }

  /**
   * @return The Cursor used when the mouse is over this Figure
   */
  public Cursor getCursor() {
    return m_cursor;
  }

  /**
   * Sets the cursor.
   */
  public void setCursor(Cursor cursor) {
    if (m_cursor != cursor) {
      m_cursor = cursor;
      updateCursor();
    }
  }

  /**
   * Returns <code>true</code> if this Figure is opaque.
   */
  public boolean isOpaque() {
    return m_opaque;
  }

  /**
   * Sets this Figure to be opaque if <i>opaque</i> is <code>true</code> and transparent if
   * <i>opaque</i> is <code>false</code>.
   */
  public void setOpaque(boolean opaque) {
    if (m_opaque != opaque) {
      m_opaque = opaque;
      repaint();
    }
  }

  /**
   * @return <code>true</code> if the figure's visibility flag is set
   */
  public boolean isVisible() {
    return m_visible;
  }

  /**
   * Sets this Figure's visibility.
   */
  public void setVisible(boolean visible) {
    if (m_visible != visible) {
      resetState();
      m_visible = visible;
      resetState();
    }
  }

  /**
   * Returns the receiver's tool tip text, or <code>null</code> if it has not been set.
   */
  public String getToolTipText() {
    return m_toolTipText;
  }

  /**
   * Sets the receiver's tool tip text to the argument, which may be <code>null</code> indicating
   * that no tool tip text should be shown.
   */
  public void setToolTipText(String toolTipText) {
    m_toolTipText = toolTipText;
  }

  /**
   * @return custom tool tip provider {@link ICustomTooltipProvider}, or <code>null</code> if it has
   *         not been set.
   */
  public ICustomTooltipProvider getCustomTooltipProvider() {
    return m_customTooltipProvider;
  }

  /**
   * Sets the custom tool tip provider {@link ICustomTooltipProvider} to the argument, which may be
   * <code>null</code> indicating that no tool tip text should be shown.
   */
  public void setCustomTooltipProvider(ICustomTooltipProvider provider) {
    m_customTooltipProvider = provider;
  }
}