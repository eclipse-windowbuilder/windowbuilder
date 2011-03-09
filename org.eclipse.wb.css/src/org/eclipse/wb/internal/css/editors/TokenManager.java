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
package org.eclipse.wb.internal.css.editors;

import com.google.common.collect.Maps;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import java.util.Map;

/**
 * Accessor for {@link IToken} values.
 * 
 * @author scheglov_ke
 * @coverage CSS.editor
 */
public final class TokenManager {
  private final IPreferenceStore m_preferenceStore;
  private final Map<String, IToken> m_tokenTable = Maps.newHashMap();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TokenManager(IPreferenceStore preferenceStore) {
    m_preferenceStore = preferenceStore;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public IToken getToken(String prefKey) {
    Token token = (Token) m_tokenTable.get(prefKey);
    if (token == null) {
      String colorName = m_preferenceStore.getString(prefKey);
      RGB rgb = StringConverter.asRGB(colorName);
      token = new Token(new TextAttribute(getColor(rgb)));
      m_tokenTable.put(prefKey, token);
    }
    return token;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Preferences
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean affectsTextPresentation(PropertyChangeEvent event) {
    Token token = (Token) m_tokenTable.get(event.getProperty());
    return token != null;
  }

  public void handlePreferenceStoreChanged(PropertyChangeEvent event) {
    String prefKey = event.getProperty();
    Token token = (Token) m_tokenTable.get(prefKey);
    if (token != null) {
      String colorName = m_preferenceStore.getString(prefKey);
      RGB rgb = StringConverter.asRGB(colorName);
      token.setData(new TextAttribute(getColor(rgb)));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Colors
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final Map<RGB, Color> m_rgbColors = Maps.newHashMap();
  private static final Map<Integer, Color> m_systemColors = Maps.newHashMap();

  /**
   * @return the shared instance of system {@link Color}.
   */
  public static Color getColor(int id) {
    Color color = m_systemColors.get(id);
    if (color == null) {
      color = Display.getDefault().getSystemColor(id);
      m_systemColors.put(id, color);
    }
    return color;
  }

  /**
   * @return the shared instance of {@link Color} with given components.
   */
  public static Color getColor(int red, int green, int blue) {
    return getColor(new RGB(red, green, blue));
  }

  /**
   * @return the shared instance of {@link Color} for given {@link RGB}.
   */
  public static Color getColor(RGB rgb) {
    Color color = m_rgbColors.get(rgb);
    if (color == null) {
      color = new Color(Display.getCurrent(), rgb);
      m_rgbColors.put(rgb, color);
    }
    return color;
  }
}
