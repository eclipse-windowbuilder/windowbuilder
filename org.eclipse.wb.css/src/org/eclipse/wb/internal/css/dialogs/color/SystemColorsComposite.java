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
package org.eclipse.wb.internal.css.dialogs.color;

import org.eclipse.wb.internal.core.utils.ui.dialogs.color.AbstractColorDialog;
import org.eclipse.wb.internal.core.utils.ui.dialogs.color.AbstractColorsGridComposite;
import org.eclipse.wb.internal.core.utils.ui.dialogs.color.ColorInfo;
import org.eclipse.wb.internal.core.utils.ui.dialogs.color.ColorsGridComposite;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import java.lang.reflect.Method;

/**
 * Composite for selecting one of the named system colors.
 * 
 * @author scheglov_ke
 * @coverage CSS.ui
 */
public class SystemColorsComposite extends AbstractColorsGridComposite {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SystemColorsComposite(Composite parent, int style, AbstractColorDialog colorPickerDialog) {
    super(parent, style, colorPickerDialog);
    {
      ColorsGridComposite colorsGrid = createColorsGroup(this, null, SYSTEM_COLORS);
      colorsGrid.showNames(25);
      colorsGrid.setCellHeight(25);
      colorsGrid.setColumns(2);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // System colors (from http://www.newobjects.com/pages/ndl/alp/af-sysColor.htm)
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final int COLOR_SCROLLBAR = 0;
  private static final int COLOR_BACKGROUND = 1;
  private static final int COLOR_ACTIVECAPTION = 2;
  private static final int COLOR_INACTIVECAPTION = 3;
  private static final int COLOR_MENU = 4;
  private static final int COLOR_WINDOW = 5;
  private static final int COLOR_WINDOWFRAME = 6;
  private static final int COLOR_MENUTEXT = 7;
  private static final int COLOR_WINDOWTEXT = 8;
  private static final int COLOR_CAPTIONTEXT = 9;
  private static final int COLOR_ACTIVEBORDER = 10;
  private static final int COLOR_INACTIVEBORDER = 11;
  private static final int COLOR_APPWORKSPACE = 12;
  private static final int COLOR_HIGHLIGHT = 13;
  private static final int COLOR_HIGHLIGHTTEXT = 14;
  private static final int COLOR_BTNFACE = 15;
  private static final int COLOR_BTNSHADOW = 16;
  private static final int COLOR_GRAYTEXT = 17;
  private static final int COLOR_BTNTEXT = 18;
  private static final int COLOR_INACTIVECAPTIONTEXT = 19;
  private static final int COLOR_BTNHIGHLIGHT = 20;
  private static final int COLOR_3DDKSHADOW = 21;
  //private static final int COLOR_3DLIGHT = 22;
  private static final int COLOR_INFOTEXT = 23;
  private static final int COLOR_INFOBK = 24;
  //private static final int COLOR_DESKTOP = COLOR_BACKGROUND;
  private static final int COLOR_3DFACE = COLOR_BTNFACE;
  private static final int COLOR_3DSHADOW = COLOR_BTNSHADOW;
  private static final int COLOR_3DHIGHLIGHT = COLOR_BTNHIGHLIGHT;
  private static final int COLOR_3DHILIGHT = COLOR_BTNHIGHLIGHT;
  //private static final int COLOR_BTNHILIGHT = COLOR_BTNHIGHLIGHT;
  public static final ColorInfo[] SYSTEM_COLORS =
      new ColorInfo[]{
          createColorInfo("ActiveBorder", "Border color of the active windows.", COLOR_ACTIVEBORDER),
          createColorInfo("ActiveCaption", "Active window caption background.", COLOR_ACTIVECAPTION),
          createColorInfo(
              "AppWorkspace",
              "Background of the MDI frame windows.",
              COLOR_APPWORKSPACE),
          createColorInfo("Background", "Desktop background.", COLOR_BACKGROUND),
          createColorInfo("ButtonFace", "Background color of the buttons.", COLOR_BTNFACE),
          createColorInfo("ButtonHighlight", "Highlight color for the buttons.", COLOR_BTNHIGHLIGHT),
          createColorInfo(
              "ButtonShadow",
              "Shadow color for three-dimensional display elements.",
              COLOR_BTNSHADOW),
          createColorInfo("ButtonText", "Text on push buttons.", COLOR_BTNTEXT),
          createColorInfo(
              "CaptionText",
              "Text in caption, size box, and scrollbar arrow box.",
              COLOR_CAPTIONTEXT),
          createColorInfo("GrayText", "Text color for the disabled items.", COLOR_GRAYTEXT),
          createColorInfo(
              "Highlight",
              "Highlight color - selected menu items, list box selections, etc.",
              COLOR_HIGHLIGHT),
          createColorInfo(
              "HighlightText",
              "Text color for the highlighted items.",
              COLOR_HIGHLIGHTTEXT),
          createColorInfo("InactiveBorder", "Inactive window border.", COLOR_INACTIVEBORDER),
          createColorInfo("InactiveCaption", "Inactive window caption.", COLOR_INACTIVECAPTION),
          createColorInfo(
              "InactiveCaptionText",
              "Color of text in an inactive caption.",
              COLOR_INACTIVECAPTIONTEXT),
          createColorInfo("InfoBackground", "Background color for tooltip controls.", COLOR_INFOBK),
          createColorInfo("InfoText", "Text color for tooltip controls.", COLOR_INFOTEXT),
          createColorInfo("Menu", "Menu background.", COLOR_MENU),
          createColorInfo("MenuText", "Text in menus.", COLOR_MENUTEXT),
          createColorInfo("Scrollbar", "Scroll bar gray area.", COLOR_SCROLLBAR),
          createColorInfo(
              "ThreeDDarkShadow",
              "Dark shadow for three-dimensional display elements.",
              COLOR_3DDKSHADOW),
          createColorInfo(
              "ThreeDFace",
              "Face color for three-dimensional display elements.",
              COLOR_3DFACE),
          createColorInfo(
              "ThreeDHighlight",
              "Highlight color for three-dimensional display elements.",
              COLOR_3DHIGHLIGHT),
          createColorInfo(
              "ThreeDLightShadow",
              "Light color for three-dimensional display elements.",
              COLOR_3DHILIGHT),
          createColorInfo(
              "ThreeDShadow",
              "Dark shadow for three-dimensional display elements.",
              COLOR_3DSHADOW),
          createColorInfo("Window", "Window background.", COLOR_WINDOW),
          createColorInfo("WindowFrame", "Window frame.", COLOR_WINDOWFRAME),
          createColorInfo("WindowText", "Text in windows.", COLOR_WINDOWTEXT),};

  /**
   * @return the {@link ColorInfo} for given system color id (Windows only).
   */
  private static ColorInfo createColorInfo(String name, String description, int systemColorId) {
    RGB rgb;
    try {
      // prepare color handle
      Integer handle;
      {
        Class<?> c_OS = Class.forName("org.eclipse.swt.internal.win32.OS");
        Method m_OS_GetSysColor = c_OS.getMethod("GetSysColor", new Class[]{int.class});
        handle = (Integer) m_OS_GetSysColor.invoke(null, new Object[]{new Integer(systemColorId)});
      }
      // prepare RGB
      {
        Method m_Color_win32_new =
            Color.class.getMethod("win32_new", new Class[]{Device.class, int.class});
        Color color =
            (Color) m_Color_win32_new.invoke(null, new Object[]{Display.getDefault(), handle});
        rgb = color.getRGB();
        color.dispose();
      }
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
    //
    return new ColorInfo(name, description, rgb);
  }
}
