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
package org.eclipse.wb.internal.core.model.description;

import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Implementation of {@link ExposingRule} that checks package of {@link Class} declaring given
 * {@link Method}.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class ExposingPackageRule extends ExposingRule {
  private final boolean m_include;
  private final String m_packageName;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ExposingPackageRule(boolean include, String packageName) {
    m_include = include;
    m_packageName = packageName;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Filter
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Boolean filter(Method method) {
    String packageName = CodeUtils.getPackage(method.getDeclaringClass().getName());
    if (packageName.equals(m_packageName)) {
      return m_include;
    }
    return null;
  }

  @Override
  public Boolean filter(Field field) {
    String packageName = CodeUtils.getPackage(field.getDeclaringClass().getName());
    if (packageName.equals(m_packageName)) {
      return m_include;
    }
    return null;
  }
}
