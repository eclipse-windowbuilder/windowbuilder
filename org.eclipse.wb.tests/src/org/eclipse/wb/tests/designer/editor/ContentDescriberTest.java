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
package org.eclipse.wb.tests.designer.editor;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.describer.JavaSourceUiDescriber;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.tests.designer.core.AbstractJavaTest;
import org.eclipse.wb.tests.designer.core.TestBundle;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.ui.ide.IDE;

/**
 * Test for {@link JavaSourceUiDescriber}.
 * 
 * @author scheglov_ke
 */
public class ContentDescriberTest extends AbstractJavaTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    DesignerPlugin.getPreferences().setValue(IPreferenceConstants.P_EDITOR_RECOGNIZE_GUI, true);
    if (m_testProject == null) {
      do_projectCreate();
    }
  }

  @Override
  protected void tearDown() throws Exception {
    DesignerPlugin.getPreferences().setToDefault(IPreferenceConstants.P_EDITOR_RECOGNIZE_GUI);
    super.tearDown();
    do_projectDispose();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_notGUI() throws Exception {
    IFile file =
        setFileContentSrc(
            "test/Test.java",
            getSourceDQ("package test;", "public class Test {", "  // filler", "}"));
    waitForContentType();
    assertFalse(isDesignerType(file));
  }

  public void test_disableRecognition() throws Exception {
    DesignerPlugin.getPreferences().setValue(IPreferenceConstants.P_EDITOR_RECOGNIZE_GUI, false);
    IFile file =
        setFileContentSrc(
            "test/Test.java",
            getSourceDQ(
                "package test;",
                "import javax.swing.*;",
                "public class Test extends JPanel {",
                "  // filler",
                "}"));
    waitForContentType();
    assertFalse(isDesignerType(file));
  }

  public void test_useExcludePattern() throws Exception {
    TestBundle testBundle = new TestBundle();
    try {
      testBundle.addExtension(
          "org.eclipse.wb.core.designerContentPatterns",
          new String[]{"<excludePattern>use this string to exclude</excludePattern>"});
      testBundle.install();
      //
      IFile file =
          setFileContentSrc(
              "test/Test.java",
              getSourceDQ(
                  "package test;",
                  "import javax.swing.*;",
                  "public class Test extends JPanel {",
                  "  // use this string to exclude",
                  "}"));
      waitForContentType();
      assertFalse(isDesignerType(file));
    } finally {
      testBundle.dispose();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // AWT/Swing
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_AWT_Applet() throws Exception {
    IFile file =
        setFileContentSrc(
            "test/Test.java",
            getSourceDQ(
                "package test;",
                "import java.applet.Applet;",
                "public class Test extends Applet {",
                "  // filler",
                "}"));
    waitForContentType();
    assertTrue(isDesignerType(file));
  }

  public void test_Swing() throws Exception {
    IFile file =
        setFileContentSrc(
            "test/Test.java",
            getSourceDQ(
                "package test;",
                "import javax.swing.*;",
                "public class Test extends JPanel {",
                "  // filler",
                "}"));
    waitForContentType();
    assertTrue(isDesignerType(file));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // SWT/RCP
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_SWT() throws Exception {
    IFile file =
        setFileContentSrc(
            "test/Test.java",
            getSourceDQ(
                "package test;",
                "public class Test {",
                "  // org.eclipse.swt.widgets.Button",
                "}"));
    waitForContentType();
    assertTrue(isDesignerType(file));
  }

  public void test_RCP_ActionBarAdvisor() throws Exception {
    IFile file =
        setFileContentSrc(
            "test/Test.java",
            getSourceDQ(
                "package test;",
                "public class Test {",
                "  // org.eclipse.ui.application.ActionBarAdvisor",
                "}"));
    waitForContentType();
    assertTrue(isDesignerType(file));
  }

  public void test_RCP_IPerspectiveFactory() throws Exception {
    IFile file =
        setFileContentSrc(
            "test/Test.java",
            getSourceDQ(
                "package test;",
                "public class Test {",
                "  // org.eclipse.ui.IPerspectiveFactory",
                "}"));
    waitForContentType();
    assertTrue(isDesignerType(file));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GWT
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_GWT() throws Exception {
    IFile file =
        setFileContentSrc(
            "test/Test.java",
            getSourceDQ(
                "package test;",
                "public class Test {",
                "  // com.google.gwt.user.client.ui.Button",
                "}"));
    waitForContentType();
    assertTrue(isDesignerType(file));
  }

  public void test_GWTExt() throws Exception {
    IFile file =
        setFileContentSrc(
            "test/Test.java",
            getSourceDQ(
                "package test;",
                "public class Test {",
                "  // com.gwtext.client.widgets.Panel",
                "}"));
    waitForContentType();
    assertTrue(isDesignerType(file));
  }

  public void test_ExtGWT() throws Exception {
    IFile file =
        setFileContentSrc(
            "test/Test.java",
            getSourceDQ(
                "package test;",
                "public class Test {",
                "  // com.extjs.gxt.ui.client.widget.Button",
                "}"));
    waitForContentType();
    assertTrue(isDesignerType(file));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private static boolean isDesignerType(IFile file) {
    IContentType contentType = IDE.getContentType(file);
    return "org.eclipse.wb.core.javaSourceGUI".equals(contentType.getId());
  }

  private void waitForContentType() throws Exception {
    waitEventLoop(1);
    waitForAutoBuild();
  }
}
