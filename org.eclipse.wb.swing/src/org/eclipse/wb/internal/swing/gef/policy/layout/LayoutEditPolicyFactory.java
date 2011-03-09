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
package org.eclipse.wb.internal.swing.gef.policy.layout;

import org.eclipse.wb.core.gef.policy.layout.ILayoutEditPolicyFactory;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.graphical.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.swing.gef.policy.layout.gbl.GridBagLayoutEditPolicy;
import org.eclipse.wb.internal.swing.model.layout.BorderLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.BoxLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.CardLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.GridLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.absolute.AbstractAbsoluteLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.gbl.AbstractGridBagLayoutInfo;
import org.eclipse.wb.internal.swing.model.layout.spring.SpringLayoutInfo;

/**
 * Implementation of {@link ILayoutEditPolicyFactory} for Swing.
 * 
 * @author scheglov_ke
 * @author mitin_aa
 * @coverage swing.gef.policy
 */
public final class LayoutEditPolicyFactory implements ILayoutEditPolicyFactory {
  ////////////////////////////////////////////////////////////////////////////
  //
  // ILayoutEditPolicyFactory
  //
  ////////////////////////////////////////////////////////////////////////////
  public LayoutEditPolicy createLayoutEditPolicy(EditPart context, Object model) {
    if (model instanceof AbstractAbsoluteLayoutInfo) {
      return new AbsoluteLayoutEditPolicy((AbstractAbsoluteLayoutInfo) model);
    }
    if (model instanceof BorderLayoutInfo) {
      return new BorderLayoutEditPolicy((BorderLayoutInfo) model);
    }
    if (model instanceof CardLayoutInfo) {
      return new CardLayoutEditPolicy((CardLayoutInfo) model);
    }
    if (model instanceof FlowLayoutInfo) {
      return new FlowLayoutEditPolicy((FlowLayoutInfo) model);
    }
    if (model instanceof GridLayoutInfo) {
      return new GridLayoutEditPolicy((GridLayoutInfo) model);
    }
    if (model instanceof BoxLayoutInfo) {
      return new BoxLayoutEditPolicy((BoxLayoutInfo) model);
    }
    if (model instanceof AbstractGridBagLayoutInfo) {
      return new GridBagLayoutEditPolicy((AbstractGridBagLayoutInfo) model);
    }
    if (model instanceof SpringLayoutInfo) {
      return new SpringLayoutEditPolicy((SpringLayoutInfo) model);
    }
    // not found
    return null;
  }
}
