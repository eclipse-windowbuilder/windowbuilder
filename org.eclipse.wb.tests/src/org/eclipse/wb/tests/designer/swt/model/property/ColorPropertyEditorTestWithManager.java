/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
import org.eclipse.wb.internal.rcp.ToolkitProvider;
import org.eclipse.wb.internal.swt.model.jface.resource.ManagerContainerInfo;
import org.eclipse.wb.internal.swt.model.property.editor.color.ColorPropertyEditor;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.preferences.IPreferenceConstants;
import org.eclipse.wb.tests.designer.tests.common.GenericPropertyNoValue;

import org.eclipse.jface.resource.LocalResourceManager;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests for {@link ColorPropertyEditor} with {@link LocalResourceManager}.
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
	@Before
	public void setUp() throws Exception {
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
	@Test
	public void test_textSource_noValue() throws Exception {
		GenericProperty property = new GenericPropertyNoValue(null, null, ColorPropertyEditor.INSTANCE);
		assertNull(PropertyEditorTestUtils.getText(property));
		assertNull(PropertyEditorTestUtils.getClipboardSource(property));
	}

	/**
	 * Color creation using constructor with separate <code>int</code> values.
	 */
	@Test
	public void test_getText_constructor_ints() throws Exception {
		assert_getText_getClipboardSource_forSource(
				"new Color(null, 1, 2, 3)",
				"1, 2, 3",
				ColorPropertyEditor.getInvocationSource(shell(), 1, 2, 3));
	}

	/**
	 * Color creation using constructor with RGB argument.
	 */
	@Test
	public void test_getText_constructor_RGB() throws Exception {
		assert_getText_getClipboardSource_forSource(
				"new Color(null, new RGB(1, 2, 3))",
				"1, 2, 3",
				ColorPropertyEditor.getInvocationSource(shell(), 1, 2, 3));
	}

	private CompositeInfo shell() throws Exception {
		return parseComposite("public class Test extends Shell {}");
	}

	/**
	 * The call to setBackground() must occur AFTER the resource manager was
	 * created.
	 */
	@Test
	public void test_textSource_order() throws Exception {
		CompositeInfo shell = parseComposite(
				"// filler filler filler",
				"public class Test extends Shell {",
				"  public Test() {",
				"  }",
				"}");
		ManagerContainerInfo.getResourceManagerInfo(shell);
		shell.addMethodInvocation("setBackground(org.eclipse.swt.graphics.Color)",
				ColorPropertyEditor.getInvocationSource(shell, 1, 2, 3));
		shell.refresh();
		assertEditor(
				"// filler filler filler",
				"public class Test extends Shell {",
				"  private LocalResourceManager localResourceManager;",
				"  public Test() {",
				"    createResourceManager();",
				"    setBackground(localResourceManager.create(ColorDescriptor.createFrom(new RGB(1, 2, 3))));",
				"  }",
				"  private void createResourceManager() {",
				"    localResourceManager = new LocalResourceManager(JFaceResources.getResources(),this);",
				"  }",
				"}");
	}
}