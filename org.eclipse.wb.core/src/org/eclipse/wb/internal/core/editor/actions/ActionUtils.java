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
package org.eclipse.wb.internal.core.editor.actions;

import org.eclipse.wb.internal.core.DesignerPlugin;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;

/**
 * Utilities for Designer {@link Action}'s.
 *
 * @author scheglov_ke
 * @coverage core.editor.action
 */
public class ActionUtils {
  /**
   * Copies text, image, accelerator, etc from {@link ActionFactory} to target {@link IAction}.
   */
  public static void copyPresentation(IAction target, ActionFactory actionFactory) {
    IWorkbenchAction action = actionFactory.create(DesignerPlugin.getActiveWorkbenchWindow());
    try {
      target.setText(action.getText());
      target.setToolTipText(action.getToolTipText());
      target.setDescription(action.getDescription());
      //
      target.setImageDescriptor(action.getImageDescriptor());
      target.setDisabledImageDescriptor(action.getDisabledImageDescriptor());
      target.setHoverImageDescriptor(action.getHoverImageDescriptor());
      //
      target.setId(action.getId());
      target.setActionDefinitionId(action.getActionDefinitionId());
      target.setAccelerator(action.getAccelerator());
    } finally {
      action.dispose();
    }
  }
}
