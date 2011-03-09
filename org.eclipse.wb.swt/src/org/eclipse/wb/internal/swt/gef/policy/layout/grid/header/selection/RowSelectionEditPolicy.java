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
package org.eclipse.wb.internal.swt.gef.policy.layout.grid.header.selection;

import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.internal.swt.gef.policy.layout.grid.header.edit.RowHeaderEditPart;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

/**
 * Implementation of {@link SelectionEditPolicy} for {@link RowHeaderEditPart}.
 * 
 * @author scheglov_ke
 * @coverage swt.gef.GridLayout
 */
public final class RowSelectionEditPolicy<C extends IControlInfo>
    extends
      DimensionSelectionEditPolicy<C> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public RowSelectionEditPolicy(LayoutEditPolicy mainPolicy) {
    super(mainPolicy);
  }
}
