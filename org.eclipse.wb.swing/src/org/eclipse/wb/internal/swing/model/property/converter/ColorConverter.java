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
import org.eclipse.wb.internal.core.utils.ui.dialogs.color.ColorInfo;
import org.eclipse.wb.internal.swing.model.property.editor.color.AwtColors;

import java.awt.Color;

/**
 * The {@link ExpressionConverter} for {@link Color}.
 * 
 * @author lobas_av
 * @coverage swing.property.converter
 */
public class ColorConverter extends ExpressionConverter {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final ExpressionConverter INSTANCE = new ColorConverter();

  private ColorConverter() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ExpressionConverter
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toJavaSource(JavaInfo javaInfo, Object value) throws Exception {
    // handle "null" value
    if (value == null) {
      return "(java.awt.Color) null";
    }
    // find constant value
    for (ColorInfo[] colorInfos : new ColorInfo[][]{
        AwtColors.getColors_AWT(),
        AwtColors.getColors_System(),
        AwtColors.getColors_Swing(),}) {
      for (ColorInfo colorInfo : colorInfos) {
        if (value.equals(colorInfo.getToolkitColor())) {
          return (String) colorInfo.getData();
        }
      }
    }
    // Color constructor
    Color color = (Color) value;
    int alpha = color.getAlpha();
    String alphaParameter = alpha == 255 ? "" : ", " + Integer.toString(alpha);
    return "new java.awt.Color("
        + Integer.toString(color.getRed())
        + ", "
        + Integer.toString(color.getGreen())
        + ", "
        + Integer.toString(color.getBlue())
        + alphaParameter
        + ")";
  }
}