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
package org.eclipse.wb.tests.designer.swing.model.component;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.StaticFieldPropertyEditor;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.apache.commons.lang.ArrayUtils;

import java.lang.reflect.Field;

import javax.swing.AbstractButton;

/**
 * Test properties {@link StaticFieldPropertyEditor} of {@link AbstractButton}.
 *
 * @author scheglov_ke
 */
public class AbstractButtonTest extends SwingModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	public void test_horizontal() throws Exception {
		ContainerInfo panel =
				(ContainerInfo) parseSource(
						"test",
						"Test.java",
						getSourceDQ(
								"package test;",
								"import javax.swing.*;",
								"class Test extends JPanel {",
								"  Test() {",
								"    add(new JButton());",
								"  }",
								"}"));
		ComponentInfo button = panel.getChildrenComponents().get(0);
		checkStaticFieldsProperty(button, "horizontalAlignment", new String[]{
				"LEFT",
				"CENTER",
				"RIGHT",
				"LEADING",
		"TRAILING"});
		checkStaticFieldsProperty(button, "verticalTextPosition", new String[]{
				"TOP",
				"CENTER",
		"BOTTOM"});
	}

	private void checkStaticFieldsProperty(ComponentInfo button,
			String propertyTitle,
			String[] expectedNames) throws Exception, NoSuchFieldException, IllegalAccessException {
		// prepare property
		Property property = button.getPropertyByTitle(propertyTitle);
		assertNotNull(property);
		// check editor class
		PropertyEditor editor = property.getEditor();
		assertTrue(editor instanceof StaticFieldPropertyEditor);
		// check fields
		Field namesField = editor.getClass().getDeclaredField("m_names");
		namesField.setAccessible(true);
		ArrayUtils.isEquals(expectedNames, namesField.get(editor));
	}
}
