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
package org.eclipse.wb.internal.swing.databinding.wizards.autobindings;

import org.eclipse.wb.internal.core.databinding.wizards.autobindings.AbstractDescriptor;

/**
 * AutoBinding UpdateStrategy descriptor.
 *
 * @author lobas_av
 * @coverage bindings.swing.wizard.auto
 */
public final class AutoBindingUpdateStrategyDescriptor extends AbstractDescriptor {
  private String m_sourceCode;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getSourceCode() {
    return m_sourceCode;
  }

  public void setSourceCode(String sourceCode) {
    m_sourceCode = sourceCode;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Default
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean isDefault(Object property) {
    return true;
  }
}