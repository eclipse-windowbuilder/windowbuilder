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

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.variable.AbstractImplicitVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;

/**
 * Implementation of {@link VariableSupport} for implicit {@link LayoutInfo}.
 * 
 * @author scheglov_ke
 * @coverage swing.model.layout
 */
public final class ImplicitLayoutVariableSupport extends AbstractImplicitVariableSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ImplicitLayoutVariableSupport(JavaInfo javaInfo) {
    super(javaInfo);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "implicit-layout";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean isDefault() {
    return true;
  }

  @Override
  public String getTitle() throws Exception {
    return "(implicit layout)";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Materializing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected JavaInfo getParent() {
    return m_javaInfo.getParentJava();
  }
}
