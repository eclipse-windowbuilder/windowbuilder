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
package org.eclipse.wb.internal.core.model.property.editor.style.impl;

import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.BooleanPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.style.StylePropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.style.SubStylePropertyImpl;
import org.eclipse.wb.internal.core.model.property.editor.style.actions.BooleanStyleAction;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;

/**
 * This class represent "setUsingEqual" property implementation.
 *
 * Note: Java only.
 *
 * @author Jaime Wren
 * @author lobas_av
 * @coverage core.model.property.editor
 */
public final class BooleanUsingEqualsStylePropertyImpl extends SubStylePropertyImpl {
  private final String m_sFlag;
  private final long m_flag;
  private final String m_className;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BooleanUsingEqualsStylePropertyImpl(StylePropertyEditor editor,
      String title,
      String sFlag,
      long flag,
      String className) {
    super(editor, title);
    m_sFlag = sFlag;
    m_flag = flag;
    m_className = className;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // As string
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void getAsString(StringBuilder builder) {
    builder.append(getTitle());
    builder.append(" boolean: ");
    builder.append(m_sFlag);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // PropertyEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public PropertyEditor createEditor() {
    return BooleanPropertyEditor.INSTANCE;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Style
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public long getFlag(String sFlag) {
    return m_flag;
  }

  @Override
  public String getFlagValue(Property property) throws Exception {
    if (property instanceof GenericProperty) {
      String expressionString = getCurrentSource((GenericProperty) property);
      if (expressionString.indexOf('.' + m_sFlag) != -1) {
        return m_sFlag;
      }
    }
    return null;
  }

  private boolean isSet(Property property) throws Exception {
    return getFlagValue(property) != null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Value
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Object getValue(Property property) throws Exception {
    return isSet(property) ? Boolean.TRUE : Boolean.FALSE;
  }

  @Override
  public void setValue(Property property, Object value) throws Exception {
    GenericProperty genericProperty = (GenericProperty) property;
    String source = getSource(genericProperty, value != Property.UNKNOWN_VALUE && (Boolean) value);
    genericProperty.setExpression(source, Property.UNKNOWN_VALUE);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Popup menu
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void contributeActions(Property property, IMenuManager manager) throws Exception {
    // create
    IAction action = new BooleanStyleAction(property, this);
    // configure
    action.setChecked(isSet(property));
    // add to menu
    manager.add(action);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private String getCurrentSource(final GenericProperty genericProperty) throws Exception {
    return genericProperty.getExpression().toString();
  }

  private String getSource(final GenericProperty genericProperty, final boolean boolValue)
      throws Exception {
    // addClassAndDefault must be true, case and point: ButtonField.NON_FOCUSABLE
    return getSource(genericProperty, boolValue, true, " | ");
  }

  private String getSource(final GenericProperty genericProperty,
      final boolean boolValue,
      final boolean addClassAndDefault,
      final String separator) throws Exception {
    String currentSource = getCurrentSource(genericProperty);
    currentSource = currentSource.trim();
    // "ELLIPSIS" == m_sFlag
    final boolean isFlagInSource = currentSource.indexOf('.' + m_sFlag) != -1;
    // 4 cases
    if (boolValue && !isFlagInSource) {
      // if: the user checked the style & it is not in the current source
      // then: append it to the current source and
      if (addClassAndDefault) {
        return currentSource + separator + m_className + '.' + m_sFlag;
      } else {
        return currentSource + separator + m_sFlag;
      }
    } else if (boolValue && isFlagInSource) {
      // else if: the user checked the style & it is in the current source
      // then: do nothing
      return currentSource;
    } else if (!boolValue && !isFlagInSource) {
      // else if: the user un-checked the style & it is not in the current source
      // then: do nothing
      return currentSource;
    } else if (!boolValue && isFlagInSource) {
      // else if: the user un-checked the style & it is in the current source
      // then: remove the style flag from the source
      int beginIndex = currentSource.indexOf('.' + m_sFlag);
      int endIndex = currentSource.indexOf('.' + m_sFlag) + m_sFlag.length() + 1;
      // adjust beginIndex to include the "*Field." part of the style bit
      for (; beginIndex > 0; beginIndex--) {
        final char ch = currentSource.charAt(beginIndex);
        if (!Character.isLetter(ch) && ch != '.') {
          break;
        }
      }
      final int firstSeparatorOccurrence = currentSource.indexOf('|');
      if (firstSeparatorOccurrence == -1) {
        // don't remove the flag since changing the source to "" would throw a bug
        return currentSource;
      } else if (firstSeparatorOccurrence <= beginIndex) {
        // the '|' is before the style we are removing, update the beginIndex
        beginIndex = firstSeparatorOccurrence;
      } else if (firstSeparatorOccurrence >= endIndex) {
        // the '|' is after the style we are removing, update the endIndex
        endIndex = firstSeparatorOccurrence + 1;
      }
      return (currentSource.substring(0, beginIndex) + currentSource.substring(endIndex)).trim();
    } else {
      throw new IllegalStateException();
    }
  }
}