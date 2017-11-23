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
package org.eclipse.wb.tests.designer.swing.laf;

import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.parser.JavaInfoParser;
import org.eclipse.wb.internal.swing.laf.LafSupport;
import org.eclipse.wb.internal.swing.laf.model.CategoryInfo;
import org.eclipse.wb.internal.swing.laf.model.LafInfo;
import org.eclipse.wb.internal.swing.laf.model.SeparatorLafInfo;
import org.eclipse.wb.internal.swing.laf.model.SystemLafInfo;
import org.eclipse.wb.internal.swing.laf.model.UndefinedLafInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.core.TestBundle;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.plaf.metal.MetalLookAndFeel;

/**
 * Tests for Swing LookAndFeel support.
 * 
 * @author mitin_aa
 */
public class LookAndFeelTest extends SwingModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void tearDown() throws Exception {
    UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
    if (m_lastParseInfo != null) {
      LafInfo undefinedLAF = UndefinedLafInfo.INSTANCE;
      LafSupport.selectLAF(m_lastParseInfo, undefinedLAF);
    }
    super.tearDown();
  }

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
   * Test for finding main() method in primary type of compilation unit.
   */
  public void test_getMainMethod() throws Exception {
    parseContainer(
        "class Test {",
        "  public static void main(String[] args) {",
        "    JPanel panel = new JPanel();",
        "  }",
        "}");
    MethodDeclaration mainMethod = LafSupport.getMainMethod(m_lastEditor);
    assertNotNull(mainMethod);
  }

  /**
   * Test for finding main() method in primary type of compilation unit when no main() method.
   */
  public void test_getSetLookAndFeel_no() throws Exception {
    parseContainer(
        "class Test {",
        "  public static void main(String[] args) {",
        "    JPanel panel = new JPanel();",
        "  }",
        "}");
    MethodDeclaration mainMethod = LafSupport.getMainMethod(m_lastEditor);
    MethodInvocation setLookAndFeelMethod = LafSupport.getSetLookAndFeelMethod(mainMethod);
    assertNull(setLookAndFeelMethod);
  }

  /**
   * Test for finding UIManager.setLookAndFeel() when it has {@link String} argument.
   */
  public void test_getSetLookAndFeel_String() throws Exception {
    parseContainer(
        "class Test {",
        "  public static void main(String[] args) {",
        "    try {",
        "      UIManager.setLookAndFeel('com.sun.java.swing.plaf.motif.MotifLookAndFeel');",
        "      JPanel panel = new JPanel();",
        "    } catch (Throwable e) {",
        "    }",
        "  }",
        "}");
    MethodDeclaration mainMethod = LafSupport.getMainMethod(m_lastEditor);
    MethodInvocation setLookAndFeelMethod = LafSupport.getSetLookAndFeelMethod(mainMethod);
    assertNotNull(setLookAndFeelMethod);
  }

  /**
   * Test for finding UIManager.setLookAndFeel() when it has {@link LookAndFeel} argument.
   */
  public void test_getSetLookAndFeel_LookAndFeel() throws Exception {
    parseContainer(
        "class Test {",
        "  public static void main(String[] args) {",
        "    try {",
        "      UIManager.setLookAndFeel(new com.sun.java.swing.plaf.motif.MotifLookAndFeel());",
        "      JPanel panel = new JPanel();",
        "    } catch (Throwable e) {",
        "    }",
        "  }",
        "}");
    MethodDeclaration mainMethod = LafSupport.getMainMethod(m_lastEditor);
    MethodInvocation setLookAndFeelMethod = LafSupport.getSetLookAndFeelMethod(mainMethod);
    assertNotNull(setLookAndFeelMethod);
  }

  /**
   * Test for adding UIManager.setLookAndFeel(String) when no setLookAndFeel method found.
   */
  public void test_addSetLookAndFeel() throws Exception {
    parseContainer(
        "class Test {",
        "  public static void main(String[] args) {",
        "    try {",
        "      JPanel panel = new JPanel();",
        "    } catch (Throwable e) {",
        "    }",
        "  }",
        "}");
    LafInfo lafInfo = new LafInfo("Metal", "Metal", MetalLookAndFeel.class.getName());
    lafInfo.applyInMain(m_lastEditor);
    assertEditor(
        "class Test {",
        "  public static void main(String[] args) {",
        "    try {",
        "      UIManager.setLookAndFeel('javax.swing.plaf.metal.MetalLookAndFeel');",
        "    } catch (Throwable e) {",
        "      e.printStackTrace();",
        "    }",
        "    try {",
        "      JPanel panel = new JPanel();",
        "    } catch (Throwable e) {",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for modifying UIManager.setLookAndFeel() when this method with {@link String} argument
   * found.
   */
  public void test_modifySetLookAndFeel_String() throws Exception {
    parseContainer(
        "class Test {",
        "  public static void main(String[] args) {",
        "    try {",
        "      UIManager.setLookAndFeel('com.sun.java.swing.plaf.motif.MotifLookAndFeel');",
        "      JPanel panel = new JPanel();",
        "    } catch (Throwable e) {",
        "    }",
        "  }",
        "}");
    LafInfo lafInfo = new LafInfo("Metal", "Metal", MetalLookAndFeel.class.getName());
    lafInfo.applyInMain(m_lastEditor);
    assertEditor(
        "class Test {",
        "  public static void main(String[] args) {",
        "    try {",
        "      UIManager.setLookAndFeel('javax.swing.plaf.metal.MetalLookAndFeel');",
        "      JPanel panel = new JPanel();",
        "    } catch (Throwable e) {",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for modifying UIManager.setLookAndFeel() when this method with {@link LookAndFeel}
   * argument found.
   */
  public void test_modifySetLookAndFeel_LookAndFeel() throws Exception {
    parseContainer(
        "class Test {",
        "  public static void main(String[] args) {",
        "    try {",
        "      UIManager.setLookAndFeel(new com.sun.java.swing.plaf.motif.MotifLookAndFeel());",
        "      JPanel panel = new JPanel();",
        "    } catch (Throwable e) {",
        "    }",
        "  }",
        "}");
    LafInfo lafInfo = new LafInfo("Metal", "Metal", MetalLookAndFeel.class.getName());
    lafInfo.applyInMain(m_lastEditor);
    assertEditor(
        "import javax.swing.plaf.metal.MetalLookAndFeel;",
        "class Test {",
        "  public static void main(String[] args) {",
        "    try {",
        "      UIManager.setLookAndFeel(new MetalLookAndFeel());",
        "      JPanel panel = new JPanel();",
        "    } catch (Throwable e) {",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for adding UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()) when no
   * setLookAndFeel method invocation found and "&lt;system&gt;" look-and-feel selected.
   */
  public void test_addSetSystemLookAndFeel() throws Exception {
    parseContainer(
        "class Test {",
        "  public static void main(String[] args) {",
        "    try {",
        "      JPanel panel = new JPanel();",
        "    } catch (Throwable e) {",
        "    }",
        "  }",
        "}");
    LafInfo lafInfo = SystemLafInfo.INSTANCE;
    lafInfo.applyInMain(m_lastEditor);
    assertEditor(
        "class Test {",
        "  public static void main(String[] args) {",
        "    try {",
        "      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());",
        "    } catch (Throwable e) {",
        "      e.printStackTrace();",
        "    }",
        "    try {",
        "      JPanel panel = new JPanel();",
        "    } catch (Throwable e) {",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for modifying current UIManager.setLookAndFeel(String) method invocation to
   * UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()) when "&lt;system&gt;"
   * look-and-feel selected.
   */
  public void test_modifySetSystemLookAndFeel_String() throws Exception {
    parseContainer(
        "class Test {",
        "  public static void main(String[] args) {",
        "    try {",
        "      UIManager.setLookAndFeel('com.sun.java.swing.plaf.motif.MotifLookAndFeel');",
        "      JPanel panel = new JPanel();",
        "    } catch (Throwable e) {",
        "    }",
        "  }",
        "}");
    LafInfo lafInfo = SystemLafInfo.INSTANCE;
    lafInfo.applyInMain(m_lastEditor);
    assertEditor(
        "class Test {",
        "  public static void main(String[] args) {",
        "    try {",
        "      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());",
        "      JPanel panel = new JPanel();",
        "    } catch (Throwable e) {",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for modifying current UIManager.setLookAndFeel(LookAndFeel) method invocation to
   * UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()) when "&lt;system&gt;"
   * look-and-feel selected.
   */
  public void test_modifySetSystemLookAndFeel_LookAndFeel() throws Exception {
    parseContainer(
        "class Test {",
        "  public static void main(String[] args) {",
        "    try {",
        "      UIManager.setLookAndFeel(new com.sun.java.swing.plaf.motif.MotifLookAndFeel());",
        "      JPanel panel = new JPanel();",
        "    } catch (Throwable e) {",
        "    }",
        "  }",
        "}");
    LafInfo lafInfo = SystemLafInfo.INSTANCE;
    lafInfo.applyInMain(m_lastEditor);
    assertEditor(
        "class Test {",
        "  public static void main(String[] args) {",
        "    try {",
        "      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());",
        "      JPanel panel = new JPanel();",
        "    } catch (Throwable e) {",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for removing UIManager.setLookAndFeel() method invocation when "&lt;undefined&gt;"
   * look-and-feel selected and try statement body is not empty.
   */
  public void test_removeSetSystemLookAndFeel_without_try() throws Exception {
    parseContainer(
        "class Test {",
        "  public static void main(String[] args) {",
        "    try {",
        "      UIManager.setLookAndFeel(new com.sun.java.swing.plaf.motif.MotifLookAndFeel());",
        "      JPanel panel = new JPanel();",
        "    } catch (Throwable e) {",
        "    }",
        "  }",
        "}");
    LafInfo lafInfo = UndefinedLafInfo.INSTANCE;
    lafInfo.applyInMain(m_lastEditor);
    assertEditor(
        "class Test {",
        "  public static void main(String[] args) {",
        "    try {",
        "      JPanel panel = new JPanel();",
        "    } catch (Throwable e) {",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test for removing UIManager.setLookAndFeel() method invocation when "&lt;undefined&gt;"
   * look-and-feel selected and try statement body is empty.
   */
  public void test_removeSetSystemLookAndFeel_with_try() throws Exception {
    parseContainer(
        "class Test {",
        "  public static void main(String[] args) {",
        "    try {",
        "      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());",
        "    } catch (Throwable e) {",
        "      e.printStackTrace();",
        "    } finally {",
        "      System.out.println();",
        "    }",
        "    try {",
        "      JPanel panel = new JPanel();",
        "    } catch (Throwable ex) {",
        "    }",
        "  }",
        "}");
    LafInfo lafInfo = UndefinedLafInfo.INSTANCE;
    lafInfo.applyInMain(m_lastEditor);
    assertEditor(
        "class Test {",
        "  public static void main(String[] args) {",
        "    try {",
        "      JPanel panel = new JPanel();",
        "    } catch (Throwable ex) {",
        "    }",
        "  }",
        "}");
  }

  /**
   * Test fetching LAF list.
   */
  public void test_getLAFList() throws Exception {
    List<CategoryInfo> categoryList = LafSupport.getLAFCategoriesList();
    assertNotNull(categoryList);
    assertFalse(categoryList.isEmpty());
    // check LAF classes
    for (CategoryInfo categoryInfo : categoryList) {
      List<LafInfo> lafList = categoryInfo.getLAFList();
      for (LafInfo lafInfo : lafList) {
        if (!(lafInfo instanceof SeparatorLafInfo)) {
          assertNotNull(lafInfo.getLookAndFeelInstance());
        }
      }
    }
  }

  /**
   * Tests 'condition' attribute for LaFs register.
   */
  public void test_getLAFList_condition() throws Exception {
    TestBundle testBundle = new TestBundle();
    try {
      testBundle.addExtension(
          "org.eclipse.wb.swing.lookAndFeel",
          new String[]{
              "<category id='test' name='Test'>",
              "  <LookAndFeel id='test1' class='Test1LookAndFeel' jarFile='' name='Test1' condition='true'/>",
              "  <LookAndFeel id='test2' class='Test2LookAndFeel' jarFile='' name='Test2' condition='false'/>",
              "</category>"});
      testBundle.install();
      LafSupport.reloadLAFList();
      // check LAF classes
      boolean laf1Available = false;
      boolean laf2Available = false;
      for (CategoryInfo categoryInfo : LafSupport.getLAFCategoriesList()) {
        for (LafInfo lafInfo : categoryInfo.getLAFList()) {
          laf1Available |= "Test1LookAndFeel".equals(lafInfo.getClassName());
          laf2Available |= "Test2LookAndFeel".equals(lafInfo.getClassName());
        }
      }
      assertTrue(laf1Available);
      assertFalse(laf2Available);
    } finally {
      testBundle.dispose();
    }
  }

  /**
   * Test that &lt;system&gt; LAF set by default.
   */
  public void test_get_selected_LAF_default() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test {",
            "  public static void main(String[] args) {",
            "    JPanel panel = new JPanel();",
            "  }",
            "}");
    LafInfo selectedLAF = LafSupport.getSelectedLAF(panel);
    if (EnvironmentUtils.IS_LINUX) {
      assertThat(selectedLAF.getClassName()).isEqualTo("javax.swing.plaf.metal.MetalLookAndFeel");
    } else {
      assertInstanceOf(SystemLafInfo.class, selectedLAF);
    }
  }

  /**
   * Test changing default LAF to &lt;undefined&gt;.
   */
  public void test_get_selected_changed() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "class Test {",
            "  public static void main(String[] args) {",
            "    JPanel panel = new JPanel();",
            "  }",
            "}");
    // set <undefined> LAF (<system> set by default) 
    LafInfo undefinedLAF = UndefinedLafInfo.INSTANCE;
    LafSupport.selectLAF(panel, undefinedLAF);
    LafInfo selectedLAF = LafSupport.getSelectedLAF(panel);
    assertSame(undefinedLAF, selectedLAF);
  }

  /**
   * Test changing default LAF to &lt;undefined&gt; and reparse.
   */
  public void test_get_selected_changed_reparse() throws Exception {
    String[] source =
        new String[]{
            "class Test {",
            "  public static void main(String[] args) {",
            "    JPanel panel = new JPanel();",
            "  }",
            "}"};
    ContainerInfo panel = parseContainer(source);
    // set <undefined> LAF
    LafInfo undefinedLAF = UndefinedLafInfo.INSTANCE;
    LafSupport.selectLAF(panel, undefinedLAF);
    panel = (ContainerInfo) JavaInfoParser.parse(m_lastEditor.getModelUnit());
    LafInfo selectedLAF = LafSupport.getSelectedLAF(panel);
    assertSame(undefinedLAF, selectedLAF);
  }

  /**
   * Test changing Motif LAF installed in main to &lt;system&gt; and reparse.
   */
  public void test_get_selected_changed_reparse_installed_in_main() throws Exception {
    String[] source =
        new String[]{
            "class Test {",
            "  public static void main(String[] args) {",
            "    try {",
            "      UIManager.setLookAndFeel(\"com.sun.java.swing.plaf.motif.MotifLookAndFeel\");",
            "    } catch (Throwable e) {",
            "    }",
            "    JPanel panel = new JPanel();",
            "  }",
            "}"};
    ContainerInfo panel = parseContainer(source);
    // set <system> LAF
    LafInfo systemLAF = SystemLafInfo.INSTANCE;
    LafSupport.selectLAF(panel, systemLAF);
    // emulate "apply in main" option
    systemLAF.applyInMain(m_lastEditor);
    // reparse
    panel = (ContainerInfo) JavaInfoParser.parse(m_lastEditor.getModelUnit());
    LafInfo selectedLAF = LafSupport.getSelectedLAF(panel);
    assertSame(systemLAF, selectedLAF);
  }

  /**
   * Tests default LAF, it should be &lt;system&gt;
   */
  public void test_get_defaultLAF() throws Exception {
    LafInfo defaultLAF = LafSupport.getDefaultLAF();
    if (EnvironmentUtils.IS_LINUX) {
      assertThat(defaultLAF.getClassName()).isEqualTo("javax.swing.plaf.metal.MetalLookAndFeel");
    } else {
      assertInstanceOf(SystemLafInfo.class, defaultLAF);
    }
  }
}
