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
package org.eclipse.wb.internal.swt.gefTree.policy;

import org.eclipse.wb.core.gefTree.policy.ObjectLayoutEditPolicy;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.swt.gef.ControlsLayoutRequestValidator;
import org.eclipse.wb.internal.swt.model.layout.absolute.IAbsoluteLayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;

/**
 * Implementation of {@link LayoutEditPolicy} for {@link IAbsoluteLayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage swt.gefTree.policy
 */
public final class AbsoluteLayoutEditPolicy<C extends IControlInfo>
    extends
      ObjectLayoutEditPolicy<C> {
  private final IAbsoluteLayoutInfo<C> m_layout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbsoluteLayoutEditPolicy(IAbsoluteLayoutInfo<C> layout) {
    super(layout.getUnderlyingModel());
    m_layout = layout;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Requests
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean isGoodReferenceChild(Request request, EditPart editPart) {
    Object model = editPart.getModel();
    return isControl(model);
  }

  @Override
  protected ILayoutRequestValidator getRequestValidator() {
    return ControlsLayoutRequestValidator.INSTANCE;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void command_CREATE(C control, C reference) throws Exception {
    Dimension preferredSize = control.getPreferredSize();
    m_layout.commandCreate(control, reference);
    m_layout.commandChangeBounds(control, new Point(0, 0), preferredSize);
  }

  @Override
  protected void command_MOVE(C control, C reference) throws Exception {
    m_layout.commandMove(control, reference);
    m_layout.commandChangeBounds(control, null, control.getModelBounds().getSize());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if given object is {@link IControlInfo}.
   */
  private boolean isControl(Object model) {
    return model instanceof IControlInfo;
  }
}