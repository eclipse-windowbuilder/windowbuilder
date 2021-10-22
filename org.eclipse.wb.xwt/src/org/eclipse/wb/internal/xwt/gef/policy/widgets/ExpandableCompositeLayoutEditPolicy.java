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
package org.eclipse.wb.internal.xwt.gef.policy.widgets;

import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.xwt.gef.policy.AbstractPositionCompositeLayoutEditPolicy;
import org.eclipse.wb.internal.xwt.model.forms.ExpandableCompositeInfo;

/**
 * {@link LayoutEditPolicy} for {@link ExpandableCompositeInfo}.
 *
 * @author scheglov_ke
 * @coverage XWT.gef.policy
 */
public final class ExpandableCompositeLayoutEditPolicy
    extends
      AbstractPositionCompositeLayoutEditPolicy {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Composite
  //
  ////////////////////////////////////////////////////////////////////////////
  public ExpandableCompositeLayoutEditPolicy(ExpandableCompositeInfo composite) {
    super(composite);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Positions
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addFeedbacks() throws Exception {
    addFeedback2(0.8, 0.0, 1.0, 0.2, new Insets(0, 0, 1, 0), "Text client", "textClient");
    addFeedback2(0.0, 0.2, 1.0, 1.0, new Insets(0, 0, 0, 0), "Client", "client");
  }
}
