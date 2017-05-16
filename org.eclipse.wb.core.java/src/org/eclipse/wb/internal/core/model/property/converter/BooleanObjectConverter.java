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
package org.eclipse.wb.internal.core.model.property.converter;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;

import org.eclipse.jdt.core.IJavaProject;

/**
 * The {@link ExpressionConverter} for {@link Boolean}.
 *
 * @author scheglov_ke
 * @coverage core.model.property.converter
 */
public final class BooleanObjectConverter extends ExpressionConverter {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final ExpressionConverter INSTANCE = new BooleanObjectConverter();

  private BooleanObjectConverter() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ExpressionConverter
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toJavaSource(JavaInfo javaInfo, Object value) {
    if (value == null) {
      return "(Boolean) null";
    }
    // has value
    boolean b = ((Boolean) value).booleanValue();
    // may be use auto-boxing
    if (javaInfo != null) {
      IJavaProject javaProject = javaInfo.getEditor().getJavaProject();
      if (ProjectUtils.isJDK15(javaProject)) {
        return Boolean.toString(b);
      }
    }
    // use explicit boxing
    if (b) {
      return "Boolean.TRUE";
    } else {
      return "Boolean.FALSE";
    }
  }
}
