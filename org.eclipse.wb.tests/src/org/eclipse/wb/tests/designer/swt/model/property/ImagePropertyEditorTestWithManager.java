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
package org.eclipse.wb.tests.designer.swt.model.property;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.rcp.ToolkitProvider;
import org.eclipse.wb.internal.swt.model.property.editor.image.ImagePropertyEditor;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.swt.utils.ManagerUtils;
import org.eclipse.wb.tests.designer.tests.common.GenericPropertyNoValue;

import org.apache.commons.io.FilenameUtils;

import java.io.File;

/**
 * Tests for {@link ImagePropertyEditor} with <code>SWTResourceManager</code>.
 *
 * @author lobas_av
 */
public class ImagePropertyEditorTestWithManager extends ImagePropertyEditorTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    ToolkitProvider.DESCRIPTION.getPreferences().setValue(
        IPreferenceConstants.P_USE_RESOURCE_MANAGER,
        true);
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
  // getText(), getClipboardSource()
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * No value for property.
   */
  public void test_textSource_noValue() throws Exception {
    Property property = new GenericPropertyNoValue(null, null, ImagePropertyEditor.INSTANCE);
    assertNull(PropertyEditorTestUtils.getText(property));
    assertNull(PropertyEditorTestUtils.getClipboardSource(property));
  }

  /**
   * "null" value for property.
   */
  public void test_textSource_nullValue() throws Exception {
    assert_getText_getClipboardSource_forSource("null", "(null)", "null");
  }

  /**
   * Image creation using constructor with absolute file path.
   */
  public void test_textSource_absolutePath() throws Exception {
    File file = createTempImage();
    try {
      String path = FilenameUtils.separatorsToUnix(file.getCanonicalPath());
      assert_getText_getClipboardSource_forSource("new Image(null, \"" + path + "\")", "File: "
          + path, "org.eclipse.wb.swt.SWTResourceManager.getImage(\"" + path + "\")");
    } finally {
      file.delete();
    }
  }

  /**
   * Image creation using constructor with input stream (over class resource).
   */
  public void DISABLED_test_textSource_image_over_classpath() throws Exception {
    assert_getText_getClipboardSource_forSource(
        "new Image(null, getClass().getResourceAsStream(\"/javax/swing/plaf/basic/icons/JavaCup16.png\"))",
        "Classpath: /javax/swing/plaf/basic/icons/JavaCup16.png",
        "org.eclipse.wb.swt.SWTResourceManager.getImage({wbp_classTop}, \"/javax/swing/plaf/basic/icons/JavaCup16.png\")");
  }

  /**
   * Image creation using constructor with input stream (over class resource).
   */
  public void DISABLED_test_textSource_image_over_classpath_OtherClass() throws Exception {
    assert_getText_getClipboardSource_forSource(
        "new Image(null, java.lang.String.class.getResourceAsStream(\"/javax/swing/plaf/basic/icons/JavaCup16.png\"))",
        "Classpath: /javax/swing/plaf/basic/icons/JavaCup16.png",
        "org.eclipse.wb.swt.SWTResourceManager.getImage({wbp_classTop}, \"/javax/swing/plaf/basic/icons/JavaCup16.png\")");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Code with SWTResourceManager
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Image creation using constructor with absolute file path.
   */
  public void test_textSource_absolutePath2() throws Exception {
    File file = createTempImage();
    try {
      String path = FilenameUtils.separatorsToUnix(file.getCanonicalPath());
      assert_getText_getClipboardSource_forSource2("org.eclipse.wb.swt.SWTResourceManager.getImage(\""
          + path
          + "\")", "File: " + path, "org.eclipse.wb.swt.SWTResourceManager.getImage(\"" + path + "\")");
    } finally {
      file.delete();
    }
  }

  /**
   * Image creation using constructor with input stream (over class resource).
   */
  public void test_textSource_image_over_classpath2() throws Exception {
    assert_getText_getClipboardSource_forSource2(
        "org.eclipse.wb.swt.SWTResourceManager.getImage(getClass(), \"/javax/swing/plaf/basic/icons/JavaCup16.png\")",
        "Classpath: /javax/swing/plaf/basic/icons/JavaCup16.png",
        "org.eclipse.wb.swt.SWTResourceManager.getImage({wbp_classTop}, \"/javax/swing/plaf/basic/icons/JavaCup16.png\")");
  }

  /**
   * Image creation using constructor with input stream (over class resource).
   */
  public void test_textSource_image_over_classpath_OtherClass2() throws Exception {
    assert_getText_getClipboardSource_forSource2(
        "org.eclipse.wb.swt.SWTResourceManager.getImage(java.lang.String.class, \"/javax/swing/plaf/basic/icons/JavaCup16.png\")",
        "Classpath: /javax/swing/plaf/basic/icons/JavaCup16.png",
        "org.eclipse.wb.swt.SWTResourceManager.getImage({wbp_classTop}, \"/javax/swing/plaf/basic/icons/JavaCup16.png\")");
  }

  /**
   * Checks the results of {@link ImagePropertyEditor#getText()} and
   * {@link ImagePropertyEditor#getClipboardSource()} when image is set using given source.
   */
  private void assert_getText_getClipboardSource_forSource2(String imageSource,
      String expectedText,
      String expectedClipboardSource) throws Exception {
    CompositeInfo shell =
        parseComposite(
            "// filler filler filler",
            "public class Test extends Shell {",
            "  public Test() {",
            "  }",
            "}");
    // add SWTResourceManager
    ManagerUtils.ensure_SWTResourceManager(shell);
    // set "image" property
    shell.addMethodInvocation("setImage(org.eclipse.swt.graphics.Image)", imageSource);
    shell.refresh();
    //
    Property property = shell.getPropertyByTitle("image");
    assertEquals(expectedText, PropertyEditorTestUtils.getText(property));
    assertEquals(expectedClipboardSource, PropertyEditorTestUtils.getClipboardSource(property));
  }
}