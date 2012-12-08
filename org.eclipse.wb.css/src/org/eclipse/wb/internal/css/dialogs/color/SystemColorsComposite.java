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
import org.eclipse.wb.internal.css.Messages;

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
      ColorsGridComposite colorsGrid = createColorsGroup(this, null, getSystemColors());
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
  private static ColorInfo[] SYSTEM_COLORS = null;

  public static ColorInfo[] getSystemColors() {
    if (SYSTEM_COLORS == null) {
      SYSTEM_COLORS =
          new ColorInfo[]{
              createColorInfo(
                  Messages.SystemColorsComposite_activeBorder,
                  Messages.SystemColorsComposite_activeBorderDescription,
                  COLOR_ACTIVEBORDER),
              createColorInfo(
                  Messages.SystemColorsComposite_activeCaption,
                  Messages.SystemColorsComposite_activeCaptionDescription,
                  COLOR_ACTIVECAPTION),
              createColorInfo(
                  Messages.SystemColorsComposite_appWorkspace,
                  Messages.SystemColorsComposite_appWorkspaceDescription,
                  COLOR_APPWORKSPACE),
              createColorInfo(
                  Messages.SystemColorsComposite_background,
                  Messages.SystemColorsComposite_backgroundDescription,
                  COLOR_BACKGROUND),
              createColorInfo(
                  Messages.SystemColorsComposite_buttonFace,
                  Messages.SystemColorsComposite_buttonFaceDescription,
                  COLOR_BTNFACE),
              createColorInfo(
                  Messages.SystemColorsComposite_buttonHighlight,
                  Messages.SystemColorsComposite_buttonHighlightDescription,
                  COLOR_BTNHIGHLIGHT),
              createColorInfo(
                  Messages.SystemColorsComposite_buttonShadow,
                  Messages.SystemColorsComposite_buttonShadowDescription,
                  COLOR_BTNSHADOW),
              createColorInfo(
                  Messages.SystemColorsComposite_buttonText,
                  Messages.SystemColorsComposite_buttonTextDescription,
                  COLOR_BTNTEXT),
              createColorInfo(
                  Messages.SystemColorsComposite_captionText,
                  Messages.SystemColorsComposite_captionTextDescription,
                  COLOR_CAPTIONTEXT),
              createColorInfo(
                  Messages.SystemColorsComposite_grayText,
                  Messages.SystemColorsComposite_grayTextDescription,
                  COLOR_GRAYTEXT),
              createColorInfo(
                  Messages.SystemColorsComposite_highlight,
                  Messages.SystemColorsComposite_highlightDescription,
                  COLOR_HIGHLIGHT),
              createColorInfo(
                  Messages.SystemColorsComposite_highlightText,
                  Messages.SystemColorsComposite_highlightTextDescription,
                  COLOR_HIGHLIGHTTEXT),
              createColorInfo(
                  Messages.SystemColorsComposite_inactiveBorder,
                  Messages.SystemColorsComposite_inactiveBorderDescription,
                  COLOR_INACTIVEBORDER),
              createColorInfo(
                  Messages.SystemColorsComposite_inactiveCaption,
                  Messages.SystemColorsComposite_inactiveCaptionDescription,
                  COLOR_INACTIVECAPTION),
              createColorInfo(
                  Messages.SystemColorsComposite_inactiveText,
                  Messages.SystemColorsComposite_inactiveTextDescription,
                  COLOR_INACTIVECAPTIONTEXT),
              createColorInfo(
                  Messages.SystemColorsComposite_infoBackground,
                  Messages.SystemColorsComposite_infoBackgroundDescription,
                  COLOR_INFOBK),
              createColorInfo(
                  Messages.SystemColorsComposite_infoText,
                  Messages.SystemColorsComposite_infoTextDescription,
                  COLOR_INFOTEXT),
              createColorInfo(
                  Messages.SystemColorsComposite_menu,
                  Messages.SystemColorsComposite_menuDescription,
                  COLOR_MENU),
              createColorInfo(
                  Messages.SystemColorsComposite_menuText,
                  Messages.SystemColorsComposite_menuTextDescription,
                  COLOR_MENUTEXT),
              createColorInfo(
                  Messages.SystemColorsComposite_scrollbar,
                  Messages.SystemColorsComposite_scrollbarDescription,
                  COLOR_SCROLLBAR),
              createColorInfo(
                  Messages.SystemColorsComposite_shadowDark,
                  Messages.SystemColorsComposite_shadowDarkDescription,
                  COLOR_3DDKSHADOW),
              createColorInfo(
                  Messages.SystemColorsComposite_face3d,
                  Messages.SystemColorsComposite_face3dDescription,
                  COLOR_3DFACE),
              createColorInfo(
                  Messages.SystemColorsComposite_shadowHighlight,
                  Messages.SystemColorsComposite_shadowHighlightDescription,
                  COLOR_3DHIGHLIGHT),
              createColorInfo(
                  Messages.SystemColorsComposite_shadowLight,
                  Messages.SystemColorsComposite_shadowLightDescription,
                  COLOR_3DHILIGHT),
              createColorInfo(
                  Messages.SystemColorsComposite_shadow,
                  Messages.SystemColorsComposite_shadowDescription,
                  COLOR_3DSHADOW),
              createColorInfo(
                  Messages.SystemColorsComposite_window,
                  Messages.SystemColorsComposite_windowDescription,
                  COLOR_WINDOW),
              createColorInfo(
                  Messages.SystemColorsComposite_windowFrame,
                  Messages.SystemColorsComposite_windowFrameDescription,
                  COLOR_WINDOWFRAME),
              createColorInfo(
                  Messages.SystemColorsComposite_windowText,
                  Messages.SystemColorsComposite_windowTextDescription,
                  COLOR_WINDOWTEXT),};
    }
    return SYSTEM_COLORS;
  }

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

  /**
   * @return the names of all colors on this page.
   */
  private static String[] COLOR_NAMES = null;

  public static String[] getColorNames() {
    if (COLOR_NAMES == null) {
      ColorInfo[] systemColors = getSystemColors();
      COLOR_NAMES = new String[systemColors.length];
      int index = 0;
      for (ColorInfo colorInfo : systemColors) {
        COLOR_NAMES[index++] = colorInfo.getName();
      }
    }
    return COLOR_NAMES;
  }

  /**
   * @return the {@link RGB} of given named color, may be <code>null</code>.
   */
  public static RGB getRGB(String name) {
    for (ColorInfo colorInfo : getSystemColors()) {
      if (colorInfo.getName().equals(name)) {
        return colorInfo.getRGB();
      }
    }
    return null;
  }
}
