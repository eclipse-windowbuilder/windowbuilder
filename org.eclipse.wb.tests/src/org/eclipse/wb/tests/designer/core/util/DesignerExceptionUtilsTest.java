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
package org.eclipse.wb.tests.designer.core.util;

import org.eclipse.wb.internal.core.editor.errors.ErrorEntryInfo;
import org.eclipse.wb.internal.core.utils.exception.DesignerException;
import org.eclipse.wb.internal.core.utils.exception.DesignerExceptionUtils;
import org.eclipse.wb.internal.core.utils.exception.FatalDesignerException;
import org.eclipse.wb.internal.core.utils.exception.ICoreExceptionConstants;
import org.eclipse.wb.tests.designer.core.TestBundle;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test for {@link DesignerExceptionUtils}.
 * 
 * @author scheglov_ke
 */
public class DesignerExceptionUtilsTest extends DesignerTestCase {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void tearDown() throws Exception {
    DesignerExceptionUtils.flushErrorEntriesCache();
    super.tearDown();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getRootCause()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getRootCause_noCause() {
    Throwable e = new Throwable();
    assertSame(e, DesignerExceptionUtils.getRootCause(e));
  }

  public void test_getRootCause_singleCause() {
    Throwable e1 = new Throwable();
    Throwable e2 = new Throwable(e1);
    assertSame(e1, DesignerExceptionUtils.getRootCause(e2));
  }

  public void test_getRootCause_twoCauses() {
    Throwable e1 = new Throwable();
    Throwable e2 = new Throwable(e1);
    Throwable e3 = new Throwable(e2);
    assertSame(e1, DesignerExceptionUtils.getRootCause(e3));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getDesignerCause()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getDesignerCause_noDesignerException() throws Exception {
    Throwable e = new Throwable();
    assertSame(e, DesignerExceptionUtils.getDesignerCause(e));
  }

  public void test_getDesignerCause_itself() throws Exception {
    Throwable e = new DesignerException(-1);
    assertSame(e, DesignerExceptionUtils.getDesignerCause(e));
  }

  public void test_getDesignerCause_withInner() throws Exception {
    Throwable e = new Throwable();
    Throwable e1 = new DesignerException(-1, e);
    assertSame(e1, DesignerExceptionUtils.getDesignerCause(e1));
  }

  public void test_getDesignerCause_inError() throws Exception {
    Throwable e = new DesignerException(-1);
    Throwable e1 = new Error(e);
    assertSame(e, DesignerExceptionUtils.getDesignerCause(e1));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getDesignerException()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getDesignerException_noDesignerException() throws Exception {
    Throwable e = new Throwable();
    try {
      DesignerExceptionUtils.getDesignerException(e);
      fail();
    } catch (ClassCastException e_) {
    }
  }

  public void test_getDesignerException_itself() throws Exception {
    Throwable e = new DesignerException(-1);
    assertSame(e, DesignerExceptionUtils.getDesignerException(e));
  }

  public void test_getDesignerException_withInner() throws Exception {
    Throwable e = new Throwable();
    Throwable e1 = new DesignerException(-1, e);
    assertSame(e1, DesignerExceptionUtils.getDesignerException(e1));
  }

  public void test_getDesignerException_inError() throws Exception {
    Throwable e = new DesignerException(-1);
    Throwable e1 = new Error(e);
    assertSame(e, DesignerExceptionUtils.getDesignerException(e1));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getSourcePosition()
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_getSourcePosition_notSet() throws Exception {
    Throwable throwable = new Error();
    assertEquals(-1, DesignerExceptionUtils.getSourcePosition(throwable));
  }

  public void test_getSourcePosition_wasSet() throws Exception {
    Throwable throwable = new Error();
    DesignerExceptionUtils.setSourcePosition(throwable, 123);
    assertEquals(123, DesignerExceptionUtils.getSourcePosition(throwable));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // isWarning()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link DesignerExceptionUtils#isWarning(Throwable)}.
   */
  public void test_isWarning_notDesignerException() throws Exception {
    Throwable e = new Error();
    assertFalse(DesignerExceptionUtils.isWarning(e));
  }

  /**
   * Test for {@link DesignerExceptionUtils#isWarning(Throwable)}.
   */
  public void test_isWarning_notWarning() throws Exception {
    TestBundle testBundle = new TestBundle();
    try {
      testBundle.setFile(
          "resources/exceptions.xml",
          getSource(
              "<exceptions>",
              "  <exception id='-1000' title='My title'></exception>",
              "</exceptions>"));
      testBundle.addExtension(
          "org.eclipse.wb.core.exceptions",
          "<file path='resources/exceptions.xml'/>");
      testBundle.install();
      //
      Throwable e = new DesignerException(-1000);
      assertFalse(DesignerExceptionUtils.isWarning(e));
    } finally {
      testBundle.dispose();
    }
  }

  /**
   * Test for {@link DesignerExceptionUtils#isWarning(Throwable)}.
   */
  public void test_isWarning_true() throws Exception {
    TestBundle testBundle = new TestBundle();
    try {
      testBundle.setFile(
          "resources/exceptions.xml",
          getSource(
              "<exceptions>",
              "  <exception id='-1000' title='My title' warning='true'></exception>",
              "</exceptions>"));
      testBundle.addExtension(
          "org.eclipse.wb.core.exceptions",
          "<file path='resources/exceptions.xml'/>");
      testBundle.install();
      //
      Throwable e = new DesignerException(-1000);
      assertTrue(DesignerExceptionUtils.isWarning(e));
    } finally {
      testBundle.dispose();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // isFatal()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link DesignerExceptionUtils#isFatal(Throwable)}.
   */
  public void test_isFatal_false() throws Exception {
    Throwable e = new Error();
    assertFalse(DesignerExceptionUtils.isFatal(e));
  }

  /**
   * Test for {@link DesignerExceptionUtils#isFatal(Throwable)}.
   */
  public void test_isFatal_directly() throws Exception {
    Throwable fde = new FatalDesignerException(-1);
    assertTrue(DesignerExceptionUtils.isFatal(fde));
  }

  /**
   * Test for {@link DesignerExceptionUtils#isFatal(Throwable)}.
   */
  public void test_isFatal_indirectly() throws Exception {
    Throwable fde = new FatalDesignerException(-1);
    Throwable e = new Error(fde);
    assertTrue(DesignerExceptionUtils.isFatal(e));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getExceptionTitle()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link DesignerExceptionUtils#getExceptionTitle(int)}.
   */
  public void test_getExceptionTitle_notFound() throws Exception {
    String title = DesignerExceptionUtils.getExceptionTitle(-1000);
    assertEquals("No description", title);
  }

  /**
   * Test for {@link DesignerExceptionUtils#getExceptionTitle(int)}.
   */
  public void test_getExceptionTitle_hasTitle() throws Exception {
    TestBundle testBundle = new TestBundle();
    try {
      testBundle.setFile(
          "resources/exceptions.xml",
          getSourceDQ(
              "<exceptions>",
              "  <exception id='-1000' title='My title'></exception>",
              "</exceptions>"));
      testBundle.addExtension(
          "org.eclipse.wb.core.exceptions",
          "<file path='resources/exceptions.xml'/>");
      testBundle.install();
      try {
        String title = DesignerExceptionUtils.getExceptionTitle(-1000);
        assertEquals("My title", title);
      } finally {
        testBundle.uninstall();
      }
    } finally {
      testBundle.dispose();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getErrorDescription(int)
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link DesignerExceptionUtils#getErrorEntry(int, String...)}.
   */
  public void test_getErrorEntry_notFound() throws Exception {
    ErrorEntryInfo entry = DesignerExceptionUtils.getErrorEntry(-1000);
    assertNotNull(entry);
    assertEquals(-1000, entry.getCode());
    assertEquals("WindowBuilder error", entry.getTitle());
    assertEquals("No detailed description found for error (-1000).", entry.getDescription());
  }

  /**
   * Test for {@link DesignerExceptionUtils#getErrorEntry(int, String...)}.
   */
  public void test_getErrorEntry_basic() throws Exception {
    TestBundle testBundle = new TestBundle();
    try {
      testBundle.setFile(
          "resources/exceptions.xml",
          getSourceDQ(
              "<exceptions>",
              "  <exception id='-1000' title='My title'>",
              "  My description {0} + {1}.",
              "  </exception>",
              "</exceptions>"));
      testBundle.addExtension(
          "org.eclipse.wb.core.exceptions",
          "<file path='resources/exceptions.xml'/>");
      testBundle.install();
      try {
        ErrorEntryInfo entry = DesignerExceptionUtils.getErrorEntry(-1000, "aaa", "bbb");
        assertNotNull(entry);
        assertEquals(-1000, entry.getCode());
        assertEquals("My title", entry.getTitle());
        assertEquals("My description aaa + bbb.", entry.getDescription().trim());
      } finally {
        testBundle.uninstall();
      }
    } finally {
      testBundle.dispose();
    }
  }

  /**
   * Test for {@link DesignerExceptionUtils#getErrorEntry(int, String...)}.
   */
  public void test_getErrorEntry_escapeParametersForHTML() throws Exception {
    TestBundle testBundle = new TestBundle();
    try {
      testBundle.setFile(
          "resources/exceptions.xml",
          getSourceDQ(
              "<exceptions>",
              "  <exception id='-1000' title='My title'>",
              "  My description {0}",
              "  </exception>",
              "</exceptions>"));
      testBundle.addExtension(
          "org.eclipse.wb.core.exceptions",
          "<file path='resources/exceptions.xml'/>");
      testBundle.install();
      try {
        ErrorEntryInfo entry = DesignerExceptionUtils.getErrorEntry(-1000, "<msg>");
        assertEquals("My description &lt;msg&gt;", entry.getDescription().trim());
      } finally {
        testBundle.uninstall();
      }
    } finally {
      testBundle.dispose();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getErrorDescription(Throwable)
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link DesignerExceptionUtils#getErrorDescription(Throwable)}.
   */
  public void test_getErrorDescription_DesignerException() throws Exception {
    int code = ICoreExceptionConstants.FUTURE;
    Throwable e = new DesignerException(code);
    //
    ErrorEntryInfo entry = DesignerExceptionUtils.getErrorEntry(e);
    assertEquals(code, entry.getCode());
  }

  /**
   * Test for {@link DesignerExceptionUtils#getErrorDescription(Throwable)}.
   */
  public void test_getErrorDescription_genericThrowable() throws Exception {
    Throwable e = new Error("foo");
    ErrorEntryInfo entry = DesignerExceptionUtils.getErrorEntry(e);
    assertEquals(ICoreExceptionConstants.UNEXPECTED, entry.getCode());
    assertEquals("Internal Error", entry.getTitle());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // rewriteException()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link DesignerExceptionUtils#rewriteException(Throwable)}.
   */
  public void test_rewriteException() throws Exception {
    // generic Exception
    {
      Throwable e = new Exception();
      Throwable rewritten = DesignerExceptionUtils.rewriteException(e);
      assertSame(e, rewritten);
    }
    // incomplete installation
    {
      Throwable e = new NoClassDefFoundError("org/eclipse/wb/Foo");
      Throwable rewritten = DesignerExceptionUtils.rewriteException(e);
      assertNotSame(e, rewritten);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // hasTraceElementsSequence()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link DesignerExceptionUtils#hasTraceElementsSequence(Throwable, String[][])}.
   */
  public void test_hasTraceElementsSequence() throws Exception {
    Throwable e = new Exception();
    // no such sequence
    assertFalse(DesignerExceptionUtils.hasTraceElementsSequence(e, new String[][]{
        {"no.such.Class", "noSuchMethod"},
        {"no.Matter", "noMatter"}}));
    // Eclipse launcher sequence
    assertTrue(DesignerExceptionUtils.hasTraceElementsSequence(e, new String[][]{
        {"org.eclipse.equinox.launcher.Main", "run"},
        {"org.eclipse.equinox.launcher.Main", "main"}}));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // getExceptionHTML
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link DesignerExceptionUtils#getExceptionHTML(Throwable)}.
   */
  public void test_getExceptionHTML() throws Exception {
    TestBundle testBundle = new TestBundle();
    try {
      testBundle.setFile(
          "resources/exceptions.xml",
          getSourceDQ(
              "<exceptions>",
              "  <exception id='-1000' title='My title'>",
              "  My description {0} + {1}.",
              "  </exception>",
              "</exceptions>"));
      testBundle.addExtension(
          "org.eclipse.wb.core.exceptions",
          "<file path='resources/exceptions.xml'/>");
      testBundle.install();
      try {
        Throwable e = new DesignerException(-1000, "AAA", "BBB");
        String html = DesignerExceptionUtils.getExceptionHTML(e);
        assertThat(html).contains("My description AAA + BBB.");
        assertThat(html).contains("javascript:toggleVisibleAll()");
        assertThat(html).contains("javascript:toggleVisibleAll()");
        assertThat(html).contains("Show stack trace.");
        assertThat(html).contains("Hide stack trace.");
        assertThat(html).contains(
            "at org.eclipse.wb.tests.designer.core.util.DesignerExceptionUtilsTest.test_getExceptionHTML");
      } finally {
        testBundle.uninstall();
      }
    } finally {
      testBundle.dispose();
    }
  }
}
