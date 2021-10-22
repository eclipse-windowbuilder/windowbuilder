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
package org.eclipse.wb.internal.rcp.databinding.emf.model.bindables;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Helper class for two directional convert EMF properties names (Java Beans format <-> EMF format).
 *
 * @author lobas_av
 * @coverage bindings.rcp.emf.model
 */
public final class EmfCodeGenUtil {
  public static String unformat(String emfClassName) {
    int removeIndex = 0;
    for (int length = emfClassName.length(); removeIndex < length; removeIndex++) {
      if (emfClassName.charAt(removeIndex) != '_') {
        break;
      }
    }
    int removeLength = emfClassName.length();
    for (int i = removeLength - 1; i >= 0; i--) {
      if (emfClassName.charAt(i) == '_') {
        removeLength--;
      } else {
        break;
      }
    }
    if (removeIndex > 0 || removeLength < emfClassName.length()) {
      emfClassName = emfClassName.substring(removeIndex, removeLength);
    }
    emfClassName = StringUtils.capitalize(emfClassName.toLowerCase());
    StringBuffer result = new StringBuffer();
    for (int i = 0, length = emfClassName.length(); i < length; i++) {
      char ch = emfClassName.charAt(i);
      if (ch == '_') {
        result.append(Character.toUpperCase(emfClassName.charAt(i + 1)));
        i++;
      } else {
        result.append(ch);
      }
    }
    return result.toString();
  }

  /**
   * Formats a name by parsing it into words separated by underscores and/or mixed-casing and then
   * recombining them using the specified separator. A prefix can also be given to be recognized as
   * a separate word or to be trimmed. Leading underscores can be ignored or can cause a leading
   * separator to be prepended.
   *
   * @since 2.2
   */
  public static String format(String name,
      char separator,
      String prefix,
      boolean includePrefix,
      boolean includeLeadingSeparator) {
    String leadingSeparators = includeLeadingSeparator ? getLeadingSeparators(name, '_') : null;
    if (leadingSeparators != null) {
      name = name.substring(leadingSeparators.length());
    }
    List<String> parsedName = new ArrayList<String>();
    if (prefix != null
        && name.startsWith(prefix)
        && name.length() > prefix.length()
        && Character.isUpperCase(name.charAt(prefix.length()))) {
      name = name.substring(prefix.length());
      if (includePrefix) {
        parsedName = parseName(prefix, '_');
      }
    }
    if (name.length() != 0) {
      parsedName.addAll(parseName(name, '_'));
    }
    StringBuilder result = new StringBuilder();
    for (Iterator<String> nameIter = parsedName.iterator(); nameIter.hasNext();) {
      String nameComponent = nameIter.next();
      result.append(nameComponent);
      if (nameIter.hasNext() && nameComponent.length() > 1) {
        result.append(separator);
      }
    }
    if (result.length() == 0 && prefix != null) {
      result.append(prefix);
    }
    return leadingSeparators != null ? "_" + result.toString() : result.toString();
  }

  private static String getLeadingSeparators(String name, char separator) {
    int i = 0;
    for (int len = name.length(); i < len && name.charAt(i) == separator; i++) {
      // the for loop's condition finds the separator
    }
    return i != 0 ? name.substring(0, i) : null;
  }

  /**
   * This method breaks sourceName into words delimited by separator and/or mixed-case naming.
   */
  public static List<String> parseName(String sourceName, char separator) {
    List<String> result = new ArrayList<String>();
    if (sourceName != null) {
      StringBuilder currentWord = new StringBuilder();
      boolean lastIsLower = false;
      for (int index = 0, length = sourceName.length(); index < length; ++index) {
        char curChar = sourceName.charAt(index);
        if (Character.isUpperCase(curChar)
            || !lastIsLower
            && Character.isDigit(curChar)
            || curChar == separator) {
          if (lastIsLower
              && currentWord.length() > 1
              || curChar == separator
              && currentWord.length() > 0) {
            result.add(currentWord.toString());
            currentWord = new StringBuilder();
          }
          lastIsLower = false;
        } else {
          if (!lastIsLower) {
            int currentWordLength = currentWord.length();
            if (currentWordLength > 1) {
              char lastChar = currentWord.charAt(--currentWordLength);
              currentWord.setLength(currentWordLength);
              result.add(currentWord.toString());
              currentWord = new StringBuilder();
              currentWord.append(lastChar);
            }
          }
          lastIsLower = true;
        }
        if (curChar != separator) {
          currentWord.append(curChar);
        }
      }
      result.add(currentWord.toString());
    }
    return result;
  }
}