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
package org.eclipse.wb.internal.ercp.gefTree.policy;

import org.eclipse.wb.core.gefTree.policy.ObjectLayoutEditPolicy;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.ercp.model.widgets.mobile.ListBoxInfo;
import org.eclipse.wb.internal.ercp.model.widgets.mobile.ListBoxItemInfo;

/**
 * Implementation of {@link LayoutEditPolicy} for {@ListBoxInfo}.
 * 
 * @author lobas_av
 * @coverage swt.gef.policy
 */
public class ListBoxLayoutEditPolicy extends ObjectLayoutEditPolicy<ListBoxItemInfo> {
  private final ListBoxInfo m_listBox;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ListBoxLayoutEditPolicy(ListBoxInfo listBox) {
    super(listBox);
    m_listBox = listBox;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Reference children
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean isGoodReferenceChild(Request request, EditPart editPart) {
    return editPart.getModel() instanceof ListBoxItemInfo;
  }

  @Override
  protected ILayoutRequestValidator getRequestValidator() {
    return org.eclipse.wb.internal.ercp.gef.policy.ListBoxLayoutEditPolicy.VALIDATOR;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void command_CREATE(ListBoxItemInfo item, ListBoxItemInfo nextItem) throws Exception {
    m_listBox.add(item, nextItem);
  }

  @Override
  protected void command_MOVE(ListBoxItemInfo item, ListBoxItemInfo nextItem) throws Exception {
    m_listBox.move(item, nextItem);
  }
}