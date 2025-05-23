/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
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

import org.eclipse.jface.resource.LocalResourceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

/**
 * Tests for {@link CursorPropertyEditor} with {@link LocalResourceManager}.
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
				"org.eclipse.swt.widgets.Display.getCurrent().getSystemCursor(org.eclipse.swt.SWT.CURSOR_WAIT)");
	}

	@Test
	public void test_useConstructor_notStyle() throws Exception {
		assert_getText_getClipboardSource_forSource(
				"new Cursor(null, 1/*SWT.CURSOR_WAIT*/)",
				null,
				null);
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
		// set "cursor" property
		shell.addMethodInvocation(
				"setCursor(org.eclipse.swt.graphics.Cursor)",
				"org.eclipse.swt.widgets.Display.getCurrent().getSystemCursor(org.eclipse.swt.SWT.CURSOR_CROSS)");
		assertEditor(
				"// filler filler filler",
				"public class Test extends Shell {",
				"  public Test() {",
				"    setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_CROSS));",
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
					"// filler filler filler",
					"public class Test extends Shell {",
					"  public Test() {",
					"    setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_HELP));",
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
		// set cursor
		ReflectionUtils.invokeMethod(propertyEditor, "toPropertyEx("
				+ "org.eclipse.wb.internal.core.model.property.Property,"
				+ "org.eclipse.swt.custom.CCombo,"
				+ "int)", property, null, 0);
		assertEditor(
				"// filler filler filler",
				"public class Test extends Shell {",
				"  public Test() {",
				"    setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_ARROW));",
				"  }",
				"}");
	}
}