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
import org.eclipse.wb.internal.swt.model.property.editor.font.FontPropertyEditor;
import org.eclipse.wb.internal.swt.model.widgets.CompositeInfo;
import org.eclipse.wb.tests.designer.rcp.RcpModelTest;

/**
 * Tests for {@link FontPropertyEditor}.
 *
 * @author lobas_av
 */
public abstract class FontPropertyEditorTest extends RcpModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Checks the results of {@link FontPropertyEditor#getText()} and
	 * {@link FontPropertyEditor#getClipboardSource()} when Image is set using given source.
	 */
	protected final void assert_getText_getClipboardSource_forSource(String fontSource,
			String expectedText,
			String expectedClipboardSource) throws Exception {
		CompositeInfo shell =
				parseComposite(
						"// filler filler filler",
						"public class Test extends Shell {",
						"  public Test() {",
						"    setFont(" + fontSource + ");",
						"  }",
						"}");
		shell.refresh();
		//
		Property property = shell.getPropertyByTitle("font");
		assertEquals(expectedText, PropertyEditorTestUtils.getText(property));
		assertEquals(expectedClipboardSource, PropertyEditorTestUtils.getClipboardSource(property));
	}
}