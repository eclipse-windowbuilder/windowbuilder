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
package org.eclipse.wb.internal.swt.gefTree;

import org.eclipse.wb.core.gefTree.policy.layout.ILayoutEditPolicyFactory;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.tree.policies.LayoutEditPolicy;
import org.eclipse.wb.internal.swt.gefTree.policy.AbsoluteLayoutEditPolicy;
import org.eclipse.wb.internal.swt.gefTree.policy.FormLayoutEditPolicy;
import org.eclipse.wb.internal.swt.model.layout.absolute.AbsoluteLayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.form.FormLayoutInfo;
import org.eclipse.wb.internal.swt.model.layout.form.FormLayoutInfoImplAutomatic;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

/**
 * Implementation of {@link ILayoutEditPolicyFactory} for SWT.
 *
 * @author scheglov_ke
 * @author mitin_aa
 * @coverage swt.gefTree
 */
public final class LayoutEditPolicyFactory implements ILayoutEditPolicyFactory {
  ////////////////////////////////////////////////////////////////////////////
  //
  // ILayoutEditPolicyFactory
  //
  ////////////////////////////////////////////////////////////////////////////
  public LayoutEditPolicy createLayoutEditPolicy(EditPart context, Object model) {
    if (model instanceof AbsoluteLayoutInfo) {
      return new AbsoluteLayoutEditPolicy<ControlInfo>((AbsoluteLayoutInfo) model);
    }
    if (model instanceof FormLayoutInfo) {
      FormLayoutInfo formLayoutInfo = (FormLayoutInfo) model;
      if (formLayoutInfo.getImpl() instanceof FormLayoutInfoImplAutomatic) {
        return new FormLayoutEditPolicy(formLayoutInfo);
      } else {
        // TODO:
      }
    }
    return null;
  }
}