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

import org.eclipse.wb.core.gef.IEditPartConfigurator;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.internal.ercp.ToolkitProvider;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

/**
 * Contributes {@link CommandDropLayoutEditPolicy} to the {@link ControlInfo}.
 * 
 * @author scheglov_ke
 * @coverage swt.gef.policy
 */
public final class CommandDropPolicyConfigurator implements IEditPartConfigurator {
  public void configure(EditPart context, EditPart editPart) {
    if (editPart.getModel() instanceof ControlInfo) {
      ControlInfo control = (ControlInfo) editPart.getModel();
      if (control.getDescription().getToolkit() == ToolkitProvider.DESCRIPTION) {
        editPart.installEditPolicy(new CommandDropLayoutEditPolicy(control));
      }
    }
  }
}
