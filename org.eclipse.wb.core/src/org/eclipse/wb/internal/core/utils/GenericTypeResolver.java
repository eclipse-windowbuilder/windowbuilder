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
package org.eclipse.wb.internal.core.utils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.Map;

/**
 * Resolver for type variables.
 *
 * @author scheglov_ke
 * @coverage core.util
 */
public class GenericTypeResolver {
  protected final GenericTypeResolver m_parent;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GenericTypeResolver(GenericTypeResolver parent) {
    m_parent = parent;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public String resolve(Type typeToResolve) {
    if (typeToResolve instanceof Class<?>) {
      return ((Class<?>) typeToResolve).getCanonicalName();
    }
    if (typeToResolve instanceof TypeVariable<?>) {
      String result = resolveTypeVariable((TypeVariable<?>) typeToResolve);
      if (result != null) {
        return result;
      }
    }
    if (typeToResolve instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) typeToResolve;
      StringBuilder buffer = new StringBuilder();
      buffer.append(((Class<?>) parameterizedType.getRawType()).getName());
      {
        buffer.append("<");
        boolean firstTypeArgument = true;
        for (Type type : parameterizedType.getActualTypeArguments()) {
          if (firstTypeArgument) {
            firstTypeArgument = false;
          } else {
            buffer.append(", ");
          }
          buffer.append(GenericsUtils.getTypeName(this, type));
        }
        buffer.append(">");
      }
      return buffer.toString();
    }
    if (typeToResolve instanceof WildcardType) {
      WildcardType wildcardType = (WildcardType) typeToResolve;
      Type[] upperBounds = wildcardType.getUpperBounds();
      if (upperBounds.length == 1) {
        return GenericsUtils.getTypeName(this, upperBounds[0]);
      }
    }
    // call parent
    if (m_parent != null) {
      return m_parent.resolve(typeToResolve);
    }
    throw new GenericTypeError("Can not resolve actual type for: " + typeToResolve);
  }

  protected String resolveTypeVariable(TypeVariable<?> variable) {
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Superclass
  //
  ////////////////////////////////////////////////////////////////////////////
  private static Map<String, Type> getActualTypeArguments(Class<?> declarationClass,
      Class<?> currentClass,
      Type[] arguments) {
    // Prepare map of current Class type parameters to arguments.
    Map<String, Type> currentArguments = Maps.newHashMap();
    TypeVariable<?>[] typeParameters = currentClass.getTypeParameters();
    for (int i = 0; i < typeParameters.length; i++) {
      TypeVariable<?> typeParameter = typeParameters[i];
      currentArguments.put(typeParameter.getName(), arguments[i]);
    }
    // If current is declaration Class, we are done.
    if (currentClass == declarationClass) {
      return currentArguments;
    }
    // Prepare all super Types.
    List<Type> superTypes = Lists.newArrayList();
    if (currentClass.getGenericSuperclass() != null) {
      superTypes.add(currentClass.getGenericSuperclass());
    }
    for (Type superInterface : currentClass.getGenericInterfaces()) {
      superTypes.add(superInterface);
    }
    // Check every super Type.
    for (Type superType : superTypes) {
      Class<?> superClass;
      Type superArguments[];
      if (superType instanceof ParameterizedType) {
        ParameterizedType parameterizedSuperType = (ParameterizedType) superType;
        superClass = (Class<?>) parameterizedSuperType.getRawType();
        superArguments = parameterizedSuperType.getActualTypeArguments();
        for (int i = 0; i < superArguments.length; i++) {
          Type argument = superArguments[i];
          if (argument instanceof TypeVariable<?>) {
            String name = ((TypeVariable<?>) argument).getName();
            Type resolvedArgument = currentArguments.get(name);
            Assert.isNotNull(resolvedArgument);
            superArguments[i] = resolvedArgument;
          }
        }
      } else {
        superClass = (Class<?>) superType;
        superArguments = new Type[0];
      }
      Map<String, Type> map = getActualTypeArguments(declarationClass, superClass, superArguments);
      if (map != null) {
        return map;
      }
    }
    return null;
  }

  public static GenericTypeResolver superClass(final GenericTypeResolver parent,
      Class<?> actualClass,
      Class<?> declarationClass) {
    Assert.isTrue(declarationClass.isAssignableFrom(declarationClass));
    final Map<String, Type> declarationClassArguments =
        getActualTypeArguments(declarationClass, actualClass, actualClass.getTypeParameters());
    return new GenericTypeResolver(parent) {
      @Override
      protected String resolveTypeVariable(TypeVariable<?> variable) {
        String variableName = variable.getName();
        Type variableType = declarationClassArguments.get(variableName);
        return parent.resolve(variableType);
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Fixed
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * {@link GenericTypeResolver} for {@link TypeVariable} with fixed name.
   */
  public static GenericTypeResolver fixed(final String name, Class<?> clazz) {
    final String clazzName = ReflectionUtils.getCanonicalName(clazz);
    return new GenericTypeResolver(EMPTY) {
      @Override
      protected String resolveTypeVariable(TypeVariable<?> variable) {
        if (variable.getName().equals(name)) {
          return clazzName;
        }
        return null;
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Argument of method
  //
  ////////////////////////////////////////////////////////////////////////////
  public static GenericTypeResolver argumentOfMethod(final GenericTypeResolver parent,
      Method method,
      int index) {
    Type genericParameter = method.getGenericParameterTypes()[index];
    final Map<String, Type> typeArguments = getTypeArguments(genericParameter);
    return new GenericTypeResolver(parent) {
      @Override
      protected String resolveTypeVariable(TypeVariable<?> variable) {
        String variableName = variable.getName();
        Type variableType = typeArguments.get(variableName);
        if (variableType != null) {
          return parent.resolve(variableType);
        }
        return null;
      }
    };
  }

  private static Map<String, Type> getTypeArguments(Type type) {
    Map<String, Type> typeArguments = Maps.newTreeMap();
    if (type instanceof ParameterizedType) {
      ParameterizedType parameterized = (ParameterizedType) type;
      Type[] actualTypeArguments = parameterized.getActualTypeArguments();
      Class<?> raw = (Class<?>) parameterized.getRawType();
      TypeVariable<?>[] typeParameters = raw.getTypeParameters();
      for (int i = 0; i < typeParameters.length; i++) {
        TypeVariable<?> typeParameter = typeParameters[i];
        typeArguments.put(typeParameter.getName(), actualTypeArguments[i]);
      }
    }
    return typeArguments;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // EMPTY
  //
  ////////////////////////////////////////////////////////////////////////////
  public static GenericTypeResolver EMPTY = new GenericTypeResolver(null);
}
