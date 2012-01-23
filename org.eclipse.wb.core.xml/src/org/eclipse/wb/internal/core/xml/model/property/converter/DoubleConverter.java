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
package org.eclipse.wb.internal.core.xml.model.property.converter;

import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;

/**
 * {@link ExpressionConverter} for <code>double</code> values.
 * 
 * @author scheglov_ke
 * @author mitin_aa
 * @coverage XML.model.property
 */
public final class DoubleConverter extends ExpressionConverter {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final ExpressionConverter INSTANCE = new DoubleConverter();

  private DoubleConverter() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ExpressionConverter
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toSource(XmlObjectInfo object, Object value) throws Exception {
    if (value instanceof Double) {
      return Double.toString((Double) value);
    }
    return null;
  }
}
