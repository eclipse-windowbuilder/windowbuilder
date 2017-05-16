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
package org.eclipse.wb.internal.core.utils.ast;

import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Contains different AST & Java reflection utilities.
 *
 * @author sablin_aa
 * @author scheglov_ke
 * @coverage core.util.ast
 */
public class AstReflectionUtils {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private AstReflectionUtils() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Class
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Class} of given {@link ITypeBinding}.
   */
  public static Class<?> getClass(ClassLoader classLoader, ITypeBinding typeBinding)
      throws Exception {
    Assert.isNotNull(typeBinding);
    String className = AstNodeUtils.getFullyQualifiedName(typeBinding, true);
    return ReflectionUtils.getClassByName(classLoader, className);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Arguments
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Updates given "raw" values to have "varArgs" arguments as single array, as required for
   * reflection.
   */
  public static Object[] updateForVarArgs(ClassLoader classLoader,
      IMethodBinding methodBinding,
      Object[] values) throws Exception {
    if (methodBinding.isVarargs()) {
      ITypeBinding[] parameterTypes = methodBinding.getParameterTypes();
      int parameterCount = parameterTypes.length;
      // copy arguments before varArgs
      Object[] newValues = new Object[parameterCount];
      System.arraycopy(values, 0, newValues, 0, parameterCount - 1);
      // last argument is varArgs
      {
        ITypeBinding varArrayTypeBinding = parameterTypes[parameterCount - 1];
        Object varArgs =
            AstReflectionUtils.getVarArgsArgument(
                classLoader,
                values,
                parameterCount - 1,
                varArrayTypeBinding);
        newValues[parameterCount - 1] = varArgs;
      }
      // final result
      return newValues;
    }
    return values;
  }

  /**
   * Prepares "varArgs" arguments, re-packs existing values from "raw" values.
   *
   * @return the single "varArgs" argument as array.
   */
  private static Object getVarArgsArgument(ClassLoader classLoader,
      Object[] values,
      int varArrayFirstIndex,
      ITypeBinding varArrayTypeBinding) throws Exception {
    Class<?> varClass =
        AstReflectionUtils.getClass(classLoader, varArrayTypeBinding.getElementType());
    int varArgsLength = values.length - varArrayFirstIndex;
    // check, may be last argument is already array of required type
    if (varArgsLength == 1) {
      Object varArgValue = values[varArrayFirstIndex];
      if (varArgValue != null && varArgValue.getClass().isArray()) {
        if (varClass.isAssignableFrom(varArgValue.getClass().getComponentType())) {
          return varArgValue;
        }
      }
    }
    // re-pack into array
    Object varArgs = Array.newInstance(varClass, varArgsLength);
    for (int i = 0; i < varArgsLength; i++) {
      Object element = values[varArrayFirstIndex + i];
      Array.set(varArgs, i, element);
    }
    return varArgs;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Constructor} for given {@link Class} and {@link IMethodBinding}. This
   *         constructor can have any visibility, i.e. we can find even protected/private
   *         constructors.
   */
  public static Constructor<?> getConstructor(Class<?> clazz, IMethodBinding binding) {
    Assert.isLegal(binding.isConstructor());
    String signature = AstNodeUtils.getMethodSignature(binding);
    Constructor<?> constructor = ReflectionUtils.getConstructorBySignature(clazz, signature);
    if (constructor == null) {
      // find by generics
      signature = AstNodeUtils.getMethodGenericSignature(binding);
      constructor = ReflectionUtils.getConstructorByGenericSignature(clazz, signature);
    }
    return constructor;
  }

  /**
   * @return the {@link Constructor} for given {@link Class} and {@link SuperConstructorInvocation}.
   *         This constructor can have any visibility, i.e. we can find even protected/private
   *         constructors.
   */
  public static Constructor<?> getConstructor(Class<?> clazz, SuperConstructorInvocation invocation) {
    IMethodBinding binding = AstNodeUtils.getSuperBinding(invocation);
    return getConstructor(clazz, binding);
  }

  /**
   * @return the {@link Constructor} for given {@link Class} and {@link ClassInstanceCreation}. This
   *         constructor can have any visibility, i.e. we can find even protected/private
   *         constructors.
   */
  public static Constructor<?> getConstructor(Class<?> clazz, ClassInstanceCreation creation) {
    IMethodBinding binding = AstNodeUtils.getCreationBinding(creation);
    return getConstructor(clazz, binding);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Method
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Method} for given {@link Class} and {@link IMethodBinding}. This method can
   *         have any visibility, i.e. we can find even protected/private methods.
   */
  public static Method getMethod(Class<?> clazz, IMethodBinding binding) {
    Assert.isLegal(!binding.isConstructor());
    String signature = AstNodeUtils.getMethodSignature(binding);
    Method method = ReflectionUtils.getMethodBySignature(clazz, signature);
    if (method == null) {
      signature = AstNodeUtils.getMethodGenericSignature(binding);
      method = ReflectionUtils.getMethodByGenericSignature(clazz, signature);
    }
    return method;
  }

  /**
   * @return the {@link Method} for given {@link Class} and {@link IMethodBinding}. This method can
   *         have any visibility, i.e. we can find even protected/private methods.
   */
  public static Method getMethod(Class<?> clazz, MethodInvocation invocation) {
    IMethodBinding binding = AstNodeUtils.getMethodBinding(invocation);
    return getMethod(clazz, binding);
  }
}
