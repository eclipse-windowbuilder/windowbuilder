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
package org.eclipse.wb.tests.designer.swing.model.component;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipProvider;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.property.editor.models.spinner.SpinnerModelPropertyEditor;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.swtbot.swt.finder.SWTBot;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import javax.swing.JSpinner;

/**
 * Tests for {@link JSpinner} support, such as {@link SpinnerModelPropertyEditor}.
 *
 * @author scheglov_ke
 */
public class JSpinnerTest extends SwingModelTest {
	private static TimeZone TIME_ZONE;
	private static Locale LOCALE;

	@BeforeAll
	public static void setUpAll() {
		TIME_ZONE = TimeZone.getDefault();
		TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of("UTC")));
		LOCALE = Locale.getDefault();
		Locale.setDefault(Locale.ENGLISH);
	}

	@AfterAll
	public static void tearDownAll() {
		TimeZone.setDefault(TIME_ZONE);
		Locale.setDefault(LOCALE);
	}

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

	@Test
	public void test_dateModel() throws Exception {
		String source = "new SpinnerDateModel(new java.util.Date(0), null, null, java.util.Calendar.SECOND)";
		String expectedText = "01.01.1970 00:00:00, null, null, SECOND";
		String expectedTooltip = "value=01.01.1970 00:00:00\nstart=null\nend=null\nstep=SECOND";
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

	////////////////////////////////////////////////////////////////////////////
	//
	// SpinnerModelPropertyEditor
	//
	////////////////////////////////////////////////////////////////////////////

	@Test
	public void test_editor_noModel() throws Exception {
		ContainerInfo spinner = parseContainer("""
				// filler filler filler
				public class Test extends JSpinner {
					public Test() {
					}
				}""");
		spinner.refresh();

		Property modelProperty = spinner.getPropertyByTitle("model");
		PropertyEditor modelEditor = modelProperty.getEditor();
		assertEquals("Integer, 0, null, null, 1", getPropertyText(modelProperty));

		new UiContext().executeAndCheck(() -> modelEditor.activate(null, modelProperty, null), bot -> {
			SWTBot shell = bot.shell("model").bot();
			assertFalse(shell.cTabItem("List").isActive());
			assertTrue(shell.cTabItem("Number").isActive());
			assertFalse(shell.cTabItem("Date").isActive());
		});
	}

	@Test
	public void test_editor_numberModel() throws Exception {
		ContainerInfo spinner = parseContainer("""
				// filler filler filler
				public class Test extends JSpinner {
					public Test() {
						setModel(new SpinnerNumberModel(5, 0, 10, 2));
					}
				}""");
		spinner.refresh();

		Property modelProperty = spinner.getPropertyByTitle("model");
		PropertyEditor modelEditor = modelProperty.getEditor();
		assertEquals("Integer, 5, 0, 10, 2", getPropertyText(modelProperty));

		new UiContext().executeAndCheck(() -> modelEditor.activate(null, modelProperty, null), bot -> {
			SWTBot shell = bot.shell("model").bot();
			assertFalse(shell.cTabItem("List").isActive());
			assertTrue(shell.cTabItem("Number").isActive());
			assertFalse(shell.cTabItem("Date").isActive());

			assertEquals("Integer", shell.comboBox().getText(), "Number type");
			assertEquals("5", shell.spinner(0).getText(), "Initial Value");
			assertEquals("0", shell.spinner(1).getText(), "Minimum");
			assertEquals("10", shell.spinner(2).getText(), "Maximum");
			assertEquals("2", shell.spinner(3).getText(), "Step Size");
		});
	}

	@Test
	public void test_editor_listModel() throws Exception {
		ContainerInfo spinner = parseContainer("""
				// filler filler filler
				public class Test extends JSpinner {
					public Test() {
						setModel(new SpinnerListModel(new String[] {"a", "b", "c", "d", "e"}));
					}
				}""");
		spinner.refresh();

		Property modelProperty = spinner.getPropertyByTitle("model");
		PropertyEditor modelEditor = modelProperty.getEditor();
		assertEquals("a, b, c, d, e", getPropertyText(modelProperty));

		new UiContext().executeAndCheck(() -> modelEditor.activate(null, modelProperty, null), bot -> {
			SWTBot shell = bot.shell("model").bot();
			assertTrue(shell.cTabItem("List").isActive());
			assertFalse(shell.cTabItem("Number").isActive());
			assertFalse(shell.cTabItem("Date").isActive());

			assertEquals("a\nb\nc\nd\ne", shell.text().getText(), "Items");
		});
	}

	@Test
	public void test_editor_dateModel() throws Exception {
		ContainerInfo spinner = parseContainer(
				"""
						import java.util.Calendar;
						import java.util.Date;
						// filler filler filler
						public class Test extends JSpinner {
							public Test() {
								setModel(new SpinnerDateModel(new Date(1757800800000L), new Date(1757714400000L), new Date(1757887200000L), Calendar.DAY_OF_YEAR));
							}
						}""");
		spinner.refresh();

		Property modelProperty = spinner.getPropertyByTitle("model");
		PropertyEditor modelEditor = modelProperty.getEditor();
		assertEquals("13.09.2025 22:00:00, 12.09.2025 22:00:00, 14.09.2025 22:00:00, DAY_OF_YEAR",
				getPropertyText(modelProperty));

		new UiContext().executeAndCheck(() -> modelEditor.activate(null, modelProperty, null), bot -> {
			SWTBot shell = bot.shell("model").bot();
			assertFalse(shell.cTabItem("List").isActive());
			assertFalse(shell.cTabItem("Number").isActive());
			assertTrue(shell.cTabItem("Date").isActive());

			assertEquals("Sep 13, 2025, 10:00:00 PM", shell.text(0).getText(), "Initial value");
			assertEquals("Sep 12, 2025, 10:00:00 PM", shell.text(1).getText(), "Start");
			assertEquals("Sep 14, 2025, 10:00:00 PM", shell.text(2).getText(), "End");
		});
	}
}
