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
package org.eclipse.wb.internal.core.utils.ui.dialogs.color.pages;

import com.google.common.collect.ImmutableList;

import org.eclipse.wb.internal.core.utils.Messages;
import org.eclipse.wb.internal.core.utils.ui.dialogs.color.AbstractColorDialog;
import org.eclipse.wb.internal.core.utils.ui.dialogs.color.AbstractColorsGridComposite;
import org.eclipse.wb.internal.core.utils.ui.dialogs.color.ColorInfo;
import org.eclipse.wb.internal.core.utils.ui.dialogs.color.ColorInfoComparator;
import org.eclipse.wb.internal.core.utils.ui.dialogs.color.ColorsGridComposite;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;

/**
 * Composite for selecting of of the 216 web safe colors.
 *
 * @author scheglov_ke
 * @coverage core.ui
 */
public final class WebSafeColorsComposite extends AbstractColorsGridComposite {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public WebSafeColorsComposite(Composite parent, int style, AbstractColorDialog colorPickerDialog) {
    super(parent, style, colorPickerDialog);
    createSortGroup(this, ImmutableList.of(
        Messages.WebSafeColorsComposite_sortTone,
        Messages.WebSafeColorsComposite_sortHue,
        Messages.WebSafeColorsComposite_sortSaturation,
        Messages.WebSafeColorsComposite_sortLightness), ImmutableList.of(
        ColorInfoComparator.TONE,
        ColorInfoComparator.HUE,
        ColorInfoComparator.SATURATION,
        ColorInfoComparator.LIGHTNESS));
    {
      ColorsGridComposite colorsGrid = createColorsGroup(this, null, WEB_COLORS);
      colorsGrid.setCellWidth(33);
      colorsGrid.setCellHeight(18);
      colorsGrid.setColumns(12);
    }
    setComparator(ColorInfoComparator.TONE);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Colors
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final ColorInfo[] WEB_COLORS = new ColorInfo[]{
      new ColorInfo(null, new RGB(0, 0, 0)),
      new ColorInfo(null, new RGB(51, 0, 0)),
      new ColorInfo(null, new RGB(102, 0, 0)),
      new ColorInfo(null, new RGB(153, 0, 0)),
      new ColorInfo(null, new RGB(204, 0, 0)),
      new ColorInfo(null, new RGB(255, 0, 0)),
      new ColorInfo(null, new RGB(0, 0, 51)),
      new ColorInfo(null, new RGB(51, 0, 51)),
      new ColorInfo(null, new RGB(102, 0, 51)),
      new ColorInfo(null, new RGB(153, 0, 51)),
      new ColorInfo(null, new RGB(204, 0, 51)),
      new ColorInfo(null, new RGB(255, 0, 51)),
      new ColorInfo(null, new RGB(0, 0, 102)),
      new ColorInfo(null, new RGB(51, 0, 102)),
      new ColorInfo(null, new RGB(102, 0, 102)),
      new ColorInfo(null, new RGB(153, 0, 102)),
      new ColorInfo(null, new RGB(204, 0, 102)),
      new ColorInfo(null, new RGB(255, 0, 102)),
      new ColorInfo(null, new RGB(0, 0, 153)),
      new ColorInfo(null, new RGB(51, 0, 153)),
      new ColorInfo(null, new RGB(102, 0, 153)),
      new ColorInfo(null, new RGB(153, 0, 153)),
      new ColorInfo(null, new RGB(204, 0, 153)),
      new ColorInfo(null, new RGB(255, 0, 153)),
      new ColorInfo(null, new RGB(0, 0, 204)),
      new ColorInfo(null, new RGB(51, 0, 204)),
      new ColorInfo(null, new RGB(102, 0, 204)),
      new ColorInfo(null, new RGB(153, 0, 204)),
      new ColorInfo(null, new RGB(204, 0, 204)),
      new ColorInfo(null, new RGB(255, 0, 204)),
      new ColorInfo(null, new RGB(0, 0, 255)),
      new ColorInfo(null, new RGB(51, 0, 255)),
      new ColorInfo(null, new RGB(102, 0, 255)),
      new ColorInfo(null, new RGB(153, 0, 255)),
      new ColorInfo(null, new RGB(204, 0, 255)),
      new ColorInfo(null, new RGB(255, 0, 255)),
      new ColorInfo(null, new RGB(0, 51, 0)),
      new ColorInfo(null, new RGB(51, 51, 0)),
      new ColorInfo(null, new RGB(102, 51, 0)),
      new ColorInfo(null, new RGB(153, 51, 0)),
      new ColorInfo(null, new RGB(204, 51, 0)),
      new ColorInfo(null, new RGB(255, 51, 0)),
      new ColorInfo(null, new RGB(0, 51, 51)),
      new ColorInfo(null, new RGB(51, 51, 51)),
      new ColorInfo(null, new RGB(102, 51, 51)),
      new ColorInfo(null, new RGB(153, 51, 51)),
      new ColorInfo(null, new RGB(204, 51, 51)),
      new ColorInfo(null, new RGB(255, 51, 51)),
      new ColorInfo(null, new RGB(0, 51, 102)),
      new ColorInfo(null, new RGB(51, 51, 102)),
      new ColorInfo(null, new RGB(102, 51, 102)),
      new ColorInfo(null, new RGB(153, 51, 102)),
      new ColorInfo(null, new RGB(204, 51, 102)),
      new ColorInfo(null, new RGB(255, 51, 102)),
      new ColorInfo(null, new RGB(0, 51, 153)),
      new ColorInfo(null, new RGB(51, 51, 153)),
      new ColorInfo(null, new RGB(102, 51, 153)),
      new ColorInfo(null, new RGB(153, 51, 153)),
      new ColorInfo(null, new RGB(204, 51, 153)),
      new ColorInfo(null, new RGB(255, 51, 153)),
      new ColorInfo(null, new RGB(0, 51, 204)),
      new ColorInfo(null, new RGB(51, 51, 204)),
      new ColorInfo(null, new RGB(102, 51, 204)),
      new ColorInfo(null, new RGB(153, 51, 204)),
      new ColorInfo(null, new RGB(204, 51, 204)),
      new ColorInfo(null, new RGB(255, 51, 204)),
      new ColorInfo(null, new RGB(0, 51, 255)),
      new ColorInfo(null, new RGB(51, 51, 255)),
      new ColorInfo(null, new RGB(102, 51, 255)),
      new ColorInfo(null, new RGB(153, 51, 255)),
      new ColorInfo(null, new RGB(204, 51, 255)),
      new ColorInfo(null, new RGB(255, 51, 255)),
      new ColorInfo(null, new RGB(0, 102, 0)),
      new ColorInfo(null, new RGB(51, 102, 0)),
      new ColorInfo(null, new RGB(102, 102, 0)),
      new ColorInfo(null, new RGB(153, 102, 0)),
      new ColorInfo(null, new RGB(204, 102, 0)),
      new ColorInfo(null, new RGB(255, 102, 0)),
      new ColorInfo(null, new RGB(0, 102, 51)),
      new ColorInfo(null, new RGB(51, 102, 51)),
      new ColorInfo(null, new RGB(102, 102, 51)),
      new ColorInfo(null, new RGB(153, 102, 51)),
      new ColorInfo(null, new RGB(204, 102, 51)),
      new ColorInfo(null, new RGB(255, 102, 51)),
      new ColorInfo(null, new RGB(0, 102, 102)),
      new ColorInfo(null, new RGB(51, 102, 102)),
      new ColorInfo(null, new RGB(102, 102, 102)),
      new ColorInfo(null, new RGB(153, 102, 102)),
      new ColorInfo(null, new RGB(204, 102, 102)),
      new ColorInfo(null, new RGB(255, 102, 102)),
      new ColorInfo(null, new RGB(0, 102, 153)),
      new ColorInfo(null, new RGB(51, 102, 153)),
      new ColorInfo(null, new RGB(102, 102, 153)),
      new ColorInfo(null, new RGB(153, 102, 153)),
      new ColorInfo(null, new RGB(204, 102, 153)),
      new ColorInfo(null, new RGB(255, 102, 153)),
      new ColorInfo(null, new RGB(0, 102, 204)),
      new ColorInfo(null, new RGB(51, 102, 204)),
      new ColorInfo(null, new RGB(102, 102, 204)),
      new ColorInfo(null, new RGB(153, 102, 204)),
      new ColorInfo(null, new RGB(204, 102, 204)),
      new ColorInfo(null, new RGB(255, 102, 204)),
      new ColorInfo(null, new RGB(0, 102, 255)),
      new ColorInfo(null, new RGB(51, 102, 255)),
      new ColorInfo(null, new RGB(102, 102, 255)),
      new ColorInfo(null, new RGB(153, 102, 255)),
      new ColorInfo(null, new RGB(204, 102, 255)),
      new ColorInfo(null, new RGB(255, 102, 255)),
      new ColorInfo(null, new RGB(0, 153, 0)),
      new ColorInfo(null, new RGB(51, 153, 0)),
      new ColorInfo(null, new RGB(102, 153, 0)),
      new ColorInfo(null, new RGB(153, 153, 0)),
      new ColorInfo(null, new RGB(204, 153, 0)),
      new ColorInfo(null, new RGB(255, 153, 0)),
      new ColorInfo(null, new RGB(0, 153, 51)),
      new ColorInfo(null, new RGB(51, 153, 51)),
      new ColorInfo(null, new RGB(102, 153, 51)),
      new ColorInfo(null, new RGB(153, 153, 51)),
      new ColorInfo(null, new RGB(204, 153, 51)),
      new ColorInfo(null, new RGB(255, 153, 51)),
      new ColorInfo(null, new RGB(0, 153, 102)),
      new ColorInfo(null, new RGB(51, 153, 102)),
      new ColorInfo(null, new RGB(102, 153, 102)),
      new ColorInfo(null, new RGB(153, 153, 102)),
      new ColorInfo(null, new RGB(204, 153, 102)),
      new ColorInfo(null, new RGB(255, 153, 102)),
      new ColorInfo(null, new RGB(0, 153, 153)),
      new ColorInfo(null, new RGB(51, 153, 153)),
      new ColorInfo(null, new RGB(102, 153, 153)),
      new ColorInfo(null, new RGB(153, 153, 153)),
      new ColorInfo(null, new RGB(204, 153, 153)),
      new ColorInfo(null, new RGB(255, 153, 153)),
      new ColorInfo(null, new RGB(0, 153, 204)),
      new ColorInfo(null, new RGB(51, 153, 204)),
      new ColorInfo(null, new RGB(102, 153, 204)),
      new ColorInfo(null, new RGB(153, 153, 204)),
      new ColorInfo(null, new RGB(204, 153, 204)),
      new ColorInfo(null, new RGB(255, 153, 204)),
      new ColorInfo(null, new RGB(0, 153, 255)),
      new ColorInfo(null, new RGB(51, 153, 255)),
      new ColorInfo(null, new RGB(102, 153, 255)),
      new ColorInfo(null, new RGB(153, 153, 255)),
      new ColorInfo(null, new RGB(204, 153, 255)),
      new ColorInfo(null, new RGB(255, 153, 255)),
      new ColorInfo(null, new RGB(0, 204, 0)),
      new ColorInfo(null, new RGB(51, 204, 0)),
      new ColorInfo(null, new RGB(102, 204, 0)),
      new ColorInfo(null, new RGB(153, 204, 0)),
      new ColorInfo(null, new RGB(204, 204, 0)),
      new ColorInfo(null, new RGB(255, 204, 0)),
      new ColorInfo(null, new RGB(0, 204, 51)),
      new ColorInfo(null, new RGB(51, 204, 51)),
      new ColorInfo(null, new RGB(102, 204, 51)),
      new ColorInfo(null, new RGB(153, 204, 51)),
      new ColorInfo(null, new RGB(204, 204, 51)),
      new ColorInfo(null, new RGB(255, 204, 51)),
      new ColorInfo(null, new RGB(0, 204, 102)),
      new ColorInfo(null, new RGB(51, 204, 102)),
      new ColorInfo(null, new RGB(102, 204, 102)),
      new ColorInfo(null, new RGB(153, 204, 102)),
      new ColorInfo(null, new RGB(204, 204, 102)),
      new ColorInfo(null, new RGB(255, 204, 102)),
      new ColorInfo(null, new RGB(0, 204, 153)),
      new ColorInfo(null, new RGB(51, 204, 153)),
      new ColorInfo(null, new RGB(102, 204, 153)),
      new ColorInfo(null, new RGB(153, 204, 153)),
      new ColorInfo(null, new RGB(204, 204, 153)),
      new ColorInfo(null, new RGB(255, 204, 153)),
      new ColorInfo(null, new RGB(0, 204, 204)),
      new ColorInfo(null, new RGB(51, 204, 204)),
      new ColorInfo(null, new RGB(102, 204, 204)),
      new ColorInfo(null, new RGB(153, 204, 204)),
      new ColorInfo(null, new RGB(204, 204, 204)),
      new ColorInfo(null, new RGB(255, 204, 204)),
      new ColorInfo(null, new RGB(0, 204, 255)),
      new ColorInfo(null, new RGB(51, 204, 255)),
      new ColorInfo(null, new RGB(102, 204, 255)),
      new ColorInfo(null, new RGB(153, 204, 255)),
      new ColorInfo(null, new RGB(204, 204, 255)),
      new ColorInfo(null, new RGB(255, 204, 255)),
      new ColorInfo(null, new RGB(0, 255, 0)),
      new ColorInfo(null, new RGB(51, 255, 0)),
      new ColorInfo(null, new RGB(102, 255, 0)),
      new ColorInfo(null, new RGB(153, 255, 0)),
      new ColorInfo(null, new RGB(204, 255, 0)),
      new ColorInfo(null, new RGB(255, 255, 0)),
      new ColorInfo(null, new RGB(0, 255, 51)),
      new ColorInfo(null, new RGB(51, 255, 51)),
      new ColorInfo(null, new RGB(102, 255, 51)),
      new ColorInfo(null, new RGB(153, 255, 51)),
      new ColorInfo(null, new RGB(204, 255, 51)),
      new ColorInfo(null, new RGB(255, 255, 51)),
      new ColorInfo(null, new RGB(0, 255, 102)),
      new ColorInfo(null, new RGB(51, 255, 102)),
      new ColorInfo(null, new RGB(102, 255, 102)),
      new ColorInfo(null, new RGB(153, 255, 102)),
      new ColorInfo(null, new RGB(204, 255, 102)),
      new ColorInfo(null, new RGB(255, 255, 102)),
      new ColorInfo(null, new RGB(0, 255, 153)),
      new ColorInfo(null, new RGB(51, 255, 153)),
      new ColorInfo(null, new RGB(102, 255, 153)),
      new ColorInfo(null, new RGB(153, 255, 153)),
      new ColorInfo(null, new RGB(204, 255, 153)),
      new ColorInfo(null, new RGB(255, 255, 153)),
      new ColorInfo(null, new RGB(0, 255, 204)),
      new ColorInfo(null, new RGB(51, 255, 204)),
      new ColorInfo(null, new RGB(102, 255, 204)),
      new ColorInfo(null, new RGB(153, 255, 204)),
      new ColorInfo(null, new RGB(204, 255, 204)),
      new ColorInfo(null, new RGB(255, 255, 204)),
      new ColorInfo(null, new RGB(0, 255, 255)),
      new ColorInfo(null, new RGB(51, 255, 255)),
      new ColorInfo(null, new RGB(102, 255, 255)),
      new ColorInfo(null, new RGB(153, 255, 255)),
      new ColorInfo(null, new RGB(204, 255, 255)),
      new ColorInfo(null, new RGB(255, 255, 255)),};
}
