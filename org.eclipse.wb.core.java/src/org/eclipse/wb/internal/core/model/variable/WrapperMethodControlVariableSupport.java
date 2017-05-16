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
package org.eclipse.wb.internal.core.model.variable;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.WrapperByMethod;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;

/**
 * Implementation of {@link VariableSupport} for control of {@link WrapperByMethod}.
 *
 * @author scheglov_ke
 * @author sablin_aa
 * @coverage core.model.variable
 */
public class WrapperMethodControlVariableSupport extends AbstractImplicitVariableSupport {
  private final WrapperByMethod m_wrapper;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public WrapperMethodControlVariableSupport(JavaInfo javaInfo, WrapperByMethod wrapper) {
    super(javaInfo);
    m_wrapper = wrapper;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "viewer";
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
    return m_wrapper.getWrapperInfo().getVariableSupport().getTitle()
        + "."
        + m_wrapper.getControlMethod().getName()
        + "()";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Target
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void ensureInstanceReadyAt(StatementTarget target) throws Exception {
    m_wrapper.getWrapperInfo().getVariableSupport().ensureInstanceReadyAt(target);
  }

  @Override
  public StatementTarget getAssociationTarget(StatementTarget target) throws Exception {
    return m_wrapper.getWrapperInfo().getVariableSupport().getAssociationTarget(target);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Materializing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected JavaInfo getParent() {
    return m_wrapper.getWrapperInfo();
  }
}
