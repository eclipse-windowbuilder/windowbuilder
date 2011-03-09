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
package org.eclipse.wb.internal.swing.java6.gef;

import org.eclipse.wb.core.gef.policy.validator.ComponentClassLayoutRequestValidator;
import org.eclipse.wb.core.gef.policy.validator.LayoutRequestValidators;
import org.eclipse.wb.gef.core.policies.ILayoutRequestValidator;
import org.eclipse.wb.internal.layout.group.gef.GroupLayoutEditPolicy2;
import org.eclipse.wb.internal.layout.group.model.IGroupLayoutInfo;
import org.eclipse.wb.internal.swing.gef.MenuLayoutRequestValidator;

/**
 * Swing implementation for {@link GroupLayoutEditPolicy2}.
 * 
 * @author mitin_aa
 */
public class SwingGroupLayoutEditPolicy2 extends GroupLayoutEditPolicy2 {
  private static final ILayoutRequestValidator REQUEST_VALIDATOR =
      LayoutRequestValidators.finalize(LayoutRequestValidators.and(
          MenuLayoutRequestValidator.INSTANCE,
          new ComponentClassLayoutRequestValidator("javax.swing.JComponent")));

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SwingGroupLayoutEditPolicy2(IGroupLayoutInfo layout) {
    super(layout);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Requests
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected ILayoutRequestValidator getRequestValidator() {
    return REQUEST_VALIDATOR;
  }
}
