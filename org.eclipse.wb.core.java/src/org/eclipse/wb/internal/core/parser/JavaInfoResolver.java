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
package org.eclipse.wb.internal.core.parser;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.eclipse.wb.core.eval.ExecutionFlowDescription;
import org.eclipse.wb.core.eval.ExecutionFlowUtils2;
import org.eclipse.wb.core.eval.ExpressionValue;
import org.eclipse.wb.core.model.IWrapper;
import org.eclipse.wb.core.model.IWrapperInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.ObjectInfoVisitor;
import org.eclipse.wb.internal.core.model.creation.CastedSuperInvocationCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.ExposedFieldCreationSupport;
import org.eclipse.wb.internal.core.model.creation.IImplicitCreationSupport;
import org.eclipse.wb.internal.core.model.creation.ThisCreationSupport;
import org.eclipse.wb.internal.core.model.creation.WrapperMethodControlCreationSupport;
import org.eclipse.wb.internal.core.model.creation.factory.InstanceFactoryInfo;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.TypeLiteral;

import java.util.List;
import java.util.Set;

/**
 * Mapper of {@link ASTNode} into {@link JavaInfo}.
 *
 * @author scheglov_ke
 * @coverage core.model.parser
 */
public final class JavaInfoResolver {
  private final EditorState m_editorState;
  private final CompilationUnit m_unit;
  private final List<JavaInfo> m_components = Lists.newArrayList();
  private boolean m_thisJavaInfoReady = false;
  private JavaInfo m_thisJavaInfo = null;
  private JavaInfo m_rootJavaInfo = null;
  private final Set<Expression> m_expressions = Sets.newHashSet();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public JavaInfoResolver(AstEditor editor) {
    m_editorState = EditorState.get(editor);
    m_editorState.setJavaInfoResolver(this);
    m_unit = editor.getAstUnit();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets the root {@link JavaInfo} after parsing. So, after this resolver will track only
   * {@link JavaInfo} it this hierarchy.
   */
  public void setRootJavaInfo(JavaInfo rootJavaInfo) {
    m_rootJavaInfo = rootJavaInfo;
  }

  /**
   * Adds new {@link JavaInfo} during parsing.
   */
  public void addJavaInfo(JavaInfo javaInfo, Expression creation) {
    m_components.add(javaInfo);
    if (creation != null) {
      bind(javaInfo, creation);
    }
  }

  /**
   * Specifies that given {@link Expression} is creation of given {@link JavaInfo}.
   */
  public void bind(JavaInfo javaInfo, Expression expression) {
    ExpressionValue value = ExecutionFlowUtils2.ensurePermanentValue(expression);
    value.setModel(javaInfo);
  }

  /**
   * @param someJavaInfo
   *          some {@link JavaInfo} in same hierarchy.
   *
   * @return the {@link JavaInfo} for given {@link Expression}, may be <code>null</code>.
   */
  public static JavaInfo getJavaInfo(JavaInfo someJavaInfo, Expression expression) {
    Assert.isNotNull(someJavaInfo);
    EditorState editorState = EditorState.get(someJavaInfo.getEditor());
    JavaInfoResolver resolver = editorState.getJavaInfoResolver();
    return resolver.getJavaInfo(expression);
  }

  /**
   * @return the {@link JavaInfo} for given {@link Expression} or <code>null</code> if given
   *         {@link Expression} does not represent {@link JavaInfo}.
   */
  public JavaInfo getJavaInfo(Expression expression) {
    // prevent infinite recursion
    if (m_expressions.contains(expression)) {
      return null;
    }
    // analyze expression
    try {
      m_expressions.add(expression);
      return getJavaInfo0(expression);
    } catch (Throwable e) {
      throw ReflectionUtils.propagate(e);
    } finally {
      m_expressions.remove(expression);
    }
  }

  private JavaInfo getJavaInfo0(Expression expression) throws Exception {
    // check for nodes that can not be JavaInfo
    if (expression instanceof NullLiteral
        || expression instanceof BooleanLiteral
        || expression instanceof NumberLiteral
        || expression instanceof StringLiteral
        || expression instanceof TypeLiteral
        || expression instanceof Assignment
        || expression instanceof ArrayCreation
        || expression instanceof ArrayInitializer) {
      return null;
    }
    // "null" or "this"
    if (expression == null || expression instanceof ThisExpression) {
      if (!m_thisJavaInfoReady) {
        m_thisJavaInfoReady = true;
        for (JavaInfo component : getComponents()) {
          if (component.getCreationSupport() instanceof ThisCreationSupport) {
            m_thisJavaInfo = component;
            break;
          }
        }
      }
      if (m_thisJavaInfo != null) {
        m_thisJavaInfo.addRelatedNode(expression);
      }
      return m_thisJavaInfo;
    }
    // try to find tracked value
    {
      ExecutionFlowDescription flowDescription = m_editorState.getFlowDescription();
      ExpressionValue value = ExecutionFlowUtils2.getValue(flowDescription, expression);
      if (value != null) {
        // may be bound JavaInfo creation/access
        JavaInfo javaInfo = (JavaInfo) value.getModel();
        if (javaInfo != null) {
          boolean expressionPartOfCasted =
              javaInfo.getCreationSupport() instanceof CastedSuperInvocationCreationSupport
                  && expression.getLocationInParent() == CastExpression.EXPRESSION_PROPERTY;
          if (!expressionPartOfCasted) {
            javaInfo.addRelatedNode(expression);
          }
          return javaInfo;
        }
        // resolve "this"
        {
          Expression newExpression = value.getExpression();
          if (newExpression instanceof ThisExpression && newExpression != expression) {
            return getJavaInfo0(newExpression);
          }
        }
      }
    }
    // XXX special support for RCP FormToolkit
    if (AstNodeUtils.isSuccessorOf(expression, "org.eclipse.ui.forms.widgets.FormToolkit")) {
      for (JavaInfo component : getComponents()) {
        if (component instanceof InstanceFactoryInfo) {
          if (component.getCreationSupport().isJavaInfo(expression)) {
            ExecutionFlowUtils2.ensurePermanentValue(expression).setModel(component);
            component.addRelatedNode(expression);
            return component;
          }
        }
      }
    }
    // invocation
    if (expression instanceof MethodInvocation) {
      MethodInvocation invocation = (MethodInvocation) expression;
      String invName = invocation.getName().getIdentifier();
      // XXX special support for GWT RootPanel
      if (invName.equals("get")
          && AstNodeUtils.isSuccessorOf(invocation, "com.google.gwt.user.client.ui.RootPanel")) {
        for (JavaInfo component : getComponents()) {
          if (component.getCreationSupport().isJavaInfo(invocation)) {
            ExecutionFlowUtils2.ensurePermanentValue(expression).setModel(component);
            component.addRelatedNode(expression);
            return component;
          }
        }
      }
      // generic get()
      if (invName.startsWith("get")) {
        JavaInfo expressionJavaInfo = getJavaInfo(invocation.getExpression());
        // getContentPane()
        {
          JavaInfo result = getExposedJavaInfo(expressionJavaInfo, invocation);
          if (result != null) {
            return result;
          }
        }
        // viewer.getTable()
        if (expressionJavaInfo instanceof IWrapperInfo) {
          IWrapperInfo wrapperInfo = (IWrapperInfo) expressionJavaInfo;
          IWrapper wrapper = wrapperInfo.getWrapper();
          JavaInfo javaInfo = wrapper.getWrappedInfo();
          if (wrapper.isWrappedInfo(invocation)) {
            ExecutionFlowUtils2.ensurePermanentValue(expression).setModel(javaInfo);
            javaInfo.addRelatedNode(expression);
            return javaInfo;
          }
        }
      }
      return null;
    }
    // m_exposedField
    if (expression instanceof SimpleName) {
      SimpleName simpleName = (SimpleName) expression;
      JavaInfo thisJavaInfo = getJavaInfo(null);
      String fieldName = simpleName.getIdentifier();
      return getExposedJavaInfo(thisJavaInfo, expression, fieldName);
    }
    // someJavaInfo.m_exposedField
    if (expression instanceof QualifiedName) {
      QualifiedName qualifiedName = (QualifiedName) expression;
      JavaInfo hostJavaInfo = getJavaInfo(qualifiedName.getQualifier());
      String fieldName = qualifiedName.getName().getIdentifier();
      return getExposedJavaInfo(hostJavaInfo, expression, fieldName);
    }
    // no
    return null;
  }

  /**
   * @return the {@link JavaInfo} which is child of "parent" and exposed by given
   *         {@link MethodInvocation}.
   */
  private JavaInfo getExposedJavaInfo(JavaInfo parent, MethodInvocation invocation) {
    if (parent == null) {
      return null;
    }
    // direct child
    for (JavaInfo child : parent.getChildrenJava()) {
      CreationSupport childCreationSupport = child.getCreationSupport();
      if (childCreationSupport instanceof IImplicitCreationSupport) {
        if (childCreationSupport.isJavaInfo(invocation)) {
          ExecutionFlowUtils2.ensurePermanentValue(invocation).setModel(child);
          child.addRelatedNode(invocation);
          return child;
        }
      }
    }
    // logical child
    for (JavaInfo child : parent.getChildrenJava()) {
      CreationSupport childCreationSupport = child.getCreationSupport();
      if (childCreationSupport instanceof IImplicitCreationSupport) {
        JavaInfo result = getExposedJavaInfo(child, invocation);
        if (result != null) {
          return result;
        }
      }
    }
    // wrapped child
    for (JavaInfo child : parent.getChildrenJava()) {
      CreationSupport childCreationSupport = child.getCreationSupport();
      if (childCreationSupport instanceof WrapperMethodControlCreationSupport) {
        JavaInfo result = getExposedJavaInfo(child, invocation);
        if (result != null) {
          return result;
        }
      }
    }
    // replaced exposed
    {
      String expectedSetName = "set" + invocation.getName().getIdentifier().substring(3);
      for (MethodInvocation parentInvocation : parent.getMethodInvocations()) {
        if (parentInvocation.arguments().size() == 1) {
          String setName = parentInvocation.getName().getIdentifier();
          if (setName.equals(expectedSetName)) {
            Expression setExpression = DomGenerics.arguments(parentInvocation).get(0);
            JavaInfo result = getJavaInfo(setExpression);
            if (result != null) {
              ExecutionFlowUtils2.ensurePermanentValue(invocation).setModel(result);
              result.addRelatedNode(invocation);
              return result;
            }
          }
        }
      }
    }
    // not found
    return null;
  }

  /**
   * @return the {@link JavaInfo} which is child of "parent" and exposed by given
   *         {@link MethodInvocation}.
   */
  private JavaInfo getExposedJavaInfo(JavaInfo parent, Expression expression, String fieldName) {
    if (parent == null) {
      return null;
    }
    // direct child
    for (JavaInfo child : parent.getChildrenJava()) {
      if (child.getCreationSupport() instanceof ExposedFieldCreationSupport) {
        ExposedFieldCreationSupport cs = (ExposedFieldCreationSupport) child.getCreationSupport();
        if (cs.getField().getName().equals(fieldName)) {
          child.addRelatedNode(expression);
          return child;
        }
      }
    }
    // logical child
    for (JavaInfo child : parent.getChildrenJava()) {
      CreationSupport childCreationSupport = child.getCreationSupport();
      if (childCreationSupport instanceof IImplicitCreationSupport) {
        JavaInfo result = getExposedJavaInfo(child, expression, fieldName);
        if (result != null) {
          return result;
        }
      }
    }
    // not found
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String KEY_COMPONENTS_STAMP = "JavaInfoResolver.components.stamp";

  /**
   * @return the actual {@link JavaInfo} list, updates {@link #m_components} if AST was changed.
   */
  public List<JavaInfo> getComponents() throws Exception {
    if (m_rootJavaInfo == null) {
      return m_components;
    }
    // refresh m_components if needed
    Long stampComponents = (Long) m_unit.getProperty(KEY_COMPONENTS_STAMP);
    long stampAST = m_unit.getAST().modificationCount();
    if (stampComponents == null || stampAST != stampComponents.longValue()) {
      m_components.clear();
      m_rootJavaInfo.accept0(new ObjectInfoVisitor() {
        @Override
        public void endVisit(ObjectInfo objectInfo) throws Exception {
          if (objectInfo instanceof JavaInfo) {
            JavaInfo javaInfo = (JavaInfo) objectInfo;
            if (!isExplicitJavaInfo(javaInfo)) {
              return;
            }
            m_components.add(javaInfo);
          }
        }

        private boolean isExplicitJavaInfo(JavaInfo javaInfo) {
          CreationSupport creationSupport = javaInfo.getCreationSupport();
          if (creationSupport instanceof IImplicitCreationSupport) {
            return false;
          }
          return true;
        }
      });
      m_unit.setProperty(KEY_COMPONENTS_STAMP, stampAST);
    }
    // done
    return m_components;
  }
}
