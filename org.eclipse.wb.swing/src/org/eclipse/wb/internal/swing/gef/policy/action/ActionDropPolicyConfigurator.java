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
package org.eclipse.wb.internal.swing.gef.policy.action;

import org.eclipse.wb.core.gef.IEditPartConfigurator;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.internal.swing.model.bean.ActionInfo;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

import javax.swing.AbstractButton;

/**
 * Contributes {@link ActionInfo} related {@link EditPolicy}'s.
 * 
 * @author scheglov_ke
 * @coverage swing.gef.policy
 */
public final class ActionDropPolicyConfigurator implements IEditPartConfigurator {
  public void configure(EditPart context, EditPart editPart) {
    // drop ActionInfo on javax.swing.AbstractButton
    if (editPart.getModel() instanceof ComponentInfo) {
      ComponentInfo component = (ComponentInfo) editPart.getModel();
      if (AbstractButton.class.isAssignableFrom(component.getDescription().getComponentClass())) {
        editPart.installEditPolicy(new ActionDropButtonLayoutEditPolicy(component));
      }
    }
  }
}
