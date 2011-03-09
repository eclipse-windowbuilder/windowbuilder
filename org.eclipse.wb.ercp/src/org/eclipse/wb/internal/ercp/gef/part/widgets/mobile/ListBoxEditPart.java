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
package org.eclipse.wb.internal.ercp.gef.part.widgets.mobile;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.internal.ercp.gef.policy.ListBoxLayoutEditPolicy;
import org.eclipse.wb.internal.ercp.model.widgets.mobile.ListBoxInfo;
import org.eclipse.wb.internal.swt.gef.part.ControlEditPart;

/**
 * {@link EditPart} for {@link ListBoxInfo}.
 * 
 * @author lobas_av
 * @coverage swt.gef.part
 */
public final class ListBoxEditPart extends ControlEditPart {
  private final ListBoxInfo m_listBox;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ListBoxEditPart(ListBoxInfo listBox) {
    super(listBox);
    m_listBox = listBox;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Policies
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refreshEditPolicies() {
    super.refreshEditPolicies();
    installEditPolicy(EditPolicy.LAYOUT_ROLE, new ListBoxLayoutEditPolicy(m_listBox));
  }
}