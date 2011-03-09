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
package org.eclipse.wb.internal.swing.gef.policy.layout;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.Graphics;
import org.eclipse.wb.draw2d.events.IMouseListener;
import org.eclipse.wb.draw2d.events.MouseEvent;
import org.eclipse.wb.draw2d.geometry.Rectangle;

import org.eclipse.swt.graphics.Image;

/**
 * Figure for change current card selection component.
 * 
 * @author lobas_av
 * @coverage swing.gef.policy
 */
public final class CardNavigationFigure extends Figure {
  public static final int WIDTH = 10;
  public static final int HEIGHT = 14;
  private static Image m_prevImage = new Image(null,
      CardNavigationFigure.class.getResourceAsStream("prev.png"));
  private static Image m_nextImage = new Image(null,
      CardNavigationFigure.class.getResourceAsStream("next.png"));
  private final CardLayoutSelectionEditPolicy m_policy;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CardNavigationFigure(CardLayoutSelectionEditPolicy policy) {
    m_policy = policy;
    addMouseListener(new IMouseListener() {
      public void mouseUp(MouseEvent event) {
      }

      public void mouseDown(MouseEvent event) {
        event.consume();
        if (event.x < WIDTH) {
          m_policy.showPrevComponent();
        } else {
          m_policy.showNextComponent();
        }
      }

      public void mouseDoubleClick(MouseEvent event) {
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Figure
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void paintClientArea(Graphics graphics) {
    Rectangle r = getClientArea();
    graphics.drawImage(m_prevImage, r.x, r.y);
    graphics.drawImage(m_nextImage, r.x + WIDTH, r.y);
  }
}