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
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipProvider;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.property.editor.models.spinner.SpinnerModelPropertyEditor;
import org.eclipse.wb.tests.designer.Expectations;
import org.eclipse.wb.tests.designer.Expectations.StrValue;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.junit.Ignore;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Calendar;

import javax.swing.JSpinner;

/**
 * Tests for {@link JSpinner} support, such as {@link SpinnerModelPropertyEditor}.
 *
 * @author scheglov_ke
 */
public class JSpinnerTest extends SwingModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * There was bug that parser handled <code>Type.Inner</code> incorrectly.
	 */
	@Test
	public void test_setEditor() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JSpinner spinner = new JSpinner();",
						"    spinner.setEditor(new JSpinner.NumberEditor(spinner, '#'));",
						"    add(spinner);",
						"  }",
						"}");
		panel.refresh();
		assertNoErrors(panel);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// SpinnerNumberModel
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_numberModel_Integer() throws Exception {
		String source = "new SpinnerNumberModel(15, 10, 20, 1)";
		String expectedText = "Integer, 15, 10, 20, 1";
		String expectedTooltip = "type=Integer value=15 start=10 end=20 step=1";
		assertEditorTextTooltip(source, expectedText, expectedTooltip);
	}

	@Test
	public void test_numberModel_Double() throws Exception {
		String source = "new SpinnerNumberModel(15.0, 10.2, 20.0, 1.2)";
		String expectedText = "Double, 15.0, 10.2, 20.0, 1.2";
		String expectedTooltip = "type=Double value=15.0 start=10.2 end=20.0 step=1.2";
		assertEditorTextTooltip(source, expectedText, expectedTooltip);
	}

	@Test
	public void test_numberModel_null() throws Exception {
		String source = "new SpinnerNumberModel(15, 0, null, 1)";
		String expectedText = "Integer, 15, 0, null, 1";
		String expectedTooltip = "type=Integer value=15 start=0 end=null step=1";
		assertEditorTextTooltip(source, expectedText, expectedTooltip);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// SpinnerListModel
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_listModel() throws Exception {
		String source = "new SpinnerListModel(new Object[]{\"aaa\", \"bbb\", \"ccc\"})";
		String expectedText = "aaa, bbb, ccc";
		String expectedTooltip = "aaa\nbbb\nccc";
		assertEditorTextTooltip(source, expectedText, expectedTooltip);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// SpinnerDateModel
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_dateModel_getDateStep() throws Exception {
		Field[] declaredFields = Calendar.class.getDeclaredFields();
		for (Field field : declaredFields) {
			int modifiers = field.getModifiers();
			if (Modifier.isPublic(modifiers)
					&& Modifier.isStatic(modifiers)
					&& Modifier.isFinal(modifiers)) {
				// prepare expected name
				String expectedName = field.getName();
				if (expectedName.equals("DATE")) {
					expectedName = "DAY_OF_MONTH";
				}
				if (expectedName.equals("ZONE_OFFSET")) {
					break;
				}
				// check actual name
				String actualName =
						(String) ReflectionUtils.invokeMethod2(
								SpinnerModelPropertyEditor.class,
								"getDateStep",
								int.class,
								field.getInt(null));
				assertEquals(expectedName, actualName);
			}
		}
		// some unknown field
		assertNull(ReflectionUtils.invokeMethod2(
				SpinnerModelPropertyEditor.class,
				"getDateStep",
				int.class,
				0xDEADBEEF));
	}

	@Ignore
	@Test
	public void test_dateModel() throws Exception {
		String source =
				"new SpinnerDateModel(new java.util.Date(0), null, null, java.util.Calendar.SECOND)";
		String expectedText =
				Expectations.get(
						"01.01.1970 03:00:00, null, null, SECOND",
						new StrValue("America/New_York", "31.12.1969 19:00:00, null, null, SECOND"),
						new StrValue("America/Los_Angeles", "31.12.1969 16:00:00, null, null, SECOND"));
		String expectedTooltip =
				Expectations.get(
						"value=01.01.1970 03:00:00\nstart=null\nend=null\nstep=SECOND",
						new StrValue("America/New_York",
								"value=31.12.1969 19:00:00\nstart=null\nend=null\nstep=SECOND"),
						new StrValue("America/Los_Angeles",
								"value=31.12.1969 16:00:00\nstart=null\nend=null\nstep=SECOND"));
		assertEditorTextTooltip(source, expectedText, expectedTooltip);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Asserts that {@link SpinnerModelPropertyEditor} has expected text and tooltip.
	 */
	private void assertEditorTextTooltip(String modelSource,
			String expectedText,
			String expectedTooltip) throws Exception {
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JSpinner spinner = new JSpinner();",
						"    spinner.setModel(" + modelSource + ");",
						"    add(spinner);",
						"  }",
						"}");
		panel.refresh();
		ComponentInfo spinner = panel.getChildrenComponents().get(0);
		Property modelProperty = spinner.getPropertyByTitle("model");
		PropertyEditor modelEditor = modelProperty.getEditor();
		// text
		{
			String text =
					(String) ReflectionUtils.invokeMethod2(
							modelEditor,
							"getText",
							Property.class,
							modelProperty);
			assertEquals(expectedText, text);
		}
		// tooltip
		{
			String tooltip = getPropertyTooltipText(modelEditor, modelProperty);
			assertEquals(expectedTooltip, tooltip);
			// position
			PropertyTooltipProvider provider = modelEditor.getAdapter(PropertyTooltipProvider.class);
			assertSame(PropertyTooltipProvider.BELOW, provider.getTooltipPosition());
		}
	}
}
