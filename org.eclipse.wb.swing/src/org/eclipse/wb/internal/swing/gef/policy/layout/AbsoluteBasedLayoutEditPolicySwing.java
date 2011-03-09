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
package org.eclipse.wb.internal.swing.gef.policy.layout;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.gef.policy.layout.absolute.AbsoluteBasedLayoutEditPolicy;
import org.eclipse.wb.internal.core.laf.BaselineSupportHelper;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.swing.ToolkitProvider;
import org.eclipse.wb.internal.swing.gef.ComponentsLayoutRequestValidator;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.layout.LayoutInfo;

import java.util.List;

/**
 * Generic {@link LayoutEditPolicy} for absolute based Swing layouts.
 * 
 * @author mitin_aa
 * @author scheglov_ke
 * @coverage swing.gef.policy
 */
public abstract class AbsoluteBasedLayoutEditPolicySwing
    extends
      AbsoluteBasedLayoutEditPolicy<ComponentInfo> {
  private final LayoutInfo m_layout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbsoluteBasedLayoutEditPolicySwing(LayoutInfo layout) {
    super(layout);
    m_layout = layout;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Requests
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected ILayoutRequestValidator getRequestValidator() {
    return ComponentsLayoutRequestValidator.INSTANCE;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IVisualDataProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public List<ComponentInfo> getAllComponents() {
    List<ComponentInfo> components = Lists.newArrayList();
    components.addAll(m_layout.getContainer().getChildrenComponents());
    return components;
  }

  public int getBaseline(IAbstractComponentInfo component) {
    return BaselineSupportHelper.getBaseline(component.getObject());
  }

  public Dimension getComponentPreferredSize(IAbstractComponentInfo component) {
    ComponentInfo componentInfo = (ComponentInfo) component;
    return componentInfo.getPreferredSize();
  }

  public Dimension getContainerSize() {
    IAbstractComponentInfo composite = m_layout.getContainer();
    Rectangle compositeBounds = composite.getModelBounds().getCopy();
    Insets clientAreaInsets = composite.getClientAreaInsets();
    return compositeBounds.crop(clientAreaInsets).getSize();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Misc 
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected ToolkitDescription getToolkit() {
    return ToolkitProvider.DESCRIPTION;
  }
}