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

import java.util.Comparator;

/**
 * Comparator for {@link ColorInfo} object using different criteria.
 *
 * @author scheglov_ke
 * @coverage core.ui
 */
public class ColorInfoComparator implements Comparator<ColorInfo> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instances
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final ColorInfoComparator TONE = new ColorInfoComparator();
  public static final ColorInfoComparator NAME = new ColorInfoComparator();
  public static final ColorInfoComparator HUE = new ColorInfoComparator();
  public static final ColorInfoComparator SATURATION = new ColorInfoComparator();
  public static final ColorInfoComparator LIGHTNESS = new ColorInfoComparator();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private ColorInfoComparator() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Comparator
  //
  ////////////////////////////////////////////////////////////////////////////
  public int compare(ColorInfo color_1, ColorInfo color_2) {
    if (this == TONE) {
      return getTone(color_1) - getTone(color_2);
    }
    if (this == NAME) {
      return color_1.m_name.compareTo(color_2.m_name);
    }
    if (this == HUE) {
      int hueCompare = Float.compare(color_1.m_hue, color_2.m_hue);
      if (hueCompare != 0) {
        return hueCompare;
      }
      return SATURATION.compare(color_1, color_2);
    }
    if (this == SATURATION) {
      int saturationCompare = Float.compare(color_1.m_saturation, color_2.m_saturation);
      if (saturationCompare != 0) {
        return saturationCompare;
      }
      return LIGHTNESS.compare(color_1, color_2);
    }
    if (this == LIGHTNESS) {
      return Float.compare(color_1.m_lightness, color_2.m_lightness);
    }
    return 0;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private static int getTone(ColorInfo colorInfo) {
    int r = colorInfo.m_rgb.red;
    int g = colorInfo.m_rgb.green;
    int b = colorInfo.m_rgb.blue;
    int result = r << 16 | g << 8 | b;
    return result;
  }
}
