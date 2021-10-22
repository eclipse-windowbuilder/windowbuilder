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
package org.eclipse.wb.internal.rcp.gef.part.forms;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.graphical.policies.TerminatorLayoutEditPolicy;
import org.eclipse.wb.internal.rcp.gef.policy.forms.ExpandableCompositeLayoutEditPolicy;
import org.eclipse.wb.internal.rcp.model.forms.ExpandableCompositeInfo;
import org.eclipse.wb.internal.swt.gef.part.CompositeEditPart;

/**
 * {@link EditPart} for {@link ExpandableCompositeInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.part
 */
public final class ExpandableCompositeEditPart extends CompositeEditPart {
  private final ExpandableCompositeInfo m_composite;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ExpandableCompositeEditPart(ExpandableCompositeInfo composite) {
    super(composite);
    m_composite = composite;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Policy
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refreshEditPolicies() {
    super.refreshEditPolicies();
    installEditPolicy(new ExpandableCompositeLayoutEditPolicy(m_composite));
    installEditPolicy(new TerminatorLayoutEditPolicy());
  }
}
