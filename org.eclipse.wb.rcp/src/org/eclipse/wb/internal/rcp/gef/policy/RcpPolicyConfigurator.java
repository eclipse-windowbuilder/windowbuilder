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
package org.eclipse.wb.internal.rcp.gef.policy;

import org.eclipse.wb.core.gef.IEditPartConfigurator;
import org.eclipse.wb.core.gef.part.menu.IMenuObjectEditPart;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.internal.rcp.gef.policy.jface.ControlDecorationDropLayoutEditPolicy;
import org.eclipse.wb.internal.rcp.gef.policy.jface.FieldEditorDropRequestProcessor;
import org.eclipse.wb.internal.rcp.gef.policy.jface.action.ActionDropRequestProcessor;
import org.eclipse.wb.internal.rcp.model.jface.action.MenuManagerInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

/**
 * Configures RCP related {@link EditPart}'s.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.policy
 */
public final class RcpPolicyConfigurator implements IEditPartConfigurator {
  public void configure(EditPart context, EditPart editPart) {
    editPart.addRequestProcessor(FieldEditorDropRequestProcessor.INSTANCE);
    // allow drop Action on MenuManager
    if (editPart instanceof IMenuObjectEditPart) {
      IMenuObjectEditPart menuEditPart = (IMenuObjectEditPart) editPart;
      if (menuEditPart.getMenuModel().getToolkitModel() instanceof MenuManagerInfo) {
        editPart.addRequestProcessor(ActionDropRequestProcessor.INSTANCE);
      }
    }
    // allow drop ControlDecoration on ControlInfo
    if (editPart.getModel() instanceof ControlInfo) {
      ControlInfo control = (ControlInfo) editPart.getModel();
      editPart.installEditPolicy(new ControlDecorationDropLayoutEditPolicy(control));
    }
  }
}
