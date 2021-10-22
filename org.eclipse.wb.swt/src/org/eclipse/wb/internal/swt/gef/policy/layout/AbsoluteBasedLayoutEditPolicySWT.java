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
package org.eclipse.wb.internal.swt.gef.policy.layout;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.gef.policy.layout.absolute.AbsoluteBasedLayoutEditPolicy;
import org.eclipse.wb.internal.core.laf.BaselineSupportHelper;
import org.eclipse.wb.internal.swt.gef.ControlsLayoutRequestValidator;
import org.eclipse.wb.internal.swt.model.layout.ILayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import java.util.List;

/**
 * Generic {@link LayoutEditPolicy} for absolute based SWT layouts.
 *
 * @author mitin_aa
 * @coverage swt.gef.policy
 */
public abstract class AbsoluteBasedLayoutEditPolicySWT<C extends IControlInfo>
    extends
      AbsoluteBasedLayoutEditPolicy<C> {
  private final ILayoutInfo<C> m_layout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbsoluteBasedLayoutEditPolicySWT(ILayoutInfo<C> layout) {
    super(layout.getUnderlyingModel());
    m_layout = layout;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Requests
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected ILayoutRequestValidator getRequestValidator() {
    return ControlsLayoutRequestValidator.INSTANCE;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IVisualDataProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public List<C> getAllComponents() {
    List<C> components = Lists.newArrayList();
    components.addAll(m_layout.getControls());
    return components;
  }

  public int getBaseline(IAbstractComponentInfo component) {
    return BaselineSupportHelper.getBaseline(component.getObject());
  }

  public Dimension getComponentPreferredSize(IAbstractComponentInfo component) {
    @SuppressWarnings("unchecked")
    C componentInfo = (C) component;
    return componentInfo.getPreferredSize();
  }

  public Dimension getContainerSize() {
    IAbstractComponentInfo composite = m_layout.getComposite();
    Rectangle compositeBounds = composite.getModelBounds().getCopy();
    Insets clientAreaInsets = composite.getClientAreaInsets();
    return compositeBounds.crop(clientAreaInsets).getSize();
  }
}