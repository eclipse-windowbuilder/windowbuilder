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

import java.awt.Font;

import javax.swing.UIManager;

/**
 * Information about {@link Font} from {@link UIManager}.
 * 
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class UiManagerFontInfo extends FontInfo {
  private final String m_key;
  private final Font m_font;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public UiManagerFontInfo(String key, Font font) {
    m_key = key;
    m_font = font;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the key value of font in {@link UIManager}.
   */
  public String getKey() {
    return m_key;
  }

  /**
   * @return the only value text, without key in contrast to {@link #getText()}.
   */
  public String getValueText() {
    return getText(m_font);
  }

  /**
   * @return the inner {@link Font}.
   */
  @Override
  public Font getFont() {
    return m_font;
  }

  @Override
  public String getText() {
    return m_key + ", " + getText(m_font);
  }

  @Override
  public String getSource() throws Exception {
    return "javax.swing.UIManager.getFont("
        + StringConverter.INSTANCE.toJavaSource(null, m_key)
        + ")";
  }
}
