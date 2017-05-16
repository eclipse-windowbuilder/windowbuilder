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

import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;

/**
 * Composite for selecting named color (HTML or SVG).
 *
 * @author scheglov_ke
 * @coverage core.ui
 */
public final class NamedColorsComposite extends AbstractColorsGridComposite {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public NamedColorsComposite(Composite parent, int style, AbstractColorDialog colorDialog) {
    super(parent, style, colorDialog);
    createSortGroup(this, ImmutableList.of(
        Messages.NamedColorsComposite_sortTone,
        Messages.NamedColorsComposite_sortHue,
        Messages.NamedColorsComposite_sortSaturation,
        Messages.NamedColorsComposite_sortLightness,
        Messages.NamedColorsComposite_sortName), ImmutableList.of(
        ColorInfoComparator.TONE,
        ColorInfoComparator.HUE,
        ColorInfoComparator.SATURATION,
        ColorInfoComparator.LIGHTNESS,
        ColorInfoComparator.NAME));
    createColorsGroup(this, Messages.NamedColorsComposite_htmlGroup, HTML_COLORS);
    createColorsGroup(this, Messages.NamedColorsComposite_svgGroup, SVG_COLORS);
    setComparator(ColorInfoComparator.TONE);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Colors
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final ColorInfo[] HTML_COLORS = new ColorInfo[]{
      new ColorInfo("Black", new RGB(0, 0, 0)),
      new ColorInfo("Silver", new RGB(192, 192, 192)),
      new ColorInfo("Gray", new RGB(128, 128, 128)),
      new ColorInfo("White", new RGB(255, 255, 255)),
      new ColorInfo("Maroon", new RGB(128, 0, 0)),
      new ColorInfo("Red", new RGB(255, 0, 0)),
      new ColorInfo("Purple", new RGB(128, 0, 128)),
      new ColorInfo("Fuchsia", new RGB(255, 0, 255)),
      new ColorInfo("Green", new RGB(0, 128, 0)),
      new ColorInfo("Lime", new RGB(0, 255, 0)),
      new ColorInfo("Olive", new RGB(128, 128, 0)),
      new ColorInfo("Yellow", new RGB(255, 255, 0)),
      new ColorInfo("Navy", new RGB(0, 0, 128)),
      new ColorInfo("Blue", new RGB(0, 0, 255)),
      new ColorInfo("Teal", new RGB(0, 128, 128)),
      new ColorInfo("Aqua", new RGB(0, 255, 255)),};
  private static final ColorInfo[] SVG_COLORS = new ColorInfo[]{
      new ColorInfo("indianred", new RGB(205, 92, 92)),
      new ColorInfo("darksalmon", new RGB(233, 150, 122)),
      new ColorInfo("lightcoral", new RGB(240, 128, 128)),
      new ColorInfo("salmon", new RGB(250, 128, 114)),
      new ColorInfo("orangered", new RGB(255, 69, 0)),
      new ColorInfo("red", new RGB(255, 0, 0)),
      new ColorInfo("crimson", new RGB(220, 20, 60)),
      new ColorInfo("firebrick", new RGB(178, 34, 34)),
      new ColorInfo("mediumvioletred", new RGB(199, 21, 133)),
      new ColorInfo("pink", new RGB(255, 192, 203)),
      new ColorInfo("lightpink", new RGB(255, 182, 193)),
      new ColorInfo("hotpink", new RGB(255, 105, 180)),
      new ColorInfo("deeppink", new RGB(255, 20, 147)),
      new ColorInfo("palevioletred", new RGB(219, 112, 147)),
      new ColorInfo("darkkhaki", new RGB(189, 183, 107)),
      new ColorInfo("khaki", new RGB(240, 230, 140)),
      new ColorInfo("palegoldenrod", new RGB(238, 232, 170)),
      new ColorInfo("lightgoldenrodyellow", new RGB(250, 250, 210)),
      new ColorInfo("lightyellow", new RGB(255, 255, 224)),
      new ColorInfo("lemonchiffon", new RGB(255, 250, 205)),
      new ColorInfo("yellow", new RGB(255, 255, 0)),
      new ColorInfo("gold", new RGB(255, 215, 0)),
      new ColorInfo("papayawhip", new RGB(255, 239, 213)),
      new ColorInfo("moccasin", new RGB(255, 228, 181)),
      new ColorInfo("peachpuff", new RGB(255, 218, 185)),
      new ColorInfo("cyan", new RGB(0, 255, 255)),
      new ColorInfo("aqua", new RGB(0, 255, 255)),
      new ColorInfo("aquamarine", new RGB(127, 255, 212)),
      new ColorInfo("turquoise", new RGB(64, 224, 208)),
      new ColorInfo("mediumturquoise", new RGB(72, 209, 204)),
      new ColorInfo("darkturquoise", new RGB(0, 206, 209)),
      new ColorInfo("cadetblue", new RGB(95, 158, 160)),
      new ColorInfo("lightcyan", new RGB(224, 255, 255)),
      new ColorInfo("paleturquoise", new RGB(175, 238, 238)),
      new ColorInfo("powderblue", new RGB(176, 224, 230)),
      new ColorInfo("lightsteelblue", new RGB(176, 196, 222)),
      new ColorInfo("steelblue", new RGB(70, 130, 180)),
      new ColorInfo("lightblue", new RGB(173, 216, 230)),
      new ColorInfo("skyblue", new RGB(135, 206, 235)),
      new ColorInfo("lightskyblue", new RGB(135, 206, 250)),
      new ColorInfo("deepskyblue", new RGB(0, 191, 255)),
      new ColorInfo("cornflowerblue", new RGB(100, 149, 237)),
      new ColorInfo("royalblue", new RGB(65, 105, 225)),
      new ColorInfo("mediumslateblue", new RGB(123, 104, 238)),
      new ColorInfo("dodgerblue", new RGB(30, 144, 255)),
      new ColorInfo("blue", new RGB(0, 0, 255)),
      new ColorInfo("mediumblue", new RGB(0, 0, 205)),
      new ColorInfo("darkblue", new RGB(0, 0, 139)),
      new ColorInfo("navy", new RGB(0, 0, 128)),
      new ColorInfo("midnightblue", new RGB(25, 25, 112)),
      new ColorInfo("lightsalmon", new RGB(255, 160, 122)),
      new ColorInfo("orange", new RGB(255, 165, 0)),
      new ColorInfo("darkorange", new RGB(255, 140, 0)),
      new ColorInfo("coral", new RGB(255, 127, 80)),
      new ColorInfo("tomato", new RGB(255, 99, 71)),
      new ColorInfo("mediumspringgreen", new RGB(0, 250, 154)),
      new ColorInfo("springgreen", new RGB(0, 255, 127)),
      new ColorInfo("palegreen", new RGB(152, 251, 152)),
      new ColorInfo("greenyellow", new RGB(173, 255, 47)),
      new ColorInfo("chartreuse", new RGB(127, 255, 0)),
      new ColorInfo("lawngreen", new RGB(124, 252, 0)),
      new ColorInfo("lime", new RGB(0, 255, 0)),
      new ColorInfo("lightgreen", new RGB(144, 238, 144)),
      new ColorInfo("yellowgreen", new RGB(154, 205, 50)),
      new ColorInfo("limegreen", new RGB(50, 205, 50)),
      new ColorInfo("mediumseagreen", new RGB(60, 179, 113)),
      new ColorInfo("darkseagreen", new RGB(143, 188, 143)),
      new ColorInfo("forestgreen", new RGB(34, 139, 34)),
      new ColorInfo("seagreen", new RGB(46, 139, 87)),
      new ColorInfo("green", new RGB(0, 128, 0)),
      new ColorInfo("olivedrab", new RGB(107, 142, 35)),
      new ColorInfo("olive", new RGB(128, 128, 0)),
      new ColorInfo("darkolivegreen", new RGB(85, 107, 47)),
      new ColorInfo("darkgreen", new RGB(0, 100, 0)),
      new ColorInfo("mediumaquamarine", new RGB(102, 205, 170)),
      new ColorInfo("lightseagreen", new RGB(32, 178, 170)),
      new ColorInfo("darkcyan", new RGB(0, 139, 139)),
      new ColorInfo("teal", new RGB(0, 128, 128)),
      new ColorInfo("lavender", new RGB(230, 230, 250)),
      new ColorInfo("thistle", new RGB(216, 191, 216)),
      new ColorInfo("plum", new RGB(221, 160, 221)),
      new ColorInfo("violet", new RGB(238, 130, 238)),
      new ColorInfo("fuchsia", new RGB(255, 0, 255)),
      new ColorInfo("magenta", new RGB(255, 0, 255)),
      new ColorInfo("orchid", new RGB(218, 112, 214)),
      new ColorInfo("mediumorchid", new RGB(186, 85, 211)),
      new ColorInfo("darkorchid", new RGB(153, 50, 204)),
      new ColorInfo("blueviolet", new RGB(138, 43, 226)),
      new ColorInfo("darkviolet", new RGB(148, 0, 211)),
      new ColorInfo("mediumpurple", new RGB(147, 112, 219)),
      new ColorInfo("slateblue", new RGB(106, 90, 205)),
      new ColorInfo("purple", new RGB(128, 0, 128)),
      new ColorInfo("darkmagenta", new RGB(139, 0, 139)),
      new ColorInfo("darkslateblue", new RGB(72, 61, 139)),
      new ColorInfo("indigo", new RGB(75, 0, 130)),
      new ColorInfo("honeydew", new RGB(240, 255, 240)),
      new ColorInfo("mintcream", new RGB(245, 255, 250)),
      new ColorInfo("azure", new RGB(240, 255, 255)),
      new ColorInfo("aliceblue", new RGB(240, 248, 255)),
      new ColorInfo("ghostwhite", new RGB(248, 248, 255)),
      new ColorInfo("whitesmoke", new RGB(245, 245, 245)),
      new ColorInfo("lavenderblush", new RGB(255, 240, 245)),
      new ColorInfo("mistyrose", new RGB(255, 228, 225)),
      new ColorInfo("antiquewhite", new RGB(250, 235, 215)),
      new ColorInfo("seashell", new RGB(255, 245, 238)),
      new ColorInfo("snow", new RGB(255, 250, 250)),
      new ColorInfo("white", new RGB(255, 255, 255)),
      new ColorInfo("beige", new RGB(245, 245, 220)),
      new ColorInfo("linen", new RGB(250, 240, 230)),
      new ColorInfo("oldlace", new RGB(253, 245, 230)),
      new ColorInfo("floralwhite", new RGB(255, 250, 240)),
      new ColorInfo("ivory", new RGB(255, 255, 240)),
      new ColorInfo("gainsboro", new RGB(220, 220, 220)),
      new ColorInfo("lightgray", new RGB(211, 211, 211)),
      new ColorInfo("lightgrey", new RGB(211, 211, 211)),
      new ColorInfo("silver", new RGB(192, 192, 192)),
      new ColorInfo("darkgray", new RGB(169, 169, 169)),
      new ColorInfo("darkgrey", new RGB(169, 169, 169)),
      new ColorInfo("gray", new RGB(128, 128, 128)),
      new ColorInfo("grey", new RGB(128, 128, 128)),
      new ColorInfo("dimgray", new RGB(105, 105, 105)),
      new ColorInfo("dimgrey", new RGB(105, 105, 105)),
      new ColorInfo("darkslategray", new RGB(47, 79, 79)),
      new ColorInfo("darkslategrey", new RGB(47, 79, 79)),
      new ColorInfo("lightslategray", new RGB(119, 136, 153)),
      new ColorInfo("lightslategrey", new RGB(119, 136, 153)),
      new ColorInfo("slategray", new RGB(112, 128, 144)),
      new ColorInfo("slategrey", new RGB(112, 128, 144)),
      new ColorInfo("cornsilk", new RGB(255, 248, 220)),
      new ColorInfo("blanchedalmond", new RGB(255, 235, 205)),
      new ColorInfo("bisque", new RGB(255, 228, 196)),
      new ColorInfo("navajowhite", new RGB(255, 222, 173)),
      new ColorInfo("wheat", new RGB(245, 222, 179)),
      new ColorInfo("sandybrown", new RGB(244, 164, 96)),
      new ColorInfo("goldenrod", new RGB(218, 165, 32)),
      new ColorInfo("darkgoldenrod", new RGB(184, 134, 11)),
      new ColorInfo("peru", new RGB(205, 133, 63)),
      new ColorInfo("chocolate", new RGB(210, 105, 30)),
      new ColorInfo("maroon", new RGB(128, 0, 0)),
      new ColorInfo("saddlebrown", new RGB(139, 69, 19)),
      new ColorInfo("brown", new RGB(165, 42, 42)),
      new ColorInfo("sienna", new RGB(160, 82, 45)),
      new ColorInfo("darkred", new RGB(139, 0, 0)),
      new ColorInfo("burlywood", new RGB(222, 184, 135)),
      new ColorInfo("tan", new RGB(210, 180, 140)),
      new ColorInfo("rosybrown", new RGB(188, 143, 143)),
      new ColorInfo("black", new RGB(0, 0, 0)),};
  private static final String COLOR_NAMES[] = new String[HTML_COLORS.length + SVG_COLORS.length];

  /**
   * @return the names of all colors on this page.
   */
  public static String[] getColorNames() {
    if (COLOR_NAMES[0] == null) {
      int index = 0;
      for (ColorInfo colorInfo : HTML_COLORS) {
        COLOR_NAMES[index++] = colorInfo.getName();
      }
      for (ColorInfo colorInfo : SVG_COLORS) {
        COLOR_NAMES[index++] = colorInfo.getName();
      }
    }
    return COLOR_NAMES;
  }

  /**
   * @return the {@link RGB} of given named color, may be <code>null</code>.
   */
  public static RGB getRGB(String name) {
    for (ColorInfo colorInfo : HTML_COLORS) {
      if (colorInfo.getName().equals(name)) {
        return colorInfo.getRGB();
      }
    }
    for (ColorInfo colorInfo : SVG_COLORS) {
      if (colorInfo.getName().equals(name)) {
        return colorInfo.getRGB();
      }
    }
    return null;
  }
}
