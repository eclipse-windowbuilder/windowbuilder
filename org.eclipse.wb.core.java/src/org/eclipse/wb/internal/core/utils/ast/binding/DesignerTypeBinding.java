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
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

/**
 * Implementation of {@link ITypeBinding}.
 * <p>
 * We use our implementations of bindings because standard ones reference objects from internal
 * compiler's AST. This is not problem for Eclipse itself, but we parse very often, for every change
 * in editor, so we can end up with a lot of referenced objects.
 *
 * @author scheglov_ke
 * @coverage core.util.ast
 */
public final class DesignerTypeBinding implements ITypeBinding {
  private final IPackageBinding m_packageBinding;
  private final String m_name;
  private final String m_key;
  // flags
  private final boolean m_class;
  private final boolean m_primitive;
  private final boolean m_nullType;
  private final boolean m_interface;
  private final boolean m_enum;
  private final boolean m_topLevel;
  private final boolean m_member;
  private final boolean m_nested;
  private final boolean m_local;
  private final boolean m_anonymous;
  // generics
  private final boolean m_genericType;
  private final boolean m_parameterizedType;
  private final boolean m_typeVariable;
  private ITypeBinding m_typeDeclaration;
  private ITypeBinding[] m_typeArguments;
  private ITypeBinding[] m_typeParameters;
  private ITypeBinding[] m_typeBounds;
  // modifiers
  private final int m_modifiers;
  private final int m_declaredModifiers;
  // array
  private final boolean m_array;
  private final ITypeBinding m_elementType;
  private final int m_dimensions;
  // elements
  private final ITypeBinding m_declaringClass;
  private final ITypeBinding m_superclass;
  private ITypeBinding[] m_interfaces;
  private final IMethodBinding[] m_declaredMethods;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  DesignerTypeBinding(BindingContext context, String fullyQualifiedName, ITypeBinding binding) {
    if (fullyQualifiedName != null) {
      context.register(fullyQualifiedName, this);
    }
    {
      IPackageBinding jdtPackageBinding = binding.getPackage();
      if (jdtPackageBinding != null) {
        m_packageBinding = new DesignerPackageBinding(jdtPackageBinding);
      } else {
        m_packageBinding = null;
      }
    }
    m_name = binding.getName();
    m_key = binding.getKey();
    // flags
    m_class = binding.isClass();
    m_primitive = binding.isPrimitive();
    m_nullType = binding.isNullType();
    m_interface = binding.isInterface();
    m_enum = binding.isEnum();
    m_topLevel = binding.isTopLevel();
    m_nested = binding.isNested();
    m_member = binding.isMember();
    m_local = binding.isLocal();
    m_anonymous = binding.isAnonymous();
    // generics
    m_genericType = binding.isGenericType();
    m_parameterizedType = binding.isParameterizedType();
    m_typeVariable = binding.isTypeVariable();
    {
      ITypeBinding typeDeclaration = binding.getTypeDeclaration();
      if (typeDeclaration == binding) {
        m_typeDeclaration = this;
      } else {
        m_typeDeclaration = context.get(typeDeclaration);
      }
    }
    {
      ITypeBinding[] typeArguments = binding.getTypeArguments();
      m_typeArguments = new ITypeBinding[typeArguments.length];
      for (int i = 0; i < typeArguments.length; i++) {
        ITypeBinding typeArgument = typeArguments[i];
        m_typeArguments[i] = context.get(typeArgument, true);
      }
    }
    {
      ITypeBinding[] typeBounds = binding.getTypeBounds();
      m_typeBounds = new ITypeBinding[typeBounds.length];
      for (int i = 0; i < typeBounds.length; i++) {
        ITypeBinding typeBound = typeBounds[i];
        m_typeBounds[i] = context.get(typeBound);
      }
    }
    {
      ITypeBinding[] typeParameters = binding.getTypeParameters();
      m_typeParameters = new ITypeBinding[typeParameters.length];
      for (int i = 0; i < typeParameters.length; i++) {
        ITypeBinding typeParameter = typeParameters[i];
        m_typeParameters[i] = context.get(typeParameter);
      }
    }
    // modifiers
    m_modifiers = binding.getModifiers();
    m_declaredModifiers = binding.getDeclaredModifiers();
    // array
    m_array = binding.isArray();
    if (binding.getElementType() != null) {
      m_elementType = context.get(binding.getElementType());
    } else {
      m_elementType = null;
    }
    m_dimensions = binding.getDimensions();
    // elements
    m_declaringClass = context.get(binding.getDeclaringClass());
    m_superclass = context.get(binding.getSuperclass(), true);
    {
      ITypeBinding[] jdtInterfaces = binding.getInterfaces();
      m_interfaces = new ITypeBinding[jdtInterfaces.length];
      for (int i = 0; i < jdtInterfaces.length; i++) {
        ITypeBinding jdtInterface = jdtInterfaces[i];
        m_interfaces[i] = context.get(jdtInterface);
      }
    }
    {
      IMethodBinding[] methods = binding.getDeclaredMethods();
      m_declaredMethods = new IMethodBinding[methods.length];
      for (int i = 0; i < methods.length; i++) {
        IMethodBinding method = methods[i];
        m_declaredMethods[i] = context.get(method);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds new {@link ITypeBinding} into implemented interfaces.
   */
  public void addInterface(ITypeBinding typeBinding) {
    ITypeBinding[] newInterfaces = new ITypeBinding[m_interfaces.length + 1];
    System.arraycopy(m_interfaces, 0, newInterfaces, 0, m_interfaces.length);
    newInterfaces[newInterfaces.length - 1] = typeBinding;
    m_interfaces = newInterfaces;
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
  // ITypeBinding
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getBinaryName() {
    throw new IllegalArgumentException();
  }

  public boolean isPrimitive() {
    return m_primitive;
  }

  public boolean isNullType() {
    return m_nullType;
  }

  public boolean isArray() {
    return m_array;
  }

  public ITypeBinding getElementType() {
    return m_elementType;
  }

  public int getDimensions() {
    return m_dimensions;
  }

  public boolean isClass() {
    return m_class;
  }

  public boolean isInterface() {
    return m_interface;
  }

  public boolean isEnum() {
    return m_enum;
  }

  public boolean isAnnotation() {
    throw new IllegalArgumentException();
  }

  public ITypeBinding[] getTypeParameters() {
    return m_typeParameters;
  }

  public boolean isTypeVariable() {
    return m_typeVariable;
  }

  public ITypeBinding[] getTypeBounds() {
    return m_typeBounds;
  }

  public boolean isParameterizedType() {
    return m_parameterizedType;
  }

  public ITypeBinding[] getTypeArguments() {
    return m_typeArguments;
  }

  public ITypeBinding getErasure() {
    throw new IllegalArgumentException();
  }

  public boolean isRawType() {
    throw new IllegalArgumentException();
  }

  public boolean isWildcardType() {
    throw new IllegalArgumentException();
  }

  public ITypeBinding getBound() {
    throw new IllegalArgumentException();
  }

  public boolean isUpperbound() {
    throw new IllegalArgumentException();
  }

  public String getName() {
    return m_name;
  }

  public IPackageBinding getPackage() {
    return m_packageBinding;
  }

  public ITypeBinding getDeclaringClass() {
    return m_declaringClass;
  }

  public ITypeBinding getSuperclass() {
    return m_superclass;
  }

  public ITypeBinding[] getInterfaces() {
    return m_interfaces;
  }

  public int getModifiers() {
    return m_modifiers;
  }

  public int getDeclaredModifiers() {
    return m_declaredModifiers;
  }

  public boolean isTopLevel() {
    return m_topLevel;
  }

  public boolean isNested() {
    return m_nested;
  }

  public boolean isMember() {
    return m_member;
  }

  public boolean isLocal() {
    return m_local;
  }

  public boolean isAnonymous() {
    return m_anonymous;
  }

  public ITypeBinding[] getDeclaredTypes() {
    throw new IllegalArgumentException();
  }

  public IVariableBinding[] getDeclaredFields() {
    throw new IllegalArgumentException();
  }

  public IMethodBinding[] getDeclaredMethods() {
    return m_declaredMethods;
  }

  public boolean isFromSource() {
    throw new IllegalArgumentException();
  }

  public String getQualifiedName() {
    throw new IllegalArgumentException();
  }

  public int getKind() {
    throw new IllegalArgumentException();
  }

  public boolean isDeprecated() {
    throw new IllegalArgumentException();
  }

  public boolean isSynthetic() {
    throw new IllegalArgumentException();
  }

  public IJavaElement getJavaElement() {
    throw new IllegalArgumentException();
  }

  public String getKey() {
    return m_key;
  }

  public boolean isEqualTo(IBinding binding) {
    throw new IllegalArgumentException();
  }

  //
  // For Eclipse 2.0 support
  //
  /*public String getQualifiedName(ITypeBinding binding) {
   ITypeBinding declaringType;
   IPackageBinding packageBinding;
   String scope;
   if (binding == null) {
   return "";
   }
   if (binding.isPrimitive()) {
   return binding.getName();
   }
   declaringType = binding.getDeclaringClass();
   if (declaringType == null) {
   packageBinding = binding.getPackage();
   if (packageBinding == null) {
   return "";
   }
   scope = packageBinding.getName();
   } else {
   scope = getQualifiedName(declaringType);
   }
   return scope + "." + binding.getName();
   }*/
  //
  // New in Eclipse 3.1
  //
  public boolean isAssignmentCompatible(ITypeBinding type) {
    throw new IllegalArgumentException();
  }

  public boolean isCastCompatible(ITypeBinding type) {
    throw new IllegalArgumentException();
  }

  public boolean isGenericType() {
    return m_genericType;
  }

  public boolean isSubTypeCompatible(ITypeBinding type) {
    throw new IllegalArgumentException();
  }

  public ITypeBinding getTypeDeclaration() {
    return m_typeDeclaration;
  }

  public IMethodBinding getDeclaringMethod() {
    throw new IllegalArgumentException();
  }

  public ITypeBinding getWildcard() {
    throw new IllegalArgumentException();
  }

  public boolean isCapture() {
    throw new IllegalArgumentException();
  }

  //
  // New in Eclipse 3.2M5
  //
  public ITypeBinding getComponentType() {
    throw new IllegalArgumentException();
  }

  public IAnnotationBinding[] getAnnotations() {
    throw new IllegalArgumentException();
  }

  //
  // New in Eclipse 3.3M2
  //
  public ITypeBinding createArrayType(int dimension) {
    throw new IllegalArgumentException();
  }

  //
  // New in Eclipse 3.3M6
  //
  public boolean isRecovered() {
    throw new IllegalArgumentException();
  }

  //
  // New in Eclipse 3.5M4
  //
  public int getRank() {
    throw new IllegalArgumentException();
  }

  public ITypeBinding getGenericTypeOfWildcardType() {
    throw new IllegalArgumentException();
  }

  public IMethodBinding getFunctionalInterfaceMethod() {
    throw new IllegalArgumentException();
  }

  public IAnnotationBinding[] getTypeAnnotations() {
    throw new IllegalArgumentException();
  }

  public IBinding getDeclaringMember() {
    return null;
  }

  public boolean isIntersectionType() {
    // TODO Auto-generated method stub
    return false;
  }
}
