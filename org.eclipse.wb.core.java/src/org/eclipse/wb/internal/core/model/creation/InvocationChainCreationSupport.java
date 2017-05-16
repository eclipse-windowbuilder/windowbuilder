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
package org.eclipse.wb.internal.core.model.creation;

import org.eclipse.wb.core.model.JavaInfo;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.ui.part.ViewPart;

/**
 * Implementation of {@link CreationSupport} for chain of {@link MethodInvocation}'s for some
 * {@link JavaInfo} .
 * <p>
 * For example in {@link ViewPart} code
 * <code>getViewSite().getActionBars().getToolBarManager()</code> to access instance of
 * {@link IToolBarManager}.
 *
 * @author scheglov_ke
 * @coverage core.model.creation
 */
public final class InvocationChainCreationSupport extends CreationSupport {
  private final MethodInvocation m_invocation;
  private final String m_chainSignatures;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public InvocationChainCreationSupport(MethodInvocation invocation, String chainSignatures) {
    m_invocation = invocation;
    m_chainSignatures = chainSignatures;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "invocationChain: " + m_chainSignatures;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public ASTNode getNode() {
    return m_invocation;
  }

  @Override
  public boolean isJavaInfo(ASTNode node) {
    return node == m_invocation;
  }
}
