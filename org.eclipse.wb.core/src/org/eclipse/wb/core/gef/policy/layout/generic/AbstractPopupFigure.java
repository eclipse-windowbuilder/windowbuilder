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
package org.eclipse.wb.core.gef.policy.layout.generic;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.Graphics;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.ICursorConstants;
import org.eclipse.wb.draw2d.events.IMouseListener;
import org.eclipse.wb.draw2d.events.MouseEvent;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.internal.core.utils.ui.DrawUtils;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

/**
 * Abstract {@link Figure} that shows images of fixed size and open popup menu on click.
 *
 * @author scheglov_ke
 * @coverage core.gef.policy.generic
 */
public abstract class AbstractPopupFigure extends Figure {
  private static final int MARGIN = 6;
  private static final Color COLOR_BACKGROUND = DrawUtils.getShiftedColor(
      IColorConstants.white,
      -32);
  private static final Color COLOR_FOREGROUND = DrawUtils.getShiftedColor(
      IColorConstants.white,
      -64);
  private final IEditPartViewer m_viewer;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates new {@link AbstractPopupFigure}.
   *
   * @param width
   *          the width of the image.
   * @param height
   *          the height of the image.
   */
  public AbstractPopupFigure(IEditPartViewer viewer, int width, int height) {
    m_viewer = viewer;
    // configure figure
    setSize(width + MARGIN, height + MARGIN);
    setBackground(COLOR_BACKGROUND);
    setForeground(COLOR_FOREGROUND);
    setCursor(ICursorConstants.HAND);
    // add mouse listener
    addMouseListener(new IMouseListener() {
      public void mouseDown(MouseEvent event) {
        event.consume();
        // prepare IMenuManager
        MenuManager manager = new MenuManager();
        fillMenu(manager);
        // open context menu
        Control control = m_viewer.getControl();
        Menu menu = manager.createContextMenu(control);
        menu.setVisible(true);
      }

      public void mouseUp(MouseEvent event) {
      }

      public void mouseDoubleClick(MouseEvent event) {
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Paint
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void paintClientArea(Graphics graphics) {
    Rectangle clientArea = getClientArea();
    // draw filled rectangle
    graphics.fillRectangle(clientArea);
    graphics.drawRectangle(clientArea.getResized(-1, -1));
    // draw image
    {
      Image image = getImage();
      if (image != null) {
        org.eclipse.swt.graphics.Rectangle imageBounds = image.getBounds();
        int x = (clientArea.width - imageBounds.width) / 2;
        int y = (clientArea.height - imageBounds.height) / 2;
        graphics.drawImage(image, x, y);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Abstract methods
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the image to display.
   */
  protected abstract Image getImage();

  /**
   * Creates the actions on given {@link IMenuManager}.
   */
  protected abstract void fillMenu(IMenuManager manager);
}
