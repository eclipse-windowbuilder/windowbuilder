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

import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.TopBoundsSupport;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import java.awt.Window;

import javax.swing.JDialog;
import javax.swing.JFrame;

/**
 * Implementation of {@link TopBoundsSupport} for {@link JFrame}, {@link JDialog}.
 * 
 * @author scheglov_ke
 * @author mitin_aa
 * @coverage swing.model.top
 */
public final class WindowTopBoundsSupport extends SwingTopBoundsSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public WindowTopBoundsSupport(ContainerInfo container) {
    super(container);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // TopBoundsSupport
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void apply() throws Exception {
    try {
      // check for: pack()
      if (JavaInfoUtils.hasTrueParameter(m_component, "topBounds.pack")
          || hasMethodInvocations(new String[]{"pack()"})) {
        ((Window) m_component.getObject()).pack();
        return;
      }
      // continue in "super"
      super.apply();
    } finally {
      // Force peer's creating.
      // We need this to force resize/layout for children containers/components.
      if (m_component.getObject() instanceof Window) {
        Window window = (Window) m_component.getObject();
        // bug/feature in MacOSX Java5 (at least in build 1.5.0_16-b06-284):
        // if the window is not valid and has no peer, making it visible leads to 
        // invalid insets (0x80000000) returned by peer's native window.
        // the workaround it to create peer by invoking 'addNotify()' method.
        if (!window.isDisplayable()) {
          window.addNotify();
        }
        window.validate();
      }
    }
  }
}
