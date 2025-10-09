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

import org.eclipse.wb.internal.core.model.clipboard.IClipboardSourceProvider;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.internal.swing.model.property.editor.border.BorderPropertyEditor;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;
import org.eclipse.wb.tests.gef.UiContext;

import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotList;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;

import org.junit.jupiter.api.Test;

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
	public void test_editor_defaultBorder() throws Exception {
		ContainerInfo panel = parseContainer("""
				public class Test extends JPanel {
					public Test() {
					}
				}""");
		panel.refresh();

		Property modelProperty = panel.getPropertyByTitle("border");
		PropertyEditor modelEditor = modelProperty.getEditor();
		new UiContext().executeAndCheck(() -> modelEditor.activate(null, modelProperty, null), bot -> {
			SWTBot shell = bot.shell("Border editor").bot();
			assertEquals("(default)", shell.comboBox().getText());
		});
	}

	@Test
	public void test_editor_noBorder() throws Exception {
		ContainerInfo panel = parseContainer("""
				public class Test extends JPanel {
					public Test() {
						setBorder(null);
					}
				}""");
		panel.refresh();

		Property modelProperty = panel.getPropertyByTitle("border");
		PropertyEditor modelEditor = modelProperty.getEditor();
		new UiContext().executeAndCheck(() -> modelEditor.activate(null, modelProperty, null), bot -> {
			SWTBot shell = bot.shell("Border editor").bot();
			assertEquals("(no border)", shell.comboBox().getText());
		});
	}

	@Test
	public void test_editor_bevelBorder() throws Exception {
		ContainerInfo panel = parseContainer("""
				public class Test extends JPanel {
					public Test() {
						setBorder(new BevelBorder(BevelBorder.RAISED, new Color(255, 0, 0), new Color(0, 255, 0), new Color(0, 0, 255), new Color(255, 0, 255)));
					}
				}""");
		panel.refresh();

		Property modelProperty = panel.getPropertyByTitle("border");
		PropertyEditor modelEditor = modelProperty.getEditor();
		new UiContext().executeAndCheck(() -> modelEditor.activate(null, modelProperty, null), bot -> {
			SWTBot shell = bot.shell("Border editor").bot();
			assertEquals("BevelBorder", shell.comboBox().getText());
			assertEquals("(255, 0, 0)", shell.label(2).getText());
			assertEquals("(0, 255, 0)", shell.label(4).getText());
			assertEquals("(0, 0, 255)", shell.label(6).getText());
			assertEquals("(255, 0, 255)", shell.label(8).getText());
			assertTrue(shell.radio("raised").isSelected());

			shell.radio("lowered").click();
			shell.toolbarButtonWithTooltip("Reset color to default").click();

			assertEquals("(default)", shell.label(2).getText());
			assertTrue(shell.radio("lowered").isSelected());

			shell.button("OK").click();
		});

		assertEditor("""
				public class Test extends JPanel {
					public Test() {
						setBorder(new BevelBorder(BevelBorder.LOWERED, null, new Color(0, 255, 0), new Color(0, 0, 255), new Color(255, 0, 255)));
					}
				}""");
	}

	@Test
	public void test_editor_compoundBorder() throws Exception {
		ContainerInfo panel = parseContainer("""
				public class Test extends JPanel {
					public Test() {
						setBorder(new javax.swing.border.CompoundBorder());
					}
				}""");
		panel.refresh();

		Property modelProperty = panel.getPropertyByTitle("border");
		PropertyEditor modelEditor = modelProperty.getEditor();
		new UiContext().executeAndCheck(() -> modelEditor.activate(null, modelProperty, null), bot -> {
			SWTBotShell shellBot = bot.shell("Border editor");
			SWTBot shell = shellBot.bot();

			assertEquals("", shell.textWithLabel("Outside border:").getText());
			assertEquals("", shell.textWithLabel("Inside border:").getText());

			shell.button("Edit...").click();

			SWTBot shell2 = bot.shell("Border editor", shellBot.widget).bot();
			shell2.comboBox().setSelection("EmptyBorder");
			shell2.button("OK").click();

			assertEquals("javax.swing.border.EmptyBorder", shell.textWithLabel("Outside border:").getText());
			assertEquals("", shell.textWithLabel("Inside border:").getText());

			shell.button("Edit...", 1).click();

			SWTBot shell3 = bot.shell("Border editor", shellBot.widget).bot();
			shell3.comboBox().setSelection("EtchedBorder");
			shell3.button("OK").click();

			assertEquals("javax.swing.border.EmptyBorder", shell.textWithLabel("Outside border:").getText());
			assertEquals("javax.swing.border.EtchedBorder", shell.textWithLabel("Inside border:").getText());

			shell.button("Swap").click();

			assertEquals("javax.swing.border.EtchedBorder", shell.textWithLabel("Outside border:").getText());
			assertEquals("javax.swing.border.EmptyBorder", shell.textWithLabel("Inside border:").getText());

			shell.button("OK").click();
		});

		assertEditor("""
				public class Test extends JPanel {
					public Test() {
						setBorder(new CompoundBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), new EmptyBorder(0, 0, 0, 0)));
					}
				}""");
	}

	@Test
	public void test_editor_emptyBorder() throws Exception {
		ContainerInfo panel = parseContainer("""
				public class Test extends JPanel {
					public Test() {
						setBorder(new EmptyBorder(1, 2, 3, 4));
					}
				}""");
		panel.refresh();

		Property modelProperty = panel.getPropertyByTitle("border");
		PropertyEditor modelEditor = modelProperty.getEditor();
		new UiContext().executeAndCheck(() -> modelEditor.activate(null, modelProperty, null), bot -> {
			SWTBot shell = bot.shell("Border editor").bot();
			assertEquals(1, shell.spinnerWithLabel("Top:").getSelection());
			assertEquals(2, shell.spinnerWithLabel("Left:").getSelection());
			assertEquals(3, shell.spinnerWithLabel("Bottom:").getSelection());
			assertEquals(4, shell.spinnerWithLabel("Right:").getSelection());

			shell.spinnerWithLabel("Top:").setSelection(5);
			shell.spinnerWithLabel("Left:").setSelection(6);
			shell.spinnerWithLabel("Bottom:").setSelection(7);
			shell.spinnerWithLabel("Right:").setSelection(8);

			assertEquals(5, shell.spinnerWithLabel("Top:").getSelection());
			assertEquals(6, shell.spinnerWithLabel("Left:").getSelection());
			assertEquals(7, shell.spinnerWithLabel("Bottom:").getSelection());
			assertEquals(8, shell.spinnerWithLabel("Right:").getSelection());

			shell.button("OK").click();
		});

		assertEditor("""
				public class Test extends JPanel {
					public Test() {
						setBorder(new EmptyBorder(5, 6, 7, 8));
					}
				}""");
	}

	@Test
	public void test_editor_etchedBorder() throws Exception {
		ContainerInfo panel = parseContainer("""
				public class Test extends JPanel {
					public Test() {
						setBorder(new EtchedBorder(EtchedBorder.RAISED, new Color(0, 255, 0), new Color(0, 0, 255)));
					}
				}""");
		panel.refresh();

		Property modelProperty = panel.getPropertyByTitle("border");
		PropertyEditor modelEditor = modelProperty.getEditor();
		new UiContext().executeAndCheck(() -> modelEditor.activate(null, modelProperty, null), bot -> {
			SWTBot shell = bot.shell("Border editor").bot();
			assertEquals("EtchedBorder", shell.comboBox().getText());
			assertEquals("(0, 255, 0)", shell.label(2).getText());
			assertEquals("(0, 0, 255)", shell.label(4).getText());
			assertTrue(shell.radio("raised").isSelected());

			shell.radio("lowered").click();
			shell.toolbarButtonWithTooltip("Reset color to default").click();

			assertEquals("(default)", shell.label(2).getText());
			assertTrue(shell.radio("lowered").isSelected());

			shell.button("OK").click();
		});

		assertEditor("""
				public class Test extends JPanel {
					public Test() {
						setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, new Color(0, 0, 255)));
					}
				}""");
	}

	@Test
	public void test_editor_lineBorder() throws Exception {
		ContainerInfo panel = parseContainer("""
				public class Test extends JPanel {
					public Test() {
						setBorder(new LineBorder(new Color(0, 0, 255), 5, true));
					}
				}""");
		panel.refresh();

		Property modelProperty = panel.getPropertyByTitle("border");
		PropertyEditor modelEditor = modelProperty.getEditor();
		new UiContext().executeAndCheck(() -> modelEditor.activate(null, modelProperty, null), bot -> {
			SWTBot shell = bot.shell("Border editor").bot();
			assertEquals("LineBorder", shell.comboBox().getText());
			assertEquals("(0, 0, 255)", shell.label(1).getText());
			assertEquals(5, shell.spinnerWithLabel("Thickness:").getSelection());
			assertTrue(shell.radio("rounded").isSelected());

			shell.radio("square").click();
			shell.spinnerWithLabel("Thickness:").setSelection(10);
			shell.toolbarButtonWithTooltip("Reset color to default").click();

			assertEquals("(default)", shell.label(1).getText());
			assertEquals(10, shell.spinnerWithLabel("Thickness:").getSelection());
			assertTrue(shell.radio("square").isSelected());

			shell.button("OK").click();
		});

		assertEditor("""
				public class Test extends JPanel {
					public Test() {
						setBorder(new LineBorder(null, 10));
					}
				}""");
	}

	@Test
	public void test_editor_matteBorder() throws Exception {
		ContainerInfo panel = parseContainer("""
				public class Test extends JPanel {
					public Test() {
						setBorder(new MatteBorder(1, 2, 3, 4, (java.awt.Color) new Color(0, 0, 255)));
					}
				}""");
		panel.refresh();

		Property modelProperty = panel.getPropertyByTitle("border");
		PropertyEditor modelEditor = modelProperty.getEditor();
		new UiContext().executeAndCheck(() -> modelEditor.activate(null, modelProperty, null), bot -> {
			SWTBot shell = bot.shell("Border editor").bot();
			assertEquals("MatteBorder", shell.comboBox().getText());
			assertEquals("(0, 0, 255)", shell.label(1).getText());
			assertEquals(1, shell.spinnerWithLabel("Top:").getSelection());
			assertEquals(2, shell.spinnerWithLabel("Left:").getSelection());
			assertEquals(3, shell.spinnerWithLabel("Bottom:").getSelection());
			assertEquals(4, shell.spinnerWithLabel("Right:").getSelection());

			shell.toolbarButtonWithTooltip("Reset color to default").click();
			shell.spinnerWithLabel("Top:").setSelection(5);
			shell.spinnerWithLabel("Left:").setSelection(6);
			shell.spinnerWithLabel("Bottom:").setSelection(7);
			shell.spinnerWithLabel("Right:").setSelection(8);

			assertEquals("(default)", shell.label(1).getText());
			assertEquals(5, shell.spinnerWithLabel("Top:").getSelection());
			assertEquals(6, shell.spinnerWithLabel("Left:").getSelection());
			assertEquals(7, shell.spinnerWithLabel("Bottom:").getSelection());
			assertEquals(8, shell.spinnerWithLabel("Right:").getSelection());

			shell.button("OK").click();
		});

		assertEditor("""
				public class Test extends JPanel {
					public Test() {
						setBorder(new MatteBorder(5, 6, 7, 8, (Color) null));
					}
				}""");
	}

	@Test
	public void test_editor_softBevelBorder() throws Exception {
		ContainerInfo panel = parseContainer("""
				public class Test extends JPanel {
					public Test() {
						setBorder(new SoftBevelBorder(BevelBorder.RAISED, new Color(255, 0, 0), new Color(0, 255, 0), new Color(0, 0, 255), new Color(255, 0, 255)));
					}
				}""");
		panel.refresh();

		Property modelProperty = panel.getPropertyByTitle("border");
		PropertyEditor modelEditor = modelProperty.getEditor();
		new UiContext().executeAndCheck(() -> modelEditor.activate(null, modelProperty, null), bot -> {
			SWTBot shell = bot.shell("Border editor").bot();
			assertEquals("SoftBevelBorder", shell.comboBox().getText());
			assertEquals("(255, 0, 0)", shell.label(2).getText());
			assertEquals("(0, 255, 0)", shell.label(4).getText());
			assertEquals("(0, 0, 255)", shell.label(6).getText());
			assertEquals("(255, 0, 255)", shell.label(8).getText());
			assertTrue(shell.radio("raised").isSelected());

			shell.radio("lowered").click();
			shell.toolbarButtonWithTooltip("Reset color to default").click();

			assertEquals("(default)", shell.label(2).getText());
			assertTrue(shell.radio("lowered").isSelected());

			shell.button("OK").click();
		});

		assertEditor("""
				public class Test extends JPanel {
					public Test() {
						setBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, new Color(0, 255, 0), new Color(0, 0, 255), new Color(255, 0, 255)));
					}
				}""");
	}

	@Test
	public void test_editor_titledBorder() throws Exception {
		ContainerInfo panel = parseContainer("""
				public class Test extends JPanel {
					public Test() {
						setBorder(new TitledBorder(new LineBorder(null), "MyTitle", TitledBorder.TRAILING, TitledBorder.BOTTOM, null, Color.RED));
					}
				}""");
		panel.refresh();

		Property modelProperty = panel.getPropertyByTitle("border");
		PropertyEditor modelEditor = modelProperty.getEditor();
		new UiContext().executeAndCheck(() -> modelEditor.activate(null, modelProperty, null), bot -> {
			SWTBotShell shellBot = bot.shell("Border editor");
			SWTBot shell = shellBot.bot();
			assertEquals("TitledBorder", shell.comboBox().getText());
			assertEquals("MyTitle", shell.textWithLabel("Title:").getText());
			assertEquals("Trailing", shell.comboBoxWithLabel("Title justification:").getText());
			assertEquals("Bottom", shell.comboBoxWithLabel("Title position:").getText());
			assertEquals("(255, 0, 0)", shell.label(4).getText());
			assertEquals("javax.swing.border.LineBorder", shell.textWithLabel("Border:").getText());

			shell.textWithLabel("Title:").setText("NotMyTitle");
			shell.comboBoxWithLabel("Title justification:").setSelection("Left");
			shell.comboBoxWithLabel("Title position:").setSelection("Below Top");
			shell.toolbarButtonWithTooltip("Reset color to default").click();

			shell.button("Edit...").click();
			SWTBot shell2 = bot.shell("Border editor", shellBot.widget).bot();
			shell2.comboBox().setSelection("EmptyBorder");
			shell2.button("OK").click();

			assertEquals("TitledBorder", shell.comboBox().getText());
			assertEquals("NotMyTitle", shell.textWithLabel("Title:").getText());
			assertEquals("Left", shell.comboBoxWithLabel("Title justification:").getText());
			assertEquals("Below Top", shell.comboBoxWithLabel("Title position:").getText());
			assertEquals("(default)", shell.label(4).getText());
			assertEquals("javax.swing.border.EmptyBorder", shell.textWithLabel("Border:").getText());

			shell.button("OK").click();
		});

		assertEditor("""
				public class Test extends JPanel {
					public Test() {
						setBorder(new TitledBorder(new EmptyBorder(0, 0, 0, 0), "NotMyTitle", TitledBorder.LEFT, TitledBorder.BELOW_TOP, null, null));
					}
				}""");
	}

	@Test
	public void test_editor_swingBorder() throws Exception {
		ContainerInfo panel = parseContainer("""
				public class Test extends JPanel {
					public Test() {
						setBorder(UIManager.getBorder("Button.border"));
					}
				}""");
		panel.refresh();

		Property modelProperty = panel.getPropertyByTitle("border");
		PropertyEditor modelEditor = modelProperty.getEditor();
		new UiContext().executeAndCheck(() -> modelEditor.activate(null, modelProperty, null), bot -> {
			SWTBot shell = bot.shell("Border editor").bot();
			assertEquals("Swing", shell.comboBox().getText());

			SWTBotList list = shell.list();
			assertEquals(1, list.selectionCount());
			assertEquals("Button.border", list.selection()[0]);

			list.select("TitledBorder.border");

			assertEquals(1, list.selectionCount());
			assertEquals("TitledBorder.border", list.selection()[0]);

			shell.button("OK").click();
		});

		assertEditor("""
				public class Test extends JPanel {
					public Test() {
						setBorder(UIManager.getBorder("TitledBorder.border"));
					}
				}""");
	}
}