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
package org.eclipse.wb.internal.swing.model.layout.gbl.actions;

import org.eclipse.wb.internal.swing.model.layout.gbl.AbstractGridBagConstraintsInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.RowInfo;

import org.eclipse.jface.action.Action;

/**
 * {@link Action} for modifying vertical alignment.
 * 
 * @author scheglov_ke
 * @coverage swing.model.layout
 */
public final class SetAlignmentVerticalAction extends AbstractAction {
  private final RowInfo.Alignment m_alignment;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SetAlignmentVerticalAction(AbstractGridBagConstraintsInfo constraints,
      String text,
      String iconPath,
      RowInfo.Alignment alignment) {
    super(constraints, text, AS_RADIO_BUTTON, iconPath, false);
    m_alignment = alignment;
    // set check for current alignment
    setChecked(constraints.getVerticalAlignment() == m_alignment);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Run
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void runEx() throws Exception {
    m_constraints.setVerticalAlignment(m_alignment);
  }
}