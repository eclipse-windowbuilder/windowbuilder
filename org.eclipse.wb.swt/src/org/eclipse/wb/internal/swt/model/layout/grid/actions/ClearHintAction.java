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
import org.eclipse.swt.SWT;

/**
 * {@link Action} for clearing horizontal/vertical hint.
 *
 * @author scheglov_ke
 * @coverage swt.model.layout
 */
public final class ClearHintAction extends AbstractAction {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ClearHintAction(IGridDataInfo gridData, String text, boolean horizontal) {
    super(gridData, text, AS_PUSH_BUTTON, null, horizontal);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Run
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void runEx() throws Exception {
    if (m_horizontal) {
      m_gridData.setWidthHint(SWT.DEFAULT);
    } else {
      m_gridData.setHeightHint(SWT.DEFAULT);
    }
  }
}