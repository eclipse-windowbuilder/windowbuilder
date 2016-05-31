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
package org.eclipse.wb.internal.core.utils.ast.binding;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import org.apache.commons.lang.ArrayUtils;

/**
 * Implementation of {@link IMethodBinding}.
 *
 * We use our implementations of bindings because standard ones reference objects from internal
 * compiler's AST. This is not problem for Eclipse itself, but we parse very often, for every change
 * in editor, so we can end up with a lot of referenced objects.
 *
 * @author scheglov_ke
 * @coverage core.util.ast
 */
public final class DesignerMethodBinding implements IMethodBinding {
  private final String m_name;
  private final int m_modifiers;
  private final boolean m_constructor;
  private final boolean m_varargs;
  private final ITypeBinding m_declaringClass;
  private final ITypeBinding m_returnType;
  private ITypeBinding[] m_parameterTypes;
  private ITypeBinding[] m_exceptionTypes;
  private DesignerMethodBinding m_methodDeclaration;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  DesignerMethodBinding(BindingContext context, IMethodBinding binding) {
    m_name = binding.getName();
    m_modifiers = binding.getModifiers();
    m_constructor = binding.isConstructor();
    m_varargs = binding.isVarargs();
    m_declaringClass = context.get(binding.getDeclaringClass());
    m_returnType = context.get(binding.getReturnType());
    {
      ITypeBinding[] parameterTypes = binding.getParameterTypes();
      m_parameterTypes = new ITypeBinding[parameterTypes.length];
      for (int i = 0; i < parameterTypes.length; i++) {
        ITypeBinding parameterType = parameterTypes[i];
        m_parameterTypes[i] = context.get(parameterType);
      }
    }
    {
      ITypeBinding[] exceptionTypes = binding.getExceptionTypes();
      m_exceptionTypes = new ITypeBinding[exceptionTypes.length];
      for (int i = 0; i < exceptionTypes.length; i++) {
        ITypeBinding exceptionType = exceptionTypes[i];
        m_exceptionTypes[i] = context.get(exceptionType);
      }
    }
    {
      IMethodBinding methodDeclaration = binding.getMethodDeclaration();
      if (methodDeclaration == binding) {
        m_methodDeclaration = this;
      } else {
        m_methodDeclaration = context.get(methodDeclaration);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Removes parameter type with given index.
   */
  public void removeParameterType(int index) {
    m_parameterTypes = (ITypeBinding[]) ArrayUtils.remove(m_parameterTypes, index);
    if (m_methodDeclaration != this) {
      m_methodDeclaration.removeParameterType(index);
    }
  }

  /**
   * Adds new {@link ITypeBinding} into throws exceptions.
   */
  public void addExceptionType(ITypeBinding newException) {
    m_exceptionTypes = (ITypeBinding[]) ArrayUtils.add(m_exceptionTypes, newException);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    throw new IllegalArgumentException();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IMethodBinding
  //
  ////////////////////////////////////////////////////////////////////////////
  public ITypeBinding getDeclaringClass() {
    return m_declaringClass;
  }

  public Object getDefaultValue() {
    throw new IllegalArgumentException();
  }

  public ITypeBinding[] getExceptionTypes() {
    return m_exceptionTypes;
  }

  public IMethodBinding getMethodDeclaration() {
    return m_methodDeclaration;
  }

  public String getName() {
    return m_name;
  }

  public IAnnotationBinding[] getParameterAnnotations(int paramIndex) {
    throw new IllegalArgumentException();
  }

  public ITypeBinding[] getParameterTypes() {
    return m_parameterTypes;
  }

  public ITypeBinding getReturnType() {
    return m_returnType;
  }

  public ITypeBinding getDeclaredReceiverType() {
    throw new IllegalArgumentException();
  }

  public ITypeBinding[] getTypeArguments() {
    throw new IllegalArgumentException();
  }

  public ITypeBinding[] getTypeParameters() {
    throw new IllegalArgumentException();
  }

  public boolean isAnnotationMember() {
    throw new IllegalArgumentException();
  }

  public boolean isConstructor() {
    return m_constructor;
  }

  public boolean isDefaultConstructor() {
    throw new IllegalArgumentException();
  }

  public boolean isGenericMethod() {
    throw new IllegalArgumentException();
  }

  public boolean isParameterizedMethod() {
    throw new IllegalArgumentException();
  }

  public boolean isRawMethod() {
    throw new IllegalArgumentException();
  }

  public boolean isSubsignature(IMethodBinding otherMethod) {
    throw new IllegalArgumentException();
  }

  public boolean isVarargs() {
    return m_varargs;
  }

  public boolean overrides(IMethodBinding method) {
    throw new IllegalArgumentException();
  }

  public IAnnotationBinding[] getAnnotations() {
    throw new IllegalArgumentException();
  }

  public IJavaElement getJavaElement() {
    throw new IllegalArgumentException();
  }

  public String getKey() {
    throw new IllegalArgumentException();
  }

  public int getKind() {
    throw new IllegalArgumentException();
  }

  public int getModifiers() {
    return m_modifiers;
  }

  public boolean isDeprecated() {
    throw new IllegalArgumentException();
  }

  public boolean isEqualTo(IBinding binding) {
    throw new IllegalArgumentException();
  }

  public boolean isSynthetic() {
    throw new IllegalArgumentException();
  }

  public boolean isRecovered() {
    throw new IllegalArgumentException();
  }

  public IBinding getDeclaringMember() {
    return null;
  }
}
