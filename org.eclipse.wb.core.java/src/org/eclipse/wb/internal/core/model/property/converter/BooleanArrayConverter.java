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
 * The {@link ExpressionConverter} for array of {@link boolean}'s.
 *
 * @author scheglov_ke
 * @coverage core.model.property.converter
 */
public final class BooleanArrayConverter extends ExpressionConverter {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final ExpressionConverter INSTANCE = new BooleanArrayConverter();

  private BooleanArrayConverter() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ExpressionConverter
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toJavaSource(JavaInfo javaInfo, Object value) throws Exception {
    if (value == null) {
      return "(boolean[]) null";
    } else {
      StringBuilder buffer = new StringBuilder();
      buffer.append("new boolean[] {");
      // add items
      boolean[] items = (boolean[]) value;
      for (int i = 0; i < items.length; i++) {
        boolean item = items[i];
        if (i != 0) {
          buffer.append(", ");
        }
        buffer.append(BooleanConverter.INSTANCE.toJavaSource(javaInfo, item));
      }
      //
      buffer.append("}");
      return buffer.toString();
    }
  }
}
