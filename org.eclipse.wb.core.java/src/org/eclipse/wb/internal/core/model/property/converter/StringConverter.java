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
import org.eclipse.wb.internal.core.utils.StringUtilities;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;

import org.eclipse.core.resources.IFile;

/**
 * The {@link ExpressionConverter} for {@link String}.
 *
 * @author scheglov_ke
 * @coverage core.model.property.converter
 */
public final class StringConverter extends ExpressionConverter {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final ExpressionConverter INSTANCE = new StringConverter();

  private StringConverter() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ExpressionConverter
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toJavaSource(JavaInfo javaInfo, Object value) {
    if (value == null) {
      return "(String) null";
    } else {
      String valueString = (String) value;
      String[] result = new String[]{null};
      // may be plain can be used
      if (result[0] == null) {
        result[0] = toJavaSource_UTF(javaInfo, valueString);
      }
      // escape required
      if (result[0] == null) {
        String escaped = StringUtilities.escapeJava(valueString);
        result[0] = '"' + escaped + '"';
      }
      // done
      return result[0];
    }
  }

  private static String toJavaSource_UTF(final JavaInfo javaInfo, final String valueString) {
    return ExecutionUtils.runObjectIgnore(new RunnableObjectEx<String>() {
      public String runObject() throws Exception {
        if (javaInfo != null) {
          IFile file = (IFile) javaInfo.getEditor().getModelUnit().getUnderlyingResource();
          String charset = file.getCharset();
          if (charset.equals("UTF-8") || charset.equals("UTF-16")) {
            String escaped = StringUtilities.escapeForJavaSource(valueString);
            return '"' + escaped + '"';
          }
        }
        return null;
      }
    }, null);
  }
}
