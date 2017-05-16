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

/**
 * The {@link ExpressionConverter} for array of {@link int}'s.
 *
 * @author scheglov_ke
 * @coverage core.model.property.converter
 */
public final class IntegerArrayConverter extends ExpressionConverter {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final ExpressionConverter INSTANCE = new IntegerArrayConverter();

  private IntegerArrayConverter() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ExpressionConverter
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toJavaSource(JavaInfo javaInfo, Object value) throws Exception {
    if (value == null) {
      return "(int[]) null";
    } else {
      StringBuilder buffer = new StringBuilder();
      buffer.append("new int[] {");
      // add items
      int[] items = (int[]) value;
      for (int i = 0; i < items.length; i++) {
        int item = items[i];
        if (i != 0) {
          buffer.append(", ");
        }
        buffer.append(IntegerConverter.INSTANCE.toJavaSource(javaInfo, item));
      }
      //
      buffer.append("}");
      return buffer.toString();
    }
  }
}
