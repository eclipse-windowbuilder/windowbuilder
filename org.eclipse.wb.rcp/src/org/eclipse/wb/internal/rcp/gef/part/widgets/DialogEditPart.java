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
package org.eclipse.wb.internal.rcp.gef.part.widgets;

import org.eclipse.wb.core.gef.part.AbstractComponentEditPart;
import org.eclipse.wb.core.gef.policy.selection.TopSelectionEditPolicy;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.internal.rcp.model.widgets.DialogInfo;

/**
 * {@link EditPart} for {@link DialogInfo}.
 * 
 * @author scheglov_ke
 * @author sablin_aa
 * @coverage rcp.gef.part
 */
public class DialogEditPart extends AbstractComponentEditPart {
  private final DialogInfo m_dialog;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DialogEditPart(DialogInfo dialog) {
    super(dialog);
    m_dialog = dialog;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Policies
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refreshEditPolicies() {
    super.refreshEditPolicies();
    // injecting into main {@link ShellEditPart} a {@link TopSelectionEditPolicy}. 
    for (EditPart child : getChildren()) {
      if (child.getModel() == m_dialog.getShellInfo()) {
        child.installEditPolicy(EditPolicy.SELECTION_ROLE, new TopSelectionEditPolicy(m_dialog));
      }
    }
  }
}
