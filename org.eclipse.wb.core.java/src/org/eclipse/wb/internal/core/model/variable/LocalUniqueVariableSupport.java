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

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.OperatorPrecedence;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * {@link VariableSupport} implementation for local, unique variable.
 *
 * @author scheglov_ke
 * @coverage core.model.variable
 */
public final class LocalUniqueVariableSupport extends LocalVariableSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LocalUniqueVariableSupport(JavaInfo javaInfo) {
    super(javaInfo);
  }

  public LocalUniqueVariableSupport(JavaInfo javaInfo, SimpleName variable) {
    super(javaInfo, variable);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "local-unique: " + getName();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Name
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setName(String newName) throws Exception {
    modifyName(newName);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Expressions
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getReferenceExpression(NodeTarget target) throws Exception {
    assertJavaInfoCreatedAt(target);
    if (isVisibleAtTarget(target)) {
      return getName();
    } else {
      convertLocalToField();
      return m_javaInfo.getVariableSupport().getReferenceExpression(target);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Conversion
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void convertLocalToField() throws Exception {
    AstEditor editor = m_javaInfo.getEditor();
    // add new field and use its name
    {
      String fieldName = addUniqueField(isStaticContext(), m_declaration);
      setName(fieldName);
    }
    // replace local variable declaration with assignment to field
    {
      VariableDeclaration declaration = m_declaration;
      if (declaration.getInitializer() != null) {
        replaceDeclarationWithAssignment(editor, declaration);
      } else {
        editor.removeStatement((Statement) declaration.getParent());
      }
    }
    // use field variable support
    m_javaInfo.setVariableSupport(new FieldUniqueVariableSupport(m_javaInfo, m_variable));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Adding
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String add_getVariableStatementSource(StatementTarget associationTarget) throws Exception {
    AstEditor editor = m_javaInfo.getEditor();
    // prepare component class
    String className = m_javaInfo.getDescription().getComponentClass().getName();
    // prepare variable declaration statement
    NodeTarget creationTarget = new NodeTarget(associationTarget);
    String initializer = m_javaInfo.getCreationSupport().add_getSource(creationTarget);
    String variableName =
        editor.getUniqueVariableName(
            associationTarget.getPosition(),
            NamesManager.getName(m_javaInfo),
            null);
    initializer = StringUtils.replace(initializer, "%variable-name%", variableName);
    String modifiers = prefDeclareFinal() ? "final " : "";
    return modifiers + className + " " + variableName + " = " + initializer + ";";
  }

  @Override
  public void add_setVariableStatement(Statement statement) throws Exception {
    VariableDeclarationStatement variableDeclarationStatement =
        (VariableDeclarationStatement) statement;
    VariableDeclaration fragment =
        (VariableDeclaration) variableDeclarationStatement.fragments().get(0);
    add_setVariableAndInitializer(fragment.getName(), fragment.getInitializer());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void deleteBefore() throws Exception {
    // remove "initializer in declaration" from related nodes, so prevent declaration removing
    {
      Expression initializer = m_declaration.getInitializer();
      m_javaInfo.getRelatedNodes().remove(initializer);
    }
  }

  @Override
  public void deleteAfter() throws Exception {
    if (m_javaInfo.isRoot()) {
      return;
    }
    m_javaInfo.getEditor().removeVariableDeclaration(m_declaration);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Morphing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setType(String newTypeName) throws Exception {
    AstEditor editor = m_javaInfo.getEditor();
    editor.replaceVariableType(m_declaration, newTypeName);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Inlining
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if this {@link LocalUniqueVariableSupport} has only one reference, so
   *         can be inlined.
   */
  public boolean canInline() {
    List<Expression> references = Lists.newArrayList(getReferences());
    references.remove(m_variable);
    return references.size() == 1;
  }

  /**
   * Inlines this {@link LocalUniqueVariableSupport} into its single use.
   *
   * @throws IllegalStateException
   *           if variable is used in more than one place.
   */
  public void inline() throws Exception {
    AstEditor editor = m_javaInfo.getEditor();
    // prepare single reference on component, that should be replaced with creation Expression
    Expression reference;
    {
      List<Expression> references = Lists.newArrayList(getReferences());
      references.remove(m_variable);
      Preconditions.checkState(references.size() == 1, references);
      reference = references.get(0);
    }
    // prepare "initializer"
    Expression initializer = m_declaration.getInitializer();
    AstEditor.replaceNode(initializer, initializer.getAST().newSimpleName("__wbp_tmp"));
    // replace "reference" with "initializer"
    replaceReferenceWithInitializer(reference, initializer);
    // remove VariableDeclarationStatement
    editor.removeStatement(getDeclarationStatement());
    // use EmptyVariableSupport
    m_javaInfo.setVariableSupport(new EmptyVariableSupport(m_javaInfo, initializer));
  }

  private void replaceReferenceWithInitializer(Expression reference, Expression initializer)
      throws Exception {
    AstEditor editor = m_javaInfo.getEditor();
    // check if initializer should be wrapped with ParenthesizedExpression
    if (reference.getParent() instanceof Expression) {
      Expression parent = (Expression) reference.getParent();
      int parentPrecedence = OperatorPrecedence.getExpressionPrecedence(parent);
      int initializerPrecedence = OperatorPrecedence.getExpressionPrecedence(initializer);
      if (parentPrecedence > initializerPrecedence) {
        String parenthesizedSource = "(" + editor.getSource(initializer) + ")";
        editor.replaceSubstring(
            reference.getStartPosition(),
            reference.getLength(),
            parenthesizedSource);
        // prepare ParenthesizedExpression
        ParenthesizedExpression parenthesized = reference.getAST().newParenthesizedExpression();
        parenthesized.setExpression(initializer);
        parenthesized.setSourceRange(reference.getStartPosition(), 1 + initializer.getLength() + 1);
        // replace "reference"
        AstEditor.replaceNode(reference, parenthesized);
        AstNodeUtils.moveNode(initializer, 1 + parenthesized.getStartPosition());
        m_javaInfo.addRelatedNode(parenthesized);
        return;
      }
    }
    // simple case - just replace "reference" with "initializer"
    editor.replaceSubstring(
        reference.getStartPosition(),
        reference.getLength(),
        editor.getSource(initializer));
    AstEditor.replaceNode(reference, initializer);
    AstNodeUtils.moveNode(initializer, reference.getStartPosition());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Preferences
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String BASE = "variable.localUnique.";
  public static final String P_DECLARE_FINAL = BASE + "final";

  /**
   * @return <code>true</code> if local variable should be declared "final".
   */
  private boolean prefDeclareFinal() {
    return getPreferences().getBoolean(P_DECLARE_FINAL);
  }
}
