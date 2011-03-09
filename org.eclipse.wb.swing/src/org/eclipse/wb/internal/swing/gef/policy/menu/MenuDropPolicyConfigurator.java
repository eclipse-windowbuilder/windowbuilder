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
package org.eclipse.wb.internal.swing.gef.policy.menu;

import org.eclipse.wb.core.gef.IEditPartConfigurator;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import javax.swing.JApplet;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;

/**
 * Contributes menu related {@link EditPolicy}'s.
 * 
 * @author scheglov_ke
 * @coverage swing.gef.policy
 */
public final class MenuDropPolicyConfigurator implements IEditPartConfigurator {
  public void configure(EditPart context, EditPart editPart) {
    // drop JMenuBar on JFrame, JDialog and JApply
    if (editPart.getModel() instanceof ContainerInfo) {
      ContainerInfo container = (ContainerInfo) editPart.getModel();
      Class<?> componentClass = container.getDescription().getComponentClass();
      if (JFrame.class.isAssignableFrom(componentClass)
          || JInternalFrame.class.isAssignableFrom(componentClass)
          || JDialog.class.isAssignableFrom(componentClass)
          || JApplet.class.isAssignableFrom(componentClass)) {
        editPart.installEditPolicy(new MenuBarDropLayoutEditPolicy(container));
      }
    }
    // drop JPopupMenu on any java.awt.Component
    if (editPart.getModel() instanceof ComponentInfo) {
      ComponentInfo component = (ComponentInfo) editPart.getModel();
      editPart.installEditPolicy(new MenuPopupDropLayoutEditPolicy(component));
    }
  }
}
