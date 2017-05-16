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
 * The {@link ExpressionConverter} for {@link Double}.
 *
 * @author scheglov_ke
 * @coverage core.model.property.converter
 */
public final class DoubleObjectConverter extends AbstractNumberConverter {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final ExpressionConverter INSTANCE = new DoubleObjectConverter();

  private DoubleObjectConverter() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ExpressionConverter
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toJavaSource(JavaInfo javaInfo, Object value) {
    if (value == null) {
      return "(Double) null";
    }
    // has value
    String text = ((Double) value).toString();
    // may be use auto-boxing
    if (isBoxingEnabled(javaInfo)) {
      return text;
    }
    // use explicit boxing
    return "new Double(" + text + ")";
  }
}
