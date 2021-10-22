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
import org.eclipse.wb.internal.swt.model.property.editor.font.FontPropertyEditor;
import org.eclipse.wb.internal.swt.preferences.IPreferenceConstants;
import org.eclipse.wb.tests.designer.tests.common.GenericPropertyNoValue;

/**
 * Tests for {@link FontPropertyEditor} without <code>SWTResourceManager</code>.
 *
 * @author lobas_av
 */
public class FontPropertyEditorTestNoManager extends FontPropertyEditorTest {
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
        false);
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
    Property property = new GenericPropertyNoValue(null, null, FontPropertyEditor.INSTANCE);
    assertNull(PropertyEditorTestUtils.getText(property));
    assertNull(PropertyEditorTestUtils.getClipboardSource(property));
  }

  public void test_textSource_constructor_NORMAL() throws Exception {
    assert_getText_getClipboardSource_forSource(
        "new Font(null, \"MS Shell Dlg\", 12, SWT.NORMAL)",
        "MS Shell Dlg 12",
        "new org.eclipse.swt.graphics.Font(null, \"MS Shell Dlg\", 12, org.eclipse.swt.SWT.NORMAL)");
  }

  public void test_textSource_constructor_BOLD() throws Exception {
    assert_getText_getClipboardSource_forSource(
        "new Font(null, \"MS Shell Dlg\", 12, SWT.BOLD)",
        "MS Shell Dlg 12 BOLD",
        "new org.eclipse.swt.graphics.Font(null, \"MS Shell Dlg\", 12, org.eclipse.swt.SWT.BOLD)");
  }

  public void test_textSource_constructor_ITALIC() throws Exception {
    assert_getText_getClipboardSource_forSource(
        "new Font(null, \"MS Shell Dlg\", 12, SWT.ITALIC)",
        "MS Shell Dlg 12 ITALIC",
        "new org.eclipse.swt.graphics.Font(null, \"MS Shell Dlg\", 12, org.eclipse.swt.SWT.ITALIC)");
  }

  public void test_textSource_constructor_BOLD_ITALIC() throws Exception {
    assert_getText_getClipboardSource_forSource(
        "new Font(null, \"MS Shell Dlg\", 12, SWT.BOLD | SWT.ITALIC)",
        "MS Shell Dlg 12 BOLD ITALIC",
        "new org.eclipse.swt.graphics.Font(null, \"MS Shell Dlg\", 12, org.eclipse.swt.SWT.BOLD | org.eclipse.swt.SWT.ITALIC)");
  }

  /**
   * Font creation using JFace resource <code>JFaceResources.getXXXFont()</code>.
   */
  public void test_textSource_over_JFace() throws Exception {
    assert_getText_getClipboardSource_forSource(
        "JFaceResources.getBannerFont()",
        "getBannerFont()",
        "org.eclipse.jface.resource.JFaceResources.getBannerFont()");
  }
}