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
import org.eclipse.wb.internal.swing.model.layout.gbl.DimensionInfo;

import org.eclipse.jface.action.Action;

/**
 * {@link Action} for that sets weight for {@link DimensionInfo}.
 * 
 * @author scheglov_ke
 * @coverage swing.model.layout
 */
public final class SetGrowAction extends AbstractAction {
  private final DimensionInfo m_dimension;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SetGrowAction(AbstractGridBagConstraintsInfo constraints,
      String text,
      String iconPath,
      boolean horizontal) {
    super(constraints, text, AS_CHECK_BOX, iconPath, horizontal);
    m_dimension = horizontal ? constraints.getColumn() : constraints.getRow();
    setChecked(m_dimension.hasWeight());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Run
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void runEx() throws Exception {
    m_dimension.setWeight(m_dimension.hasWeight() ? 0.0 : 1.0);
  }
}