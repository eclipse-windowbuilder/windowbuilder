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

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.utils.CompactToStringStyle;
import org.eclipse.wb.internal.css.model.CssDeclarationNode;
import org.eclipse.wb.internal.css.model.CssRuleNode;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.List;

/**
 * Property that has four sides: top, right, bottom and left (yes, strange order, but it is defined
 * by CSS standard).
 * 
 * @author scheglov_ke
 * @coverage CSS.semantics
 */
public abstract class AbstractSidedProperty extends AbstractSemanticsComposite {
  public static final String[] SIDE_NAMES = new String[]{"top", "right", "bottom", "left"};
  public static final int SIDE_TOP = 0;
  public static final int SIDE_RIGHT = 1;
  public static final int SIDE_BOTTOM = 2;
  public static final int SIDE_LEFT = 3;
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final String m_prefix;
  private final String m_suffix;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractSidedProperty(AbstractSemanticsComposite composite, String prefix, String suffix) {
    super(composite);
    m_prefix = prefix;
    m_suffix = suffix;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Rule
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Clears side values, i.e. sets their values to <code>null</code>.
   */
  @Override
  public void clear() {
    for (int side = 0; side < 4; side++) {
      getValue(side).set(null);
    }
  }

  /**
   * Extracts interesting property values from given rule.
   */
  @Override
  public final void parse(CssRuleNode rule) {
    clear();
    parseDeclarations(rule);
  }

  @Override
  protected final void parseDeclaration(String property, String value) {
    set(property, value);
  }

  /**
   * Updates given rule so that it is reflecs state of this semantic object.
   */
  @Override
  public final void update(CssRuleNode rule) {
    // remove all existing declarations
    removeDeclarations(rule, new ICssDeclarationPredicate() {
      public boolean evaluate(CssDeclarationNode declaration) {
        String property = declaration.getProperty().getValue();
        return isShorthandProperty(property) || getSide(property) != -1;
      }
    });
    // add declarations
    {
      // try to add shorthand
      {
        String top = getValue(SIDE_TOP).get();
        String right = getValue(SIDE_RIGHT).get();
        String bottom = getValue(SIDE_BOTTOM).get();
        String left = getValue(SIDE_LEFT).get();
        if (top != null && right != null && bottom != null && left != null) {
          if (top.equals(right) && top.equals(bottom) && top.equals(left)) {
            addDeclaration(rule, m_prefix, m_suffix, top);
            return;
          }
          if (top.equals(bottom) && right.equals(left)) {
            addDeclaration(rule, m_prefix, m_suffix, top + " " + right);
            return;
          }
          if (right.equals(left)) {
            addDeclaration(rule, m_prefix, m_suffix, top + " " + right + " " + bottom);
            return;
          }
          addDeclaration(rule, m_prefix, m_suffix, top + " " + right + " " + bottom + " " + left);
          return;
        }
      }
      // no shorthand can be generated, use separate declarations
      for (int side = 0; side < SIDE_NAMES.length; side++) {
        String sideName = SIDE_NAMES[side];
        String value = getValue(side).get();
        if (value != null) {
          addDeclaration(rule, m_prefix + "-" + sideName, m_suffix, value);
        }
      }
    }
  }

  /**
   * Checks if this sided property has same value on all sides (may be even <code>null</code>).
   */
  public final boolean isSingleValue() {
    String top = getValue(SIDE_TOP).get();
    String right = getValue(SIDE_RIGHT).get();
    String bottom = getValue(SIDE_BOTTOM).get();
    String left = getValue(SIDE_LEFT).get();
    return top != null
        && top.equals(right)
        && top.equals(bottom)
        && top.equals(left)
        || top == null
        && right == null
        && bottom == null
        && left == null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    // @formatter:off
		return new ToStringBuilder(this, CompactToStringStyle.INSTANCE)
			.append("top", m_values[0])
			.append("right", m_values[1])
			.append("bottom", m_values[2])
			.append("left", m_values[3])
			.toString();
		// @formatter:on
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Values access
  //
  ////////////////////////////////////////////////////////////////////////////
  private final AbstractValue[] m_values = new AbstractValue[4];

  /**
   * This method should be used by subclasses for registering their side values.
   */
  protected final void setValue(int side, AbstractValue value) {
    m_values[side] = value;
  }

  /**
   * @return {@link AbstractValue} for given side.
   */
  public final AbstractValue getValue(int side) {
    return m_values[side];
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parsing
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets value for given property if it has correct name. We know, that all our properties have
   * names like "{prefix}-top-{suffix}", or "{prefix}-{suffix}". For example "border-left-color" or
   * "border-color". It is possible that suffix is <code>null</code>, for example "margin-left".
   * 
   * @return <code>true</code> if some side value was changed
   */
  public final boolean set(String property, String value) {
    // check for shorthand
    if (isShorthandProperty(property)) {
      set(value);
      return true;
    }
    // check for single property
    {
      int side = getSide(property);
      if (side != -1) {
        getValue(side).set(value);
        return true;
      }
    }
    // no, we don't know this property
    return false;
  }

  /**
   * Checks if given property name is shorthand property for this sided property.
   */
  private boolean isShorthandProperty(String property) {
    boolean shorthandWithSuffix = m_suffix != null && (m_prefix + "-" + m_suffix).equals(property);
    boolean shorthandWithoutSuffix = m_suffix == null && m_prefix.equals(property);
    boolean shorthand = shorthandWithSuffix || shorthandWithoutSuffix;
    return shorthand;
  }

  /**
   * Returns side if given property name denotes side for this sided property, or <code>-1</code>.
   */
  private int getSide(String property) {
    for (int side = 0; side < SIDE_NAMES.length; side++) {
      String sideName = SIDE_NAMES[side];
      boolean sideWithPrefix =
          m_suffix != null && (m_prefix + "-" + sideName + "-" + m_suffix).equals(property);
      boolean sideWithoutPrefix = m_suffix == null && (m_prefix + "-" + sideName).equals(property);
      if (sideWithPrefix || sideWithoutPrefix) {
        return side;
      }
    }
    return -1;
  }

  /**
   * Sets value for all four sides.
   */
  private void set(String value) {
    String[] parts = split(value);
    for (int i = 0; i < Math.min(parts.length, 4); i++) {
      String part = parts[i];
      set(i, part);
    }
  }

  private String[] split(String value) {
    List<String> strings = Lists.newArrayList();
    //return StringUtils.split(value);
    int inFunction = 0;
    int indexNotWhite = -1;
    for (int i = 0; i < value.length(); i++) {
      char c = value.charAt(i);
      if (c == '(') {
        inFunction++;
        continue;
      }
      if (c == ')') {
        inFunction--;
        continue;
      }
      if (Character.isWhitespace(c)) {
        if (indexNotWhite == -1) {
          continue;
        }
        if (inFunction != 0) {
          continue;
        }
        String s = value.substring(indexNotWhite, i);
        strings.add(s);
        indexNotWhite = -1;
        continue;
      }
      if (indexNotWhite == -1) {
        indexNotWhite = i;
      }
    }
    if (indexNotWhite != -1) {
      String s = value.substring(indexNotWhite, value.length());
      strings.add(s);
    }
    return strings.toArray(new String[strings.size()]);
  }

  /**
   * Sets value for one or more sides depending on index of value in shorthand property.
   */
  private void set(int index, String value) {
    if (index == 0) {
      getValue(SIDE_TOP).set(value);
      getValue(SIDE_RIGHT).set(value);
      getValue(SIDE_BOTTOM).set(value);
      getValue(SIDE_LEFT).set(value);
    }
    if (index == 1) {
      getValue(SIDE_RIGHT).set(value);
      getValue(SIDE_LEFT).set(value);
    }
    if (index == 2) {
      getValue(SIDE_BOTTOM).set(value);
    }
    if (index == 3) {
      getValue(SIDE_LEFT).set(value);
    }
  }
}
