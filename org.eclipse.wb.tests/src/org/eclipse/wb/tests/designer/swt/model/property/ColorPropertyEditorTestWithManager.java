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

import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.rcp.ToolkitProvider;
import org.eclipse.wb.internal.swt.model.property.editor.color.ColorPropertyEditor;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.swt.utils.ManagerUtils;
import org.eclipse.wb.tests.designer.tests.common.GenericPropertyNoValue;

/**
 * Tests for {@link ColorPropertyEditor} with <code>SWTResourceManager</code>.
 * 
 * @author scheglov_ke
 */
public class ColorPropertyEditorTestWithManager extends ColorPropertyEditorTest {
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
    GenericProperty property = new GenericPropertyNoValue(null, null, ColorPropertyEditor.INSTANCE);
    assertNull(PropertyEditorTestUtils.getText(property));
    assertNull(PropertyEditorTestUtils.getClipboardSource(property));
  }

  /**
   * System color using "id" - SWT field.
   */
  public void test_textSource_systemConstant() throws Exception {
    assert_getText_getClipboardSource_forSource(
        "Display.getCurrent().getSystemColor(SWT.COLOR_RED)",
        "COLOR_RED",
        "org.eclipse.wb.swt.SWTResourceManager.getColor(org.eclipse.swt.SWT.COLOR_RED)");
  }

  /**
   * System color using "id" - direct number.
   */
  public void test_textSource_systemNumber() throws Exception {
    assert_getText_getClipboardSource_forSource(
        "Display.getCurrent().getSystemColor(3)",
        "COLOR_RED",
        "org.eclipse.wb.swt.SWTResourceManager.getColor(org.eclipse.swt.SWT.COLOR_RED)");
  }

  /**
   * Color creation using constructor with separate <code>int</code> values.
   */
  public void test_getText_constructor_ints() throws Exception {
    assert_getText_getClipboardSource_forSource(
        "new Color(null, 1, 2, 3)",
        "1,2,3",
        "org.eclipse.wb.swt.SWTResourceManager.getColor(1, 2, 3)");
  }

  /**
   * Color creation using constructor with RGB argument.
   */
  public void test_getText_constructor_RGB() throws Exception {
    assert_getText_getClipboardSource_forSource(
        "new Color(null, new RGB(1, 2, 3))",
        "1,2,3",
        "org.eclipse.wb.swt.SWTResourceManager.getColor(1, 2, 3)");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Code with SWTResourceManager
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * System color using "id" - SWT field.
   */
  public void test_textSource_systemConstant2() throws Exception {
    assert_getText_getClipboardSource_forSource2(
        "org.eclipse.wb.swt.SWTResourceManager.getColor(org.eclipse.swt.SWT.COLOR_RED)",
        "COLOR_RED",
        "org.eclipse.wb.swt.SWTResourceManager.getColor(org.eclipse.swt.SWT.COLOR_RED)");
  }

  /**
   * System color using "id" - direct number.
   */
  public void test_textSource_systemNumber2() throws Exception {
    assert_getText_getClipboardSource_forSource2(
        "org.eclipse.wb.swt.SWTResourceManager.getColor(3)",
        "COLOR_RED",
        "org.eclipse.wb.swt.SWTResourceManager.getColor(org.eclipse.swt.SWT.COLOR_RED)");
  }

  /**
   * Color creation using constructor with separate <code>int</code> values.
   */
  public void test_getText_constructor_ints2() throws Exception {
    assert_getText_getClipboardSource_forSource2(
        "org.eclipse.wb.swt.SWTResourceManager.getColor(1, 2, 3)",
        "1,2,3",
        "org.eclipse.wb.swt.SWTResourceManager.getColor(1, 2, 3)");
  }

  /**
   * Color creation using constructor with RGB argument.
   */
  public void test_getText_constructor_RGB2() throws Exception {
    assert_getText_getClipboardSource_forSource2(
        "org.eclipse.wb.swt.SWTResourceManager.getColor(new RGB(1, 2, 3))",
        "1,2,3",
        "org.eclipse.wb.swt.SWTResourceManager.getColor(1, 2, 3)");
  }

  /**
   * Checks the results of {@link ColorPropertyEditor#getText()} and
   * {@link ColorPropertyEditor#getClipboardSource()} when color is set using given source.
   */
  private void assert_getText_getClipboardSource_forSource2(String colorSource,
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
    // set "background" property
    shell.addMethodInvocation("setBackground(org.eclipse.swt.graphics.Color)", colorSource);
    shell.refresh();
    //
    Property property = shell.getPropertyByTitle("background");
    assertEquals(expectedText, PropertyEditorTestUtils.getText(property));
    assertEquals(expectedClipboardSource, PropertyEditorTestUtils.getClipboardSource(property));
  }
}