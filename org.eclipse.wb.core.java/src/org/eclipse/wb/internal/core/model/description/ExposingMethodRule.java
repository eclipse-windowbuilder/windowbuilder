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

import org.eclipse.wb.internal.core.utils.check.Assert;

import java.lang.reflect.Method;

/**
 * Implementation of {@link ExposingRule} that name of given {@link Method}.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class ExposingMethodRule extends ExposingRule {
  private final boolean m_include;
  private final String m_methodName;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ExposingMethodRule(boolean include, String methodName) {
    m_include = include;
    m_methodName = methodName;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Filter
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Boolean filter(Method method) {
    Assert.isLegal(method.getParameterTypes().length == 0, method.toString());
    // check method name
    if (!method.getName().equals(m_methodName)) {
      return null;
    }
    // OK, method satisfies to filter, so include/exclude it
    return m_include;
  }
}
