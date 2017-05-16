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
package org.eclipse.wb.internal.core.utils.exception;

import org.eclipse.wb.internal.core.utils.ast.AstEditor;

import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * We unable to find entry point (constructor or method) automatically.
 *
 * @author scheglov_ke
 * @coverage core.util
 */
@SuppressWarnings("serial")
public final class NoEntryPointError extends Error {
  private final AstEditor m_editor;
  private final TypeDeclaration m_typeDeclaration;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public NoEntryPointError(AstEditor editor, TypeDeclaration typeDeclaration) {
    m_editor = editor;
    m_typeDeclaration = typeDeclaration;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public AstEditor getEditor() {
    return m_editor;
  }

  public TypeDeclaration getTypeDeclaration() {
    return m_typeDeclaration;
  }
}
