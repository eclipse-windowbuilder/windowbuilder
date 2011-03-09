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
package org.eclipse.wb.internal.swing.model.property.editor.font;

import java.awt.Font;

/**
 * Information object about {@link Font}.
 * 
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public abstract class FontInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Font} value.
   */
  public abstract Font getFont();

  /**
   * @return the description to show for user.
   */
  public abstract String getText();

  /**
   * @return the Java source.
   */
  public abstract String getSource() throws Exception;

  /**
   * @return the Java source for clipboard.
   */
  public String getClipboardSource() throws Exception {
    return getSource();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the text presentation of given {@link Font}.
   */
  protected static String getText(Font font) {
    StringBuilder sb = new StringBuilder();
    // family
    sb.append(font.getFamily());
    // size
    {
      sb.append(" ");
      sb.append(font.getSize());
    }
    // style
    {
      int style = font.getStyle();
      if ((style & Font.BOLD) != 0) {
        sb.append(" Bold");
      }
      if ((style & Font.ITALIC) != 0) {
        sb.append(" Italic");
      }
    }
    // final result
    return sb.toString();
  }

  /**
   * @return the style source for given {@link Font}.
   */
  protected static String getStyleSource(int style) {
    if ((style & (Font.BOLD | Font.ITALIC)) == (Font.BOLD | Font.ITALIC)) {
      return "java.awt.Font.BOLD | java.awt.Font.ITALIC";
    } else if ((style & Font.BOLD) == Font.BOLD) {
      return "java.awt.Font.BOLD";
    } else if ((style & Font.ITALIC) == Font.ITALIC) {
      return "java.awt.Font.ITALIC";
    } else {
      return "java.awt.Font.PLAIN";
    }
  }
}
