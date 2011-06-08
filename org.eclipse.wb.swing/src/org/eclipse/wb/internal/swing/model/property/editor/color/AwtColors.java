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
package org.eclipse.wb.internal.swing.model.property.editor.color;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.ui.dialogs.color.ColorInfo;

import java.awt.Color;
import java.awt.SystemColor;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.swing.UIDefaults;
import javax.swing.UIManager;

/**
 * Container for AWT/Swing colors.
 * 
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class AwtColors {
  ////////////////////////////////////////////////////////////////////////////
  //
  // AWT
  //
  ////////////////////////////////////////////////////////////////////////////
  private static ColorInfo[] m_colors_AWT;

  /**
   * @return {@link ColorInfo}'s for colors from {@link Color}.
   */
  public static ColorInfo[] getColors_AWT() {
    if (m_colors_AWT == null) {
      List<ColorInfo> colors = Lists.newArrayList();
      try {
        Field[] colorFields = Color.class.getFields();
        for (int i = 0; i < colorFields.length; i++) {
          Field field = colorFields[i];
          String fieldName = field.getName();
          if (field.getType() == Color.class
              && fieldName.toUpperCase(Locale.ENGLISH).equals(fieldName)) {
            Color color = (Color) field.get(null);
            ColorInfo colorInfo =
                new ColorInfo(fieldName, color.getRed(), color.getGreen(), color.getBlue());
            colorInfo.setData("java.awt.Color." + fieldName);
            colorInfo.setToolkitColor(color);
            colors.add(colorInfo);
          }
        }
      } catch (Throwable e) {
        throw ReflectionUtils.propagate(e);
      }
      m_colors_AWT = colors.toArray(new ColorInfo[colors.size()]);
    }
    return m_colors_AWT;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // System
  //
  ////////////////////////////////////////////////////////////////////////////
  private static ColorInfo[] m_colors_System;

  /**
   * @return {@link ColorInfo}'s for colors from {@link SystemColor}.
   */
  public static ColorInfo[] getColors_System() {
    if (m_colors_System == null) {
      List<ColorInfo> colors = Lists.newArrayList();
      try {
        Field[] colorFields = SystemColor.class.getFields();
        for (int i = 0; i < colorFields.length; i++) {
          Field field = colorFields[i];
          String fieldName = field.getName();
          if (field.getType() == SystemColor.class) {
            Color color = (Color) field.get(null);
            ColorInfo colorInfo =
                new ColorInfo(fieldName, color.getRed(), color.getGreen(), color.getBlue());
            colorInfo.setData("java.awt.SystemColor." + fieldName);
            colorInfo.setToolkitColor(color);
            colors.add(colorInfo);
          }
        }
      } catch (Throwable e) {
        throw ReflectionUtils.propagate(e);
      }
      m_colors_System = colors.toArray(new ColorInfo[colors.size()]);
    }
    return m_colors_System;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Swing
  //
  ////////////////////////////////////////////////////////////////////////////
  private static Map<String, ColorInfo[]> m_LAFColors = Maps.newHashMap();

  //private static ColorInfo[] m_colors;
  /**
   * @return {@link ColorInfo}'s for colors from {@link UIManager}.
   */
  public static ColorInfo[] getColors_Swing() {
    String lafClassName = UIManager.getLookAndFeel().getClass().getName();
    ColorInfo[] colors = m_LAFColors.get(lafClassName);
    if (colors == null) {
      List<ColorInfo> colorList = Lists.newArrayList();
      {
        UIDefaults defaults = UIManager.getLookAndFeelDefaults();
        // prepare set of all String keys in UIManager
        Set<String> allKeys = Sets.newTreeSet();
        for (Iterator<?> I = defaults.keySet().iterator(); I.hasNext();) {
          Object key = I.next();
          if (key instanceof String) {
            allKeys.add((String) key);
          }
        }
        // add ColorInfo for each Color key
        for (String key : allKeys) {
          Color color = defaults.getColor(key);
          if (color != null) {
            ColorInfo colorInfo =
                new ColorInfo(key, color.getRed(), color.getGreen(), color.getBlue());
            colorInfo.setData("javax.swing.UIManager.getColor(\"" + key + "\")");
            colorInfo.setToolkitColor(color);
            colorList.add(colorInfo);
          }
        }
      }
      // convert into array
      colors = colorList.toArray(new ColorInfo[colorList.size()]);
      m_LAFColors.put(lafClassName, colors);
    }
    return colors;
  }
}
