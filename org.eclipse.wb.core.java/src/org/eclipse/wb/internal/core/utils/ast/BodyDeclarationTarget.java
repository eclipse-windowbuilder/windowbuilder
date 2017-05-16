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
package org.eclipse.wb.internal.core.utils.ast;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * {@link BodyDeclarationTarget} contains information about location for placing
 * {@link BodyDeclaration}.
 *
 * @author scheglov_ke
 * @coverage core.util.ast
 */
public class BodyDeclarationTarget {
  private final TypeDeclaration m_type;
  private final BodyDeclaration m_declaration;
  private final boolean m_before;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public BodyDeclarationTarget(TypeDeclaration type, boolean before) {
    this(type, null, before);
  }

  public BodyDeclarationTarget(BodyDeclaration declaration, boolean before) {
    this(null, declaration, before);
  }

  public BodyDeclarationTarget(TypeDeclaration type, BodyDeclaration declaration, boolean before) {
    m_type = type;
    m_declaration = declaration;
    m_before = before;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return (m_before ? "before " : "after ") + (m_declaration != null ? m_declaration : m_type);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Accessor
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return any target {@link ASTNode} - {@link TypeDeclaration} or {@link BodyDeclaration}.
   */
  public ASTNode getNode() {
    return m_type != null ? m_type : m_declaration;
  }

  /**
   * @return the {@link TypeDeclaration} that should contain new {@link BodyDeclaration}, or
   *         <code>null</code> if {@link #getDeclaration()} returns not <code>null</code>.
   */
  public TypeDeclaration getType() {
    return m_type;
  }

  /**
   * @return the target {@link BodyDeclaration} for new {@link BodyDeclaration}, or
   *         <code>null</code> if {@link #getType()} returns not <code>null</code>.
   */
  public BodyDeclaration getDeclaration() {
    return m_declaration;
  }

  /**
   * @return <code>true</code> if new {@link BodyDeclaration} should be added before target
   *         {@link BodyDeclaration} or in beginning of {@link TypeDeclaration}.
   */
  public boolean isBefore() {
    return m_before;
  }

  /**
   * @return negation of {@link #isBefore()}.
   */
  public boolean isAfter() {
    return !m_before;
  }
}
