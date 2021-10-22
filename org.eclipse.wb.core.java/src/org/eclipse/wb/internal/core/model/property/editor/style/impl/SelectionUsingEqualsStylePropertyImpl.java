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
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.StringComboPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.style.AbstractStylePropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.style.SubStylePropertyImpl;
import org.eclipse.wb.internal.core.model.property.editor.style.actions.RadioStyleAction;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;

import org.apache.commons.lang.ArrayUtils;

/**
 * This class represent "selectUsingEqual" property implementation.
 *
 * @author Jaime Wren
 * @author lobas_av
 * @coverage core.model.property.editor
 */
public final class SelectionUsingEqualsStylePropertyImpl extends SubStylePropertyImpl {
  private final long[] m_flags;
  private final String[] m_sFlags;
  private final String m_className;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SelectionUsingEqualsStylePropertyImpl(AbstractStylePropertyEditor editor,
      String title,
      long[] flags,
      String[] sFlags,
      String className) {
    super(editor, title);
    m_flags = flags;
    m_sFlags = sFlags;
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
    builder.append(" select: ");
    for (String sFlag : m_sFlags) {
      builder.append(" ");
      builder.append(sFlag);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // PropertyEditor
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public PropertyEditor createEditor() {
    return new StringComboPropertyEditor(m_sFlags);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Style
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public long getFlag(String sFlag) {
    return m_flags[ArrayUtils.indexOf(m_sFlags, sFlag)];
  }

  @Override
  public String getFlagValue(Property property) throws Exception {
    if (property instanceof GenericProperty) {
      String expressionString = getCurrentSource((GenericProperty) property);
      for (int i = 0; i < m_flags.length; i++) {
        if (expressionString.indexOf('.' + m_sFlags[i]) != -1) {
          return m_sFlags[i];
        }
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Value
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Object getValue(Property property) throws Exception {
    return getFlagValue(property);
  }

  //	private long getCurrentFlag(Property property) throws Exception {
  //		if (property instanceof GenericProperty) {
  //			String expressionString = getCurrentSource((GenericProperty) property);
  //			for (int i = 0; i < m_flags.length; i++) {
  //				if (expressionString.indexOf('.' + m_sFlags[i]) != -1) {
  //					return m_flags[i];
  //				}
  //			}
  //		}
  //		return 0L;
  //	}
  @Override
  public void setValue(Property property, Object value) throws Exception {
    GenericProperty genericProperty = (GenericProperty) property;
    String source = getSource(genericProperty, value.toString());
    genericProperty.setExpression(source, Property.UNKNOWN_VALUE);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Popup menu
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void contributeActions(Property property, IMenuManager manager) throws Exception {
    // separate sub-properties
    manager.add(new Separator());
    // add actions
    long style = getStyleValue(property);
    for (int i = 0; i < m_flags.length; i++) {
      // create
      IAction action = new RadioStyleAction(property, this, m_sFlags[i]);
      // configure
      if ((style & m_flags[i]) != 0) {
        action.setChecked(true);
      }
      // add to menu
      manager.add(action);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private String getCurrentSource(final GenericProperty genericProperty) throws Exception {
    return genericProperty.getExpression().toString();
  }

  private String getSource(final GenericProperty genericProperty, final String style)
      throws Exception {
    // addClassAndDefault must be true, case and point: ButtonField.NON_FOCUSABLE
    return getSource(genericProperty, style, true, " | ");
  }

  private String getSource(final GenericProperty genericProperty,
      final String style,
      final boolean addClassAndDefault,
      final String separator) throws Exception {
    String currentSource = getCurrentSource(genericProperty);
    currentSource = currentSource.trim();
    // if the new style is unknown, do nothing
    if (style.equals("UNKNOWN_VALUE")) {
      // get the style string that the new source should replace
      final String flagValue = getFlagValue(genericProperty);
      if (flagValue == null) {
        // the selection doesn't include anything to clear
        return currentSource;
      }
      // remove flagValue from the current source, and return the source
      // if there is no source, then don't bother, return the current source
      if (currentSource.length() == 0) {
        return currentSource;
      }
      int beginIndex = currentSource.indexOf('.' + flagValue);
      int endIndex = currentSource.indexOf('.' + flagValue) + flagValue.length() + 1;
      // adjust beginIndex to include the "*Field." part of the style bit
      for (; beginIndex > 0; beginIndex--) {
        final char ch = currentSource.charAt(beginIndex);
        if (!Character.isLetter(ch) && ch != '.') {
          break;
        }
      }
      // adjust beginIndex and endIndex to trim any whitespace
      for (; beginIndex > 0; beginIndex--) {
        final char ch = currentSource.charAt(beginIndex);
        if (!Character.isWhitespace(ch)) {
          break;
        }
      }
      for (; endIndex <= currentSource.length() - 1; endIndex++) {
        final char ch = currentSource.charAt(endIndex);
        if (!Character.isWhitespace(ch)) {
          break;
        }
      }
      // assume that a '|' is just before the beginIndex
      // else, then assume it is after the endIndex
      // else, we just assume that
      if (beginIndex > 0 && currentSource.charAt(beginIndex) == '|') {
        beginIndex--;
      } else if (endIndex < currentSource.length() && currentSource.charAt(endIndex) == '|') {
        endIndex++;
      } else {
        return currentSource;
      }
      return currentSource.substring(0, beginIndex)
          + currentSource.substring(endIndex, currentSource.length());
    }
    // else if the new style is not already in the currentSource, then add the new source
    else if (currentSource.indexOf('.' + style) == -1) {
      // get the style string that the new source should replace
      final String flagValue = getFlagValue(genericProperty);
      if (flagValue == null) {
        // if not replacing any source, simply append the new style source onto the existing source
        StringBuffer newSource = new StringBuffer();
        if (currentSource.length() != 0) {
          newSource.append(currentSource);
        }
        newSource.append(separator);
        if (addClassAndDefault) {
          newSource.append(m_className);
          newSource.append('.');
        }
        newSource.append(style);
        return newSource.toString();
      } else {
        // otherwise, replace the existing style String with the new selection
        //currentSource.subSequence(beginIndex, endIndex);
        int beginIndex = currentSource.indexOf('.' + flagValue);
        int endIndex = currentSource.indexOf('.' + flagValue) + flagValue.length() + 1;
        // adjust beginIndex to include the "*Field." part of the style bit
        for (; beginIndex > 0; beginIndex--) {
          final char ch = currentSource.charAt(beginIndex);
          if (!Character.isLetter(ch) && ch != '.') {
            break;
          }
        }
        // modify this code segment to use the StringBuffer so that the code is in sync with
        // the coding-pattern in the rest of the method (pattern of using the StringBuffer)
        if (addClassAndDefault) {
          currentSource = currentSource.substring(0, beginIndex)
              + m_className
              + '.'
              + style
              + currentSource.substring(endIndex);
        } else {
          currentSource =
              currentSource.substring(0, beginIndex) + style + currentSource.substring(endIndex);
        }
        return currentSource;
      }
    } else {
      return currentSource;
    }
  }
}