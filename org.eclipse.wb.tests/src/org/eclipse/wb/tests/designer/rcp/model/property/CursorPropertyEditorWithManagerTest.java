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
package org.eclipse.wb.tests.designer.rcp.model.property;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.rcp.ToolkitProvider;
import org.eclipse.wb.internal.rcp.model.property.CursorPropertyEditor;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.swt.utils.ManagerUtils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Tests for {@link CursorPropertyEditor} with <code>SWTResourceManager</code>.
 *
 * @author scheglov_ke
 */
public class CursorPropertyEditorWithManagerTest extends CursorPropertyEditorTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		ToolkitProvider.DESCRIPTION.getPreferences().setValue(
				IPreferenceConstants.P_USE_RESOURCE_MANAGER,
				true);
	}

	@Override
	@After
	public void tearDown() throws Exception {
		super.tearDown();
		do_projectDispose();
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
	@Test
	public void test_null() throws Exception {
		assert_getText_getClipboardSource_forSource("null", null, null);
	}

	@Test
	public void test_useConstructor() throws Exception {
		assert_getText_getClipboardSource_forSource(
				"new Cursor(null, SWT.CURSOR_WAIT)",
				"CURSOR_WAIT",
				"org.eclipse.wb.swt.SWTResourceManager.getCursor(org.eclipse.swt.SWT.CURSOR_WAIT)");
	}

	@Test
	public void test_useConstructor_notStyle() throws Exception {
		assert_getText_getClipboardSource_forSource(
				"new Cursor(null, 1/*SWT.CURSOR_WAIT*/)",
				null,
				null);
	}

	@Test
	public void test_useSWTResourceManager() throws Exception {
		assert_getText_getClipboardSource_forSource(
				"org.eclipse.wb.swt.SWTResourceManager.getCursor(org.eclipse.swt.SWT.CURSOR_WAIT)",
				"CURSOR_WAIT",
				"org.eclipse.wb.swt.SWTResourceManager.getCursor(org.eclipse.swt.SWT.CURSOR_WAIT)");
	}

	@Test
	public void test_combo() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"// filler filler filler",
						"public class Test extends Shell {",
						"  public Test() {",
						"  }",
						"}");
		// add SWTResourceManager
		ManagerUtils.ensure_SWTResourceManager(shell);
		// set "cursor" property
		shell.addMethodInvocation(
				"setCursor(org.eclipse.swt.graphics.Cursor)",
				"org.eclipse.wb.swt.SWTResourceManager.getCursor(org.eclipse.swt.SWT.CURSOR_CROSS)");
		assertEditor(
				"import org.eclipse.wb.swt.SWTResourceManager;",
				"// filler filler filler",
				"public class Test extends Shell {",
				"  public Test() {",
				"    setCursor(SWTResourceManager.getCursor(SWT.CURSOR_CROSS));",
				"  }",
				"}");
		//
		shell.refresh();
		Property property = shell.getPropertyByTitle("cursor");
		//
		addComboPropertyItems(property);
		// check items
		{
			List<String> items = getComboPropertyItems();
			assertEquals("CURSOR_ARROW", items.get(0));
			assertEquals("CURSOR_WAIT", items.get(1));
			assertEquals("CURSOR_CROSS", items.get(2));
			assertEquals("CURSOR_HELP", items.get(4));
		}
		// select current item
		{
			setComboPropertySelection(1);
			setComboPropertySelection(property);
			assertEquals(2, getComboPropertySelection());
		}
		// set new item
		{
			setComboPropertyValue(property, 4);
			assertEditor(
					"import org.eclipse.wb.swt.SWTResourceManager;",
					"// filler filler filler",
					"public class Test extends Shell {",
					"  public Test() {",
					"    setCursor(SWTResourceManager.getCursor(SWT.CURSOR_HELP));",
					"  }",
					"}");
		}
	}

	/**
	 * We should ensure <code>SWTResourceManager</code> when generate code for <code>Cursor</code>.
	 */
	@Test
	public void test_setValue_ensureManager() throws Exception {
		CompositeInfo shell =
				parseComposite(
						"// filler filler filler",
						"public class Test extends Shell {",
						"  public Test() {",
						"  }",
						"}");
		shell.refresh();
		// prepare property
		Property property = shell.getPropertyByTitle("cursor");
		PropertyEditor propertyEditor = property.getEditor();
		// no SWTResourceManager initially
		assertNull(m_javaProject.findType("org.eclipse.wb.swt.SWTResourceManager"));
		// set cursor
		ReflectionUtils.invokeMethod(propertyEditor, "toPropertyEx("
				+ "org.eclipse.wb.internal.core.model.property.Property,"
				+ "org.eclipse.wb.core.controls.CCombo3,"
				+ "int)", property, null, 0);
		assertEditor(
				"import org.eclipse.wb.swt.SWTResourceManager;",
				"// filler filler filler",
				"public class Test extends Shell {",
				"  public Test() {",
				"    setCursor(SWTResourceManager.getCursor(SWT.CURSOR_ARROW));",
				"  }",
				"}");
	}
}