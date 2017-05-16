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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Locale;

/**
 * The {@link ExpressionConverter} for {@link Locale}.
 *
 * @author sablin_aa
 * @coverage core.model.property.converter
 */
public final class LocaleConverter extends ExpressionConverter {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final ExpressionConverter INSTANCE = new LocaleConverter();

  private LocaleConverter() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ExpressionConverter
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toJavaSource(JavaInfo javaInfo, Object value) throws Exception {
    if (value == null) {
      return "(java.util.Locale) null";
    } else {
      // find standard values
      for (Field field : Locale.class.getFields()) {
        int fModifiers = field.getModifiers();
        if (Modifier.isFinal(fModifiers) && Modifier.isStatic(fModifiers)) {
          if (field.getType() == Locale.class && value.equals(field.get(null))) {
            return "java.util.Locale." + field.getName();
          }
        }
      }
      // convert language+country to create instance of Locale
      Locale locale = (Locale) value;
      StringBuilder buffer = new StringBuilder(256);
      buffer.append("new java.util.Locale(");
      buffer.append(StringConverter.INSTANCE.toJavaSource(javaInfo, locale.getLanguage()));
      String country = locale.getCountry();
      if (country.length() != 0) {
        buffer.append(", ");
        buffer.append(StringConverter.INSTANCE.toJavaSource(javaInfo, country));
      }
      buffer.append(")");
      return buffer.toString();
    }
  }
}
