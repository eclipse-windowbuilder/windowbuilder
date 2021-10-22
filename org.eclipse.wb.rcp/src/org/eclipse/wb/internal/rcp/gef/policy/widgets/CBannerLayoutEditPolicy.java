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
package org.eclipse.wb.internal.rcp.gef.policy.widgets;

import org.eclipse.wb.draw2d.geometry.Insets;
import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.rcp.gef.GefMessages;
import org.eclipse.wb.internal.rcp.gef.policy.AbstractPositionCompositeLayoutEditPolicy;
import org.eclipse.wb.internal.rcp.model.widgets.CBannerInfo;

/**
 * Implementation of {@link LayoutEditPolicy} for {@link CBannerInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.policy
 */
public final class CBannerLayoutEditPolicy extends AbstractPositionCompositeLayoutEditPolicy {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Composite
  //
  ////////////////////////////////////////////////////////////////////////////
  public CBannerLayoutEditPolicy(CBannerInfo composite) {
    super(composite);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Positions
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addFeedbacks() throws Exception {
    addFeedback2(
        0.0,
        0.0,
        0.5,
        0.4,
        new Insets(0, 0, 1, 1),
        GefMessages.CBannerLayoutEditPolicy_posLeft,
        "setLeft");
    addFeedback2(
        0.5,
        0.0,
        1.0,
        0.4,
        new Insets(0, 0, 1, 0),
        GefMessages.CBannerLayoutEditPolicy_posRight,
        "setRight");
    addFeedback2(
        0.0,
        0.6,
        1.0,
        1.0,
        new Insets(0, 0, 0, 0),
        GefMessages.CBannerLayoutEditPolicy_posBottom,
        "setBottom");
  }
}
