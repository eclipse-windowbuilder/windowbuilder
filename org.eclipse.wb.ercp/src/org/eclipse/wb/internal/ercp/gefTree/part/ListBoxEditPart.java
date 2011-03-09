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
package org.eclipse.wb.internal.ercp.gefTree.part;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.internal.ercp.gefTree.policy.ListBoxLayoutEditPolicy;
import org.eclipse.wb.internal.ercp.model.widgets.mobile.ListBoxInfo;
import org.eclipse.wb.internal.swt.gefTree.part.CompositeEditPart;

/**
 * {@link EditPart} for {@link ListBoxInfo}.
 * 
 * @author lobas_av
 * @coverage swt.gef.part
 */
public final class ListBoxEditPart extends CompositeEditPart {
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
  protected void createEditPolicies() {
    super.createEditPolicies();
    installEditPolicy(EditPolicy.LAYOUT_ROLE, new ListBoxLayoutEditPolicy(m_listBox));
  }
}
