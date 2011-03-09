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
package org.eclipse.wb.internal.ercp.gefTree.policy;

import org.eclipse.wb.core.gefTree.policy.SingleObjectLayoutEditPolicy;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.ercp.model.widgets.mobile.CaptionedControlInfo;
import org.eclipse.wb.internal.swt.gef.ControlsLayoutRequestValidator;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

/**
 * {@link LayoutEditPolicy} for dropping {@link ControlInfo} on {@link CaptionedControlInfo} .
 * 
 * @author scheglov_ke
 * @coverage swt.gef.policy
 */
public final class CaptionedControlLayoutEditPolicy
    extends
      SingleObjectLayoutEditPolicy<ControlInfo> {
  private final CaptionedControlInfo m_captionedControl;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CaptionedControlLayoutEditPolicy(CaptionedControlInfo composite) {
    super(composite);
    m_captionedControl = composite;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Routing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected ILayoutRequestValidator getRequestValidator() {
    return ControlsLayoutRequestValidator.INSTANCE;
  }

  @Override
  protected boolean isEmpty() {
    return m_captionedControl.getChildrenControls().isEmpty();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Command
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void command_CREATE(ControlInfo control) throws Exception {
    JavaInfoUtils.add(control, null, m_captionedControl, null);
  }

  @Override
  protected void command_ADD(ControlInfo control) throws Exception {
    JavaInfoUtils.move(control, null, m_captionedControl, null);
  }
}