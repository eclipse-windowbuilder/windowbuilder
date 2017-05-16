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
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import java.util.List;

/**
 * {@link VariableSupport} implementation for case when it is possible to have variable for this
 * component, but currently there are no variable. For example:
 *
 * <pre>
 * 	add(new JButton("My button"), BorderLayout.NORTH);
 * </pre>
 *
 * Here expression/initializer for JButton is <code>new JButton("My button")</code>.
 * <p>
 * {@link EmptyVariableSupport} itself does not supports adding new {@link JavaInfo}, but there are
 * specific sub-classes that generate one or other {@link Statement}'s with initializer expressions.
 *
 * @author scheglov_ke
 * @coverage core.model.variable
 */
public class EmptyVariableSupport extends VariableSupport {
  private Expression m_initializer;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  protected EmptyVariableSupport(JavaInfo javaInfo) {
    super(javaInfo);
  }

  public EmptyVariableSupport(JavaInfo javaInfo, Expression initializer) {
    super(javaInfo);
    m_initializer = initializer;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final String toString() {
    return "empty";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final boolean isDefault() {
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Special access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the initializer {@link Expression} that represents this {@link JavaInfo}.
   */
  public final Expression getInitializer() {
    return m_initializer;
  }

  /**
   * Converts this "empty" variable into existing local variable.
   */
  public final void materialize() throws Exception {
    m_javaInfo.getBroadcastJava().variable_emptyMaterializeBefore(this);
    if (getEnclosingField() != null) {
      materialize_newField();
    } else if (isPureInitializer()) {
      materialize_sameStatement();
    } else {
      materialize_newStatement();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Name
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final boolean hasName() {
    return true;
  }

  @Override
  public final String getName() {
    return null;
  }

  @Override
  public final void setName(String newName) throws Exception {
    materialize();
    m_javaInfo.getVariableSupport().setName(newName);
  }

  @Override
  public final String getTitle() throws Exception {
    return "(no variable)";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Expressions
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean hasExpression(NodeTarget target) {
    return true;
  }

  @Override
  public final String getReferenceExpression(NodeTarget target) throws Exception {
    ensureLocalVariable_withUpdatingTarget(target);
    return m_javaInfo.getVariableSupport().getReferenceExpression(target);
  }

  @Override
  public final String getAccessExpression(NodeTarget target) throws Exception {
    return getReferenceExpression(target) + ".";
  }

  private void ensureLocalVariable_withUpdatingTarget(NodeTarget target) throws Exception {
    StatementTarget statementTarget = target.getStatementTarget();
    if (statementTarget != null) {
      boolean targetUsesThisStatement = statementTarget.getStatement() == getStatement();
      materialize();
      if (targetUsesThisStatement) {
        statementTarget.setStatement(getStatement());
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Conversion
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final boolean canConvertLocalToField() {
    return true;
  }

  @Override
  public final void convertLocalToField() throws Exception {
    materialize();
    m_javaInfo.getVariableSupport().convertLocalToField();
  }

  @Override
  public final boolean canConvertFieldToLocal() {
    return true;
  }

  @Override
  public final void convertFieldToLocal() throws Exception {
    materialize();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Target
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final StatementTarget getStatementTarget() throws Exception {
    materialize();
    return m_javaInfo.getVariableSupport().getStatementTarget();
  }

  @Override
  public void ensureInstanceReadyAt(StatementTarget target) throws Exception {
    if (shouldMoveOnlyInitializater()) {
      ExpressionStatement statement = (ExpressionStatement) m_initializer.getParent();
      m_javaInfo.getEditor().moveStatement(statement, target);
    } else {
      materialize();
      m_javaInfo.getVariableSupport().ensureInstanceReadyAt(target);
    }
  }

  @Override
  public StatementTarget getAssociationTarget(StatementTarget target) throws Exception {
    return target;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal checks
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if initializer is direct child of {@link ExpressionStatement}, i.e.
   *         for example just <code>new Button(parent, style);</code>.
   */
  private boolean isPureInitializer() {
    return m_initializer.getParent() instanceof ExpressionStatement;
  }

  /**
   * Checks if only initializer {@link ExpressionStatement} should be moved.
   * <p>
   * In some exotic situations {@link JavaInfo} with {@link EmptyVariableSupport} still can have
   * children, so example in GWT <code>MenuBar.addItem(text, childMenuBar)</code>. So, in this case
   * we should move these children too, not just single statement.
   */
  private boolean shouldMoveOnlyInitializater() {
    if (isPureInitializer()) {
      return m_javaInfo.getChildrenJava().isEmpty();
    }
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Declaration
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Statement} of {@link #m_initializer}.
   */
  private Statement getStatement() {
    return AstNodeUtils.getEnclosingStatement(m_initializer);
  }

  /**
   * @return the {@link FieldDeclaration} that encloses {@link #m_initializer}, may be
   *         <code>null</code>.
   */
  private FieldDeclaration getEnclosingField() {
    return AstNodeUtils.getEnclosingFieldDeclaration(m_initializer);
  }

  /**
   * Converts this "empty" variable into existing local variable.<br>
   * Case when initializer {@link Expression} is direct child of {@link ExpressionStatement}.
   */
  private void materialize_sameStatement() throws Exception {
    AstEditor editor = m_javaInfo.getEditor();
    ExpressionStatement oldStatement = (ExpressionStatement) m_initializer.getParent();
    // prepare type
    ITypeBinding typeBinding;
    String typeName;
    {
      typeBinding = AstNodeUtils.getTypeBinding(m_initializer);
      typeName = editor.getTypeBindingSource(typeBinding);
    }
    // initialize position and source
    int position = oldStatement.getStartPosition();
    String source = "";
    // add type
    Type newType;
    {
      newType = editor.getParser().parseQualifiedType(position, typeName);
      source += typeName + " ";
    }
    // add variable
    String variableName;
    SimpleName newDeclarationVariable;
    {
      variableName =
          editor.getUniqueVariableName(
              m_initializer.getStartPosition(),
              NamesManager.getName(m_javaInfo),
              null);
      newDeclarationVariable =
          editor.getParser().parseVariable(
              position + source.length(),
              variableName,
              null,
              typeBinding,
              false,
              Modifier.NONE);
      source += variableName + " = ";
    }
    // move initializer
    {
      String initializerSource = editor.getSource(m_initializer);
      // replace initializer, so "free" it to use with different parent
      oldStatement.setExpression(editor.getParser().parseSimpleName(0, "__wbp_Tmp"));
      // use initializer for declaration
      AstNodeUtils.moveNode(m_initializer, position + source.length());
      source += initializerSource;
    }
    // add fragment
    VariableDeclarationFragment newFragment;
    {
      newFragment = m_initializer.getAST().newVariableDeclarationFragment();
      newFragment.setName(newDeclarationVariable);
      newFragment.setInitializer(m_initializer);
      AstNodeUtils.setSourceRange(newFragment, newDeclarationVariable, m_initializer);
    }
    // add statement
    VariableDeclarationStatement newStatement;
    {
      newStatement = m_initializer.getAST().newVariableDeclarationStatement(newFragment);
      newStatement.setType(newType);
      AstNodeUtils.setSourceRange(newStatement, newType, newFragment, 1);
      source += ";";
    }
    // update source
    editor.replaceSubstring(oldStatement.getStartPosition(), oldStatement.getLength(), source);
    // set new statement into AST
    {
      List<Statement> statements = DomGenerics.statements((Block) oldStatement.getParent());
      int index = statements.indexOf(oldStatement);
      statements.set(index, newStatement);
      editor.resolveImports(newStatement);
    }
    // use local variable support
    m_javaInfo.setVariableSupport(new LocalUniqueVariableSupport(m_javaInfo, newDeclarationVariable));
  }

  /**
   * Converts this "empty" variable into existing local variable.<br>
   * Case when initializer {@link Expression} is part of some other {@link Expression}.
   */
  private void materialize_newStatement() throws Exception {
    AstEditor editor = m_javaInfo.getEditor();
    Statement oldStatement = getStatement();
    // prepare type
    ITypeBinding typeBinding;
    String typeName;
    {
      typeBinding = AstNodeUtils.getTypeBinding(m_initializer);
      typeName = editor.getTypeBindingSource(typeBinding);
    }
    // initialize position and source
    int position = oldStatement.getStartPosition();
    String source = "";
    // add type
    Type newType;
    {
      newType = editor.getParser().parseQualifiedType(position, typeName);
      source += typeName + " ";
    }
    // add variable
    String variableName;
    SimpleName newDeclarationVariable;
    {
      variableName =
          editor.getUniqueVariableName(
              m_initializer.getStartPosition(),
              NamesManager.getName(m_javaInfo),
              null);
      newDeclarationVariable =
          editor.getParser().parseVariable(
              position + source.length(),
              variableName,
              null,
              typeBinding,
              false,
              Modifier.NONE);
      source += variableName + " = ";
    }
    // move initializer
    {
      String initializerSource = editor.getSource(m_initializer);
      int originalInitializerPosition = m_initializer.getStartPosition();
      int originalInitializerLength = m_initializer.getLength();
      // replace initializer with variable
      {
        SimpleName newUseVariable =
            editor.getParser().parseVariable(
                originalInitializerPosition,
                variableName,
                null,
                typeBinding,
                false,
                Modifier.NONE);
        //
        AstEditor.replaceNode(m_initializer, newUseVariable);
        m_javaInfo.addRelatedNode(newUseVariable);
        editor.replaceSubstring(
            originalInitializerPosition,
            originalInitializerLength,
            newUseVariable.getIdentifier());
        AstNodeUtils.moveNode(newUseVariable, originalInitializerPosition);
        editor.inlineParenthesizedExpression(newUseVariable);
      }
      // use initializer for declaration
      AstNodeUtils.moveNode(m_initializer, position + source.length());
      source += initializerSource;
    }
    // add fragment
    VariableDeclarationFragment newFragment;
    {
      newFragment = m_initializer.getAST().newVariableDeclarationFragment();
      newFragment.setName(newDeclarationVariable);
      newFragment.setInitializer(m_initializer);
      AstNodeUtils.setSourceRange(newFragment, newDeclarationVariable, m_initializer);
    }
    // add statement
    VariableDeclarationStatement newStatement;
    {
      newStatement = m_initializer.getAST().newVariableDeclarationStatement(newFragment);
      newStatement.setType(newType);
      AstNodeUtils.setSourceRange(newStatement, newType, newFragment, 1);
      source += ";";
    }
    // add EOL and prefix
    source += editor.getGeneration().getEndOfLine() + editor.getWhitespaceToLeft(position, false);
    // update source
    editor.replaceSubstring(position, 0, source);
    // add new statement to AST
    {
      List<Statement> statements = DomGenerics.statements((Block) oldStatement.getParent());
      int index = statements.indexOf(oldStatement);
      statements.add(index, newStatement);
      editor.resolveImports(newStatement);
    }
    // use local variable support
    m_javaInfo.setVariableSupport(new LocalUniqueVariableSupport(m_javaInfo, newDeclarationVariable));
  }

  /**
   * Converts this "empty" variable into {@link LocalUniqueVariableSupport}.
   * <p>
   * Case when initializer {@link Expression} is part of other {@link FieldDeclaration}.
   */
  private void materialize_newField() throws Exception {
    AstEditor editor = m_javaInfo.getEditor();
    FieldDeclaration oldField = getEnclosingField();
    // prepare type
    ITypeBinding typeBinding;
    String typeName;
    {
      typeBinding = AstNodeUtils.getTypeBinding(m_initializer);
      typeName = editor.getTypeBindingSource(typeBinding);
    }
    // initialize position and source
    int position = oldField.getStartPosition();
    String source = "";
    // modifier
    {
      source += "private ";
    }
    // add type
    Type newType;
    {
      newType = editor.getParser().parseQualifiedType(position + source.length(), typeName);
      source += typeName + " ";
    }
    // add variable
    String variableName;
    SimpleName newDeclarationVariable;
    {
      variableName =
          editor.getUniqueVariableName(
              m_initializer.getStartPosition(),
              NamesManager.getName(m_javaInfo),
              null);
      newDeclarationVariable =
          editor.getParser().parseVariable(
              position + source.length(),
              variableName,
              null,
              typeBinding,
              false,
              Modifier.NONE);
      source += variableName + " = ";
    }
    // move initializer
    {
      String initializerSource = editor.getSource(m_initializer);
      int originalInitializerPosition = m_initializer.getStartPosition();
      int originalInitializerLength = m_initializer.getLength();
      // replace initializer with variable
      {
        SimpleName newUseVariable =
            editor.getParser().parseVariable(
                originalInitializerPosition,
                variableName,
                null,
                typeBinding,
                false,
                Modifier.NONE);
        //
        AstEditor.replaceNode(m_initializer, newUseVariable);
        m_javaInfo.addRelatedNode(newUseVariable);
        editor.replaceSubstring(
            originalInitializerPosition,
            originalInitializerLength,
            newUseVariable.getIdentifier());
        AstNodeUtils.moveNode(newUseVariable, originalInitializerPosition);
        editor.inlineParenthesizedExpression(newUseVariable);
      }
      // use initializer for declaration
      AstNodeUtils.moveNode(m_initializer, position + source.length());
      source += initializerSource;
    }
    // add fragment
    VariableDeclarationFragment newFragment;
    {
      newFragment = m_initializer.getAST().newVariableDeclarationFragment();
      newFragment.setName(newDeclarationVariable);
      newFragment.setInitializer(m_initializer);
      AstNodeUtils.setSourceRange(newFragment, newDeclarationVariable, m_initializer);
    }
    // add statement
    FieldDeclaration newField;
    {
      newField = m_initializer.getAST().newFieldDeclaration(newFragment);
      newField.setType(newType);
      AstNodeUtils.setSourceRange(newField, newType, newFragment, 1);
      source += ";";
    }
    // add EOL and prefix
    source += editor.getGeneration().getEndOfLine() + editor.getWhitespaceToLeft(position, false);
    // update source
    editor.replaceSubstring(position, 0, source);
    // add new statement to AST
    {
      TypeDeclaration typeDeclaration = (TypeDeclaration) oldField.getParent();
      List<BodyDeclaration> bodyDeclarations = DomGenerics.bodyDeclarations(typeDeclaration);
      int index = bodyDeclarations.indexOf(oldField);
      bodyDeclarations.add(index, newField);
      editor.resolveImports(newField);
    }
    // use local variable support
    m_javaInfo.setVariableSupport(new FieldInitializerVariableSupport(m_javaInfo,
        newDeclarationVariable));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Adding
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Special method for initializing {@link EmptyVariableSupport} using pure {@link Expression}.
   */
  protected final void add_setInitializer(Expression initializer) throws Exception {
    m_initializer = initializer;
    m_javaInfo.addRelatedNode(m_initializer);
    m_javaInfo.getCreationSupport().add_setSourceExpression(m_initializer);
  }
}
