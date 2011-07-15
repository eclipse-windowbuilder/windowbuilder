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
package org.eclipse.wb.internal.css.semantics;

import org.eclipse.wb.internal.css.model.CssDeclarationNode;
import org.eclipse.wb.internal.css.model.CssRuleNode;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Composite property for border.
 * 
 * @author scheglov_ke
 * @coverage CSS.semantics
 */
public class BorderProperty extends AbstractSemanticsComposite {
  private static final int ALL_SIDES = Integer.MAX_VALUE;
  public static final String[] SPECIAL_WIDTHS = {"thin", "medium", "thick"};
  public static final String[] STYLES = {
      "",
      "node",
      "hidden",
      "dotted",
      "dashed",
      "solid",
      "double",
      "groove",
      "ridge",
      "inset",
      "outset"};
  private final LengthSidedProperty m_width = new LengthSidedProperty(this, "border", "width");
  private final SimpleSidedProperty m_style = new SimpleSidedProperty(this, "border", "style");
  private final SimpleSidedProperty m_color = new SimpleSidedProperty(this, "border", "color");

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BorderProperty(AbstractSemanticsComposite composite) {
    super(composite);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Rule
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void parse(CssRuleNode rule) {
    m_width.clear();
    m_style.clear();
    m_color.clear();
    for (CssDeclarationNode declaration : rule.getDeclarations()) {
      String property = declaration.getProperty().getValue();
      String value = declaration.getValue().getValue();
      // try to set single value
      {
        m_width.set(property, value);
        m_style.set(property, value);
        m_color.set(property, value);
      }
      // try to find shorthand
      {
        // determine side
        int side = -1;
        if ("border".equals(property)) {
          side = ALL_SIDES;
        } else if (property.startsWith("border-")) {
          side =
              ArrayUtils.indexOf(
                  AbstractSidedProperty.SIDE_NAMES,
                  property.substring("border-".length()));
        }
        // if side found, parse value
        if (side != -1) {
          clear(side);
          String[] parts = StringUtils.split(value);
          label_parts : for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            // style
            if (ArrayUtils.indexOf(STYLES, part) != -1) {
              setSideValue(m_style, side, part);
              continue;
            }
            // width
            {
              // ends with unit
              for (int j = 0; j < LengthValue.UNIT_NAMES.length; j++) {
                String unit = LengthValue.UNIT_NAMES[j];
                if (part.endsWith(unit)) {
                  setSideValue(m_width, side, part);
                  continue label_parts;
                }
              }
              // named width
              if (ArrayUtils.contains(SPECIAL_WIDTHS, part)) {
                setSideValue(m_width, side, part);
                continue;
              }
            }
            // if part is not style and not width, we consider it as color
            setSideValue(m_color, side, part);
          }
        }
      }
    }
  }

  @Override
  public void update(CssRuleNode rule) {
    removeDeclarations(rule, new ICssDeclarationPredicate() {
      public boolean evaluate(CssDeclarationNode declaration) {
        String property = declaration.getProperty().getValue();
        return "border".equals(property) || property.startsWith("border-");
      }
    });
    // check, may all border parts have same value, so we can generate single "border: " declaration 
    if (m_width.isSingleValue() && m_style.isSingleValue() && m_color.isSingleValue()) {
      String value = getShorthandValueForUpdate(AbstractSidedProperty.SIDE_TOP);
      if (value.length() != 0) {
        addDeclaration(rule, "border", null, value);
      }
      return;
    }
    // no, different sides have different values, so generate separate "border-...:" for separate sides
    for (int side = 0; side < 4; side++) {
      String value = getShorthandValueForUpdate(side);
      if (value.length() != 0) {
        addDeclaration(rule, "border", AbstractSidedProperty.SIDE_NAMES[side], value);
      }
    }
  }

  /**
   * @return shorthand value for given side (for using as border-top/right/bottom/left).
   */
  private String getShorthandValueForUpdate(int side) {
    String width = m_width.getValue(side).get();
    String style = m_style.getValue(side).get();
    String color = m_color.getValue(side).get();
    //
    String value = "";
    if (width != null) {
      value += " " + width;
    }
    if (style != null) {
      value += " " + style;
    }
    if (color != null) {
      value += " " + color;
    }
    return value.trim();
  }

  /**
   * Clears given side of border properties.
   */
  private void clear(int sidePattern) {
    processSides(sidePattern, new ISideHandler() {
      public void handle(int side) {
        m_width.getValue(side).set(null);
        m_style.getValue(side).set(null);
        m_color.getValue(side).set(null);
      }
    });
  }

  /**
   * Sets value for given side of sided property.
   */
  private void setSideValue(final AbstractSidedProperty sidedProperty,
      int sidePattern,
      final String value) {
    processSides(sidePattern, new ISideHandler() {
      public void handle(int side) {
        sidedProperty.getValue(side).set(value);
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Sides access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Handler for {@link BorderProperty#processSides(int, ISideHandler)}.
   */
  private interface ISideHandler {
    void handle(int side);
  }

  /**
   * Invokes given handle for all sides (if given side pattern is ALL_SIDES) or for given side.
   */
  private void processSides(int sidePattern, ISideHandler handler) {
    if (sidePattern == ALL_SIDES) {
      for (int side = 0; side < 4; side++) {
        handler.handle(side);
      }
    } else {
      handler.handle(sidePattern);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public LengthSidedProperty getWidth() {
    return m_width;
  }

  public SimpleSidedProperty getStyle() {
    return m_style;
  }

  public SimpleSidedProperty getColor() {
    return m_color;
  }
}
