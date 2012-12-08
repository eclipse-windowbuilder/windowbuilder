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

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.utils.ui.dialogs.color.AbstractColorDialog;
import org.eclipse.wb.internal.core.utils.ui.dialogs.color.pages.NamedColorsComposite;
import org.eclipse.wb.internal.core.utils.ui.dialogs.color.pages.WebSafeColorsComposite;
import org.eclipse.wb.internal.css.Messages;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;

import java.util.List;

/**
 * Dialog for color choosing.
 * 
 * @author scheglov_ke
 * @coverage CSS.ui
 */
public final class ColorDialog extends AbstractColorDialog {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ColorDialog(Shell parentShell) {
    super(parentShell);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Pages
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void addPages(Composite parent) {
    addPage(Messages.ColorDialog_namedPage, new NamedColorsComposite(parent, SWT.NONE, this));
    addPage(Messages.ColorDialog_webPage, new WebSafeColorsComposite(parent, SWT.NONE, this));
    if (SystemUtils.IS_OS_WINDOWS) {
      addPage(Messages.ColorDialog_systemPage, new SystemColorsComposite(parent, SWT.NONE, this));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private static String COLOR_NAMES[];

  /**
   * @return the names of all colors on this page.
   */
  public static String[] getColorNames() {
    if (COLOR_NAMES == null) {
      List<String> colorNames = Lists.newArrayList();
      for (String color : NamedColorsComposite.getColorNames()) {
        colorNames.add(color);
      }
      if (SystemUtils.IS_OS_WINDOWS) {
        for (String color : SystemColorsComposite.getColorNames()) {
          colorNames.add(color);
        }
      }
      COLOR_NAMES = colorNames.toArray(new String[colorNames.size()]);
    }
    return COLOR_NAMES;
  }

  /**
   * @return the {@link RGB} of given named color, may be <code>null</code>.
   */
  public static RGB getRGB(String name) {
    RGB rgb;
    rgb = NamedColorsComposite.getRGB(name);
    if (rgb == null && SystemUtils.IS_OS_WINDOWS) {
      rgb = SystemColorsComposite.getRGB(name);
    }
    if (rgb == null && name.length() == 7) {
      name = StringUtils.removeStart(name, "#");
      int r = Integer.parseInt(name.substring(0, 2), 16);
      int g = Integer.parseInt(name.substring(2, 4), 16);
      int b = Integer.parseInt(name.substring(4, 6), 16);
      rgb = new RGB(r, g, b);
    }
    if (rgb == null && name.length() == 4) {
      name = StringUtils.removeStart(name, "#");
      int r0 = Integer.parseInt(name.substring(0, 1), 16);
      int g0 = Integer.parseInt(name.substring(1, 2), 16);
      int b0 = Integer.parseInt(name.substring(2, 3), 16);
      int r = (r0 << 4) + r0;
      int g = (g0 << 4) + g0;
      int b = (b0 << 4) + b0;
      rgb = new RGB(r, g, b);
    }
    return rgb;
  }
}
