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
package org.eclipse.wb.internal.swing.gef.part.box;

import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.internal.swing.gef.part.ComponentEditPart;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

import org.eclipse.swt.graphics.Color;

import javax.swing.Box;

/**
 * The {@link EditPart} for horizontal/vertical glue from {@link Box}.
 * 
 * @author scheglov_ke
 * @coverage swing.gef.part
 */
abstract class BoxEditPart extends ComponentEditPart {
  protected static final Color COLOR_BORDER = new Color(null, 45, 90, 150);
  protected static final Color COLOR_SPRING = new Color(null, 40, 110, 200);
  protected static final int SPRING_SIZE = 12;
  protected final ComponentInfo m_component;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BoxEditPart(ComponentInfo component) {
    super(component);
    m_component = component;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Figure
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refreshVisuals() {
    Rectangle bounds = m_component.getBounds();
    if (bounds.width != 0 && bounds.height == 0) {
      bounds.height = 12;
    }
    if (bounds.height != 0 && bounds.width == 0) {
      bounds.width = 12;
    }
    getFigure().setBounds(bounds);
  }
}
