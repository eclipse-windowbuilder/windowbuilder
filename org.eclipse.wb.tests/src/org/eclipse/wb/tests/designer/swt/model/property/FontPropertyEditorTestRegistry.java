/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
import org.eclipse.wb.internal.swt.model.jface.resource.FontRegistryInfo;
import org.eclipse.wb.internal.swt.model.jface.resource.KeyFieldInfo;
import org.eclipse.wb.internal.swt.model.jface.resource.RegistryContainerInfo;
import org.eclipse.wb.internal.swt.model.property.editor.font.FontPropertyEditor;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.preferences.IPreferenceConstants;

import org.junit.Test;

import java.util.List;

/**
 * Tests for {@link FontPropertyEditor} with JFace <code>FontRegistry</code>.
 *
 * @author lobas_av
 */
public class FontPropertyEditorTestRegistry extends FontPropertyEditorTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_info() throws Exception {
		createASTCompilationUnit(
				"test",
				"MyRegistry.java",
				getTestSource(
						"public class MyRegistry extends FontRegistry {",
						"  public static final String R_KEY = '_r_key_';",
						"  public MyRegistry() {",
						"    put(R_KEY, new FontData[]{new FontData('Courier New', 14, SWT.NONE)});",
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
		List<FontRegistryInfo> fonts = containerInfo.getChildren(FontRegistryInfo.class);
		assertEquals(1, fonts.size());
		//
		FontRegistryInfo fontRegistryInfo = fonts.get(0);
		List<KeyFieldInfo> keyFields = fontRegistryInfo.getKeyFields();
		assertEquals(1, keyFields.size());
		//
		KeyFieldInfo keyFieldInfo = keyFields.get(0);
		assertEquals("R_KEY", keyFieldInfo.keyName);
		assertEquals("_r_key_", keyFieldInfo.keyValue);
		assertNull(keyFieldInfo.value);
	}

	@Test
	public void test_value() throws Exception {
		createASTCompilationUnit(
				"test",
				"MyRegistry.java",
				getTestSource(
						"public class MyRegistry extends FontRegistry {",
						"  public static final String R_KEY = '_r_key_';",
						"  public MyRegistry() {",
						"    put(R_KEY, new FontData[]{new FontData('Courier New', 14, SWT.NONE)});",
						"  }",
						"}"));
		waitForAutoBuild();
		//
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  private MyRegistry registry = new MyRegistry();",
						"  public Test() {",
						"    setFont(registry.get(MyRegistry.R_KEY));",
						"  }",
						"}");
		shell.refresh();
		assertNoErrors(shell);
		//
		Property property = shell.getPropertyByTitle("font");
		assertEquals("registry - R_KEY", PropertyEditorTestUtils.getText(property));
		//
		ToolkitProvider.DESCRIPTION.getPreferences().setValue(
				IPreferenceConstants.P_USE_RESOURCE_MANAGER,
				false);
		assertEquals(
				"new org.eclipse.swt.graphics.Font(null, \"Courier New\", 14, org.eclipse.swt.SWT.NORMAL)",
				PropertyEditorTestUtils.getClipboardSource(property));
		//
		ToolkitProvider.DESCRIPTION.getPreferences().setValue(
				IPreferenceConstants.P_USE_RESOURCE_MANAGER,
				true);
		assertEquals(
				FontPropertyEditor.getInvocationSource(shell, "Courier New", 14, "org.eclipse.swt.SWT.NORMAL"),
				PropertyEditorTestUtils.getClipboardSource(property));
	}

	@Test
	public void test_value_bold() throws Exception {
		createASTCompilationUnit(
				"test",
				"MyRegistry.java",
				getTestSource(
						"public class MyRegistry extends FontRegistry {",
						"  public static final String R_KEY = '_r_key_';",
						"  public MyRegistry() {",
						"    put(R_KEY, new FontData[]{new FontData('Courier New', 14, SWT.NONE)});",
						"  }",
						"}"));
		waitForAutoBuild();
		//
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  private MyRegistry registry = new MyRegistry();",
						"  public Test() {",
						"    setFont(registry.getBold(MyRegistry.R_KEY));",
						"  }",
						"}");
		shell.refresh();
		assertNoErrors(shell);
		//
		Property property = shell.getPropertyByTitle("font");
		assertEquals("registry - R_KEY(b)", PropertyEditorTestUtils.getText(property));
		//
		ToolkitProvider.DESCRIPTION.getPreferences().setValue(
				IPreferenceConstants.P_USE_RESOURCE_MANAGER,
				false);
		assertEquals(
				"new org.eclipse.swt.graphics.Font(null, \"Courier New\", 14, org.eclipse.swt.SWT.BOLD)",
				PropertyEditorTestUtils.getClipboardSource(property));
	}

	@Test
	public void test_value_italic() throws Exception {
		createASTCompilationUnit(
				"test",
				"MyRegistry.java",
				getTestSource(
						"public class MyRegistry extends FontRegistry {",
						"  public static final String R_KEY = '_r_key_';",
						"  public MyRegistry() {",
						"    put(R_KEY, new FontData[]{new FontData('Courier New', 14, SWT.NONE)});",
						"  }",
						"}"));
		waitForAutoBuild();
		//
		CompositeInfo shell =
				parseComposite(
						"public class Test extends Shell {",
						"  private MyRegistry registry = new MyRegistry();",
						"  public Test() {",
						"    setFont(registry.getItalic(MyRegistry.R_KEY));",
						"  }",
						"}");
		shell.refresh();
		assertNoErrors(shell);
		//
		Property property = shell.getPropertyByTitle("font");
		assertEquals("registry - R_KEY(i)", PropertyEditorTestUtils.getText(property));
		//
		ToolkitProvider.DESCRIPTION.getPreferences().setValue(
				IPreferenceConstants.P_USE_RESOURCE_MANAGER,
				false);
		assertEquals(
				"new org.eclipse.swt.graphics.Font(null, \"Courier New\", 14, org.eclipse.swt.SWT.ITALIC)",
				PropertyEditorTestUtils.getClipboardSource(property));
	}
}