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
package org.eclipse.wb.internal.core.gefTree.part;

import org.eclipse.wb.core.gefTree.part.ObjectEditPart;
import org.eclipse.wb.gef.tree.TreeEditPart;
import org.eclipse.wb.internal.core.gefTree.policy.generic.FlowContainerLayoutEditPolicy;
import org.eclipse.wb.internal.core.model.generic.FlowContainerConfigurable;
import org.eclipse.wb.internal.core.model.nonvisual.FlowContainerGroupInfo;

import java.util.List;

/**
 * {@link TreeEditPart} for {@link FlowContainerGroupInfo}.
 *
 * @author sablin_aa
 * @coverage core.gefTree
 */
public final class FlowContainerGroupEditPart extends ObjectEditPart {
  private final FlowContainerGroupInfo m_groupInfo;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FlowContainerGroupEditPart(FlowContainerGroupInfo group) {
    super(group);
    m_groupInfo = group;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Policies
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createEditPolicies() {
    super.createEditPolicies();
    {
      List<FlowContainerConfigurable> containers = m_groupInfo.getContainers();
      for (FlowContainerConfigurable container : containers) {
        installEditPolicy(new FlowContainerLayoutEditPolicy(m_groupInfo.getParent(), container));
      }
    }
  }
}
