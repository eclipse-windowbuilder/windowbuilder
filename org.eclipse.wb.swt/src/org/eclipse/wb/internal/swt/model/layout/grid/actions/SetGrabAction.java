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
package org.eclipse.wb.internal.swt.model.layout.grid.actions;

import org.eclipse.wb.internal.swt.model.layout.grid.IGridDataInfo;

import org.eclipse.jface.action.Action;

/**
 * {@link Action} for modifying horizontal/vertical grab.
 * 
 * @author scheglov_ke
 * @coverage swt.model.layout
 */
public final class SetGrabAction extends AbstractAction {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SetGrabAction(IGridDataInfo gridData, String text, String iconPath, boolean horizontal) {
    super(gridData, text, AS_CHECK_BOX, iconPath, horizontal);
    setChecked(horizontal ? gridData.getHorizontalGrab() : gridData.getVerticalGrab());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Run
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void runEx() throws Exception {
    if (m_horizontal) {
      m_gridData.setHorizontalGrab(!m_gridData.getHorizontalGrab());
    } else {
      m_gridData.setVerticalGrab(!m_gridData.getVerticalGrab());
    }
  }
}