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
package org.eclipse.wb.internal.core.model.util;

import com.google.common.collect.Maps;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.utils.GenericTypeResolver;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ParameterizedType;

import java.lang.reflect.TypeVariable;
import java.util.Map;

/**
 * {@link GenericTypeResolver} for {@link JavaInfo} creation.
 *
 * @author scheglov_ke
 * @coverage core.util
 */
public class GenericTypeResolverJavaInfo extends GenericTypeResolver {
  private final Map<String, String> m_typeArguments;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GenericTypeResolverJavaInfo(JavaInfo javaInfo) throws Exception {
    super(GenericTypeResolver.EMPTY);
    m_typeArguments = getTypeArguments(javaInfo);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation
  //
  ////////////////////////////////////////////////////////////////////////////
  private static Map<String, String> getTypeArguments(JavaInfo javaInfo) throws Exception {
    Map<String, String> typeArguments = Maps.newHashMap();
    ASTNode node = javaInfo.getCreationSupport().getNode();
    if (node instanceof ClassInstanceCreation) {
      ClassInstanceCreation creation = (ClassInstanceCreation) node;
      if (creation.getType() instanceof ParameterizedType) {
        ParameterizedType parameterizedType = (ParameterizedType) creation.getType();
        ITypeBinding binding = AstNodeUtils.getTypeBinding(parameterizedType);
        ITypeBinding[] typeParameters_bindings = binding.getTypeDeclaration().getTypeParameters();
        ITypeBinding[] typeArguments_bindings = binding.getTypeArguments();
        for (int i = 0; i < typeParameters_bindings.length; i++) {
          ITypeBinding typeParameter = typeParameters_bindings[i];
          ITypeBinding typeArgument = typeArguments_bindings[i];
          String typeParameterName = typeParameter.getName();
          String typeArgumentName = AstNodeUtils.getFullyQualifiedName(typeArgument, true);
          typeArguments.put(typeParameterName, typeArgumentName);
        }
      } else {
        Class<?> componentClass = javaInfo.getDescription().getComponentClass();
        for (TypeVariable<?> typeParameter : componentClass.getTypeParameters()) {
          java.lang.reflect.Type[] bounds = typeParameter.getBounds();
          Class<?> upperBound = (Class<?>) bounds[0];
          String upperBoundName = ReflectionUtils.getCanonicalName(upperBound);
          typeArguments.put(typeParameter.getName(), upperBoundName);
        }
      }
    }
    return typeArguments;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String resolveTypeVariable(TypeVariable<?> variable) {
    String variableName = variable.getName();
    return m_typeArguments.get(variableName);
  }
}
