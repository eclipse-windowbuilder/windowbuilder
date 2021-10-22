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
package org.eclipse.wb.internal.swt.model.util.surround;

import org.eclipse.wb.internal.core.model.creation.IImplicitCreationSupport;
import org.eclipse.wb.internal.core.model.util.surround.SurroundSupport;
import org.eclipse.wb.internal.swt.model.layout.LayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.ControlInfo;

import java.util.List;

/**
 * Abstract {@link SurroundSupport} for SWT {@link LayoutInfo}.
 *
 * @author scheglov_ke
 * @coverage swt.model.util
 */
public abstract class LayoutSurroundSupport extends SwtSurroundSupport {
  private final LayoutInfo m_layout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LayoutSurroundSupport(LayoutInfo layout) {
    super(layout.getComposite());
    m_layout = layout;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validate
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean isActive() {
    return m_layout.isActive();
  }

  @Override
  protected boolean validateComponents(List<ControlInfo> components) throws Exception {
    if (!super.validateComponents(components)) {
      return false;
    }
    // perform "surround" only for "active" layout
    if (!m_layout.isActive()) {
      return false;
    }
    // don't handle implicit
    for (ControlInfo component : components) {
      if (component.getCreationSupport() instanceof IImplicitCreationSupport) {
        return false;
      }
    }
    // OK
    return true;
  }
}
