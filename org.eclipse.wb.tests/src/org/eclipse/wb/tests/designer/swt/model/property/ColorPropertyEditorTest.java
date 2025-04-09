/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
package org.eclipse.wb.tests.designer.swt.model.property;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.rcp.ToolkitProvider;
import org.eclipse.wb.internal.swt.model.property.editor.color.ColorPropertyEditor;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.internal.swt.preferences.IPreferenceConstants;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

import org.junit.After;

/**
 * Tests for {@link ColorPropertyEditor}.
 *
 * @author scheglov_ke
 */
public abstract class ColorPropertyEditorTest extends RcpModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@After
	public void tearDown() throws Exception {
		super.tearDown();
		ToolkitProvider.DESCRIPTION.getPreferences().setToDefault(
				IPreferenceConstants.P_USE_RESOURCE_MANAGER);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Checks the results of {@link ColorPropertyEditor#getText()} and
	 * {@link ColorPropertyEditor#getClipboardSource()} when color is set using given source.
	 */
	protected final void assert_getText_getClipboardSource_forSource(String colorSource,
			String expectedText,
			String expectedClipboardSource) throws Exception {
		CompositeInfo shell =
				parseComposite(
						"// filler filler filler",
						"public class Test extends Shell {",
						"  public Test() {",
						"    setBackground(" + colorSource + ");",
						"  }",
						"}");
		shell.refresh();
		Property property = shell.getPropertyByTitle("background");
		assertEquals(expectedText, PropertyEditorTestUtils.getText(property));
		assertEquals(expectedClipboardSource, PropertyEditorTestUtils.getClipboardSource(property));
	}
}