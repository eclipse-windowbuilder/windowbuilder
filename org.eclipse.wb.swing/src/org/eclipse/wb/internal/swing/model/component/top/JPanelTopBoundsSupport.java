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
package org.eclipse.wb.internal.swing.model.component.top;

import org.eclipse.wb.internal.core.model.TopBoundsSupport;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import org.eclipse.jdt.core.dom.MethodInvocation;

import java.awt.Component;

import javax.swing.JPanel;

/**
 * Implementation of {@link TopBoundsSupport} for {@link JPanel}.
 * 
 * @author scheglov_ke
 * @coverage swing.model.top
 */
public final class JPanelTopBoundsSupport extends SwingTopBoundsSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public JPanelTopBoundsSupport(ContainerInfo container) {
    super(container);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // TopBoundsSupport
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void apply() throws Exception {
    // check for: setPreferredSize(java.awt.Dimension)void and use it for setSize()
    {
      MethodInvocation invocation =
          m_component.getMethodInvocation("setPreferredSize(java.awt.Dimension)");
      if (invocation != null) {
        Component component = (Component) m_component.getObject();
        component.setSize(component.getPreferredSize());
        return;
      }
    }
    //
    super.apply();
  }

  @Override
  public void setSize(int width, int height) throws Exception {
    // check for: setPreferredSize(java.awt.Dimension)void
    if (setSizeDimension("setPreferredSize", width, height)) {
      return;
    }
    //
    super.setSize(width, height);
  }
}
