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
package org.eclipse.wb.internal.core.gef;

import org.eclipse.wb.core.gef.IEditPartConfigurator;
import org.eclipse.wb.core.gef.policy.DirectTextPropertyEditPolicy;
import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.internal.core.gef.policy.DblClickRunScriptEditPolicy;
import org.eclipse.wb.internal.core.gef.policy.FlipBooleanPropertyEditPolicy;
import org.eclipse.wb.internal.core.utils.state.GlobalState;

import org.apache.commons.lang.StringUtils;

/**
 * {@link IEditPartConfigurator} for any {@link EditPart}.
 *
 * @author scheglov_ke
 * @coverage core.gef
 */
public final class CoreEditPartConfigurator implements IEditPartConfigurator {
  public void configure(EditPart context, EditPart editPart) {
    Object model = editPart.getModel();
    // double click
    if (GlobalState.isComponent(model)) {
      ObjectInfo component = (ObjectInfo) model;
      configureFlipBooleanProperty(editPart, component);
      configure_onDoubleClick_runScript(editPart, component);
    }
    // direct edit
    if (model instanceof AbstractComponentInfo) {
      AbstractComponentInfo item = (AbstractComponentInfo) model;
      DirectTextPropertyEditPolicy.install(editPart, item);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Allows to use <code>double-click.flipBooleanProperty</code> parameter to flip some boolean
   * property between <code>true/false</code> states, for example "expanded" property.
   */
  private void configureFlipBooleanProperty(EditPart editPart, ObjectInfo component) {
    String propertyPath =
        GlobalState.getParametersProvider().getParameter(
            component,
            "double-click.flipBooleanProperty");
    if (!StringUtils.isEmpty(propertyPath)) {
      editPart.installEditPolicy(new FlipBooleanPropertyEditPolicy(component, propertyPath));
    }
  }

  /**
   * If has "double-click.runScript", then run this MVEL script with component as context.
   */
  private void configure_onDoubleClick_runScript(EditPart editPart, ObjectInfo component) {
    String propertyPath =
        GlobalState.getParametersProvider().getParameter(component, "double-click.runScript");
    if (!StringUtils.isEmpty(propertyPath)) {
      editPart.installEditPolicy(new DblClickRunScriptEditPolicy(component, propertyPath));
    }
  }
}
