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
package org.eclipse.wb.core.gef.figure;

import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.Layer;
import org.eclipse.wb.draw2d.border.Border;
import org.eclipse.wb.draw2d.border.CompoundBorder;
import org.eclipse.wb.draw2d.border.LineBorder;
import org.eclipse.wb.draw2d.border.MarginBorder;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.internal.draw2d.Label;
import org.eclipse.wb.internal.draw2d.VerticalLabel;

import org.eclipse.swt.graphics.Color;

/**
 * Helper for displaying text on filled rectangle.
 *
 * @author scheglov_ke
 * @coverage core.gef.figure
 */
public final class TextFeedback {
  private final Layer m_layer;
  private final Label m_label;
  private Dimension m_size;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TextFeedback(Layer layer, boolean isHorizontal) {
    m_layer = layer;
    // create label
    m_label = isHorizontal ? new Label() : new VerticalLabel();
    m_label.setOpaque(true);
    m_label.setBackground(IColorConstants.tooltipBackground);
    m_label.setForeground(IColorConstants.tooltipForeground);
    {
      Border outer = new LineBorder(IColorConstants.tooltipForeground);
      Border inner = new MarginBorder(2);
      m_label.setBorder(new CompoundBorder(outer, inner));
    }
  }

  public TextFeedback(Layer layer) {
    this(layer, true);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds feedback.
   */
  public void add() {
    m_layer.add(m_label);
  }

  /**
   * Removes feedback.
   */
  public void remove() {
    m_layer.remove(m_label);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the size of feedback.
   */
  public Dimension getSize() {
    return m_label.getSize().getCopy();
  }

  /**
   * Sets the feedback text.
   */
  public void setText(String text) {
    m_label.setText(text);
    m_label.setSize(m_label.getPreferredSize());
    m_size = m_label.getSize();
  }

  /**
   * Sets the background of feedback.
   */
  public void setBackground(Color color) {
    m_label.setBackground(color);
  }

  /**
   * Sets the location of feedback.
   */
  public void setLocation(Point location) {
    m_label.setLocation(location);
  }

  /**
   * Set user defined data.
   */
  public void setData(Object data) {
    m_label.setData(data);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Location
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Moves feedback to the location at X centered in range (x, x + w) and at Y above given 'y'
   * coordinate.
   */
  public void centerHorizontallyAbove(Rectangle target, int shift) {
    int x = target.x + (target.width - m_size.width) / 2;
    int y = target.y - m_size.height - shift;
    y = Math.max(y, 1);
    m_label.setLocation(x, y);
  }

  /**
   * Moves feedback to the location at X to the right from given target {@link Rectangle} and at
   * bottom side of feedback at bottom side of target {@link Rectangle}.
   */
  public void moveRightOuter(Rectangle target, int shift) {
    int x = target.right() + shift;
    int y = target.bottom() - m_size.height;
    m_label.setLocation(x, y);
  }

  /**
   * Moves feedback to the location at (locationX - textWidth / 2, locationY - textHeight / 2).
   */
  public void moveTopLeftCenter(Point location) {
    int x = location.x - m_size.width / 2;
    int y = location.y - m_size.height / 2;
    m_label.setLocation(x, y);
  }
}