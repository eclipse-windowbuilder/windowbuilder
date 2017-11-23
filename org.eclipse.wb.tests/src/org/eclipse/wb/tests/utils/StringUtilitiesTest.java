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
package org.eclipse.wb.tests.utils;

import org.eclipse.wb.internal.core.utils.StringUtilities;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang.ArrayUtils;

/**
 * Test for {@link StringUtilities}.
 * 
 * @author scheglov_ke
 */
public class StringUtilitiesTest extends DesignerTestCase {
  public void test_normalizeWhitespaces() throws Exception {
    assertEquals("", StringUtilities.normalizeWhitespaces(""));
    assertEquals("a", StringUtilities.normalizeWhitespaces("a"));
    assertEquals("a b", StringUtilities.normalizeWhitespaces("a  b"));
    assertEquals("a b", StringUtilities.normalizeWhitespaces("a\t\r\nb"));
    assertEquals(" a ", StringUtilities.normalizeWhitespaces(" a "));
    assertEquals("a ", StringUtilities.normalizeWhitespaces("a \t\r\n"));
  }

  /**
   * Test for {@link StringUtilities#getLinePrefix(String, int)}.
   */
  public void test_getLinePrefix() throws Exception {
    // good prefix
    {
      String s =
          getSource(
              "// filler filler filler",
              "// filler filler filler",
              "// filler filler filler",
              "// filler filler filler",
              "\t\t<script>");
      int index = s.indexOf("<script>");
      String prefix = StringUtilities.getLinePrefix(s, index);
      assertEquals("\t\t", prefix);
    }
    // first line
    {
      String s = "  <script>";
      int index = s.indexOf("<script>");
      String prefix = StringUtilities.getLinePrefix(s, index);
      assertEquals("  ", prefix);
    }
    // not begin of line
    {
      String s = "<b> <script>";
      int index = s.indexOf("<script>");
      String prefix = StringUtilities.getLinePrefix(s, index);
      assertEquals(" ", prefix);
    }
    // no prefix
    {
      String s = "<b><script>";
      int index = s.indexOf("<script>");
      String prefix = StringUtilities.getLinePrefix(s, index);
      assertEquals("", prefix);
    }
  }

  public void test_removeFirstWord() throws Exception {
    assertEquals("bbb", StringUtilities.removeFirstWord("aaa bbb"));
    assertEquals("bbb", StringUtilities.removeFirstWord("   aaa   bbb  "));
    assertEquals("", StringUtilities.removeFirstWord(""));
    assertEquals("", StringUtilities.removeFirstWord(" \t\r\n"));
  }

  public void test_extractCamelCaps() throws Exception {
    assertEquals(null, StringUtilities.extractCamelCaps(null));
    assertEquals("NPE", StringUtilities.extractCamelCaps("NullPoinerException"));
  }

  /**
   * Test for {@link StringUtilities#extractCamelWords(String)}.
   */
  public void test_extractCamelWords() throws Exception {
    {
      String[] words = StringUtilities.extractCamelWords(null);
      assertThat(words).isEqualTo(ArrayUtils.EMPTY_STRING_ARRAY);
    }
    {
      String[] words = StringUtilities.extractCamelWords("NullPointerException");
      assertThat(words).isEqualTo(new String[]{"Null", "Pointer", "Exception"});
    }
  }

  public void test_indexOfFirstLowerCase() throws Exception {
    assertEquals(-1, StringUtilities.indexOfFirstLowerCase(null));
    assertEquals(-1, StringUtilities.indexOfFirstLowerCase(""));
    assertEquals(0, StringUtilities.indexOfFirstLowerCase("button"));
    assertEquals(2, StringUtilities.indexOfFirstLowerCase("JButton"));
    assertEquals(-1, StringUtilities.indexOfFirstLowerCase("ABC"));
  }

  public void test_stripLeadingUppercaseChars() throws Exception {
    assertEquals(null, StringUtilities.stripLeadingUppercaseChars(null, -1));
    assertEquals("", StringUtilities.stripLeadingUppercaseChars("", -1));
    assertEquals("button", StringUtilities.stripLeadingUppercaseChars("button", 1));
    assertEquals("utton", StringUtilities.stripLeadingUppercaseChars("Button", 0));
    assertEquals("Button", StringUtilities.stripLeadingUppercaseChars("Button", 1));
    assertEquals("Button", StringUtilities.stripLeadingUppercaseChars("Button", 2));
    assertEquals("Button", StringUtilities.stripLeadingUppercaseChars("JButton", 1));
    assertEquals("Button", StringUtilities.stripLeadingUppercaseChars("ABCButton", 1));
    assertEquals("CButton", StringUtilities.stripLeadingUppercaseChars("ABCButton", 2));
    assertEquals("AbcButton", StringUtilities.stripLeadingUppercaseChars("AbcButton", 2));
  }

  public void test_stripHtml() throws Exception {
    assertEquals(null, StringUtilities.stripHtml(null));
    assertEquals("", StringUtilities.stripHtml(""));
    assertEquals("abc", StringUtilities.stripHtml("abc"));
    assertEquals("acd", StringUtilities.stripHtml("a<b>c</b>d"));
    assertEquals("ac", StringUtilities.stripHtml("a<b attr=\"123\">c"));
  }

  public void test_deleteDuplicateCharacters() throws Exception {
    assertEquals(null, StringUtilities.removeDuplicateCharacters(null));
    assertEquals("", StringUtilities.removeDuplicateCharacters(""));
    assertEquals("ab", StringUtilities.removeDuplicateCharacters("aab"));
    assertEquals("abc", StringUtilities.removeDuplicateCharacters("abbcc"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getDifferenceIntervals() 
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getDifferenceIntervals_same() throws Exception {
    assertIntervals(new int[]{4, 0, 4, 0}, StringUtilities.getDifferenceIntervals("0123", "0123"));
  }

  public void test_getDifferenceIntervals_inner() throws Exception {
    assertIntervals(new int[]{1, 2, 1, 2}, StringUtilities.getDifferenceIntervals("0123", "0ab3"));
  }

  public void test_getDifferenceIntervals_inner2() throws Exception {
    assertIntervals(
        new int[]{2, 2, 2, 3},
        StringUtilities.getDifferenceIntervals("0123456", "01abc456"));
  }

  public void test_getDifferenceIntervals_insert() throws Exception {
    assertIntervals(
        new int[]{3, 0, 3, 3},
        StringUtilities.getDifferenceIntervals("0123", "012abc3"));
  }

  public void test_getDifferenceIntervals_end1() throws Exception {
    assertIntervals(new int[]{3, 1, 3, 0}, StringUtilities.getDifferenceIntervals("0123", "012"));
  }

  public void test_getDifferenceIntervals_end2() throws Exception {
    assertIntervals(new int[]{3, 0, 3, 1}, StringUtilities.getDifferenceIntervals("012", "0123"));
  }

  public void test_getDifferenceIntervals_begin1() throws Exception {
    assertIntervals(new int[]{0, 1, 0, 0}, StringUtilities.getDifferenceIntervals("0123", "123"));
  }

  public void test_getDifferenceIntervals_begin2() throws Exception {
    assertIntervals(new int[]{0, 0, 0, 1}, StringUtilities.getDifferenceIntervals("123", "0123"));
  }

  /**
   * Asserts that two given int's arrays are equals.
   */
  private static void assertIntervals(int[] expected, int[] actual) {
    assertTrue(
        ArrayUtils.toString(expected) + " != " + ArrayUtils.toString(actual),
        ArrayUtils.isEquals(expected, actual));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // escapeJava
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link StringUtilities#escapeJava(String)}.
   */
  public void test_escapeJava() throws Exception {
    assertEquals("a", StringUtilities.escapeJava("a"));
    assertEquals("\\t", StringUtilities.escapeJava("\t"));
    assertEquals("\\\\", StringUtilities.escapeJava("\\"));
    assertEquals("/", StringUtilities.escapeJava("/"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // escapeForJavaSource
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link StringUtilities#escapeForJavaSource(String)}.
   */
  public void test_escapeForJavaSource() throws Exception {
    assertEquals(null, StringUtilities.escapeForJavaSource(null));
    assertEquals("abc", StringUtilities.escapeForJavaSource("abc"));
    // there was bug in original
    assertEquals("/", StringUtilities.escapeForJavaSource("/"));
    // \ and "
    assertEquals("\\\\", StringUtilities.escapeForJavaSource("\\"));
    assertEquals("\\\"", StringUtilities.escapeForJavaSource("\""));
    // special characters
    assertEquals("\\b", StringUtilities.escapeForJavaSource("\b"));
    assertEquals("\\n", StringUtilities.escapeForJavaSource("\n"));
    assertEquals("\\t", StringUtilities.escapeForJavaSource("\t"));
    assertEquals("\\f", StringUtilities.escapeForJavaSource("\f"));
    assertEquals("\\r", StringUtilities.escapeForJavaSource("\r"));
    // < 32
    assertEquals("\\u0002", StringUtilities.escapeForJavaSource("\u0002"));
    assertEquals("\\u000F", StringUtilities.escapeForJavaSource("\u000f"));
    assertEquals("\\u0015", StringUtilities.escapeForJavaSource("\u0015"));
    // national characters are not changed
    assertEquals("\u0410", StringUtilities.escapeForJavaSource("\u0410"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // replace(String[],String,String)
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link StringUtilities#replace(String[], String, String)}.
   */
  public void test_replace() throws Exception {
    String[] source = {"my text", "some text"};
    // replace
    String[] target = StringUtilities.replace(source, "text", "word");
    assertThat(target).isEqualTo(new String[]{"my word", "some word"});
    // source should not be changed
    assertThat(source).isEqualTo(new String[]{"my text", "some text"});
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Latin
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link StringUtilities#isLatinCharacter(char)}.
   */
  public void test_isLatinCharacter() throws Exception {
    assertTrue(StringUtilities.isLatinCharacter('A'));
    assertTrue(StringUtilities.isLatinCharacter('P'));
    assertTrue(StringUtilities.isLatinCharacter('Z'));
    assertTrue(StringUtilities.isLatinCharacter('a'));
    assertTrue(StringUtilities.isLatinCharacter('p'));
    assertTrue(StringUtilities.isLatinCharacter('z'));
    assertFalse(StringUtilities.isLatinCharacter('0'));
    assertFalse(StringUtilities.isLatinCharacter('.'));
    assertFalse(StringUtilities.isLatinCharacter('\u0400'));
  }

  /**
   * Test for {@link StringUtilities#removeNonLatinCharacters(String)}.
   */
  public void test_removeNonLatinCharacters() throws Exception {
    assertEquals("abc", StringUtilities.removeNonLatinCharacters("abc"));
    assertEquals("ab", StringUtilities.removeNonLatinCharacters("a\u0410b"));
    assertEquals("abc", StringUtilities.removeNonLatinCharacters("a@b.c"));
  }
}
