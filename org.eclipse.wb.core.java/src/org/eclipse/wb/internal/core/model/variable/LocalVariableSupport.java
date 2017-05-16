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

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import java.util.List;

/**
 * {@link VariableSupport} implementation for local variable.
 *
 * @author scheglov_ke
 * @coverage core.model.variable
 */
public abstract class LocalVariableSupport extends AbstractSimpleVariableSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LocalVariableSupport(JavaInfo javaInfo) {
    super(javaInfo);
  }

  public LocalVariableSupport(JavaInfo javaInfo, SimpleName variable) {
    super(javaInfo, variable);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final boolean isValidStatementForChild(Statement statement) {
    StatementTarget statementTarget = new StatementTarget(statement, true);
    NodeTarget nodeTarget = new NodeTarget(statementTarget);
    return isVisibleAtTarget(nodeTarget);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Conversion
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final boolean canConvertLocalToField() {
    // right now we support only one variable declaration
    return getDeclarationStatement().fragments().size() == 1;
  }

  @Override
  public final boolean canConvertFieldToLocal() {
    return false;
  }

  @Override
  public final void convertFieldToLocal() throws Exception {
    throw new IllegalStateException();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Moving
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Ensures that variable has name (existing or re-generated) such that it does not conflict with
   * other variables related with given position (visible or can be shadowed).
   *
   * @param newPosition
   *          the position where this variable will be declared.
   * @param statementsToMove
   *          the {@link Statement}'s that will be moved.
   */
  void ensureUniqueVariableDuringMove(int newPosition, Statement[] statementsToMove)
      throws Exception {
    // prepare potentially conflicting declarations
    List<VariableDeclaration> existingDeclarations;
    {
      existingDeclarations = Lists.newArrayList();
      // prepare state
      AstEditor editor = m_javaInfo.getEditor();
      CompilationUnit unit = editor.getAstUnit();
      VariableDeclaration declaration = m_declaration;
      // in any case add visible variables
      existingDeclarations.addAll(AstNodeUtils.getVariableDeclarationsVisibleAt(unit, newPosition));
      // in any case add shadow variables in *old* position
      existingDeclarations.addAll(AstNodeUtils.getVariableDeclarationsAfter(
          unit,
          declaration.getStartPosition()));
      // if moved declaration is not in Block, then it can shadow variables below
      {
        boolean isInBlock = false;
        for (Statement statement : statementsToMove) {
          isInBlock |= statement instanceof Block && AstNodeUtils.contains(statement, declaration);
        }
        if (!isInBlock) {
          existingDeclarations.addAll(AstNodeUtils.getVariableDeclarationsAfter(unit, newPosition));
        }
      }
      // exclude this variable
      existingDeclarations.remove(declaration);
    }
    // do generate unique name
    {
      String oldName = getName();
      String newName = AstEditor.getUniqueVariableName(existingDeclarations, oldName);
      if (!oldName.equals(newName)) {
        setName(newName);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link VariableDeclarationStatement} that declares this variable.
   */
  protected final VariableDeclarationStatement getDeclarationStatement() {
    return (VariableDeclarationStatement) m_declaration.getParent();
  }

  /**
   * Adds new {@link FieldDeclaration} with name based on the name of this variable.
   *
   * @return the identifier of added field
   */
  protected final String addUniqueField(boolean isStatic, VariableDeclaration excludedVariable)
      throws Exception {
    AstEditor editor = m_javaInfo.getEditor();
    // prepare source of new field declaration
    String fieldName = m_utils.getUniqueFieldName(getName(), excludedVariable);
    String fieldSource;
    {
      Type type = getDeclarationStatement().getType();
      // prepare modifiers
      String modifiers = isStatic ? "private static " : "private ";
      // prepare source
      fieldSource = modifiers + editor.getSource(type) + " " + fieldName + ";";
    }
    // add new field declaration
    addField(fieldSource);
    // return field name
    return fieldName;
  }

  /**
   * Replaces given {@link VariableDeclarationStatement} with {@link ExpressionStatement} with
   * {@link Assignment} to the same variable.
   */
  protected final void replaceDeclarationWithAssignment(AstEditor editor,
      VariableDeclaration oldFragment) throws Exception {
    VariableDeclarationStatement oldStatement =
        (VariableDeclarationStatement) oldFragment.getParent();
    Assert.isTrue(oldStatement.fragments().size() == 1);
    int oldStart = oldStatement.getStartPosition();
    // prepare ExpressionStatement with assignment of existing expression to existing variable
    ExpressionStatement newStatement;
    {
      AST ast = m_variable.getAST();
      // prepare assignment
      Assignment newAssignment;
      {
        newAssignment = ast.newAssignment();
        // reuse variable
        {
          oldFragment.setName(ast.newSimpleName("__foo"));
          newAssignment.setLeftHandSide(m_variable);
        }
        // reuse initializer
        {
          Expression initializer = oldFragment.getInitializer();
          oldFragment.setInitializer(null);
          newAssignment.setRightHandSide(initializer);
        }
        // source range
        AstNodeUtils.setSourceRange(
            newAssignment,
            newAssignment.getLeftHandSide(),
            newAssignment.getRightHandSide());
      }
      // prepare ExpressionStatement
      newStatement = ast.newExpressionStatement(newAssignment);
      AstNodeUtils.setSourceRange(newStatement, newAssignment, 1);
    }
    // replace old expression in AST
    {
      List<Statement> oldStatements = DomGenerics.statements((Block) oldStatement.getParent());
      int index = oldStatements.indexOf(oldStatement);
      oldStatements.set(index, newStatement);
    }
    // patch source to make it same as changed AST
    editor.replaceSubstring(oldStart, m_variable.getStartPosition() - oldStart, "");
  }
}
