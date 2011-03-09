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
package org.eclipse.wb.internal.swt.gef.part;

import org.eclipse.wb.core.gef.policy.TabOrderContainerEditPolicy;
import org.eclipse.wb.core.gef.policy.layout.LayoutPolicyUtils;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.Graphics;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.policies.IRefreshableEditPolicy;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.swt.gef.policy.layout.DefaultLayoutEditPolicy;
import org.eclipse.wb.internal.swt.gef.policy.layout.DropLayoutEditPolicy;
import org.eclipse.wb.internal.swt.model.layout.LayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;

import org.eclipse.swt.SWT;

/**
 * {@link EditPart} for {@link CompositeInfo}.
 * 
 * @author lobas_av
 * @coverage swt.gef.part
 */
public class CompositeEditPart extends ControlEditPart {
  private final CompositeInfo m_composite;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CompositeEditPart(CompositeInfo composite) {
    super(composite);
    m_composite = composite;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Figure
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void drawCustomBorder(Figure figure, Graphics graphics) {
    try {
      if (m_composite.shouldDrawDotsBorder()) {
        graphics.setForegroundColor(IColorConstants.gray);
        graphics.setLineStyle(SWT.LINE_DOT);
        Rectangle area = figure.getClientArea();
        graphics.drawRectangle(0, 0, area.width - 1, area.height - 1);
      }
    } catch (Throwable e) {
    }
  }

  @Override
  protected void addChildVisual(EditPart childPart, int index) {
    super.addChildVisual(childPart, getFigure().getChildren().size() - index);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Policies
  //
  ////////////////////////////////////////////////////////////////////////////
  private LayoutInfo m_currentLayout;

  @Override
  protected void createEditPolicies() {
    super.createEditPolicies();
    // support for dropping LayoutInfo's
    if (m_composite.hasLayout()) {
      installEditPolicy(new DropLayoutEditPolicy(m_composite));
    }
    // support tab ordering for children
    installEditPolicy(
        TabOrderContainerEditPolicy.TAB_CONTAINER_ROLE,
        new TabOrderContainerEditPolicy());
  }

  @Override
  protected void refreshEditPolicies() {
    super.refreshEditPolicies();
    // support for dropping components
    if (m_composite.hasLayout()) {
      LayoutInfo layout = m_composite.getLayout();
      if (layout != m_currentLayout) {
        m_currentLayout = layout;
        LayoutEditPolicy policy = LayoutPolicyUtils.createLayoutEditPolicy(this, layout);
        if (policy == null) {
          policy = new DefaultLayoutEditPolicy();
        }
        installEditPolicy(EditPolicy.LAYOUT_ROLE, policy);
      } else {
        EditPolicy policy = getEditPolicy(EditPolicy.LAYOUT_ROLE);
        if (policy instanceof IRefreshableEditPolicy) {
          ((IRefreshableEditPolicy) policy).refreshEditPolicy();
        }
      }
    }
  }
}