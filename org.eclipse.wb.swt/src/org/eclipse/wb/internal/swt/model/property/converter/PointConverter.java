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
package org.eclipse.wb.internal.swt.model.property.converter;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.internal.core.model.property.converter.ExpressionConverter;
import org.eclipse.wb.internal.swt.support.PointSupport;

/**
 * The {@link ExpressionConverter} for {@link org.eclipse.swt.graphics.Point}.
 *
 * @author lobas_av
 * @coverage swt.property.converter
 */
public final class PointConverter extends ExpressionConverter {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final ExpressionConverter INSTANCE = new PointConverter();

  private PointConverter() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ExpressionConverter
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toJavaSource(JavaInfo javaInfo, Object value) throws Exception {
    if (value == null) {
      return "(org.eclipse.swt.graphics.Point) null";
    } else {
      Point point = PointSupport.getPoint(value);
      return "new org.eclipse.swt.graphics.Point(" + point.x + ", " + point.y + ")";
    }
  }
}