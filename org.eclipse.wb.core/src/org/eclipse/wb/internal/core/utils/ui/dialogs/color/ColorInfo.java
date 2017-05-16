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
package org.eclipse.wb.internal.core.utils.ui.dialogs.color;

import org.eclipse.swt.graphics.RGB;

/**
 * Information about color.
 *
 * @author scheglov_ke
 * @coverage core.ui
 */
public final class ColorInfo {
  public final String m_name;
  public final String m_description;
  public final RGB m_rgb;
  public final float m_hue;
  public final float m_saturation;
  public final float m_lightness;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public ColorInfo(int red, int green, int blue) {
    this(null, null, new RGB(red, green, blue));
  }

  public ColorInfo(String name, int red, int green, int blue) {
    this(name, null, new RGB(red, green, blue));
  }

  public ColorInfo(String name, RGB rgb) {
    this(name, null, rgb);
  }

  public ColorInfo(String name, String description, RGB rgb) {
    m_name = name;
    m_description = description;
    m_rgb = rgb;
    {
      float[] hsl = RGB_to_HSL(m_rgb.red, m_rgb.green, m_rgb.blue);
      m_hue = hsl[0];
      m_saturation = hsl[1];
      m_lightness = hsl[2];
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return name to show for user
   */
  public String getName() {
    if (m_name != null) {
      return m_name;
    }
    return "";
  }

  /**
   * @return title to show for user
   */
  public String getTitle() {
    String title = "";
    if (m_name != null) {
      title += m_name + " ";
    }
    if (m_rgb != null) {
      title += "#" + getHexRGB();
      title += " H:" + (int) (m_hue * 360) + "deg";
      title += " S:" + (int) (m_saturation * 100) + "%";
      title += " L:" + (int) (m_lightness * 100) + "%";
    }
    return title;
  }

  /**
   * @return RGB object of this color.
   */
  public RGB getRGB() {
    return m_rgb;
  }

  /**
   * @return RGB of this color in hex format, for example for white - "ffffff".
   */
  public String getHexRGB() {
    return getHexPartRGB(m_rgb.red) + getHexPartRGB(m_rgb.green) + getHexPartRGB(m_rgb.blue);
  }

  /**
   * @return RGB of this color in comma format, for example for white - "255, 255, 255".
   */
  public String getCommaRGB() {
    return m_rgb.red + ", " + m_rgb.green + ", " + m_rgb.blue;
  }

  private static String getHexPartRGB(int value) {
    String result = Integer.toHexString(value);
    if (result.length() < 2) {
      result = "0" + value;
    }
    return result;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Data
  //
  ////////////////////////////////////////////////////////////////////////////
  private Object m_data;

  /**
   * @return the data associated with this {@link ColorInfo}.
   */
  public Object getData() {
    return m_data;
  }

  /**
   * Sets the data associated with this {@link ColorInfo}.
   */
  public void setData(Object data) {
    m_data = data;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AWT color
  //
  ////////////////////////////////////////////////////////////////////////////
  private Object m_toolkitColor;

  /**
   * @return toolkit related color object.
   */
  public Object getToolkitColor() {
    return m_toolkitColor;
  }

  /**
   * Sets toolkit related color object.
   */
  public void setToolkitColor(Object toolkitColor) {
    m_toolkitColor = toolkitColor;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Convert color from RGB to HSL.
   *
   * See http://130.113.54.154/~monger/hsl-rgb.html for algorithm.
   */
  private static float[] RGB_to_HSL(int r, int g, int b) {
    float hue = 0.0f;
    float saturation = 0.0f;
    float lightness = 0.0f;
    //
    float r_color = r / 255f;
    float g_color = g / 255f;
    float b_color = b / 255f;
    float min_color = Math.min(Math.min(r_color, g_color), b_color);
    float max_color = Math.max(Math.max(r_color, g_color), b_color);
    // prepare lightness
    lightness = (max_color + min_color) / 2.0f;
    // prepare hue and saturation
    float dist_color = max_color - min_color;
    if (dist_color == 0.0f) {
      hue = 0.0f;
      saturation = 0.0f;
    } else {
      // prepare saturation
      if (lightness < 0.5f) {
        saturation = dist_color / (max_color + min_color);
      } else {
        saturation = dist_color / (2.0f - max_color - min_color);
      }
      // prepare hue
      if (equals(r_color, max_color)) {
        hue = (g_color - b_color) / dist_color;
      } else if (equals(g_color, max_color)) {
        hue = 2.0f + (b_color - r_color) / dist_color;
      } else if (equals(b_color, max_color)) {
        hue = 4.0f + (r_color - g_color) / dist_color;
      }
      // convert hue into [0, 1] of interval [0, 360]
      if (hue < 0.0f) {
        hue++;
      }
      hue /= 6f;
    }
    //
    return new float[]{hue, saturation, lightness};
  }

  /**
   * Checks if two given {@link float} are equal.
   */
  private static boolean equals(float a, float b) {
    return Math.abs(a - b) < 0.00001;
  }
}
