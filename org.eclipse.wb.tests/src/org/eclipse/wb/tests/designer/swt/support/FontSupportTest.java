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
package org.eclipse.wb.tests.designer.swt.support;

import org.eclipse.wb.internal.swt.support.FontSupport;

import org.eclipse.swt.SWT;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for {@link FontSupport}.
 * 
 * @author scheglov_ke
 */
public class FontSupportTest extends AbstractSupportTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Exit zone :-) XXX
  //
  ////////////////////////////////////////////////////////////////////////////
  public void _test_exit() throws Exception {
    System.exit(0);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link FontSupport#getFontClass()}.
   */
  public void test_getFontClass() throws Exception {
    Class<?> fontClass = FontSupport.getFontClass();
    assertEquals("org.eclipse.swt.graphics.Font", fontClass.getName());
  }

  /**
   * Test for {@link FontSupport#createFont(String, int, Object)}.
   */
  public void test_createFont_etc() throws Exception {
    Object font = FontSupport.createFont("Arial", 12, SWT.BOLD);
    try {
      Object fontData = FontSupport.getFontData(font);
      // check that FontData has initial values
      assertEquals("Arial", FontSupport.getFontName(fontData));
      assertEquals(12, FontSupport.getFontSize(fontData));
      assertEquals(SWT.BOLD, FontSupport.getFontStyle(fontData));
    } finally {
      // do dispose
      FontSupport.dispose(font);
    }
  }

  /**
   * Test for {@link FontSupport#getCopy(Object)}.
   */
  public void test_getCopy() throws Exception {
    Object font = FontSupport.createFont("Arial", 12, SWT.BOLD);
    Object fontCopy = FontSupport.getCopy(font);
    try {
      Object fontData = FontSupport.getFontData(font);
      Object fontDataCopy = FontSupport.getFontData(fontCopy);
      assertTrue(fontData.equals(fontDataCopy));
    } finally {
      FontSupport.dispose(font);
      FontSupport.dispose(fontCopy);
    }
  }

  /**
   * Test for {@link FontSupport#dispose(Object)}.
   */
  public void test_dispose() throws Exception {
    Object font = FontSupport.createFont("Arial", 12, SWT.BOLD);
    // not disposed yet
    assertFalse(FontSupport.isDisposed(font));
    // dispose, not disposed
    FontSupport.dispose(font);
    assertTrue(FontSupport.isDisposed(font));
    // second dispose() ignored
    FontSupport.dispose(font);
    assertTrue(FontSupport.isDisposed(font));
  }

  /**
   * Test for {@link FontSupport#getFontFamilies()}.
   */
  public void test_getFontFamilies() throws Exception {
    String[] fontFamilies = FontSupport.getFontFamilies();
    assertThat(fontFamilies).contains("Arial", "Courier", "Times New Roman");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getFontStyleSource
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link FontSupport#getFontStyleSource(Object)}.
   */
  public void test_getStringFontStyle() throws Exception {
    check_getFontStyleSource("org.eclipse.swt.SWT.NORMAL", SWT.NORMAL);
    check_getFontStyleSource("org.eclipse.swt.SWT.BOLD", SWT.BOLD);
    check_getFontStyleSource("org.eclipse.swt.SWT.ITALIC", SWT.ITALIC);
    check_getFontStyleSource("org.eclipse.swt.SWT.BOLD | org.eclipse.swt.SWT.ITALIC", SWT.BOLD
        | SWT.ITALIC);
  }

  private void check_getFontStyleSource(String expectedSource, int style) throws Exception {
    Object font = FontSupport.createFont("Arial", 10, style);
    try {
      Object fontData = FontSupport.getFontData(font);
      assertEquals(expectedSource, FontSupport.getFontStyleSource(fontData));
    } finally {
      FontSupport.dispose(font);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getFontStyleText
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link FontSupport#getFontStyleText(Object)}.
   */
  public void test_getStringFontText() throws Exception {
    check_getFontStyleText("", SWT.NORMAL);
    check_getFontStyleText("BOLD", SWT.BOLD);
    check_getFontStyleText("ITALIC", SWT.ITALIC);
    check_getFontStyleText("BOLD ITALIC", SWT.BOLD | SWT.ITALIC);
  }

  private void check_getFontStyleText(String expectedSource, int style) throws Exception {
    Object font = FontSupport.createFont("Arial", 10, style);
    try {
      Object fontData = FontSupport.getFontData(font);
      assertEquals(expectedSource, FontSupport.getFontStyleText(fontData));
    } finally {
      FontSupport.dispose(font);
    }
  }
}