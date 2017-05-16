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
package org.eclipse.wb.internal.core.utils;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Locale;

/**
 * Implements utility methods that operate on strings.
 *
 * @author scheglov_ke
 * @coverage shared.utils.string
 */
public class StringUtilities {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private StringUtilities() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Replaces all duplicated whitespace characters with single space.
   */
  public static String normalizeWhitespaces(String s) {
    int length = s.length();
    StringBuffer normalized = new StringBuffer(length);
    //
    boolean needSpace = false;
    for (int index = 0; index < length; index++) {
      char c = s.charAt(index);
      if (Character.isWhitespace(c)) {
        needSpace = true;
      } else {
        if (needSpace) {
          needSpace = false;
          normalized.append(' ');
        }
        normalized.append(c);
      }
    }
    // add trailing space
    if (needSpace) {
      normalized.append(' ');
    }
    //
    return normalized.toString();
  }

  /**
   * @return the whitespace between beginning of line and given index.
   */
  public static String getLinePrefix(String s, int index) {
    int beginOfLine = index - 1;
    while (beginOfLine >= 0) {
      char c = s.charAt(beginOfLine);
      if (c == '\r' || c == '\n' || !Character.isWhitespace(c)) {
        break;
      }
      beginOfLine--;
    }
    return s.substring(beginOfLine + 1, index);
  }

  /**
   * @return the {@link String} without first word or empty {@link String} if no more words.
   */
  public static String removeFirstWord(String s) {
    s = s.trim();
    int length = s.length();
    for (int index = 0; index < length; index++) {
      char c = s.charAt(index);
      if (Character.isWhitespace(c)) {
        return s.substring(index).trim();
      }
    }
    // no more words
    return StringUtils.EMPTY;
  }

  /**
   * Extract camel caps from specified string.
   *
   * <pre>
   * Example: NullPointException --> NPE
   *
   * <pre>
   */
  public static String extractCamelCaps(String string) {
    if (string == null) {
      return null;
    }
    StringBuffer buf = new StringBuffer(string.length());
    int length = string.length();
    for (int i = 0; i < length; i++) {
      char ch = string.charAt(i);
      if (Character.isUpperCase(ch)) {
        buf.append(ch);
      }
    }
    return buf.toString();
  }

  /**
   * Extract camel words from specified string.
   *
   * <pre>
	 * Example: NullPointException --> [Null, Pointer, Exception]
	 * Example: null --> []
   *
   * <pre>
   */
  public static String[] extractCamelWords(String string) {
    if (string == null) {
      return ArrayUtils.EMPTY_STRING_ARRAY;
    }
    int length = string.length();
    //
    int count = 0;
    for (int i = 0; i < length; i++) {
      char ch = string.charAt(i);
      if (Character.isUpperCase(ch)) {
        count++;
      }
    }
    //
    String[] words = new String[count];
    int wordNum = 0;
    int begin = -1;
    for (int i = 0; i < length; i++) {
      char ch = string.charAt(i);
      boolean isLast = i == length - 1;
      if (Character.isUpperCase(ch) || isLast) {
        if (begin >= 0) {
          int end = i;
          if (isLast) {
            end++;
          }
          String word = string.substring(begin, end);
          words[wordNum++] = word;
        }
        begin = i;
      }
    }
    return words;
  }

  /**
   * @return the index of the first lowercase letter.
   *
   * <pre>
	 * null      = -1
	 * ""        = -1
	 * "button"  = 0
	 * "JButton" = 2
	 * "ABC"     = -1
	 * </pre>
   */
  public static int indexOfFirstLowerCase(String str) {
    if (str == null) {
      return -1;
    }
    // check each character for lower case
    for (int i = 0; i < str.length(); i++) {
      char ch = str.charAt(i);
      if (Character.isLowerCase(ch)) {
        return i;
      }
    }
    // no lower case characters
    return -1;
  }

  /**
   * Strip the leading uppercase characters from the string keeping n of them.
   *
   * <pre>
	 * null, *        = null
	 * "", *          = null
	 * "Button", 1    = "Button"
	 * "JButton", 1   = "Button"
	 * "ABCButton", 1 = "Button"
	 * "ABCButton", 2 = "CButton"
	 * "AbcButton", 2 = "AbcButton"
	 * </pre>
   */
  public static String stripLeadingUppercaseChars(String string, int keepCount) {
    int index = indexOfFirstLowerCase(string);
    if (index > 0 && index > keepCount) {
      return string.substring(index - keepCount);
    }
    return string;
  }

  /**
   * Strips all HTML tags if any.
   *
   * <pre>
	 * null, *      = null
	 * "", *        = null
	 * "abc"        = "abc"
	 * "a&lt;b&gt;c&lt;/b&gt;d" = "acd"
	 * </pre>
   */
  public static String stripHtml(String str) {
    if (StringUtils.isEmpty(str)) {
      return str;
    }
    //
    StringBuffer result = new StringBuffer();
    boolean insideTag = false;
    for (int i = 0; i < str.length(); i++) {
      char c = str.charAt(i);
      if (c == '<') {
        insideTag = true;
      }
      if (!insideTag) {
        result.append(c);
      }
      if (c == '>') {
        insideTag = false;
      }
    }
    return result.toString();
  }

  /**
   * @return the {@link String} where no sequential duplicate characters.
   */
  public static String removeDuplicateCharacters(String str) {
    if (StringUtils.isEmpty(str)) {
      return str;
    }
    //
    char[] chars = str.toCharArray();
    StringBuffer buffer = new StringBuffer();
    // add unique characters
    char lastChar = 0;
    for (int i = 0; i < chars.length; i++) {
      char c = chars[i];
      if (i == 0 || c != lastChar) {
        buffer.append(c);
        lastChar = c;
      }
    }
    // result as String
    return buffer.toString();
  }

  /**
   * Finds in each of two strings interval where they are different. Outside of these intervals, on
   * prefix and suffix these strings are equal. Interval has format <code>(begin, length)</code>.
   *
   * @return the <code>int[4]</code>, with two intervals.
   */
  public static int[] getDifferenceIntervals(String s1, String s2) {
    // prefix
    int prefixLength = 0;
    for (; prefixLength < s1.length() && prefixLength < s2.length(); prefixLength++) {
      char c1 = s1.charAt(prefixLength);
      char c2 = s2.charAt(prefixLength);
      if (c1 != c2) {
        break;
      }
    }
    // suffix
    int suffixLength = 0;
    for (; suffixLength < s1.length() - prefixLength && suffixLength < s2.length() - prefixLength; suffixLength++) {
      char c1 = s1.charAt(s1.length() - 1 - suffixLength);
      char c2 = s2.charAt(s2.length() - 1 - suffixLength);
      if (c1 != c2) {
        break;
      }
    }
    // return intervals
    return new int[]{
        prefixLength,
        s1.length() - suffixLength - prefixLength,
        prefixLength,
        s2.length() - suffixLength - prefixLength};
  }

  /**
   * Performs replace in each {@link String} of array.
   */
  public static String[] replace(String strings[], String searchString, String replacement) {
    String[] result = new String[strings.length];
    for (int i = 0; i < strings.length; i++) {
      String string = strings[i];
      result[i] = StringUtils.replace(string, searchString, replacement);
    }
    return result;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Escape
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Escapes the characters in a <code>String</code> using Java String rules.
   * <p>
   * Deals correctly with quotes and control-chars (tab, backslash, cr, ff, etc.)
   */
  public static String escapeJava(String s) {
    String escaped = StringEscapeUtils.escapeJava(s);
    // TODO remove after fix https://issues.apache.org/jira/browse/LANG-421  (probably in version 3.0)
    escaped = StringUtils.replace(escaped, "\\/", "/");
    return escaped;
  }

  /**
   * Escapes {@link String} for Java source.
   *
   * @return the {@link String} that can be used in quotes in Java source. Note, that it is not
   *         converted into any encoding, i.e. still in UTF-8.
   */
  public static String escapeForJavaSource(String str) {
    if (str == null) {
      return null;
    }
    StringBuffer buffer = new StringBuffer(str.length() * 2);
    escapeForJavaSource(buffer, str);
    return buffer.toString();
  }

  /**
   * Worker method for the {@link #escapeForJavaSource(String)}.
   */
  private static void escapeForJavaSource(StringBuffer out, String str) {
    int sz = str.length();
    for (int i = 0; i < sz; i++) {
      char c = str.charAt(i);
      if (c < 32) {
        switch (c) {
          case '\b' :
            out.append('\\');
            out.append('b');
            break;
          case '\n' :
            out.append('\\');
            out.append('n');
            break;
          case '\t' :
            out.append('\\');
            out.append('t');
            break;
          case '\f' :
            out.append('\\');
            out.append('f');
            break;
          case '\r' :
            out.append('\\');
            out.append('r');
            break;
          default :
            if (c > 0xf) {
              out.append("\\u00" + hex(c));
            } else {
              out.append("\\u000" + hex(c));
            }
            break;
        }
      } else {
        switch (c) {
          case '"' :
            out.append('\\');
            out.append('"');
            break;
          case '\\' :
            out.append('\\');
            out.append('\\');
            break;
          default :
            out.append(c);
            break;
        }
      }
    }
  }

  /**
   * @return the upper case hex decimal {@link String}.
   */
  private static String hex(char c) {
    return Integer.toHexString(c).toUpperCase(Locale.ENGLISH);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Latin
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return filtered {@link String} where only latin characters are left.
   */
  public static String removeNonLatinCharacters(String s) {
    StringBuffer sb = new StringBuffer();
    char[] chars = s.toCharArray();
    for (int i = 0; i < chars.length; i++) {
      char c = chars[i];
      if (isLatinCharacter(c)) {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  /**
   * @return <code>true</code> if given character if upper/lower latin characters.
   */
  public static boolean isLatinCharacter(char c) {
    return c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z';
  }
}
