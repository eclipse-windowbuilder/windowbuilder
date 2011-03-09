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
package org.eclipse.wb.internal.swing.model.property.converter;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.property.converter.ExpressionConverter;
import org.eclipse.wb.internal.core.model.property.converter.IntegerConverter;

import java.awt.Dimension;

/**
 * The {@link ExpressionConverter} for {@link Dimension}.
 * 
 * @author scheglov_ke
 * @coverage swing.property.converter
 */
public final class DimensionConverter extends ExpressionConverter {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final ExpressionConverter INSTANCE = new DimensionConverter();

  private DimensionConverter() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ExpressionConverter
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toJavaSource(JavaInfo javaInfo, Object value) throws Exception {
    if (value == null) {
      return "(java.awt.Dimension) null";
    } else {
      Dimension dimension = (Dimension) value;
      String widthSource = IntegerConverter.INSTANCE.toJavaSource(javaInfo, dimension.width);
      String heightSource = IntegerConverter.INSTANCE.toJavaSource(javaInfo, dimension.height);
      return "new java.awt.Dimension(" + widthSource + ", " + heightSource + ")";
    }
  }
}
