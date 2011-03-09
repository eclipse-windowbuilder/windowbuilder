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
package org.eclipse.wb.internal.ercp.gef.part.widgets.mobile;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.internal.ercp.gef.policy.CaptionedControlLayoutEditPolicy;
import org.eclipse.wb.internal.ercp.model.widgets.mobile.CaptionedControlInfo;
import org.eclipse.wb.internal.swt.gef.part.CompositeEditPart;

/**
 * {@link EditPart} for {@link CaptionedControlInfo}.
 * 
 * @author scheglov_ke
 * @coverage swt.gef.part
 */
public final class CaptionedControlEditPart extends CompositeEditPart {
  private final CaptionedControlInfo m_captionedControl;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CaptionedControlEditPart(CaptionedControlInfo captionedControl) {
    super(captionedControl);
    m_captionedControl = captionedControl;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Policies
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createEditPolicies() {
    super.createEditPolicies();
    installEditPolicy(
        EditPolicy.LAYOUT_ROLE,
        new CaptionedControlLayoutEditPolicy(m_captionedControl));
  }
}
