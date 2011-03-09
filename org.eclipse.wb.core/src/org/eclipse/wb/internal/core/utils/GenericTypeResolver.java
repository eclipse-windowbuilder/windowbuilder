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

import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
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
  public static GenericTypeResolver superClass(final GenericTypeResolver parent,
      Class<?> actualClass,
      Class<?> declarationClass) {
    if (actualClass != declarationClass) {
      Type superGeneric = actualClass.getGenericSuperclass();
      if (superGeneric instanceof ParameterizedType) {
        ParameterizedType superParameterized = (ParameterizedType) superGeneric;
        final Map<String, Type> typeArguments = getTypeArguments(superGeneric);
        GenericTypeResolver thisClassResolver = new GenericTypeResolver(parent) {
          @Override
          protected String resolveTypeVariable(TypeVariable<?> variable) {
            String variableName = variable.getName();
            Type variableType = typeArguments.get(variableName);
            return parent.resolve(variableType);
          }
        };
        return superClass(
            thisClassResolver,
            (Class<?>) superParameterized.getRawType(),
            declarationClass);
      } else {
        return superClass(parent, actualClass.getSuperclass(), declarationClass);
      }
    }
    return parent;
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
      Class<?> generic = (Class<?>) parameterized.getRawType();
      TypeVariable<?>[] typeParameters = generic.getTypeParameters();
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
