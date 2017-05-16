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
package org.eclipse.wb.internal.core.model.property.accessor;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.creation.ThisCreationSupport;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipProvider;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipTextProvider;
import org.eclipse.wb.internal.core.utils.jdt.core.JavaDocUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Utilities for {@link ExpressionAccessor}'s.
 *
 * @author scheglov_ke
 * @coverage core.model.property.accessor
 */
public class AccessorUtils {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private AccessorUtils() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IAccessibleExpressionAccessor
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the implementation of {@link IAccessibleExpressionAccessor} for reflection
   *         {@link Method}.
   */
  public static IAccessibleExpressionAccessor IAccessibleExpressionAccessor_forMethod(final Method method) {
    return new IAccessibleExpressionAccessor() {
      public boolean isAccessible(JavaInfo javaInfo) {
        return Modifier.isPublic(method.getModifiers())
            || javaInfo.getCreationSupport() instanceof ThisCreationSupport;
      }
    };
  }

  /**
   * @return the implementation of {@link IAccessibleExpressionAccessor} for reflection
   *         {@link Field}.
   */
  public static IAccessibleExpressionAccessor IAccessibleExpressionAccessor_forField(final Field field) {
    return new IAccessibleExpressionAccessor() {
      public boolean isAccessible(JavaInfo javaInfo) {
        return Modifier.isPublic(field.getModifiers())
            || javaInfo.getCreationSupport() instanceof ThisCreationSupport;
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IExposableExpressionAccessor
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link IExposableExpressionAccessor}, may be <code>null</code>.
   */
  public static IExposableExpressionAccessor getExposableExpressionAccessor(Property property)
      throws Exception {
    if (property instanceof GenericPropertyImpl) {
      GenericPropertyImpl genericProperty = (GenericPropertyImpl) property;
      for (ExpressionAccessor accessor : genericProperty.getAccessors()) {
        IExposableExpressionAccessor exposableAccessor =
            accessor.getAdapter(IExposableExpressionAccessor.class);
        if (exposableAccessor != null) {
          return exposableAccessor;
        }
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // PropertyTooltipProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the implementation of {@link PropertyTooltipProvider} for reflection {@link Method}.
   */
  public static PropertyTooltipProvider PropertyTooltipProvider_forMethod(final Method method) {
    final String methodSignature = ReflectionUtils.getMethodSignature(method);
    return new PropertyTooltipTextProvider() {
      @Override
      protected String getText(Property property) throws Exception {
        IJavaProject javaProject = property.getAdapter(IJavaProject.class);
        String javaDocText =
            JavaDocUtils.getTooltip(
                javaProject,
                method.getDeclaringClass().getName(),
                methodSignature);
        if (javaDocText == null) {
          return property.getTitle();
        }
        return javaDocText;
      }
    };
  }

  /**
   * @return the implementation of {@link PropertyTooltipProvider} for reflection {@link Field}.
   */
  public static PropertyTooltipProvider PropertyTooltipProvider_forField(final Field field) {
    return new PropertyTooltipTextProvider() {
      @Override
      protected String getText(Property property) throws Exception {
        // prepare property type
        IJavaProject javaProject = property.getAdapter(IJavaProject.class);
        IType findType = javaProject.findType(field.getDeclaringClass().getName());
        // prepare java doc
        String javaDocText = JavaDocUtils.getTooltip(findType.getField(field.getName()));
        if (javaDocText == null) {
          return property.getTitle();
        }
        return javaDocText;
      }
    };
  }
}
