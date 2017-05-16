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
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Statement;

/**
 * {@link VariableSupport} implementation for field variable.
 *
 * @author scheglov_ke
 * @coverage core.model.variable
 */
public abstract class FieldVariableSupport extends AbstractSimpleVariableSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FieldVariableSupport(JavaInfo javaInfo) {
    super(javaInfo);
  }

  public FieldVariableSupport(JavaInfo javaInfo, Expression variable) {
    super(javaInfo, variable);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean isValidStatementForChild(Statement statement) {
    // When statement is Block, this means that we try to leave it (down).
    // To have good code structure, we should not leave blocks where field is assigned.
    if (statement instanceof Block) {
      return AstNodeUtils.getEnclosingBlock(m_variable) != statement;
    }
    // field is always visible, so any related statement is valid
    return true;
  }

  @Override
  public String getComponentName() {
    return m_utils.stripPrefixSuffix(
        getName(),
        JavaCore.CODEASSIST_FIELD_PREFIXES,
        JavaCore.CODEASSIST_FIELD_SUFFIXES);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Expressions
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getReferenceExpression(NodeTarget target) throws Exception {
    assertJavaInfoCreatedAt(target);
    boolean isStatic;
    {
      FieldDeclaration fieldDeclaration =
          AstNodeUtils.getEnclosingNode(m_declaration, FieldDeclaration.class);
      isStatic = AstNodeUtils.isStatic(fieldDeclaration);
    }
    String fieldName = getName();
    return !isStatic && prefixThis() ? "this." + fieldName : fieldName;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Conversion
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final boolean canConvertLocalToField() {
    return false;
  }

  @Override
  public final void convertLocalToField() throws Exception {
    throw new IllegalStateException();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Deletes {@link FieldDeclaration} of this {@link FieldVariableSupport}.
   */
  protected final void delete_removeDeclarationField() throws Exception {
    m_javaInfo.getEditor().removeVariableDeclaration(m_declaration);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Rename on "text" property modification
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  String decorateTextName(String newName) {
    return m_utils.addPrefixSuffix(
        newName,
        JavaCore.CODEASSIST_FIELD_PREFIXES,
        JavaCore.CODEASSIST_FIELD_SUFFIXES);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Preferences
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final String[] V_MODIFIER_CODE = {"private ", "", "protected ", "public "};
  public static final int V_FIELD_MODIFIER_PRIVATE = 0;
  public static final int V_FIELD_MODIFIER_PACKAGE = 1;
  public static final int V_FIELD_MODIFIER_PROTECTED = 2;
  public static final int V_FIELD_MODIFIER_PUBLIC = 3;

  /**
   * @return <code>true</code> if field access should be prefixed by "this.".
   */
  protected abstract boolean prefixThis();
}
