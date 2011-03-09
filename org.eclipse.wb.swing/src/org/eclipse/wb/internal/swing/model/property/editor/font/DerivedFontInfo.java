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

import org.eclipse.wb.internal.core.model.property.converter.StringConverter;

import org.apache.commons.lang.StringUtils;

import java.awt.Font;
import java.text.MessageFormat;

/**
 * Information object {@link Font} derived from some existing one.
 * 
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class DerivedFontInfo extends FontInfo {
  final Font m_baseFont;
  final String m_baseFontSource;
  final String m_baseFontClipboardSource;
  final String m_newFamily;
  final Boolean m_newBold;
  final Boolean m_newItalic;
  final Integer m_deltaSize;
  final Integer m_newSize;
  private final Font m_font;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DerivedFontInfo(Font baseFont,
      String baseFontSource,
      String baseFontClipboardSource,
      String newFamily,
      Boolean newBold,
      Boolean newItalic,
      Integer deltaSize,
      Integer newSize) {
    m_baseFont = baseFont;
    m_baseFontSource = baseFontSource;
    m_baseFontClipboardSource = baseFontClipboardSource;
    m_newFamily = newFamily;
    m_newBold = newBold;
    m_newItalic = newItalic;
    m_deltaSize = deltaSize;
    m_newSize = newSize;
    // create derived Font
    {
      String family = newFamily != null ? newFamily : baseFont.getFamily();
      // style
      int style = baseFont.getStyle();
      if (newBold != null) {
        if (newBold.booleanValue()) {
          style |= Font.BOLD;
        } else {
          style &= ~Font.BOLD;
        }
      }
      if (newItalic != null) {
        if (newItalic.booleanValue()) {
          style |= Font.ITALIC;
        } else {
          style &= ~Font.ITALIC;
        }
      }
      // size
      int size = baseFont.getSize();
      if (deltaSize != null) {
        size += deltaSize.intValue();
      } else if (newSize != null) {
        size = newSize.intValue();
      }
      // create derived Font
      m_font = new Font(family, style, size);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Font getFont() {
    return m_font;
  }

  @Override
  public String getText() {
    StringBuilder sb = new StringBuilder();
    // derive attributes
    {
      // family
      if (m_newFamily != null) {
        sb.append("*");
        sb.append(m_newFamily);
      }
      // size
      if (m_deltaSize != null) {
        int deltaSize = m_deltaSize.intValue();
        if (sb.length() != 0) {
          sb.append(" ");
        }
        if (deltaSize > 0) {
          sb.append("+");
          sb.append(deltaSize);
        } else if (deltaSize < 0) {
          sb.append(deltaSize);
        }
      } else if (m_newSize != null) {
        if (sb.length() != 0) {
          sb.append(" ");
        }
        sb.append(m_newSize.intValue());
      }
      // style
      if (m_newBold != null) {
        if (sb.length() != 0) {
          sb.append(" ");
        }
        sb.append(m_newBold.booleanValue() ? "+Bold" : "-Bold");
      }
      if (m_newItalic != null) {
        if (sb.length() != 0) {
          sb.append(" ");
        }
        sb.append(m_newItalic.booleanValue() ? "+Italic" : "-Italic");
      }
    }
    // if no changes, say this
    if (sb.length() == 0) {
      sb.append("<no changes>");
    }
    // "value" of Font
    {
      sb.append(", ");
      sb.append(getText(m_font));
    }
    // final result
    return sb.toString();
  }

  @Override
  public String getSource() throws Exception {
    return getSource(m_baseFontSource);
  }

  @Override
  public String getClipboardSource() throws Exception {
    if (m_baseFontClipboardSource == null) {
      return null;
    }
    return getSource(m_baseFontClipboardSource);
  }

  private String getSource(String baseFontSource) throws Exception {
    // style
    boolean styleChanged = true;
    String styleSource = baseFontSource + ".getStyle()";
    {
      if (m_newBold == null && m_newItalic == null) {
        styleChanged = false;
      } else if (m_newBold != null && m_newBold.booleanValue() && m_newItalic == null) {
        styleSource += " | java.awt.Font.BOLD";
      } else if (m_newBold != null && !m_newBold.booleanValue() && m_newItalic == null) {
        styleSource += " & ~java.awt.Font.BOLD";
      } else if (m_newBold == null && m_newItalic != null && m_newItalic.booleanValue()) {
        styleSource += " | java.awt.Font.ITALIC";
      } else if (m_newBold == null && m_newItalic != null && !m_newItalic.booleanValue()) {
        styleSource += " & ~java.awt.Font.ITALIC";
      } else if (m_newBold != null
          && m_newItalic != null
          && m_newBold.booleanValue()
          && m_newItalic.booleanValue()) {
        styleSource += " | java.awt.Font.BOLD";
        styleSource += " | java.awt.Font.ITALIC";
      } else if (m_newBold != null
          && m_newItalic != null
          && !m_newBold.booleanValue()
          && m_newItalic.booleanValue()) {
        styleSource += " & ~java.awt.Font.BOLD";
        styleSource += " | java.awt.Font.ITALIC";
      } else if (m_newBold != null
          && m_newItalic != null
          && m_newBold.booleanValue()
          && !m_newItalic.booleanValue()) {
        styleSource += " & ~java.awt.Font.ITALIC";
        styleSource += " | java.awt.Font.BOLD";
      } else if (m_newBold != null
          && m_newItalic != null
          && !m_newBold.booleanValue()
          && !m_newItalic.booleanValue()) {
        styleSource += " & ~java.awt.Font.BOLD";
        styleSource += " & ~java.awt.Font.ITALIC";
      }
    }
    // size
    boolean sizeChanged = false;
    String sizeSource = baseFontSource + ".getSize()";
    {
      if (m_deltaSize != null) {
        int deltaSize = m_deltaSize.intValue();
        if (deltaSize > 0) {
          sizeSource += " + " + deltaSize + "f";
        } else {
          sizeSource += " - " + -deltaSize + "f";
        }
        sizeChanged = true;
      } else if (m_newSize != null) {
        sizeSource = "" + m_newSize.intValue() + "f";
        sizeChanged = true;
      }
    }
    // new family
    if (m_newFamily != null) {
      sizeSource = StringUtils.removeEnd(sizeSource, "f");
      return MessageFormat.format(
          "new java.awt.Font({0}, {1}, {2})",
          StringConverter.INSTANCE.toJavaSource(null, m_newFamily),
          styleSource,
          sizeSource);
    }
    // style/size change
    if (styleChanged & sizeChanged) {
      return MessageFormat.format(
          "{0}.deriveFont({1}, {2})",
          baseFontSource,
          styleSource,
          sizeSource);
    }
    if (styleChanged) {
      return MessageFormat.format("{0}.deriveFont({1})", baseFontSource, styleSource);
    }
    if (sizeChanged) {
      return MessageFormat.format("{0}.deriveFont({1})", baseFontSource, sizeSource);
    }
    // no changes
    return null;
  }
}
