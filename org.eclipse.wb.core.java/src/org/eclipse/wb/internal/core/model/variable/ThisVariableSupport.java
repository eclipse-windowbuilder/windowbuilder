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
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.ObjectInfoVisitor;
import org.eclipse.wb.internal.core.model.creation.ExposedPropertyCreationSupport;
import org.eclipse.wb.internal.core.model.creation.IExposedCreationSupport;
import org.eclipse.wb.internal.core.model.generation.GenerationSettings;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * {@link VariableSupport} implementation for "this" component.
 *
 * @author scheglov_ke
 * @coverage core.model.variable
 */
public final class ThisVariableSupport extends AbstractNoNameVariableSupport {
  private final MethodDeclaration m_constructor;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ThisVariableSupport(JavaInfo javaInfo, MethodDeclaration constructor) {
    super(javaInfo);
    m_constructor = constructor;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "this";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getTitle() throws Exception {
    Class<?> objectClass = m_javaInfo.getDescription().getComponentClass();
    return "(" + objectClass.getName() + ")";
  }

  @Override
  public String getComponentName() {
    return "this";
  }

  @Override
  public boolean isValidStatementForChild(Statement statement) {
    return true;
  }

  /**
   * @return the constructor that used for this component.
   */
  public MethodDeclaration getConstructor() {
    return m_constructor;
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
  public String getReferenceExpression(NodeTarget target) throws Exception {
    return "this";
  }

  @Override
  public String getAccessExpression(NodeTarget target) throws Exception {
    return "";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Target
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public StatementTarget getStatementTarget() throws Exception {
    // check for "forced" method
    {
      MethodDeclaration forcedMethod = forced_getMethod(m_javaInfo);
      // target - begin of "forced" method
      if (forcedMethod != null) {
        return new StatementTarget(forcedMethod, true);
      }
    }
    // if first statement in constructor is "super", add after it
    List<Statement> statements = DomGenerics.statements(m_constructor.getBody());
    if (!statements.isEmpty()) {
      Statement statement = statements.get(0);
      if (statement instanceof SuperConstructorInvocation) {
        return new StatementTarget(statement, false);
      }
    }
    // in other case add as first statement in constructor
    return new StatementTarget(m_constructor, true);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Forced method/target
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * When there is "forced" method configured, see {@link GenerationSettings#getForcedMethodName()},
   * we should add new components into this "forced" method.
   * <p>
   * When there are no "forced" method yet, we create it, move existing nodes from constructor, and
   * return {@link StatementTarget} in this "forced" method. If there is already existing "forced"
   * method, normal {@link JavaInfoUtils#getTarget(JavaInfo, JavaInfo)} can analyze execution flow
   * and provide valid target, after all components and their nodes.
   * <p>
   * Preconditions: all components and their related {@link ASTNode}'s are located in
   * {@link #m_constructor}.
   *
   * @param javaInfo
   *          the parent to which new component should be added, should be "this" component, or any
   *          exposed child of "this" component.
   *
   * @return the {@link StatementTarget} in "forced" method, or <code>null</code> if "forced" method
   *         can not be used because preconditions are not satisfied.
   */
  public static StatementTarget getForcedTarget(JavaInfo javaInfo) throws Exception {
    if (!shouldUseForcedTarget(javaInfo)) {
      return null;
    }
    // target - end of new "forced" method
    MethodDeclaration forcedMethod = forced_getMethod(javaInfo);
    if (forcedMethod != null) {
      return new StatementTarget(forcedMethod, false);
    }
    // no "forced" method
    return null;
  }

  /**
   * @return <code>true</code> if given container is "this" component or its exposed part.
   */
  private static boolean shouldUseForcedTarget(JavaInfo container) {
    if (container.getVariableSupport() instanceof ThisVariableSupport) {
      return true;
    }
    if (container.getCreationSupport() instanceof IExposedCreationSupport) {
      IExposedCreationSupport exposedCreationSupport =
          (IExposedCreationSupport) container.getCreationSupport();
      JavaInfo host = exposedCreationSupport.getHostJavaInfo();
      return shouldUseForcedTarget(host);
    }
    if (ExposedPropertyCreationSupport.isReplacementForExposed(container)) {
      JavaInfo host = container.getParentJava();
      return shouldUseForcedTarget(host);
    }
    return false;
  }

  /**
   * If "forced" method is configured, see {@link GenerationSettings#getForcedMethodName()}, we can
   * return "forced" method in two cases:
   * <ol>
   * <li>when last statement of constructor is method invocation (in this type). In this case, we
   * return this method as "forced".</li>
   * <li>when all components and their nodes are in constructor, so we can create new "forced"
   * method using {@link #forced_getNewMethod(JavaInfo)}.</li>
   * </ol>
   * <p>
   * In other cases, we return <code>null</code>, so usual constructor handling should be used.
   *
   * @return the "forced" {@link MethodDeclaration}, or <code>null</code>.
   */
  private static MethodDeclaration forced_getMethod(JavaInfo javaInfo) throws Exception {
    javaInfo = javaInfo.getRootJava();
    if (!(javaInfo.getVariableSupport() instanceof ThisVariableSupport)) {
      return null;
    }
    MethodDeclaration constructor =
        ((ThisVariableSupport) javaInfo.getVariableSupport()).getConstructor();
    // prepare "forced" method name
    String forcedMethodName = forced_getMethodName(javaInfo);
    if (forcedMethodName == null) {
      return null;
    }
    // try to find existing "forced" method
    {
      MethodDeclaration existingMethod =
          forced_getExistingMethod(javaInfo, constructor, forcedMethodName);
      if (existingMethod != null) {
        return existingMethod;
      }
    }
    // try to create new "forced" method
    return forced_getNewMethod(javaInfo, forcedMethodName);
  }

  /**
   * @return the {@link MethodDeclaration} which can be considered as "forced". It should be invoked
   *         as last {@link Statement} in constructor.
   */
  private static MethodDeclaration forced_getExistingMethod(JavaInfo javaInfo,
      MethodDeclaration constructor,
      String forcedMethodName) {
    // prepare last statement
    Statement statement;
    {
      Block body = constructor.getBody();
      while (true) {
        List<Statement> statements = DomGenerics.statements(body);
        statement = GenericsUtils.getLastOrNull(statements);
        // dive into TryStatement
        if (statement instanceof TryStatement) {
          body = ((TryStatement) statement).getBody();
          continue;
        }
        // done
        break;
      }
    }
    // check last statement
    if (statement instanceof ExpressionStatement) {
      ExpressionStatement expressionStatement = (ExpressionStatement) statement;
      if (expressionStatement.getExpression() instanceof MethodInvocation) {
        MethodInvocation invocation = (MethodInvocation) expressionStatement.getExpression();
        if (invocation.getName().getIdentifier().equals(forcedMethodName)) {
          String methodSignature = AstNodeUtils.getMethodSignature(invocation);
          TypeDeclaration typeDeclaration = (TypeDeclaration) constructor.getParent();
          MethodDeclaration methodDeclaration =
              AstNodeUtils.getMethodBySignature(typeDeclaration, methodSignature);
          if (methodDeclaration != null) {
            return methodDeclaration;
          }
        }
      }
    }
    // invalid statement
    return null;
  }

  /**
   * If "forced" method is configured, see {@link GenerationSettings#getForcedMethodName()}, and all
   * components and their related {@link ASTNode}'s are located in constructor, we create new
   * "forced" method and move all related nodes into it. If there is already "forced" method,
   * <code>null</code> will be returned, we recognize here only one situation - when there are no
   * forced method.
   *
   * @return the new "forced" method.
   */
  private static MethodDeclaration forced_getNewMethod(JavaInfo parent, String forcedMethodName)
      throws Exception {
    // check that root component is "this" component
    final JavaInfo root;
    final MethodDeclaration constructor;
    {
      root = parent.getRootJava();
      if (!(root.getVariableSupport() instanceof ThisVariableSupport)) {
        return null;
      }
      constructor = ((ThisVariableSupport) root.getVariableSupport()).getConstructor();
    }
    // check that related nodes of all children JavaInfo's are in constructor
    {
      final boolean[] validNodes = new boolean[]{true};
      root.accept(new ObjectInfoVisitor() {
        @Override
        public void endVisit(ObjectInfo objectInfo) throws Exception {
          if (objectInfo instanceof JavaInfo) {
            JavaInfo javaInfo = (JavaInfo) objectInfo;
            for (ASTNode node : javaInfo.getRelatedNodes()) {
              MethodDeclaration method = AstNodeUtils.getEnclosingMethod(node);
              if (method != null) {
                validNodes[0] &= method == constructor;
              }
            }
          }
        }
      });
      // no, there are nodes in other methods than constructor
      if (!validNodes[0]) {
        return null;
      }
    }
    // create "forced" method
    AstEditor editor = root.getEditor();
    MethodDeclaration forcedMethod =
        editor.addMethodDeclaration(
            "private void " + forcedMethodName + "()",
            Collections.<String>emptyList(),
            new BodyDeclarationTarget(constructor, false));
    // move all statements from constructor into "forced" method
    {
      List<Statement> statements = new ArrayList<Statement>(DomGenerics.statements(constructor));
      for (Statement statement : statements) {
        // ignore "super" constructor
        if (statement instanceof SuperConstructorInvocation) {
          continue;
        }
        // skip if references parameters
        if (forced_hasReferenceOnMethodParameter(statement)) {
          continue;
        }
        // do move
        editor.moveStatement(statement, new StatementTarget(forcedMethod, false));
      }
    }
    // add forced method invocation in constructor
    editor.addStatement(forcedMethodName + "();", new StatementTarget(constructor, false));
    return forcedMethod;
  }

  /**
   * @return the name of "forced" method, or <code>null</code> if no "forced" method configured for
   *         toolkit of given {@link JavaInfo}.
   */
  private static String forced_getMethodName(JavaInfo javaInfo) {
    return javaInfo.getDescription().getToolkit().getGenerationSettings().getForcedMethodName();
  }

  /**
   * @return <code>true</code> if given {@link Statement} has references on enclosing
   *         {@link MethodDeclaration} parameters.
   */
  private static boolean forced_hasReferenceOnMethodParameter(Statement statement) {
    final MethodDeclaration method = AstNodeUtils.getEnclosingMethod(statement);
    final AtomicBoolean result = new AtomicBoolean(false);
    statement.accept(new ASTVisitor() {
      @Override
      public void endVisit(SimpleName node) {
        if (!result.get() && AstNodeUtils.isVariable(node)) {
          if (isReferenceOnParameter(node)) {
            result.set(true);
          }
        }
      }

      private boolean isReferenceOnParameter(SimpleName variable) {
        IVariableBinding binding = AstNodeUtils.getVariableBinding(variable);
        for (SingleVariableDeclaration parameter : DomGenerics.parameters(method)) {
          if (parameter.resolveBinding() == binding) {
            return true;
          }
        }
        return false;
      }
    });
    return result.get();
  }
}
