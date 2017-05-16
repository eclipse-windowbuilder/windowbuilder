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
package org.eclipse.wb.core.eval;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.eclipse.wb.core.eval.ExecutionFlowUtils.ExecutionFlowFrameVisitor;
import org.eclipse.wb.core.eval.ExecutionFlowUtils.VisitingContext;
import org.eclipse.wb.internal.core.model.variable.LazyVariableSupport.LazyVariableInformation;
import org.eclipse.wb.internal.core.model.variable.LazyVariableSupportUtils;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The utility class for handling execution flow in AST.
 *
 * We use it in any place where we need visit AST - during parsing, components creation, etc.
 *
 * @author scheglov_ke
 * @coverage core.evaluation
 */
public final class ExecutionFlowUtils2 {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private ExecutionFlowUtils2() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String STAMP_AST = "ExecutionFlowUtils2.cache_stampAST";
  private static final String STAMP_FLOW = "ExecutionFlowUtils2.cache_stampFlow";
  private static final String KEY_VALUE = "ExecutionFlowUtils2.ExpressionValue";
  private static final String KEY_VALUE_PREV = "ExecutionFlowUtils2.ExpressionValue.prev";
  private static final String KEY_VALUE_PERMANENT = "ExecutionFlowUtils2.ExpressionValue.permanent";

  /**
   * Sets the {@link ExpressionValue}.
   */
  public static void setValue0(Expression expression, ExpressionValue value) {
    expression.setProperty(KEY_VALUE, value);
  }

  /**
   * @return the existing {@link ExpressionValue} without trying to calculate it.
   */
  public static ExpressionValue getValue0(Expression expression) {
    ExpressionValue value = (ExpressionValue) expression.getProperty(KEY_VALUE);
    if (value == null) {
      value = (ExpressionValue) expression.getProperty(KEY_VALUE_PERMANENT);
    }
    return value;
  }

  /**
   * Sets the permanent {@link ExpressionValue}.
   */
  public static void setPermanentValue0(Expression expression, ExpressionValue value) {
    expression.setProperty(KEY_VALUE_PERMANENT, value);
  }

  /**
   * @return the existing permanent {@link ExpressionValue} without trying to calculate it.
   */
  public static ExpressionValue getPermanentValue0(Expression expression) {
    return (ExpressionValue) expression.getProperty(KEY_VALUE_PERMANENT);
  }

  /**
   * @return ensures that {@link Expression} has permanent {@link ExpressionValue}, uses existing
   *         value or creates new one.
   */
  public static ExpressionValue ensurePermanentValue(Expression expression) {
    ExpressionValue value;
    {
      // usually there is already some ExpressionValue
      value = (ExpressionValue) expression.getProperty(KEY_VALUE);
      // if no, create new one
      if (value == null) {
        value = new ExpressionValue(expression);
      }
    }
    // use this value as permanent
    expression.setProperty(KEY_VALUE_PERMANENT, value);
    // done
    return value;
  }

  /**
   * Clears permanent {@link ExpressionValue} associated with given {@link Expression}. Permanent
   * value is used to associate model, so practically we remove reference on model.
   */
  public static void clearPermanentValue(Expression expression) {
    ExpressionValue value = (ExpressionValue) expression.getProperty(KEY_VALUE_PERMANENT);
    if (value != null) {
      value.setModel(null);
      expression.setProperty(KEY_VALUE_PERMANENT, null);
    }
  }

  public static ExpressionValue getValue(ExecutionFlowDescription flowDescription,
      Expression expression) {
    ensureValues(flowDescription);
    ExpressionValue value = (ExpressionValue) expression.getProperty(KEY_VALUE);
    if (value == null) {
      value = (ExpressionValue) expression.getProperty(KEY_VALUE_PERMANENT);
    }
    return value;
  }

  public static ExpressionValue getValuePrev(ExecutionFlowDescription flowDescription,
      Expression expression) {
    ensureValues(flowDescription);
    return (ExpressionValue) expression.getProperty(KEY_VALUE_PREV);
  }

  public static void ensureValues(ExecutionFlowDescription flowDescription) {
    ASTNode rootNode = flowDescription.getCompilationUnit();
    long stampAST = rootNode.getAST().modificationCount();
    long stampFlow = flowDescription.modificationCount();
    Long cache_stampAST = (Long) rootNode.getProperty(STAMP_AST);
    Long cache_stampFlow = (Long) rootNode.getProperty(STAMP_FLOW);
    if (cache_stampAST == null
        || cache_stampAST.longValue() != stampAST
        || cache_stampFlow == null
        || cache_stampFlow.longValue() != stampFlow) {
      rootNode.setProperty(STAMP_AST, stampAST);
      rootNode.setProperty(STAMP_FLOW, stampFlow);
      trackValues(flowDescription, rootNode);
    }
  }

  private static void trackValues(ExecutionFlowDescription flowDescription, ASTNode root) {
    ValuesVisitor visitor = new ValuesVisitor();
    ExecutionFlowUtils.visit(new VisitingContext(true), flowDescription, visitor);
    visitor.whenLeave_type_visitRestMethods();
  }

  private static class ValuesVisitor extends ExecutionFlowFrameVisitor {
    ////////////////////////////////////////////////////////////////////////////
    //
    // Frame
    //
    ////////////////////////////////////////////////////////////////////////////
    private Frame m_frame;
    private Frame m_typeFrame;
    private boolean m_ignoreAssignments;
    private final Set<MethodDeclaration> m_visitedMethods = Sets.newHashSet();

    @Override
    public boolean enterFrame(ASTNode node) {
      m_frame = new Frame(node, m_frame);
      defineMethodParameters(node);
      if (node instanceof TypeDeclaration) {
        m_typeFrame = m_frame;
      }
      return true;
    }

    @Override
    public void leaveFrame(ASTNode node) {
      m_frame = m_frame.getParent();
      whenLeave_method_rememberVisited(node);
    }

    /**
     * When leave {@link TypeDeclaration}, visit all {@link MethodDeclaration}s which were not
     * visited yet, to use values assigned to fields.
     */
    void whenLeave_type_visitRestMethods() {
      if (m_typeFrame != null) {
        m_frame = m_typeFrame;
        m_ignoreAssignments = true;
        TypeDeclaration typeDeclaration = (TypeDeclaration) m_typeFrame.getNode();
        for (MethodDeclaration method : typeDeclaration.getMethods()) {
          if (!m_visitedMethods.contains(method)) {
            method.accept(this);
          }
        }
      }
    }

    /**
     * Remember that {@link MethodDeclaration} was visited, to skip it in
     * {@link #whenLeave_type_visitRestMethods(ASTNode)} later.
     */
    private void whenLeave_method_rememberVisited(ASTNode node) {
      if (node instanceof MethodDeclaration) {
        MethodDeclaration method = (MethodDeclaration) node;
        m_visitedMethods.add(method);
      }
    }

    private void defineMethodParameters(ASTNode node) {
      // prepare declaration
      if (!(node instanceof MethodDeclaration)) {
        return;
      }
      MethodDeclaration declaration = (MethodDeclaration) node;
      List<SingleVariableDeclaration> parameters = DomGenerics.parameters(declaration);
      // prepare invocation
      ASTNode invocation;
      {
        String key = ExecutionFlowUtils.KEY_FRAME_INVOCATION;
        invocation = (ASTNode) declaration.getProperty(key);
      }
      // no invocation: value of parameter == its name
      if (invocation == null) {
        for (int i = 0; i < parameters.size(); i++) {
          SingleVariableDeclaration parameter = parameters.get(i);
          define(parameter, (Expression) null);
        }
        return;
      }
      // use argument as value for parameter
      List<Expression> arguments = DomGenerics.arguments(invocation);
      for (int i = 0; i < parameters.size(); i++) {
        SingleVariableDeclaration parameter = parameters.get(i);
        if (arguments.size() > i) {
          Expression argument = arguments.get(i);
          define(parameter, argument);
        }
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Define
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public void endVisit(SimpleName node) {
      String identifier = node.getIdentifier();
      ExpressionValue value = m_frame.getValue(identifier);
      if (value != null) {
        node.setProperty(KEY_VALUE, value);
      }
    }

    @Override
    public void endVisit(FieldAccess node) {
      if (node.getExpression() instanceof ThisExpression) {
        SimpleName variable = node.getName();
        String identifier = variable.getIdentifier();
        ExpressionValue value = m_typeFrame.getValue(identifier);
        if (value != null) {
          node.setProperty(KEY_VALUE, value);
        }
      }
    }

    @Override
    public void endVisit(ParenthesizedExpression node) {
      Expression expression = node.getExpression();
      ExpressionValue value = createValue(expression);
      if (value != null) {
        node.setProperty(KEY_VALUE, value);
      }
    }

    @Override
    public void endVisit(CastExpression node) {
      Expression expression = node.getExpression();
      ExpressionValue value = createValue(expression);
      if (value != null) {
        node.setProperty(KEY_VALUE, value);
      }
    }

    @Override
    public void endVisit(MethodInvocation node) {
      MethodDeclaration method = AstNodeUtils.getLocalMethodDeclaration(node);
      if (method != null) {
        // may be "lazy"
        {
          LazyVariableInformation information = LazyVariableSupportUtils.getInformation(method);
          if (information != null) {
            Expression expression = information.getCreation();
            ExpressionValue value = createValue(expression);
            if (value != null) {
              node.setProperty(KEY_VALUE, value);
            }
            return;
          }
        }
        // may be "return value;"
        boolean isLocalStaticFactory = AstNodeUtils.isStatic(method);
        if (!isLocalStaticFactory) {
          List<Statement> statements = DomGenerics.statements(method);
          Statement lastStatement = GenericsUtils.getLastOrNull(statements);
          if (lastStatement instanceof ReturnStatement) {
            ReturnStatement returnStatement = (ReturnStatement) lastStatement;
            Expression expression = returnStatement.getExpression();
            if (expression != null) {
              ExpressionValue value = createValue(expression);
              node.setProperty(KEY_VALUE, value);
            }
          }
        }
      }
    }

    @Override
    public void endVisit(VariableDeclarationFragment node) {
      define(node, node.getInitializer());
    }

    @Override
    public void endVisit(PostfixExpression node) {
      Expression operand = node.getOperand();
      if (operand instanceof SimpleName) {
        SimpleName variable = (SimpleName) operand;
        String identifier = variable.getIdentifier();
        // remember previous value
        variable.setProperty(KEY_VALUE_PREV, m_frame.getValue(identifier));
        // set new value
        ExpressionValue value = createValue(node);
        m_frame.setValue(identifier, value);
        variable.setProperty(KEY_VALUE, value);
      }
    }

    @Override
    public void endVisit(Assignment node) {
      if (m_ignoreAssignments) {
        return;
      }
      Expression leftSide = node.getLeftHandSide();
      Expression rightSide = node.getRightHandSide();
      if (leftSide instanceof SimpleName) {
        SimpleName variable = (SimpleName) leftSide;
        String identifier = variable.getIdentifier();
        ExpressionValue value = createValue(rightSide);
        m_frame.setValue(identifier, value);
        variable.setProperty(KEY_VALUE, value);
      }
      if (leftSide instanceof FieldAccess) {
        FieldAccess leftFieldAccess = (FieldAccess) leftSide;
        if (leftFieldAccess.getExpression() instanceof ThisExpression) {
          SimpleName variable = leftFieldAccess.getName();
          ExpressionValue value = createValue(rightSide);
          {
            String identifier = variable.getIdentifier();
            m_typeFrame.setValue(identifier, value);
          }
          leftFieldAccess.setProperty(KEY_VALUE, value);
        }
      }
    }

    private void define(VariableDeclaration node, Expression initializer) {
      ExpressionValue value;
      if (initializer != null) {
        value = createValue(initializer);
      } else {
        value = createValue(node.getName());
      }
      // set value
      SimpleName variable = node.getName();
      variable.setProperty(KEY_VALUE, value);
      // fill frame
      String identifier = variable.getIdentifier();
      m_frame.define(identifier, value);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Utils
    //
    ////////////////////////////////////////////////////////////////////////////
    private ExpressionValue createValue(Expression expression) {
      // get existing value
      ExpressionValue value;
      value = (ExpressionValue) expression.getProperty(KEY_VALUE);
      if (value == null) {
        value = (ExpressionValue) expression.getProperty(KEY_VALUE_PERMANENT);
      }
      // create new value
      if (value == null) {
        value = new ExpressionValue(expression);
        expression.setProperty(KEY_VALUE, value);
      }
      // done
      return value;
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Frame
  //
  ////////////////////////////////////////////////////////////////////////////
  private static class Frame {
    private final ASTNode m_node;
    private final Frame m_parent;
    private final Map<String, ExpressionValue> m_variables = Maps.newHashMap();

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public Frame(ASTNode node, Frame parent) {
      m_node = node;
      m_parent = parent;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    public ASTNode getNode() {
      return m_node;
    }

    public Frame getParent() {
      return m_parent;
    }

    public void define(String name, ExpressionValue value) {
      m_variables.put(name, value);
    }

    public void setValue(String name, ExpressionValue value) {
      if (m_variables.containsKey(name)) {
        m_variables.put(name, value);
      } else if (m_parent != null) {
        m_parent.setValue(name, value);
      }
    }

    public ExpressionValue getValue(String name) {
      ExpressionValue value = m_variables.get(name);
      if (value != null) {
        return value;
      }
      if (m_parent != null) {
        return m_parent.getValue(name);
      }
      return null;
    }
  }
}
