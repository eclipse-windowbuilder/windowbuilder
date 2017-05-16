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
package org.eclipse.wb.internal.core.gefTree;

import org.eclipse.wb.core.gef.IEditPartConfigurator;
import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.gefTree.policy.generic.FlowContainerLayoutEditPolicy;
import org.eclipse.wb.internal.core.gefTree.policy.generic.SimpleContainerLayoutEditPolicy;
import org.eclipse.wb.internal.core.model.generic.FlowContainer;
import org.eclipse.wb.internal.core.model.generic.FlowContainerConfigurable;
import org.eclipse.wb.internal.core.model.generic.FlowContainerFactory;
import org.eclipse.wb.internal.core.model.generic.SimpleContainer;
import org.eclipse.wb.internal.core.model.generic.SimpleContainerFactory;
import org.eclipse.wb.internal.core.model.nonvisual.FlowContainerGroupInfo;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Configures generic simple/flow containers behavior.
 *
 * @author scheglov_ke
 * @coverage core.gefTree
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
    List<SimpleContainer> containers = new SimpleContainerFactory(component, false).get();
    for (SimpleContainer container : containers) {
      EditPolicy layoutPolicy = new SimpleContainerLayoutEditPolicy(component, container);
      editPart.installEditPolicy(container, layoutPolicy);
    }
  }

  /**
   * Flow containers: FlowPanel, HorizontalPanel, VerticalPanel, etc.
   */
  private void configureFlowContainer(EditPart editPart, JavaInfo component) {
    List<FlowContainer> containers = new FlowContainerFactory(component, false).get();
    for (FlowContainer container : containers) {
      EditPolicy layoutPolicy = new FlowContainerLayoutEditPolicy(component, container);
      editPart.installEditPolicy(container, layoutPolicy);
      // support groups
      if (component instanceof AbstractComponentInfo
          && container instanceof FlowContainerConfigurable) {
        configureGroupInfo(
            editPart,
            (AbstractComponentInfo) component,
            (FlowContainerConfigurable) container);
      }
    }
  }

  private void configureGroupInfo(EditPart editPart,
      JavaInfo component,
      FlowContainerConfigurable container) {
    String groupName = container.getGroupName();
    if (StringUtils.isEmpty(groupName)) {
      return;
    }
    // configure group
    FlowContainerGroupInfo groupInfo = getGroupInfoByName(component, groupName);
    if (groupInfo != null) {
      groupInfo.addContainer(container);
    }
  }

  private FlowContainerGroupInfo getGroupInfoByName(JavaInfo component, String groupName) {
    // find group info
    {
      List<FlowContainerGroupInfo> groupInfos = component.getChildren(FlowContainerGroupInfo.class);
      for (FlowContainerGroupInfo groupInfo : groupInfos) {
        if (groupInfo.getCaption().equals(groupName)) {
          return groupInfo;
        }
      }
    }
    // create group info
    FlowContainerGroupInfo groupInfo;
    try {
      groupInfo = new FlowContainerGroupInfo(component.getEditor(), component, groupName);
    } catch (Exception e) {
      DesignerPlugin.log("FlowContainerGroupInfo creation error: " + e.getMessage(), e);
      groupInfo = null;
    }
    return groupInfo;
  }
}
