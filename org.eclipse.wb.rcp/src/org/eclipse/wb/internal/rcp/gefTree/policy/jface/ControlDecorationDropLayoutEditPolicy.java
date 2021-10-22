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
package org.eclipse.wb.internal.rcp.gefTree.policy.jface;

import org.eclipse.wb.core.gef.policy.validator.LayoutRequestValidators;
import org.eclipse.wb.core.gefTree.policy.SingleObjectLayoutEditPolicy;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.rcp.model.jface.ControlDecorationInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

/**
 * {@link LayoutEditPolicy} for dropping {@link ControlDecorationInfo} on {@link ControlInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.policy
 */
public final class ControlDecorationDropLayoutEditPolicy
    extends
      SingleObjectLayoutEditPolicy<ControlDecorationInfo> {
  private static final ILayoutRequestValidator VALIDATOR =
      LayoutRequestValidators.modelType(ControlDecorationInfo.class);
  private final ControlInfo m_control;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ControlDecorationDropLayoutEditPolicy(ControlInfo control) {
    super(control);
    m_control = control;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Requests
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected ILayoutRequestValidator getRequestValidator() {
    return VALIDATOR;
  }

  @Override
  protected boolean isEmpty() {
    return m_control.getParent() != null
        && m_control.getChildren(ControlDecorationInfo.class).isEmpty();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void command_CREATE(ControlDecorationInfo decoration) throws Exception {
    decoration.command_CREATE(m_control);
  }

  @Override
  protected void command_ADD(ControlDecorationInfo decoration) throws Exception {
    decoration.command_ADD(m_control);
  }
}