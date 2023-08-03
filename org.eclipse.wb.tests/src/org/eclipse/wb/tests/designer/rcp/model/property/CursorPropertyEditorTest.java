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
import org.eclipse.wb.internal.rcp.ToolkitProvider;
import org.eclipse.wb.internal.rcp.model.property.CursorPropertyEditor;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.swt.utils.ManagerUtils;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;
import org.eclipse.wb.tests.designer.swt.model.property.PropertyEditorTestUtils;

import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Shell;

import org.junit.After;
import org.junit.Before;

/**
 * Tests for {@link CursorPropertyEditor}.
 *
 * @author scheglov_ke
 */
public abstract class CursorPropertyEditorTest extends RcpModelTest {
	protected Shell m_shell;

	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		if (m_shell == null) {
			m_shell = new Shell();
		}
	}

	@Override
	@After
	public void tearDown() throws Exception {
		super.tearDown();
		ToolkitProvider.DESCRIPTION.getPreferences().setToDefault(
				IPreferenceConstants.P_USE_RESOURCE_MANAGER);
		if (m_shell != null) {
			m_shell.dispose();
			m_shell = null;
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Checks the "text" and "clipboard source" from {@link CursorPropertyEditor}, when {@link Cursor}
	 * is set using given source.
	 */
	protected final void assert_getText_getClipboardSource_forSource(String cursorSource,
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
		// set "cursor" property
		shell.addMethodInvocation("setCursor(org.eclipse.swt.graphics.Cursor)", cursorSource);
		shell.refresh();
		// validate
		Property property = shell.getPropertyByTitle("cursor");
		assertNotNull(property);
		assertEquals(expectedText, PropertyEditorTestUtils.getText(property));
		assertEquals(expectedClipboardSource, PropertyEditorTestUtils.getClipboardSource(property));
	}
}