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
import org.eclipse.wb.internal.swt.model.jface.resource.ColorRegistryInfo;
import org.eclipse.wb.internal.swt.model.jface.resource.KeyFieldInfo;
import org.eclipse.wb.internal.swt.model.jface.resource.RegistryContainerInfo;
import org.eclipse.wb.internal.swt.model.property.editor.color.ColorPropertyEditor;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.preferences.IPreferenceConstants;

import java.util.List;

/**
 * Tests for {@link ColorPropertyEditor} with JFace <code>ColorRegistry</code>.
 *
 * @author lobas_av
 */
public class ColorPropertyEditorTestRegistry extends ColorPropertyEditorTest {
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
  public void test_info() throws Exception {
    createASTCompilationUnit(
        "test",
        "MyRegistry.java",
        getTestSource(
            "public class MyRegistry extends ColorRegistry {",
            "  public static final String R_KEY = '_r_key_';",
            "  public MyRegistry() {",
            "    put(R_KEY, new RGB(10, 10, 10));",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  private MyRegistry registry = new MyRegistry();",
            "  public Test() {",
            "  }",
            "}");
    shell.refresh();
    //
    List<RegistryContainerInfo> children = shell.getChildren(RegistryContainerInfo.class);
    assertEquals(1, children.size());
    RegistryContainerInfo containerInfo = children.get(0);
    //
    assertSame(containerInfo, RegistryContainerInfo.get(shell));
    //
    List<ColorRegistryInfo> colors = containerInfo.getChildren(ColorRegistryInfo.class);
    assertEquals(1, colors.size());
    //
    ColorRegistryInfo colorRegistryInfo = colors.get(0);
    List<KeyFieldInfo> keyFields = colorRegistryInfo.getKeyFields();
    assertEquals(1, keyFields.size());
    //
    KeyFieldInfo keyFieldInfo = keyFields.get(0);
    assertEquals("R_KEY", keyFieldInfo.keyName);
    assertEquals("_r_key_", keyFieldInfo.keyValue);
    assertNull(keyFieldInfo.value);
  }

  public void test_value() throws Exception {
    createASTCompilationUnit(
        "test",
        "MyRegistry.java",
        getTestSource(
            "public class MyRegistry extends ColorRegistry {",
            "  public static final String R_KEY = '_r_key_';",
            "  public MyRegistry() {",
            "    put(R_KEY, new RGB(10, 10, 10));",
            "  }",
            "}"));
    waitForAutoBuild();
    //
    CompositeInfo shell =
        parseComposite(
            "public class Test extends Shell {",
            "  private MyRegistry registry = new MyRegistry();",
            "  public Test() {",
            "    setBackground(registry.get(MyRegistry.R_KEY));",
            "  }",
            "}");
    shell.refresh();
    //
    Property property = shell.getPropertyByTitle("background");
    assertEquals("registry - R_KEY", PropertyEditorTestUtils.getText(property));
    //
    ToolkitProvider.DESCRIPTION.getPreferences().setValue(
        IPreferenceConstants.P_USE_RESOURCE_MANAGER,
        false);
    assertEquals(
        "new org.eclipse.swt.graphics.Color(null, 10, 10, 10)",
        PropertyEditorTestUtils.getClipboardSource(property));
    //
    ToolkitProvider.DESCRIPTION.getPreferences().setValue(
        IPreferenceConstants.P_USE_RESOURCE_MANAGER,
        true);
    assertEquals(
        "org.eclipse.wb.swt.SWTResourceManager.getColor(10, 10, 10)",
        PropertyEditorTestUtils.getClipboardSource(property));
  }
}