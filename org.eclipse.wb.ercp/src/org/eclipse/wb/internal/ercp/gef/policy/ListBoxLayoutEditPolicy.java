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
package org.eclipse.wb.internal.ercp.gef.policy;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.gef.policy.layout.flow.ObjectFlowLayoutEditPolicy;
import org.eclipse.wb.core.gef.policy.validator.LayoutRequestValidators;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.handles.MoveHandle;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.internal.ercp.model.widgets.mobile.ListBoxInfo;
import org.eclipse.wb.internal.ercp.model.widgets.mobile.ListBoxItemInfo;

import java.util.List;

/**
 * Implementation of {@link LayoutEditPolicy} for {@ListBoxInfo}.
 * 
 * @author lobas_av
 * @coverage swt.gef.policy
 */
public final class ListBoxLayoutEditPolicy extends ObjectFlowLayoutEditPolicy<ListBoxItemInfo> {
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
  // Decorate Childs
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void decorateChild(EditPart child) {
    child.installEditPolicy(EditPolicy.SELECTION_ROLE, new SelectionEditPolicy() {
      @Override
      protected List<Handle> createSelectionHandles() {
        List<Handle> handles = Lists.newArrayList();
        MoveHandle moveHandle = new MoveHandle(getHost());
        moveHandle.setForeground(IColorConstants.red);
        handles.add(moveHandle);
        return handles;
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Reference children
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final ILayoutRequestValidator VALIDATOR =
      LayoutRequestValidators.modelType(ListBoxItemInfo.class);

  @Override
  protected boolean isGoodReferenceChild(Request request, EditPart editPart) {
    return editPart.getModel() instanceof ListBoxItemInfo;
  }

  @Override
  protected ILayoutRequestValidator getRequestValidator() {
    return VALIDATOR;
  }

  @Override
  protected boolean isHorizontal(Request request) {
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void command_CREATE(ListBoxItemInfo newObject, ListBoxItemInfo referenceObject)
      throws Exception {
    m_listBox.add(newObject, referenceObject);
  }

  @Override
  protected void command_MOVE(ListBoxItemInfo object, ListBoxItemInfo referenceObject)
      throws Exception {
    m_listBox.move(object, referenceObject);
  }
}