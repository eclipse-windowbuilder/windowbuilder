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
package org.eclipse.wb.internal.swing.model.component;

import org.eclipse.wb.core.eval.AstEvaluationEngine;
import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.eval.InvocationEvaluatorInterceptor;
import org.eclipse.wb.internal.core.model.util.PlaceholderUtils;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.model.ModelMessages;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.text.MessageFormat;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;

/**
 * For custom AWT {@link Component} try to find and use default constructor.
 * 
 * @author scheglov_ke
 * @coverage swing.model
 */
public final class SwingInvocationEvaluatorInterceptor extends InvocationEvaluatorInterceptor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // InvocationEvaluatorInterceptor
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Object evaluate(EvaluationContext context,
      ClassInstanceCreation expression,
      ITypeBinding typeBinding,
      Class<?> clazz,
      Constructor<?> actualConstructor,
      Object[] arguments) throws Exception {
    if (isSwingComponent(clazz)) {
      return evaluateSwing(context, expression, clazz, actualConstructor, arguments);
    }
    return AstEvaluationEngine.UNKNOWN;
  }

  private Object evaluateSwing(EvaluationContext context,
      ClassInstanceCreation expression,
      Class<?> clazz,
      Constructor<?> actualConstructor,
      Object[] arguments) {
    PlaceholderUtils.clear(expression);
    processJList(clazz, actualConstructor, arguments);
    // try actual constructor
    try {
      return actualConstructor.newInstance(arguments);
    } catch (Throwable e) {
      context.addException(expression, e);
      PlaceholderUtils.addException(expression, e);
    }
    // some exception happened, try default constructor (if actual was not default)
    boolean isActualDefault = actualConstructor.getParameterTypes().length == 0;
    if (!isActualDefault) {
      try {
        Constructor<?> defaultConstructor =
            ReflectionUtils.getConstructorBySignature(clazz, "<init>()");
        if (defaultConstructor != null) {
          return defaultConstructor.newInstance();
        }
      } catch (Throwable e) {
        context.addException(expression, e);
        PlaceholderUtils.addException(expression, e);
      }
    }
    // still no success, use placeholder
    PlaceholderUtils.markPlaceholder(expression);
    return createPlaceholder(clazz);
  }

  @Override
  public Object evaluateAnonymous(EvaluationContext context,
      ClassInstanceCreation expression,
      ITypeBinding typeBinding,
      ITypeBinding typeBindingConcrete,
      IMethodBinding methodBinding,
      Object[] arguments) throws Exception {
    if (AstNodeUtils.isSuccessorOf(typeBindingConcrete, "java.awt.Component")) {
      String componentClassName = AstNodeUtils.getFullyQualifiedName(typeBindingConcrete, true);
      Class<?> componentClass = context.getClassLoader().loadClass(componentClassName);
      Constructor<?> constructor = ReflectionUtils.getConstructor(componentClass);
      if (constructor != null) {
        return constructor.newInstance();
      }
    }
    return AstEvaluationEngine.UNKNOWN;
  }

  @Override
  public Object evaluate(EvaluationContext context,
      MethodInvocation invocation,
      IMethodBinding methodBinding,
      Class<?> clazz,
      Method method,
      Object[] argumentValues) {
    // JComboBox
    if (ReflectionUtils.isSuccessorOf(clazz, "javax.swing.JComboBox")) {
      String signature = ReflectionUtils.getMethodSignature(method);
      // ignore JComboBox.setModel(null)
      if (signature.equals("setModel(javax.swing.ComboBoxModel)") && argumentValues[0] == null) {
        return null;
      }
      // ignore JComboBox.setRenderer(null)
      if (signature.equals("setRenderer(javax.swing.ListCellRenderer)")
          && argumentValues[0] == null) {
        return null;
      }
    }
    // JList
    if (ReflectionUtils.isSuccessorOf(clazz, "javax.swing.JList")) {
      String signature = ReflectionUtils.getMethodSignature(method);
      // ignore JList.setListData((Object[]) null)
      if (signature.equals("setListData(java.lang.Object[])") && argumentValues[0] == null) {
        return null;
      }
      // ignore JList.setListData((Vector) null)
      if (signature.equals("setListData(java.util.Vector)") && argumentValues[0] == null) {
        return null;
      }
    }
    // use default handling
    return AstEvaluationEngine.UNKNOWN;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  private static boolean isSwingComponent(Class<?> clazz) {
    return ReflectionUtils.isSuccessorOf(clazz, "java.awt.Component");
  }

  /**
   * {@link javax.swing.JList} constructors accept <code>null</code> as model values, but later
   * throw exception. So, we should fix these values and use empty collections.
   */
  private static void processJList(Class<?> clazz,
      Constructor<?> actualConstructor,
      Object[] arguments) {
    String signature = ReflectionUtils.getConstructorSignature(actualConstructor);
    if (clazz == JList.class || clazz == JComboBox.class) {
      if ("<init>(java.lang.Object[])".equals(signature) && arguments[0] == null) {
        arguments[0] = new Object[0];
      }
      if ("<init>(java.util.Vector)".equals(signature) && arguments[0] == null) {
        arguments[0] = new java.util.Vector<Object>();
      }
    }
    if (clazz == JComboBox.class) {
      if ("<init>(javax.swing.ComboBoxModel)".equals(signature) && arguments[0] == null) {
        arguments[0] = new javax.swing.DefaultComboBoxModel();
      }
    }
  }

  /**
   * @return the {@link Component} to use as placeholder instead of real component that can not be
   *         created because of some exception.
   */
  private static Component createPlaceholder(Class<?> clazz) {
    String message =
        MessageFormat.format(
            ModelMessages.SwingInvocationEvaluatorInterceptor_placeholderMessage,
            clazz.getName());
    JPanel panel = new JPanel();
    panel.setLayout(new BorderLayout(5, 5));
    panel.setBackground(new Color(0xFF, 0xCC, 0xCC));
    {
      JTextArea textArea = new JTextArea(message);
      textArea.setLineWrap(true);
      textArea.setWrapStyleWord(true);
      textArea.setOpaque(false);
      textArea.setFont(new JLabel().getFont());
      panel.add(textArea);
    }
    return panel;
  }
}
