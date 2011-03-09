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
package org.eclipse.wb.internal.rcp.model.forms;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * {@link CreationSupport} for {@link FormToolkit} described by {@link FormToolkitAccess}.
 * 
 * @author scheglov_ke
 * @coverage rcp.model.forms
 */
public final class FormToolkitCreationSupport extends CreationSupport {
  private final JavaInfo m_hostJavaInfo;
  private final FormToolkitAccess m_toolkitAccess;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FormToolkitCreationSupport(JavaInfo hostJavaInfo, FormToolkitAccess toolkitAccess) {
    m_hostJavaInfo = hostJavaInfo;
    m_toolkitAccess = toolkitAccess;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "toolkitAccess: " + m_toolkitAccess.getReferenceExpression();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public ASTNode getNode() {
    return m_hostJavaInfo.getCreationSupport().getNode();
  }

  @Override
  public boolean isJavaInfo(ASTNode node) {
    return m_toolkitAccess.isToolkit(node);
  }
}
