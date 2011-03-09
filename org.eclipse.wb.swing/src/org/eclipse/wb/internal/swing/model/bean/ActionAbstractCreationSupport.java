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
package org.eclipse.wb.internal.swing.model.bean;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.eval.AstEvaluationEngine;
import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.eval.ExecutionFlowUtils.ExecutionFlowFrameVisitor;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.ConstructorDescription;
import org.eclipse.wb.internal.core.model.description.ParameterDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

/**
 * Implementation of {@link CreationSupport} for {@link Action}.
 * 
 * @author sablin_aa
 * @coverage swing.model
 */
public abstract class ActionAbstractCreationSupport extends CreationSupport
    implements
      IActionSupport {
  protected ClassInstanceCreation m_creation;
  protected TypeDeclaration m_typeDeclaration;
  protected List<Block> m_initializingBlocks;
  protected ComponentDescription m_typeDescription;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  protected ActionAbstractCreationSupport() throws Exception {
  }

  protected ActionAbstractCreationSupport(ClassInstanceCreation creation) throws Exception {
    setCreation(creation);
    updateTypeDescription();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "abstractAction";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // State
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets the {@link ClassInstanceCreation}, initializes internal state.
   */
  protected void setCreation(ClassInstanceCreation creation) throws Exception {
    m_creation = creation;
    setCreationEx();
    addInitializationBlocks();
  }

  /**
   * Additional processing of {@link ClassInstanceCreation} in superclasses.
   */
  protected void setCreationEx() {
  }

  /**
   * Adds initializer {@link Block}s into {@link #m_initializingBlocks}.
   */
  protected void addInitializationBlocks() {
    m_initializingBlocks = Lists.newArrayList();
    for (Initializer typeInitializer : DomGenerics.initializers(m_typeDeclaration, false)) {
      m_initializingBlocks.add(typeInitializer.getBody());
    }
  }

  /**
   * Prepares {@link ComponentDescription} for super class, in field {@link #m_typeDescription}.
   */
  protected void updateTypeDescription() throws Exception {
    if (m_typeDescription == null && m_javaInfo != null && m_typeDeclaration != null) {
      // prepare super Class
      String superClassName = getActionClassName();
      Class<?> superClass = JavaInfoUtils.getClassLoader(m_javaInfo).loadClass(superClassName);
      // load description for super Class
      m_typeDescription =
          ComponentDescriptionHelper.getDescription(m_javaInfo.getEditor(), superClass);
    }
  }

  protected String getActionClassName() throws Exception {
    ITypeBinding typeBinding = AstNodeUtils.getTypeBinding(m_typeDeclaration);
    return AstNodeUtils.getFullyQualifiedName(typeBinding.getSuperclass(), true);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public ASTNode getNode() {
    return m_creation;
  }

  @Override
  public boolean isJavaInfo(ASTNode node) {
    return node == m_creation;
  }

  @Override
  public void setJavaInfo(JavaInfo javaInfo) throws Exception {
    super.setJavaInfo(javaInfo);
    updateTypeDescription();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Evaluation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean canBeEvaluated() {
    return false;
  }

  @Override
  public Object create(EvaluationContext context, ExecutionFlowFrameVisitor visitor)
      throws Exception {
    AbstractAction action = create_createObject(context);
    create_evaluateInitialization(context, action);
    // OK, we have Action instance
    return action;
  }

  /**
   * @return {@link AbstractAction} instance.
   */
  protected abstract AbstractAction create_createObject(EvaluationContext context) throws Exception;

  /**
   * Evaluate putValue(key,value) invocations in top-level constructor statements
   */
  protected void create_evaluateInitialization(EvaluationContext context, AbstractAction action)
      throws Exception {
    for (Block constructorBlock : m_initializingBlocks) {
      for (Statement statement : DomGenerics.statements(constructorBlock)) {
        create_evaluateStatement(context, action, statement);
      }
    }
  }

  /**
   * Evaluate any {@link Statement} in initialization blocks.
   */
  protected void create_evaluateStatement(EvaluationContext context,
      AbstractAction action,
      Statement statement) throws Exception {
    if (statement instanceof ExpressionStatement) {
      ExpressionStatement expressionStatement = (ExpressionStatement) statement;
      updateAction_ExpressionStatement(context, action, expressionStatement);
    }
  }

  /**
   * Updates {@link Action} instance using {@link Action#putValue(String, Object)} invocation.
   */
  private void updateAction_ExpressionStatement(EvaluationContext context,
      AbstractAction action,
      ExpressionStatement expressionStatement) throws Exception {
    if (expressionStatement.getExpression() instanceof MethodInvocation) {
      MethodInvocation invocation = (MethodInvocation) expressionStatement.getExpression();
      if (invocation.getExpression() == null
          && AstNodeUtils.getMethodSignature(invocation).equals(
              "putValue(java.lang.String,java.lang.Object)")) {
        // evaluate key/value
        List<Expression> arguments = DomGenerics.arguments(invocation);
        String key = (String) AstEvaluationEngine.evaluate(context, arguments.get(0));
        Object value = AstEvaluationEngine.evaluate(context, arguments.get(1));
        // put value
        action.putValue(key, value);
      }
    }
  }

  /**
   * Evaluate constructor arguments.
   */
  protected void evaluateConstructorArguments(EvaluationContext context,
      AbstractAction action,
      ConstructorDescription constructor,
      List<Expression> arguments) throws Exception {
    for (ParameterDescription parameter : constructor.getParameters()) {
      String key = parameter.getTag("actionKey");
      if (key != null) {
        Expression argument = arguments.get(parameter.getIndex());
        Object value = AstEvaluationEngine.evaluate(context, argument);
        action.putValue(key, value);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean canDelete() {
    return true;
  }

  @Override
  public void delete() throws Exception {
    JavaInfoUtils.deleteJavaInfo(m_javaInfo, true);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IActionSupport
  //
  ////////////////////////////////////////////////////////////////////////////
  public ASTNode getCreation() {
    return m_creation;
  }

  public List<Block> getInitializationBlocks() {
    return m_initializingBlocks;
  }
}
