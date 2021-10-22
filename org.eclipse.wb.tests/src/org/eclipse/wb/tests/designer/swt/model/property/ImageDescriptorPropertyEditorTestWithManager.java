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
import org.eclipse.wb.internal.swt.model.property.editor.image.ImageDescriptorPropertyEditor;
import org.eclipse.wb.internal.swt.preferences.IPreferenceConstants;
import org.eclipse.wb.tests.designer.tests.common.GenericPropertyNoValue;

import org.eclipse.jface.resource.ImageDescriptor;

import org.apache.commons.io.FilenameUtils;

import java.io.File;

/**
 * Tests for {@link ImageDescriptorPropertyEditor} with <code>ResourceManager</code>.
 *
 * @author lobas_av
 * @author scheglov_ke
 */
public class ImageDescriptorPropertyEditorTestWithManager extends ImageDescriptorPropertyEditorTest {
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
    Property property =
        new GenericPropertyNoValue(null, null, ImageDescriptorPropertyEditor.INSTANCE);
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
   * Test for {@link ImageDescriptor#createFromFile(Class, String)} with <code>null</code> as
   * location, so absolute path.
   */
  public void test_textSource_absolutePath() throws Exception {
    File file = createTempImage();
    try {
      String path = FilenameUtils.separatorsToUnix(file.getCanonicalPath());
      String source = "ImageDescriptor.createFromFile(null, \"" + path + "\")";
      assert_getText_getClipboardSource_forSource(
          source,
          "File: " + path,
          "org.eclipse.wb.swt.ResourceManager.getImageDescriptor(\"" + path + "\")");
    } finally {
      file.delete();
    }
  }

  /**
   * Test for {@link ImageDescriptor#createFromFile(Class, String)} with this {@link Class} as
   * location.
   */
  public void test_textSource_image_over_classpath() throws Exception {
    assert_getText_getClipboardSource_forSource(
        "ImageDescriptor.createFromFile(getClass(), \"/javax/swing/plaf/basic/icons/JavaCup16.png\")",
        "Classpath: /javax/swing/plaf/basic/icons/JavaCup16.png",
        "org.eclipse.wb.swt.ResourceManager.getImageDescriptor({wbp_classTop}, \"/javax/swing/plaf/basic/icons/JavaCup16.png\")");
  }

  /**
   * Test for {@link ImageDescriptor#createFromFile(Class, String)} with some other {@link Class} as
   * location.
   */
  public void test_textSource_image_over_classpath_OtherClass() throws Exception {
    assert_getText_getClipboardSource_forSource(
        "ImageDescriptor.createFromFile(String.class, \"/javax/swing/plaf/basic/icons/JavaCup16.png\")",
        "Classpath: /javax/swing/plaf/basic/icons/JavaCup16.png",
        "org.eclipse.wb.swt.ResourceManager.getImageDescriptor({wbp_classTop}, \"/javax/swing/plaf/basic/icons/JavaCup16.png\")");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Code with ResourceManager
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for <code>ResourceManager.getImageDescriptor(absolutePath)</code>.
   */
  public void test_textSource_absolutePath2() throws Exception {
    File file = createTempImage();
    try {
      String path = FilenameUtils.separatorsToUnix(file.getCanonicalPath());
      assert_getText_getClipboardSource_forSource(
          "org.eclipse.wb.swt.ResourceManager.getImageDescriptor(\"" + path + "\")",
          "File: " + path,
          "org.eclipse.wb.swt.ResourceManager.getImageDescriptor(\"" + path + "\")");
    } finally {
      file.delete();
    }
  }

  /**
   * Test for <code>ResourceManager.getImageDescriptor(Class, resourcePath)</code>.
   */
  public void test_textSource_image_over_classpath2() throws Exception {
    assert_getText_getClipboardSource_forSource(
        "org.eclipse.wb.swt.ResourceManager.getImageDescriptor(getClass(), \"/javax/swing/plaf/basic/icons/JavaCup16.png\")",
        "Classpath: /javax/swing/plaf/basic/icons/JavaCup16.png",
        "org.eclipse.wb.swt.ResourceManager.getImageDescriptor({wbp_classTop}, \"/javax/swing/plaf/basic/icons/JavaCup16.png\")");
  }

  /**
   * Test for <code>ResourceManager.getImageDescriptor(Class, resourcePath)</code>, some other
   * {@link Class} as location.
   */
  public void test_textSource_image_over_classpath_OtherClass2() throws Exception {
    assert_getText_getClipboardSource_forSource(
        "org.eclipse.wb.swt.ResourceManager.getImageDescriptor(String.class, \"/javax/swing/plaf/basic/icons/JavaCup16.png\")",
        "Classpath: /javax/swing/plaf/basic/icons/JavaCup16.png",
        "org.eclipse.wb.swt.ResourceManager.getImageDescriptor({wbp_classTop}, \"/javax/swing/plaf/basic/icons/JavaCup16.png\")");
  }
}