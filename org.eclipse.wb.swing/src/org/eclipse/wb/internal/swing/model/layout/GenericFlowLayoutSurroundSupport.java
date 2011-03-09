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
package org.eclipse.wb.internal.swing.model.layout;

import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.util.surround.LayoutSurroundSupport;

import java.util.List;

/**
 * Helper for surrounding {@link ComponentInfo}'s on {@link GenericFlowLayoutInfo} with some
 * {@link ContainerInfo}.
 * 
 * @author scheglov_ke
 * @coverage swing.model.layout
 */
public final class GenericFlowLayoutSurroundSupport extends LayoutSurroundSupport {
  private final GenericFlowLayoutInfo m_layout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GenericFlowLayoutSurroundSupport(GenericFlowLayoutInfo layout) {
    super(layout);
    m_layout = layout;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean validateComponents(List<ComponentInfo> components) throws Exception {
    // check that components are adjacent
    {
      List<ComponentInfo> allComponents = m_layout.getContainer().getChildrenComponents();
      if (!GenericsUtils.areAdjacent(allComponents, components)) {
        return false;
      }
    }
    // continue
    return super.validateComponents(components);
  }

  @Override
  protected void addContainer(ContainerInfo container, List<ComponentInfo> components)
      throws Exception {
    m_layout.add(container, components.get(0));
  }
}
