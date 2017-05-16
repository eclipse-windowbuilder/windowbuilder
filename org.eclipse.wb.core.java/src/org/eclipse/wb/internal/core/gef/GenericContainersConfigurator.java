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
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.internal.core.gef.policy.layout.generic.FlowContainerLayoutEditPolicy;
import org.eclipse.wb.internal.core.gef.policy.layout.generic.SimpleContainerLayoutEditPolicy;
import org.eclipse.wb.internal.core.model.generic.FlowContainer;
import org.eclipse.wb.internal.core.model.generic.FlowContainerFactory;
import org.eclipse.wb.internal.core.model.generic.SimpleContainer;
import org.eclipse.wb.internal.core.model.generic.SimpleContainerFactory;

import java.util.List;

/**
 * Configures generic simple/flow containers behavior.
 *
 * @author scheglov_ke
 * @coverage core.gef
 */
public final class GenericContainersConfigurator implements IEditPartConfigurator {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IEditPartConfigurator
  //
  ////////////////////////////////////////////////////////////////////////////
  public void configure(EditPart context, EditPart editPart) {
    if (editPart.getModel() instanceof JavaInfo) {
      JavaInfo component = (JavaInfo) editPart.getModel();
      configureComponent(editPart, component);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Configuring
  //
  ////////////////////////////////////////////////////////////////////////////
  private void configureComponent(EditPart editPart, JavaInfo component) {
    configureSimpleContainer(editPart, component);
    configureFlowContainer(editPart, component);
  }

  /**
   * Simple containers: SimplePanel, CaptionPanel, etc.
   */
  private void configureSimpleContainer(EditPart editPart, JavaInfo component) {
    List<SimpleContainer> containers = new SimpleContainerFactory(component, true).get();
    for (SimpleContainer container : containers) {
      EditPolicy layoutPolicy = new SimpleContainerLayoutEditPolicy(component, container);
      editPart.installEditPolicy(container, layoutPolicy);
    }
  }

  /**
   * Flow containers: FlowPanel, HorizontalPanel, VerticalPanel, etc.
   */
  private void configureFlowContainer(EditPart editPart, JavaInfo component) {
    List<FlowContainer> containers = new FlowContainerFactory(component, true).get();
    for (FlowContainer container : containers) {
      EditPolicy layoutPolicy = new FlowContainerLayoutEditPolicy(component, container);
      editPart.installEditPolicy(container, layoutPolicy);
    }
  }
}
