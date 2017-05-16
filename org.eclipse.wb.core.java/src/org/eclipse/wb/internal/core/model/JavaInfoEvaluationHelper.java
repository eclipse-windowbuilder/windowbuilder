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
package org.eclipse.wb.internal.core.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.eclipse.wb.core.eval.AstEvaluationEngine;
import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.eval.ExecutionFlowUtils.ExecutionFlowFrameVisitor;
import org.eclipse.wb.core.eval.IExpressionEvaluator;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.eval.evaluators.AnonymousEvaluationError;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.IMethodParameterEvaluator;
import org.eclipse.wb.internal.core.model.creation.IThisMethodParameterEvaluator;
import org.eclipse.wb.internal.core.model.creation.ThisCreationSupport;
import org.eclipse.wb.internal.core.model.description.MethodDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.ThisExpression;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

/**
 * Helper for evaluating parts of AST related with some {@link JavaInfo}.
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public abstract class JavaInfoEvaluationHelper {
  /**
   * The key for accessing {@link AstEditor} using
   * {@link EvaluationContext#getArbitraryValue(Object)}.
   */
  public static final String KEY_EDITOR = "KEY_EXPRESSION_VALUE";
  /**
   * The key for flag on {@link SingleVariableDeclaration}, when {@link Boolean#TRUE}, when
   * {@link #KEY_PARAMETER_VALUE} can be used to access value.
   */
  public static final String KEY_PARAMETER_HAS_VALUE = "KEY_PARAMETER_HAS_VALUE";
  /**
   * The key for accessing value of {@link SingleVariableDeclaration}, when execution flow routed
   * from "binary flow" into {@link MethodDeclaration}.
   */
  public static final String KEY_PARAMETER_VALUE = "KEY_PARAMETER_VALUE";
  ////////////////////////////////////////////////////////////////////////////
  //
  // Value access
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String KEY_EXPRESSION_VALUE = "KEY_EXPRESSION_VALUE";
  private static final Object NULL_VALUE = new Object() {
    @Override
    public String toString() {
      return "NULL_VALUE";
    }
  };

  /**
   * @return <code>true</code> if given {@link Expression} has value property.
   */
  public static boolean hasValue(Expression expression) {
    return expression.properties().containsKey(KEY_EXPRESSION_VALUE);
  }

  /**
   * @return the value of {@link Expression} evaluated during AST execution, or <code>null</code> if
   *         given {@link Expression} was not evaluated.
   */
  public static Object getValue(Expression expression) {
    Object value = expression.getProperty(KEY_EXPRESSION_VALUE);
    if (value == NULL_VALUE) {
      return null;
    }
    return value;
  }

  /**
   * Sets the value associated with {@link Expression} during AST evaluation.
   */
  public static void setValue(Expression expression, Object value) {
    if (value == null) {
      value = NULL_VALUE;
    }
    if (expression != null) {
      expression.setProperty(KEY_EXPRESSION_VALUE, value);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Return value access
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String KEY_RETURN_VALUE = "KEY_RETURN_VALUE";
  private static final String KEY_EVALUATE_RETURN_VALUE = "KEY_EVALUATE_RETURN_VALUE";

  /**
   * @return the value of {@link ReturnStatement} evaluated during AST execution, or
   *         <code>null</code> if returned {@link Expression} was not evaluated.
   */
  public static Object getReturnValue(MethodDeclaration methodDeclaration) {
    return methodDeclaration.getProperty(KEY_RETURN_VALUE);
  }

  /**
   * Sets the value associated with {@link Expression} of {@link ReturnStatement} during AST
   * evaluation.
   */
  private static void setReturnValue(MethodDeclaration methodDeclaration, Object value) {
    methodDeclaration.setProperty(KEY_RETURN_VALUE, value);
  }

  /**
   * Specifies if {@link ReturnStatement} of given {@link MethodDeclaration} should be evaluated.
   */
  public static void shouldEvaluateReturnValue(MethodDeclaration methodDeclaration, boolean evaluate) {
    methodDeclaration.setProperty(KEY_EVALUATE_RETURN_VALUE, evaluate ? Boolean.TRUE : null);
  }

  /**
   * @return <code>true</code> if {@link ReturnStatement} of given {@link MethodDeclaration} should
   *         be evaluated.
   */
  private static boolean shouldEvaluateReturnValue(MethodDeclaration methodDeclaration) {
    return methodDeclaration.getProperty(KEY_EVALUATE_RETURN_VALUE) == Boolean.TRUE;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final AstEditor m_editor;
  private final EditorState m_state;
  private final ExecutionFlowFrameVisitor m_visitor;
  private final EvaluationContext m_context;
  private final Set<MethodInvocation> m_impossibleJavaInfo = Sets.newHashSet();
  private final Set<Expression> m_evaluatedExpressions = Sets.newHashSet();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public JavaInfoEvaluationHelper(AstEditor editor, ExecutionFlowFrameVisitor visitor) {
    m_editor = editor;
    m_state = EditorState.get(editor);
    m_visitor = visitor;
    // prepare evaluation context
    {
      m_context = createEvaluationContext();
      // remember ASTEditor
      m_context.putArbitraryValue(KEY_EDITOR, editor);
      // evaluator for JavaInfo objects
      m_context.addEvaluator(new IExpressionEvaluator() {
        public Object evaluate(EvaluationContext context,
            Expression expression,
            ITypeBinding typeBinding,
            String typeQualifiedName) throws Exception {
          if (m_impossibleJavaInfo.contains(expression)) {
            return AstEvaluationEngine.UNKNOWN;
          }
          if (isChainedInvocation(expression)) {
            return AstEvaluationEngine.UNKNOWN;
          }
          //
          JavaInfo javaInfo = getJavaInfoRepresentedBy(expression);
          if (javaInfo != null) {
            Object object = javaInfo.getObject();
            if (object != null) {
              return object;
            }
          }
          return AstEvaluationEngine.UNKNOWN;
        }

        private boolean isChainedInvocation(Expression expression) {
          if (expression instanceof MethodInvocation) {
            MethodInvocation invocation = (MethodInvocation) expression;
            JavaInfo javaInfo = getJavaInfoRepresentedBy(invocation.getExpression());
            if (javaInfo != null) {
              String signature = AstNodeUtils.getMethodSignature(invocation);
              MethodDescription method = javaInfo.getDescription().getMethod(signature);
              return method != null && method.hasTrueTag("returnThis");
            }
          }
          return false;
        }
      });
    }
  }

  private EvaluationContext createEvaluationContext() {
    return new EvaluationContext(m_state.getEditorLoader(), m_state.getFlowDescription()) {
      ////////////////////////////////////////////////////////////////////////////
      //
      // Access
      //
      ////////////////////////////////////////////////////////////////////////////
      @Override
      public String getSource(ASTNode node) {
        if (node == null) {
          return "<null>";
        }
        return m_editor.getSource(node);
      }

      ////////////////////////////////////////////////////////////////////////////
      //
      // Evaluation
      //
      ////////////////////////////////////////////////////////////////////////////
      @Override
      public void evaluationRequested(Expression expression) throws Exception {
        super.evaluationRequested(expression);
        m_state.getVisitedNodes().add(expression);
      }

      @Override
      public void evaluationSuccessful(Expression expression, Object value) throws Exception {
        super.evaluationSuccessful(expression, value);
        // remember value of Expression
        setValue(expression, value);
        // remember object for JavaInfo
        {
          JavaInfo javaInfo = getJavaInfoRepresentedBy(expression);
          if (javaInfo != null) {
            boolean noObjectYet = javaInfo.getObject() == null;
            if (noObjectYet) {
              javaInfo.setObject(value);
            }
          }
        }
        // check for Assignment
        if (expression instanceof Assignment) {
          Assignment assignment = (Assignment) expression;
          Expression leftHandExpression = assignment.getLeftHandSide();
          // check for: javaInfo.fieldName = someExpression
          if (leftHandExpression instanceof QualifiedName) {
            QualifiedName leftQualifiedName = (QualifiedName) leftHandExpression;
            Name leftQualifier = leftQualifiedName.getQualifier();
            JavaInfo javaInfo = getJavaInfoRepresentedBy(leftQualifier);
            if (javaInfo != null) {
              Object javaInfoObject = javaInfo.getObject();
              Assert.isNotNull(javaInfoObject);
              // set value of field
              String fieldName = leftQualifiedName.getName().getIdentifier();
              ReflectionUtils.setField(javaInfoObject, fieldName, value);
            }
          }
        }
      }

      @Override
      public Object evaluationFailed(Expression expression, Throwable e) throws Exception {
        // fail, if fatal
        if (DesignerExceptionUtils.isFatal(e)) {
          return AstEvaluationEngine.UNKNOWN;
        }
        // non-strict mode
        if (!isStrictEvaluationMode()) {
          // only for not JavaInfo
          if (getJavaInfoRepresentedBy(expression) == null) {
            m_state.getBadRefreshNodes().add(expression, e);
            ITypeBinding binding = AstNodeUtils.getTypeBinding(expression);
            return getDefaultValue(binding);
          }
        }
        // default handling (fail)
        return super.evaluationFailed(expression, e);
      }

      @Override
      public void addException(ASTNode node, Throwable e) {
        m_state.getBadRefreshNodes().add(node, e);
      }

      ////////////////////////////////////////////////////////////////////////////
      //
      // evaluator for "null"/"this" expression of MethodInvocation
      //
      ////////////////////////////////////////////////////////////////////////////
      private MethodInvocation m_invocation;

      @Override
      public Object evaluate(Expression expression) throws Exception {
        // null/this as "expression" of some MethodInvocation
        if (expression == null || expression instanceof ThisExpression) {
          JavaInfo javaInfo = getThisJavaInfo();
          if (javaInfo != null) {
            if (expression == null) {
              thisJavaInfoNodeProcessed(javaInfo, m_invocation);
            }
            return javaInfo.getObject();
          }
        }
        // if CreationSupport says that it should be used for Object creation, then do this
        {
          JavaInfo javaInfo = getJavaInfoRepresentedBy(expression);
          if (javaInfo != null) {
            CreationSupport creationSupport = javaInfo.getCreationSupport();
            if (!creationSupport.canBeEvaluated()) {
              Object object = javaInfo.getObject();
              if (object != null) {
                return object;
              }
              return evaluateJavaInfoUsingCreationSupport(javaInfo);
            }
          }
        }
        // remember last MethodInvocation
        if (expression instanceof MethodInvocation) {
          m_invocation = (MethodInvocation) expression;
        } else {
          m_invocation = null;
        }
        // not a "this"
        return AstEvaluationEngine.UNKNOWN;
      }

      ////////////////////////////////////////////////////////////////////////////
      //
      // Unknown parameters
      //
      ////////////////////////////////////////////////////////////////////////////
      @Override
      public Object evaluateUnknownParameter(MethodDeclaration methodDeclaration,
          SingleVariableDeclaration parameter) throws Exception {
        {
          Object value = evaluateUnknownParameter0(methodDeclaration, parameter);
          if (value != AstEvaluationEngine.UNKNOWN) {
            return value;
          }
        }
        // default handling - will throw exception
        return super.evaluateUnknownParameter(methodDeclaration, parameter);
      }
    };
  }

  private Object evaluateUnknownParameter0(MethodDeclaration method,
      SingleVariableDeclaration parameter) throws Exception {
    // check for external value
    if (parameter.getProperty(KEY_PARAMETER_HAS_VALUE) == Boolean.TRUE) {
      return parameter.getProperty(KEY_PARAMETER_VALUE);
    }
    // check JavaDoc tags
    {
      Object value =
          evaluateJavadocTagExpression(
              m_editor,
              m_context,
              method.getJavadoc(),
              "@wbp.eval.method.parameter",
              parameter.getName().getIdentifier());
      if (value != AstEvaluationEngine.UNKNOWN) {
        return value;
      }
    }
    // prepare more information
    String signature = AstNodeUtils.getMethodSignature(method);
    int index = DomGenerics.parameters(method).indexOf(parameter);
    // check for JavaInfo evaluator
    {
      JavaInfo thisJavaInfo = getThisJavaInfo();
      if (thisJavaInfo instanceof IThisMethodParameterEvaluator) {
        IThisMethodParameterEvaluator evaluator = (IThisMethodParameterEvaluator) thisJavaInfo;
        Object value = evaluator.evaluateParameter(m_context, method, signature, parameter, index);
        if (value != AstEvaluationEngine.UNKNOWN) {
          return value;
        }
      }
    }
    // use IMethodParameterEvaluator
    {
      List<IMethodParameterEvaluator> evaluators =
          ExternalFactoriesHelper.getElementsInstances(
              IMethodParameterEvaluator.class,
              "org.eclipse.wb.core.java.unknownParameterEvaluators",
              "evaluator");
      for (IMethodParameterEvaluator evaluator : evaluators) {
        Object value = evaluator.evaluateParameter(m_context, method, signature, parameter, index);
        if (value != AstEvaluationEngine.UNKNOWN) {
          return value;
        }
      }
    }
    // non-strict mode
    if (!isStrictEvaluationMode()) {
      ITypeBinding binding = AstNodeUtils.getTypeBinding(parameter);
      return getDefaultValue(binding);
    }
    // unknown
    return AstEvaluationEngine.UNKNOWN;
  }

  public static Object getDefaultValue(ITypeBinding binding) {
    // no binding (compilation error), so Object
    if (binding == null) {
      return null;
    }
    // primitive
    if (binding.isPrimitive()) {
      String className = AstNodeUtils.getFullyQualifiedName(binding, true);
      return ReflectionUtils.getDefaultValue(className);
    }
    // String
    if (AstNodeUtils.isSuccessorOf(binding, "java.lang.String")) {
      return "<dynamic>";
    }
    // Collections
    if (AstNodeUtils.isSuccessorOf(binding, "java.util.LinkedList")) {
      return Lists.newLinkedList();
    }
    if (AstNodeUtils.isSuccessorOf(binding, "java.util.Vector")) {
      return new java.util.Vector<Object>();
    }
    if (AstNodeUtils.isSuccessorOf(binding, "java.util.List")) {
      return Lists.newArrayList();
    }
    if (AstNodeUtils.isSuccessorOf(binding, "java.util.Set")) {
      return Sets.newHashSet();
    }
    if (AstNodeUtils.isSuccessorOf(binding, "java.util.Map")) {
      return Maps.newHashMap();
    }
    // Object
    return null;
  }

  private static boolean isStrictEvaluationMode() {
    return DesignerPlugin.getDefault().getPreferenceStore().getBoolean(
        org.eclipse.wb.internal.core.preferences.IPreferenceConstants.P_CODE_STRICT_EVALUATE);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link EvaluationContext} that is used in this {@link JavaInfoEvaluationHelper}
   *         session.
   */
  public final EvaluationContext getContext() {
    return m_context;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Evaluating
  //
  ////////////////////////////////////////////////////////////////////////////
  public final void evaluate(ASTNode node) {
    try {
      // Expression that is JavaInfo creation
      if (isPossibleJavaInfoCreationExpression(node)) {
        Expression expression = (Expression) node;
        JavaInfo javaInfo = getJavaInfoRepresentedBy(expression);
        if (javaInfo != null) {
          evaluateExpression(expression);
          return;
        }
      }
      // check for ReturnStatement
      if (node instanceof ReturnStatement) {
        ReturnStatement returnStatement = (ReturnStatement) node;
        if (shouldEvaluateReturnStatement(returnStatement)) {
          Expression expression = returnStatement.getExpression();
          Object object = evaluateExpression(expression);
          setReturnValue(AstNodeUtils.getEnclosingMethod(returnStatement), object);
        }
      }
      // check for ExpressionStatement
      if (node instanceof ExpressionStatement) {
        ExpressionStatement expressionStatement = (ExpressionStatement) node;
        Expression expression = expressionStatement.getExpression();
        // check for MethodInvocation
        if (expression instanceof MethodInvocation) {
          MethodInvocation invocation = (MethodInvocation) expression;
          // if "expression" of this invocation is JavaInfo
          {
            JavaInfo javaInfo = getJavaInfoRepresentedBy(invocation.getExpression());
            if (javaInfo != null) {
              // add invocation only for "this" component, for other invocations we already have
              // "expression" as related node
              if (invocation.getExpression() == null) {
                // add related node, if it is declared in component class, i.e. is not local
                Class<?> componentClass = javaInfo.getDescription().getComponentClass();
                if (ReflectionUtils.getMethodBySignature(
                    componentClass,
                    AstNodeUtils.getMethodSignature(invocation)) != null) {
                  javaInfo.addRelatedNode(invocation);
                }
              }
              // check that it is possible to evaluate this invocation
              if (!javaInfo.shouldEvaluateInvocation(invocation)) {
                return;
              }
              // OK, now we can evaluate
              m_impossibleJavaInfo.add(invocation);
              evaluateMethodInvocation(javaInfo, invocation);
              return;
            }
          }
        }
        // check for SuperMethodInvocation
        if (expression instanceof SuperMethodInvocation) {
          SuperMethodInvocation invocation = (SuperMethodInvocation) expression;
          evaluateExpression(invocation);
        }
        // check for Assignment
        if (expression instanceof Assignment) {
          evaluateAssignment(expression);
        }
      }
    } catch (Throwable e) {
      ReflectionUtils.propagate(e);
    }
  }

  private static boolean isPossibleJavaInfoCreationExpression(ASTNode node) {
    if (node instanceof CastExpression) {
      CastExpression castExpression = (CastExpression) node;
      return isPossibleJavaInfoCreationExpression(castExpression.getExpression());
    }
    return node instanceof ClassInstanceCreation
        || node instanceof MethodInvocation
        || node instanceof SuperMethodInvocation;
  }

  private void evaluateAssignment(Expression expression) throws Exception {
    Assignment assignment = (Assignment) expression;
    Expression leftHandExpression = assignment.getLeftHandSide();
    Expression rightHandExpression = assignment.getRightHandSide();
    // check for: someName = javaInfoExpression
    if (getJavaInfoRepresentedBy(rightHandExpression) != null) {
      evaluateExpression(rightHandExpression);
    }
    // check for: javaInfo.fieldName = someExpression
    {
      Expression leftQualifier = AstNodeUtils.getFieldAccessQualifier(leftHandExpression);
      if (leftQualifier != null) {
        JavaInfo javaInfo = getJavaInfoRepresentedBy(leftQualifier);
        if (javaInfo != null) {
          evaluateExpression(leftQualifier);
          Object javaInfoObject = javaInfo.getObject();
          Assert.isNotNull(javaInfoObject);
          // prepare field
          Field field;
          {
            String fieldName = AstNodeUtils.getFieldAccessName(leftHandExpression).getIdentifier();
            field = ReflectionUtils.getFieldByName(javaInfoObject.getClass(), fieldName);
          }
          // set value of field
          if (field != null) {
            Object value = evaluateExpression(rightHandExpression);
            field.set(javaInfoObject, value);
          }
        }
      }
    }
  }

  /**
   * @return the value of given {@link MethodInvocation} of {@link JavaInfo}.
   */
  public final void evaluateMethodInvocation(JavaInfo javaInfo, MethodInvocation invocation)
      throws Exception {
    try {
      evaluateExpression(invocation);
    } catch (Error e) {
      if (AnonymousEvaluationError.is(e)) {
      } else {
        throw e;
      }
    }
  }

  /**
   * @return the value of given {@link Expression}.
   */
  public final Object evaluateExpression(Expression expression) throws Exception {
    if (m_evaluatedExpressions.contains(expression)) {
      return getValue(expression);
    }
    m_evaluatedExpressions.add(expression);
    // evaluate
    Object result = AstEvaluationEngine.evaluate(m_context, expression);
    Assert.isTrue(result != AstEvaluationEngine.UNKNOWN);
    return result;
  }

  /**
   * Evaluates given {@link JavaInfo} using its {@link CreationSupport}.
   *
   * @return the resulting object.
   */
  public final Object evaluateJavaInfoUsingCreationSupport(JavaInfo javaInfo) throws Exception {
    // create object
    Object object = javaInfo.getCreationSupport().create(m_context, m_visitor);
    Assert.isNotNull(object);
    // set it as value and return
    javaInfo.setObject(object);
    return object;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the root {@link JavaInfo}, possible {@link JavaInfo} for {@link ThisExpression}.
   */
  protected JavaInfo getRootJavaInfo() {
    throw new NotImplementedException(getClass());
  }

  /**
   * We use this method to send notification, that {@link ASTNode} for "this" {@link JavaInfo} was
   * processed.
   */
  protected void thisJavaInfoNodeProcessed(JavaInfo javaInfo, ASTNode node) throws Exception {
    throw new NotImplementedException(getClass());
  }

  /**
   * @return the {@link JavaInfo} that represents given {@link Expression}.
   */
  protected abstract JavaInfo getJavaInfoRepresentedBy(Expression expression);

  /**
   * @return the {@link JavaInfo} that should be used for "this" {@link Expression}.
   */
  private JavaInfo getThisJavaInfo() {
    JavaInfo rootJavaInfo = getRootJavaInfo();
    // check that root JavaInfo is "this"
    if (rootJavaInfo != null) {
      if (rootJavaInfo.getCreationSupport() instanceof ThisCreationSupport) {
        return rootJavaInfo;
      }
    }
    // no, some different JavaInfo (or may be not JavaInfo at all)
    return null;
  }

  private boolean shouldEvaluateReturnStatement(ReturnStatement returnStatement) throws Exception {
    // check forced evaluation
    {
      MethodDeclaration enclosingMethod = AstNodeUtils.getEnclosingMethod(returnStatement);
      if (shouldEvaluateReturnValue(enclosingMethod)) {
        return true;
      }
    }
    // evaluate if JavaInfo
    Expression expression = returnStatement.getExpression();
    return getJavaInfoRepresentedBy(expression) != null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Unknown parameters support
  //
  ////////////////////////////////////////////////////////////////////////////
  //private static final String TAG_EVAL_PARAMETER = "@wbp.eval.method.parameter";
  /**
   * Evaluates value of expression specified in JavaDoc comments like this:
   *
   * @wbp.eval.method.parameter someName someExpression
   *
   * @param javadoc
   *          the {@link Javadoc} that should be checked.
   * @param tagName
   *          the name of tag, "@wbp.eval.method.parameter" in example above.
   * @param expectedName
   *          the name of expression, "someName" in example above.
   */
  public static Object evaluateJavadocTagExpression(AstEditor editor,
      EvaluationContext context,
      Javadoc javadoc,
      String tagName,
      String expectedName) throws Exception {
    if (javadoc != null) {
      for (TagElement tag : DomGenerics.tags(javadoc)) {
        if (tagName.equals(tag.getTagName())) {
          // extract <name> and <expression text>
          String name;
          String expressionText;
          {
            String errorMessage =
                "'" + tagName + " <name> = <expression>' expected but '" + tag + "' found.";
            Assert.isLegal(tag.fragments().size() == 1, errorMessage);
            // prepare TextElement parts
            TextElement textElement = (TextElement) tag.fragments().get(0);
            String text = textElement.getText().trim();
            String[] parts = StringUtils.split(text);
            Assert.isLegal(parts.length >= 2, errorMessage);
            // assign parts
            name = parts[0];
            expressionText = text.substring(name.length()).trim();
          }
          // check that we found expected <name>
          if (name.equals(expectedName)) {
            Object value = tag.getProperty(KEY_EXPRESSION_VALUE);
            // evaluate value only one time
            if (value == null) {
              Expression expression =
                  editor.getParser().parseExpression(javadoc.getStartPosition(), expressionText);
              value = AstEvaluationEngine.evaluate(context, expression);
              tag.setProperty(KEY_EXPRESSION_VALUE, value);
            }
            // return value
            return value;
          }
        }
      }
    }
    // no value found
    return AstEvaluationEngine.UNKNOWN;
  }
}
