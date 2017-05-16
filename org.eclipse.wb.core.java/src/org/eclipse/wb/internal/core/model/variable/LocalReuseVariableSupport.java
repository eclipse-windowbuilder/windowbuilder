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
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.check.Assert;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import java.util.List;

/**
 * {@link VariableSupport} implementation for local, reused variable.
 *
 * @author scheglov_ke
 * @coverage core.model.variable
 */
public final class LocalReuseVariableSupport extends LocalVariableSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LocalReuseVariableSupport(JavaInfo javaInfo, SimpleName variable) {
    super(javaInfo, variable);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "local-reused: " + getName();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Name
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setName(String newName) throws Exception {
    convertLocalToField();
    m_javaInfo.getVariableSupport().setName(newName);
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
    // if old declaration is with our initializer, split it on declaration and assignment
    if (m_declaration.getName() == m_variable) {
      splitVariable();
    }
    // add new field and use its name
    {
      String fieldName = addUniqueField(isStaticContext(), null);
      replaceComponentReferences(fieldName);
    }
    // use field variable support
    m_javaInfo.setVariableSupport(new FieldUniqueVariableSupport(m_javaInfo, m_variable));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Morphing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void setType(String newTypeName) throws Exception {
    // if old declaration is with our initializer, split it on declaration and assignment
    if (m_declaration.getName() == m_variable) {
      splitVariable();
    }
    // check that existing assignment is part of ExpressionStatement
    Assignment oldAssignment;
    ExpressionStatement oldStatement;
    {
      Assert.isTrue(
          m_variable.getLocationInParent() == Assignment.LEFT_HAND_SIDE_PROPERTY
              && m_variable.getParent().getLocationInParent() == ExpressionStatement.EXPRESSION_PROPERTY,
          "Variable should be part of Assignment in ExpressionStatement, for "
              + AstNodeUtils.getEnclosingStatement(m_variable));
      oldAssignment = (Assignment) m_variable.getParent();
      oldStatement = (ExpressionStatement) AstNodeUtils.getEnclosingStatement(oldAssignment);
    }
    // convert Assignment into VariableDeclarationStatement with required Type
    {
      AstEditor editor = m_javaInfo.getEditor();
      AST ast = m_variable.getAST();
      // initial values
      int position = oldStatement.getStartPosition();
      String source = "";
      // add type
      Type newType;
      {
        newType = editor.getParser().parseQualifiedType(position, newTypeName);
        source += newTypeName + " ";
      }
      // add variable
      SimpleName newVariable;
      {
        // prepare identifier
        String identifier;
        {
          identifier = AstNodeUtils.getVariableName(m_variable);
          identifier = editor.getUniqueVariableName(position, identifier, null);
          replaceComponentReferences(identifier);
        }
        // prepare variable
        newVariable =
            editor.getParser().parseVariable(
                position + source.length(),
                identifier,
                null,
                AstNodeUtils.getTypeBinding(newType),
                false,
                Modifier.NONE);
        source += identifier;
      }
      // add " = "
      {
        source += " = ";
      }
      // add initializer
      Expression newInitializer;
      {
        newInitializer = oldAssignment.getRightHandSide();
        oldAssignment.setRightHandSide(ast.newSimpleName("__bar__"));
        String initializerSource = editor.getSource(newInitializer);
        AstNodeUtils.moveNode(newInitializer, position + source.length());
        source += initializerSource;
      }
      // create fragment
      VariableDeclarationFragment newFragment;
      {
        newFragment = ast.newVariableDeclarationFragment();
        newFragment.setName(newVariable);
        newFragment.setInitializer(newInitializer);
        AstNodeUtils.setSourceRange(newFragment, newVariable, newInitializer);
      }
      // create statement
      VariableDeclarationStatement newStatement;
      {
        newStatement = ast.newVariableDeclarationStatement(newFragment);
        newStatement.setType(newType);
        AstNodeUtils.setSourceRange(newStatement, newType, newFragment, 1);
        source += ";";
      }
      // replace old statement with new statement
      {
        List<Statement> statements = DomGenerics.statements((Block) oldStatement.getParent());
        int index = statements.indexOf(oldStatement);
        // replace without "oldStatement", so its parts are not moved
        statements.remove(index);
        editor.replaceSubstring(oldStatement, source);
        // now we can add "newStatement", we created it already with correct positions
        statements.add(index, newStatement);
        // finalize source modifications
        editor.resolveImports(newStatement);
      }
      // now our JavaInfo uses LocalUniqueVariableSupport
      m_javaInfo.setVariableSupport(new LocalUniqueVariableSupport(m_javaInfo, newVariable));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Split
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * This method should be used if this variable is initialized in
   * {@link VariableDeclarationFragment} and we want split it on separate
   * {@link VariableDeclarationStatement} and assignment. There is such quick assist in Eclipse.
   */
  private void splitVariable() throws Exception {
    AstEditor editor = m_javaInfo.getEditor();
    // prepare current information
    VariableDeclaration oldFragment = m_declaration;
    VariableDeclarationStatement oldStatement = getDeclarationStatement();
    // add new VariableDeclarationStatement before oldStatement
    // we should create new variable because "m_variable" is place of _assignment_
    {
      int position = oldStatement.getStartPosition();
      String source = "";
      // add type
      Type newType;
      {
        Type oldType = oldStatement.getType();
        newType = editor.getParser().parseType(position, oldType);
        source += editor.getSource(oldType) + " ";
      }
      // add variable
      SimpleName newVariable;
      {
        String identifier = AstNodeUtils.getVariableName(m_variable);
        newVariable =
            editor.getParser().parseVariable(
                position + source.length(),
                identifier,
                null,
                AstNodeUtils.getTypeBinding(newType),
                false,
                Modifier.NONE);
        source += identifier;
      }
      // create fragment
      VariableDeclarationFragment newFragment;
      {
        newFragment = m_variable.getAST().newVariableDeclarationFragment();
        newFragment.setName(newVariable);
        AstNodeUtils.copySourceRange(newFragment, newVariable);
      }
      // create statement
      VariableDeclarationStatement newStatement;
      {
        newStatement = m_variable.getAST().newVariableDeclarationStatement(newFragment);
        newStatement.setType(newType);
        AstNodeUtils.setSourceRange(newStatement, newType, newFragment, 1);
        source += ";";
      }
      // modify source
      {
        String prefix = editor.getWhitespaceToLeft(position, false);
        source += editor.getGeneration().getEndOfLine() + prefix;
        editor.replaceSubstring(position, 0, source);
      }
      // add new statement to AST
      {
        List<Statement> statements = DomGenerics.statements((Block) oldStatement.getParent());
        int index = statements.indexOf(oldStatement);
        statements.add(index, newStatement);
      }
    }
    // convert old declaration to assignment
    replaceDeclarationWithAssignment(editor, oldFragment);
    rememberDeclaration();
  }
}
