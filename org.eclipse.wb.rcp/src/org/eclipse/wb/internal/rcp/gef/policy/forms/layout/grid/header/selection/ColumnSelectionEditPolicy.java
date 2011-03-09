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
package org.eclipse.wb.internal.rcp.gef.policy.forms.layout.grid.header.selection;

import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.internal.rcp.gef.policy.forms.layout.grid.header.edit.ColumnHeaderEditPart;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

/**
 * Implementation of {@link SelectionEditPolicy} for {@link ColumnHeaderEditPart}.
 * 
 * @author scheglov_ke
 * @coverage rcp.gef.policy
 */
public final class ColumnSelectionEditPolicy<C extends IControlInfo>
    extends
      DimensionSelectionEditPolicy<C> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ColumnSelectionEditPolicy(LayoutEditPolicy mainPolicy) {
    super(mainPolicy);
  }
}
