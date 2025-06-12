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
package org.eclipse.wb.tests.designer.swing.model.property;

import org.eclipse.wb.core.controls.CSpinner;
import org.eclipse.wb.internal.core.model.clipboard.IClipboardSourceProvider;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.internal.swing.model.property.editor.border.BorderPropertyEditor;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;
import org.eclipse.wb.tests.gef.UiContext;
import org.eclipse.wb.tests.utils.SWTBotCSpinner;

import static org.eclipse.swtbot.swt.finder.matchers.WidgetOfType.widgetOfType;

import org.eclipse.swtbot.swt.finder.SWTBot;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Test for {@link BorderPropertyEditor}.
 *
 * @author scheglov_ke
 */
public class BorderPropertyEditorTest extends SwingModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// getText()
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_getText_defaultBorder() throws Exception {
		ContainerInfo panel = parseContainer("""
				// filler filler filler
				public class Test extends JPanel {
					public Test() {
					}
				}""");
		panel.refresh();
		// property
		Property borderProperty = panel.getPropertyByTitle("border");
		assertEquals(null, getPropertyText(borderProperty));
	}

	@Test
	public void test_getText_noBorder() throws Exception {
		ContainerInfo panel = parseContainer("""
				public class Test extends JPanel {
					public Test() {
						setBorder(null);
					}
				}""");
		panel.refresh();
		// property
		Property borderProperty = panel.getPropertyByTitle("border");
		assertEquals("(no border)", getPropertyText(borderProperty));
	}

	@Test
	public void test_getText_EmptyBorder() throws Exception {
		ContainerInfo panel = parseContainer("""
				public class Test extends JPanel {
					public Test() {
						setBorder(new EmptyBorder(0, 0, 0, 0));
					}
				}""");
		panel.refresh();
		// property
		Property borderProperty = panel.getPropertyByTitle("border");
		assertEquals("EmptyBorder", getPropertyText(borderProperty));
	}

	@Test
	public void test_getClipboardSource_EmptyBorder() throws Exception {
		final ContainerInfo panel = parseContainer("""
				public class Test extends JPanel {
					public Test() {
						{
							JButton button = new JButton();
							button.setBorder(new EmptyBorder(1, 2, 3, 4));
							add(button);
						}
					}
				}""");
		panel.refresh();
		ComponentInfo button = getJavaInfoByName("button");
		// property
		{
			GenericProperty borderProperty = (GenericProperty) button.getPropertyByTitle("border");
			PropertyEditor propertyEditor = borderProperty.getEditor();
			assertEquals(
					"new javax.swing.border.EmptyBorder(1, 2, 3, 4)",
					((IClipboardSourceProvider) propertyEditor).getClipboardSource(borderProperty));
		}
		// do copy/paste
		doCopyPaste(button, new PasteProcedure<ComponentInfo>() {
			@Override
			public void run(ComponentInfo copy) throws Exception {
				((FlowLayoutInfo) panel.getLayout()).add(copy, null);
			}
		});
		assertEditor("""
				public class Test extends JPanel {
					public Test() {
						{
							JButton button = new JButton();
							button.setBorder(new EmptyBorder(1, 2, 3, 4));
							add(button);
						}
						{
							JButton button = new JButton();
							button.setBorder(new EmptyBorder(1, 2, 3, 4));
							add(button);
						}
					}
				}""");
	}

	/**
	 * If we can not copy/paste expression, because it contains references on variables or something
	 * other, then ignore.
	 */
	@Test
	public void test_getClipboardSource_hasNotConstants() throws Exception {
		final ContainerInfo panel = parseContainer("""
				public class Test extends JPanel {
					public Test() {
						{
							JButton button = new JButton();
							int notConst = 4;
							button.setBorder(new EmptyBorder(1, 2, 3, notConst));
							add(button);
						}
					}
				}""");
		panel.refresh();
		ComponentInfo button = getJavaInfoByName("button");
		// property
		{
			GenericProperty borderProperty = (GenericProperty) button.getPropertyByTitle("border");
			PropertyEditor propertyEditor = borderProperty.getEditor();
			IClipboardSourceProvider pe = (IClipboardSourceProvider) propertyEditor;
			String cs = pe.getClipboardSource(borderProperty);
			assertEquals(null, cs);
		}
		// do copy/paste
		doCopyPaste(button, copy -> ((FlowLayoutInfo) panel.getLayout()).add(copy, null));
		assertEditor("""
				public class Test extends JPanel {
					public Test() {
						{
							JButton button = new JButton();
							int notConst = 4;
							button.setBorder(new EmptyBorder(1, 2, 3, notConst));
							add(button);
						}
						{
							JButton button = new JButton();
							add(button);
						}
					}
				}""");
	}

	/**
	 * https://github.com/eclipse-windowbuilder/windowbuilder/issues/1049
	 */
	@Test
	public void test_modifyBorder() throws Exception {
		ContainerInfo panel = parseContainer("""
				// filler filler filler
				public class Test extends JPanel {
					public Test() {
					}
				}""");
		panel.refresh();
		// property
		Property borderProperty = panel.getPropertyByTitle("border");

		// Executed while the dialog is open
		new UiContext().executeAndCheck(() -> {
			PropertyEditor editor = BorderPropertyEditor.INSTANCE;
			ReflectionUtils.invokeMethod2(editor, "openDialog", Property.class, borderProperty);
		}, bot -> {
			SWTBot dialogBot = bot.shell("Border editor").bot();
			dialogBot.comboBox().setSelection("EmptyBorder");

			List<SWTBotCSpinner> spinners = getCSpinners(dialogBot);
			assertEquals(spinners.size(), 4);

			spinners.get(0).setSelection(10);
			spinners.get(1).setSelection(15);
			spinners.get(2).setSelection(20);
			spinners.get(3).setSelection(25);
			dialogBot.button("OK").click();
		});

		panel.refresh();
		assertEditor("""
				// filler filler filler
				public class Test extends JPanel {
					public Test() {
						setBorder(new EmptyBorder(10, 15, 20, 25));
					}
				}""");
	}

	/**
	 * @return {@link SWTBotCSpinner}s of this dialog.
	 */
	private List<SWTBotCSpinner> getCSpinners(SWTBot shell) {
		List<SWTBotCSpinner> spinners = new ArrayList<>();
		for (CSpinner spinner : shell.getFinder().findControls(widgetOfType(CSpinner.class))) {
			spinners.add(new SWTBotCSpinner(spinner));
		}
		return spinners;
	}
}