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
package org.eclipse.wb.tests.designer.core.model.property.editor;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.LocalePropertyEditor;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.junit.Test;

import java.util.Locale;

/**
 * Test for {@link LocalePropertyEditor}.
 *
 * @author scheglov_ke
 */
public class LocalePropertyEditorTest extends SwingModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_setText() throws Exception {
		setFileContentSrc(
				"test/MyButton.java",
				getTestSource(
						"import java.util.Locale;",
						"public class MyButton extends JButton {",
						"  public void setFoo(Locale locale) {",
						"  }",
						"}"));
		waitForAutoBuild();
		// parse
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    MyButton button = new MyButton();",
						"    add(button);",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		//
		Property property = button.getPropertyByTitle("foo");
		assertInstanceOf(LocalePropertyEditor.class, property.getEditor());
		// initially no value, so not text
		assertEquals(null, getPropertyText(property));
		// set value
		property.setValue(Locale.GERMAN);
		assertEditor(
				"import java.util.Locale;",
				"public class Test extends JPanel {",
				"  public Test() {",
				"    MyButton button = new MyButton();",
				"    button.setFoo(Locale.GERMAN);",
				"    add(button);",
				"  }",
				"}");
		assertEquals(Locale.GERMAN.getDisplayName(), getPropertyText(property));
	}
}