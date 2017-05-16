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
package org.eclipse.wb.internal.core.databinding.utils;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.eval.AstEvaluationEngine;
import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.state.EditorState;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import org.osgi.framework.Version;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Various utilities for Bindings.
 *
 * @author lobas_av
 * @coverage bindings.utils
 */
public final class CoreUtils {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Collections
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Helper method for cast any {@link List} to generic list.
   */
  @SuppressWarnings("unchecked")
  public static <T> List<T> cast(List<?> list) {
    return (List<T>) list;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AST evaluation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Helper method for evaluate {@link Expression}.
   */
  @SuppressWarnings("unchecked")
  public static <T> T evaluate(Class<T> objectType, AstEditor editor, Expression expression)
      throws Exception {
    Object object = evaluateObject(editor, expression);
    if (object != null) {
      Assert.instanceOf(objectType, object);
    }
    return (T) object;
  }

  public static <T> T evaluate(Class<T> objectType,
      AstEditor editor,
      Expression[] expressions,
      int index) throws Exception {
    return index == -1 ? null : evaluate(objectType, editor, expressions[index]);
  }

  /**
   * Helper method for evaluate {@link Expression}.
   */
  public static Object evaluateObject(AstEditor editor, Expression expression) throws Exception {
    EditorState state = EditorState.get(editor);
    EvaluationContext context =
        new EvaluationContext(state.getEditorLoader(), state.getFlowDescription());
    return AstEvaluationEngine.evaluate(context, expression);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AST
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return string reference for {@link ASTNode}.
   */
  public static String getNodeReference(ASTNode node) {
    // simple and qualified names
    if (node instanceof QualifiedName) {
      QualifiedName name = (QualifiedName) node;
      return AstNodeUtils.getFullyQualifiedName(name.getQualifier(), false)
          + "."
          + name.getName().getIdentifier();
    }
    if (node instanceof SimpleName) {
      SimpleName name = (SimpleName) node;
      return name.getIdentifier();
    }
    // method invocation
    if (node instanceof MethodInvocation) {
      // method reference
      MethodInvocation invocation = (MethodInvocation) node;
      String reference = invocation.getName().getIdentifier() + "()";
      // invocation expression reference
      Expression expression = invocation.getExpression();
      if (expression != null) {
        reference = getNodeReference(expression) + "." + reference;
      }
      //
      return reference;
    }
    // field
    if (node instanceof FieldAccess) {
      // field name reference
      FieldAccess fieldAccess = (FieldAccess) node;
      String reference = fieldAccess.getName().getIdentifier();
      // field expression reference
      Expression expression = fieldAccess.getExpression();
      // skip "this" reference that we use all bean fields without "this" prefix.
      if (expression instanceof ThisExpression) {
        return reference;
      }
      return getNodeReference(expression) + "." + reference;
    }
    // this
    if (node instanceof ThisExpression) {
      // this reference
      ThisExpression thisExpression = (ThisExpression) node;
      String reference = "this";
      // this qualifier reference
      Name qualifier = thisExpression.getQualifier();
      if (qualifier != null) {
        reference = qualifier.getFullyQualifiedName() + ".this";
      }
      //
      return reference;
    }
    // CIC
    if (node instanceof ClassInstanceCreation) {
      ClassInstanceCreation creation = (ClassInstanceCreation) node;
      return "new "
          + AstNodeUtils.getFullyQualifiedName(creation, false)
          + "("
          + creation.arguments().size()
          + ")";
    }
    // unknown
    Assert.fail("Unknown reference: " + node);
    return null;
  }

  /**
   * Work like {@link #getNodeReference(ASTNode)} but without assert unknown reference.
   */
  public static String getSafeNodeReference(ASTNode node) {
    if (node instanceof Name
        || node instanceof MethodInvocation
        || node instanceof FieldAccess
        || node instanceof ThisExpression
        || node instanceof ClassInstanceCreation) {
      return getNodeReference(node);
    }
    return null;
  }

  private static final String TYPE_PROPERTY = "49100913-2275-4e82-8382-c1ef7d8d62b8";

  /**
   * XXX
   *
   * @param rootNode
   * @param methodName
   * @return
   * @throws Exception
   */
  public static List<VariableDeclarationFragment> getLocalFragments(TypeDeclaration rootNode,
      String methodName) throws Exception {
    final List<VariableDeclarationFragment> fragments = Lists.newArrayList();
    MethodDeclaration initDataBindings =
        AstNodeUtils.getMethodBySignature(rootNode, methodName + "()");
    if (initDataBindings != null) {
      initDataBindings.accept(new ASTVisitor() {
        @Override
        public void endVisit(VariableDeclarationFragment fragment) {
          IVariableBinding variableBinding = AstNodeUtils.getVariableBinding(fragment);
          ITypeBinding binding = variableBinding == null ? null : variableBinding.getType();
          if (isIncludeTypeBinding(binding)) {
            fragment.setProperty(TYPE_PROPERTY, binding);
            fragments.add(fragment);
          }
        }
      });
    }
    return fragments;
  }

  /**
   * @return all {@link VariableDeclarationFragment} for compilation unit fields.
   */
  public static List<VariableDeclarationFragment> getFieldFragments(TypeDeclaration rootNode)
      throws Exception {
    List<VariableDeclarationFragment> fragments = Lists.newArrayList();
    //
    for (FieldDeclaration fieldDeclaration : rootNode.getFields()) {
      Type type = fieldDeclaration.getType();
      if (type == null || AstNodeUtils.getTypeBinding(type) == null) {
        continue;
      }
      //
      for (VariableDeclarationFragment fragment : DomGenerics.fragments(fieldDeclaration)) {
        // ignore primitives and arrays
        if (isIncludeType(type)) {
          fragment.setProperty(TYPE_PROPERTY, type);
          fragments.add(fragment);
        }
      }
    }
    // sort fragments by position
    Collections.sort(fragments, new Comparator<VariableDeclarationFragment>() {
      public int compare(VariableDeclarationFragment fragment1,
          VariableDeclarationFragment fragment2) {
        return fragment1.getStartPosition() - fragment2.getStartPosition();
      }
    });
    return fragments;
  }

  /**
   * XXX
   *
   * @param <T>
   * @param fragment
   * @param clearProperty
   * @return
   */
  @SuppressWarnings("unchecked")
  public static <T> T getType(VariableDeclarationFragment fragment, boolean clearProperty) {
    T type = (T) fragment.getProperty(TYPE_PROPERTY);
    Assert.isNotNull(type);
    if (clearProperty) {
      fragment.setProperty(TYPE_PROPERTY, null);
    }
    return type;
  }

  /**
   * XXX
   *
   * @param type
   * @return
   */
  public static boolean isIncludeType(Type type) {
    return !type.isPrimitiveType() && !type.isArrayType();
  }

  /**
   * XXX
   *
   * @param binding
   * @return
   */
  public static boolean isIncludeTypeBinding(ITypeBinding binding) {
    return binding != null && !binding.isPrimitive() && !binding.isArray();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Signature
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return full method signature: (class signature).(method signature)
   */
  public static String getMethodSignature(MethodInvocation invocation) {
    IMethodBinding methodBinding = AstNodeUtils.getMethodBinding(invocation);
    if (methodBinding == null) {
      return null;
    }
    ITypeBinding invocationType = methodBinding.getDeclaringClass();
    return AstNodeUtils.getFullyQualifiedName(invocationType, false)
        + "."
        + AstNodeUtils.getMethodSignature(invocation);
  }

  /**
   * @return full creation signature: (class signature).(constructor signature)
   */
  public static String getCreationSignature(ClassInstanceCreation creation) {
    return AstNodeUtils.getFullyQualifiedName(creation, false)
        + "."
        + AstNodeUtils.getCreationSignature(creation);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Generics
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the array of {@link Expression}'s for given list of them.
   */
  public static Expression[] getExpressionArray(List<Expression> expressionList) {
    return expressionList.toArray(new Expression[expressionList.size()]);
  }

  public static boolean useGenerics(IJavaProject javaProject) {
    try {
      String versionValue = javaProject.getOption(JavaCore.COMPILER_SOURCE, true);
      Version version = new Version(versionValue);
      return version.getMinor() >= 5;
    } catch (Throwable e) {
    }
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // String
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * If <code>string</code> is <code>null</code> returned <code>defaultString</code> otherwise
   * returned <code>prefix + string + prefix</code>.
   */
  public static String getDefaultString(String string, String prefix, String defaultString) {
    if (string == null) {
      return defaultString;
    }
    return prefix + string + prefix;
  }

  /**
   * Joins the elements of the provided array into a single String containing the provided list of
   * elements.
   */
  public static String joinStrings(String delimeter, String... strings) {
    StringBuffer buffer = new StringBuffer();
    for (String string : strings) {
      if (string != null) {
        if (buffer.length() > 0) {
          buffer.append(delimeter);
        }
        buffer.append(string);
      }
    }
    return buffer.toString();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Class
  //
  ////////////////////////////////////////////////////////////////////////////
  public static ClassLoader classLoader(JavaInfo javaInfo) {
    return EditorState.get(javaInfo.getEditor()).getEditorLoader();
  }

  /**
   * Load class with given <code>className</code> over given <code>classLoader</code> and suppress
   * {@link ClassNotFoundException}.
   */
  public static Class<?> loadClass(ClassLoader classLoader, String className) {
    try {
      return load(classLoader, className);
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  /**
   * Load class with given <code>className</code> over given <code>classLoader</code>. Support load
   * inner classes over Java class name (auto replace last "." to "$").
   */
  public static Class<?> load(ClassLoader classLoader, String className)
      throws ClassNotFoundException {
    try {
      return classLoader.loadClass(className);
    } catch (ClassNotFoundException e) {
      int index = className.lastIndexOf('.');
      if (index > 0) {
        try {
          return classLoader.loadClass(className.substring(0, index)
              + "$"
              + className.substring(index + 1));
        } catch (Throwable t) {
        }
      }
      throw e;
    }
  }

  /**
   * @return invoke {@link Class#isAssignableFrom(Class)} if given <code>base</code> is't
   *         <code>null</code> otherwise <code>false</code>.
   */
  public static boolean isAssignableFrom(Class<?> baseClass, Class<?> testClass) {
    return baseClass != null && baseClass.isAssignableFrom(testClass);
  }

  /**
   * Load given <code>baseClass</code> and invoke {@link Class#isAssignableFrom(Class)} for given
   * <code>testClass</code>.
   */
  public static boolean isAssignableFrom(ClassLoader classLoader,
      String baseClass,
      Class<?> testClass) {
    return isAssignableFrom(loadClass(classLoader, baseClass), testClass);
  }

  /**
   * @return {@link Class} {@code testClass} if it not {@code null} otherwise {@code defaultClass}.
   */
  public static Class<?> getClass(Class<?> testClass, Class<?> defaultClass) {
    return testClass == null ? defaultClass : testClass;
  }

  /**
   * @return the name of given {@link Class}. For inner classes do replace "$" to "."
   */
  public static String getClassName(Class<?> clazz) {
    return clazz.getName().replace('$', '.');
  }
}