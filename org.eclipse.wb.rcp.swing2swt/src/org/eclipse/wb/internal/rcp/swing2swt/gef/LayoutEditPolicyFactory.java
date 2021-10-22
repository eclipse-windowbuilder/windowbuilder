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
package org.eclipse.wb.internal.rcp.swing2swt.gef;

import org.eclipse.wb.core.gef.policy.layout.ILayoutEditPolicyFactory;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.rcp.swing2swt.layout.BorderLayoutInfo;

/**
 * Implementation of {@link ILayoutEditPolicyFactory} for Swing2SWT.
 *
 * @author scheglov_ke
 * @coverage rcp.swing2swt.gef
 */
public final class LayoutEditPolicyFactory implements ILayoutEditPolicyFactory {
  ////////////////////////////////////////////////////////////////////////////
  //
  // ILayoutEditPolicyFactory
  //
  ////////////////////////////////////////////////////////////////////////////
  public LayoutEditPolicy createLayoutEditPolicy(EditPart context, Object model) {
    if (model instanceof BorderLayoutInfo) {
      return new BorderLayoutEditPolicy((BorderLayoutInfo) model);
    }
    // not found
    return null;
  }
}
