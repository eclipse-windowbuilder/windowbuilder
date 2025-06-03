/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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

import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.model.clipboard.JavaInfoMemento;
import org.eclipse.wb.internal.core.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.internal.swing.model.layout.FlowLayoutInfo;
import org.eclipse.wb.internal.swing.model.property.editor.font.DerivedFontInfo;
import org.eclipse.wb.internal.swing.model.property.editor.font.ExplicitFontInfo;
import org.eclipse.wb.internal.swing.model.property.editor.font.FontInfo;
import org.eclipse.wb.internal.swing.model.property.editor.font.FontPropertyEditor;
import org.eclipse.wb.internal.swing.model.property.editor.font.NullFontInfo;
import org.eclipse.wb.internal.swing.model.property.editor.font.UiManagerFontInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

import java.awt.Font;

import javax.swing.UIManager;

/**
 * Test for {@link FontPropertyEditor}.
 *
 * @author scheglov_ke
 */
public class FontPropertyEditorTest extends SwingModelTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
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
	// FontInfo and its subclasses
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link NullFontInfo}.
	 */
	@Test
	public void test_FontInfo_Null() throws Exception {
		FontInfo fontInfo = new NullFontInfo();
		assertSame(null, fontInfo.getFont());
		assertEquals(null, fontInfo.getText());
		assertEquals("null", fontInfo.getSource());
	}

	/**
	 * Test for {@link ExplicitFontInfo}.
	 */
	@Test
	public void test_FontInfo_Explicit() throws Exception {
		// PLAIN
		{
			Font font = new Font("Arial", Font.PLAIN, 12);
			ExplicitFontInfo fontInfo = new ExplicitFontInfo(font);
			assertSame(font, fontInfo.getFont());
			Assertions.assertThat(fontInfo.getText()).matches("\\w+ 12");
			Assertions.assertThat(fontInfo.getSource()).matches("new java.awt.Font\\(\"\\w+\", java.awt.Font.PLAIN, 12\\)");
			assertEquals(fontInfo.getSource(), fontInfo.getClipboardSource());
		}
		// BOLD
		{
			Font font = new Font("Arial", Font.BOLD, 12);
			ExplicitFontInfo fontInfo = new ExplicitFontInfo(font);
			assertSame(font, fontInfo.getFont());
			Assertions.assertThat(fontInfo.getText()).matches("\\w+ 12 Bold");
			Assertions.assertThat(fontInfo.getSource()).matches("new java.awt.Font\\(\"\\w+\", java.awt.Font.BOLD, 12\\)");
			assertEquals(fontInfo.getSource(), fontInfo.getClipboardSource());
		}
		// ITALIC
		{
			Font font = new Font("Arial", Font.ITALIC, 12);
			ExplicitFontInfo fontInfo = new ExplicitFontInfo(font);
			assertSame(font, fontInfo.getFont());
			Assertions.assertThat(fontInfo.getText()).matches("\\w+ 12 Italic");
			Assertions.assertThat(fontInfo.getSource()).matches("new java.awt.Font\\(\"\\w+\", java.awt.Font.ITALIC, 12\\)");
			assertEquals(fontInfo.getSource(), fontInfo.getClipboardSource());
		}
		// BOLD + ITALIC
		{
			Font font = new Font("Arial", Font.BOLD | Font.ITALIC, 12);
			ExplicitFontInfo fontInfo = new ExplicitFontInfo(font);
			assertSame(font, fontInfo.getFont());
			Assertions.assertThat(fontInfo.getText()).matches("\\w+ 12 Bold Italic");
			Assertions.assertThat(fontInfo.getSource())
			.matches("new java.awt.Font\\(\"\\w+\", java.awt.Font.BOLD \\| java.awt.Font.ITALIC, 12\\)");
			assertEquals(fontInfo.getSource(), fontInfo.getClipboardSource());
		}
	}

	/**
	 * Test for {@link UiManagerFontInfo}.
	 */
	@Test
	public void test_FontInfo_UIManager() throws Exception {
		Font font = new Font("Arial", Font.PLAIN, 12);
		UiManagerFontInfo fontInfo = new UiManagerFontInfo("key", font);
		assertSame(font, fontInfo.getFont());
		assertEquals("key", fontInfo.getKey());
		Assertions.assertThat(fontInfo.getValueText()).matches("\\w+ 12");
		Assertions.assertThat(fontInfo.getText()).matches("key, \\w+ 12");
		assertEquals("javax.swing.UIManager.getFont(\"key\")", fontInfo.getSource());
		assertEquals(fontInfo.getSource(), fontInfo.getClipboardSource());
	}

	/**
	 * Test for {@link DerivedFontInfo}.
	 */
	@Test
	public void test_FontInfo_Derived() throws Exception {
		Font baseFont = new Font("Arial", Font.BOLD, 12);
		String baseFontSource = "button.getFont()";
		String baseFontClipboardSource = "%this%.getFont()";
		// no changes
		{
			FontInfo fontInfo =
					new DerivedFontInfo(baseFont,
							baseFontSource,
							baseFontClipboardSource,
							null,
							null,
							null,
							null,
							null);
			Font font = fontInfo.getFont();
			assertEquals(Font.BOLD, font.getStyle());
			assertEquals(12, font.getSize());
			if (EnvironmentUtils.IS_WINDOWS) {
				assertEquals("Arial", font.getFamily());
				assertEquals("<no changes>, Arial 12 Bold", fontInfo.getText());
			}
			assertNull(fontInfo.getSource());
			assertNull(fontInfo.getClipboardSource());
		}
		// new family
		{
			FontInfo fontInfo =
					new DerivedFontInfo(baseFont,
							baseFontSource,
							baseFontClipboardSource,
							"Tahoma",
							null,
							null,
							null,
							null);
			Font font = fontInfo.getFont();
			assertEquals(Font.BOLD, font.getStyle());
			assertEquals(12, font.getSize());
			if (EnvironmentUtils.IS_WINDOWS) {
				assertEquals("Tahoma", font.getFamily());
				assertEquals("*Tahoma, Tahoma 12 Bold", fontInfo.getText());
				assertEquals(
						"new java.awt.Font(\"Tahoma\", button.getFont().getStyle(), button.getFont().getSize())",
						fontInfo.getSource());
				assertEquals(
						"new java.awt.Font(\"Tahoma\", %this%.getFont().getStyle(), %this%.getFont().getSize())",
						fontInfo.getClipboardSource());
			}
		}
		// new family +5
		{
			FontInfo fontInfo =
					new DerivedFontInfo(baseFont,
							baseFontSource,
							baseFontClipboardSource,
							"Tahoma",
							null,
							null,
							Integer.valueOf(5),
							null);
			Font font = fontInfo.getFont();
			assertEquals(Font.BOLD, font.getStyle());
			assertEquals(17, font.getSize());
			if (EnvironmentUtils.IS_WINDOWS) {
				assertEquals("Tahoma", font.getFamily());
				assertEquals("*Tahoma +5, Tahoma 17 Bold", fontInfo.getText());
				assertEquals("new java.awt.Font(\"Tahoma\", button.getFont().getStyle(), button.getFont().getSize() + 5)",
						fontInfo.getSource());
				assertEquals("new java.awt.Font(\"Tahoma\", %this%.getFont().getStyle(), %this%.getFont().getSize() + 5)",
						fontInfo.getClipboardSource());
			}
		}
		// new family =20
		{
			FontInfo fontInfo =
					new DerivedFontInfo(baseFont,
							baseFontSource,
							baseFontClipboardSource,
							"Tahoma",
							null,
							null,
							null,
							Integer.valueOf(20));
			Font font = fontInfo.getFont();
			assertEquals(Font.BOLD, font.getStyle());
			assertEquals(20, font.getSize());
			if (EnvironmentUtils.IS_WINDOWS) {
				assertEquals("Tahoma", font.getFamily());
				assertEquals("*Tahoma 20, Tahoma 20 Bold", fontInfo.getText());
				assertEquals("new java.awt.Font(\"Tahoma\", button.getFont().getStyle(), 20)", fontInfo.getSource());
			}
		}
		// +Bold
		{
			FontInfo fontInfo =
					new DerivedFontInfo(baseFont,
							baseFontSource,
							baseFontClipboardSource,
							null,
							Boolean.TRUE,
							null,
							null,
							null);
			Font font = fontInfo.getFont();
			assertEquals(Font.BOLD, font.getStyle());
			assertEquals(12, font.getSize());
			if (EnvironmentUtils.IS_WINDOWS) {
				assertEquals("Arial", font.getFamily());
				assertEquals("+Bold, Arial 12 Bold", fontInfo.getText());
				assertEquals("button.getFont().deriveFont(button.getFont().getStyle() | java.awt.Font.BOLD)",
						fontInfo.getSource());
			}
		}
		// -Bold
		{
			FontInfo fontInfo =
					new DerivedFontInfo(baseFont,
							baseFontSource,
							baseFontClipboardSource,
							null,
							Boolean.FALSE,
							null,
							null,
							null);
			Font font = fontInfo.getFont();
			assertEquals(Font.PLAIN, font.getStyle());
			assertEquals(12, font.getSize());
			if (EnvironmentUtils.IS_WINDOWS) {
				assertEquals("Arial", font.getFamily());
				assertEquals("-Bold, Arial 12", fontInfo.getText());
				assertEquals("button.getFont().deriveFont(button.getFont().getStyle() & ~java.awt.Font.BOLD)",
						fontInfo.getSource());
			}
		}
		// +Italic
		{
			FontInfo fontInfo =
					new DerivedFontInfo(baseFont,
							baseFontSource,
							baseFontClipboardSource,
							null,
							null,
							Boolean.TRUE,
							null,
							null);
			Font font = fontInfo.getFont();
			assertEquals(Font.BOLD | Font.ITALIC, font.getStyle());
			assertEquals(12, font.getSize());
			if (EnvironmentUtils.IS_WINDOWS) {
				assertEquals("Arial", font.getFamily());
				assertEquals("+Italic, Arial 12 Bold Italic", fontInfo.getText());
				assertEquals("button.getFont().deriveFont(button.getFont().getStyle() | java.awt.Font.ITALIC)",
						fontInfo.getSource());
			}
		}
		// -Italic
		{
			FontInfo fontInfo =
					new DerivedFontInfo(baseFont,
							baseFontSource,
							baseFontClipboardSource,
							null,
							null,
							Boolean.FALSE,
							null,
							null);
			Font font = fontInfo.getFont();
			assertEquals(Font.BOLD, font.getStyle());
			assertEquals(12, font.getSize());
			if (EnvironmentUtils.IS_WINDOWS) {
				assertEquals("Arial", font.getFamily());
				assertEquals("-Italic, Arial 12 Bold", fontInfo.getText());
			}
			assertEquals(
					"button.getFont().deriveFont(button.getFont().getStyle() & ~java.awt.Font.ITALIC)",
					fontInfo.getSource());
		}
		// +Bold +Italic
		{
			FontInfo fontInfo =
					new DerivedFontInfo(baseFont,
							baseFontSource,
							baseFontClipboardSource,
							null,
							Boolean.TRUE,
							Boolean.TRUE,
							null,
							null);
			Font font = fontInfo.getFont();
			assertEquals(Font.BOLD | Font.ITALIC, font.getStyle());
			assertEquals(12, font.getSize());
			if (EnvironmentUtils.IS_WINDOWS) {
				assertEquals("Arial", font.getFamily());
				assertEquals("+Bold +Italic, Arial 12 Bold Italic", fontInfo.getText());
			}
			assertEquals(
					"button.getFont().deriveFont(button.getFont().getStyle() | java.awt.Font.BOLD | java.awt.Font.ITALIC)",
					fontInfo.getSource());
		}
		// -Bold +Italic
		{
			FontInfo fontInfo =
					new DerivedFontInfo(baseFont,
							baseFontSource,
							baseFontClipboardSource,
							null,
							Boolean.FALSE,
							Boolean.TRUE,
							null,
							null);
			Font font = fontInfo.getFont();
			assertEquals(Font.ITALIC, font.getStyle());
			assertEquals(12, font.getSize());
			if (EnvironmentUtils.IS_WINDOWS) {
				assertEquals("Arial", font.getFamily());
				assertEquals("-Bold +Italic, Arial 12 Italic", fontInfo.getText());
			}
			assertEquals(
					"button.getFont().deriveFont(button.getFont().getStyle() & ~java.awt.Font.BOLD | java.awt.Font.ITALIC)",
					fontInfo.getSource());
		}
		// +Bold -Italic
		{
			FontInfo fontInfo =
					new DerivedFontInfo(baseFont,
							baseFontSource,
							baseFontClipboardSource,
							null,
							Boolean.TRUE,
							Boolean.FALSE,
							null,
							null);
			Font font = fontInfo.getFont();
			assertEquals(Font.BOLD, font.getStyle());
			assertEquals(12, font.getSize());
			if (EnvironmentUtils.IS_WINDOWS) {
				assertEquals("Arial", font.getFamily());
				assertEquals("+Bold -Italic, Arial 12 Bold", fontInfo.getText());
			}
			assertEquals(
					"button.getFont().deriveFont(button.getFont().getStyle() & ~java.awt.Font.ITALIC | java.awt.Font.BOLD)",
					fontInfo.getSource());
		}
		// -Bold -Italic
		{
			FontInfo fontInfo =
					new DerivedFontInfo(baseFont,
							baseFontSource,
							baseFontClipboardSource,
							null,
							Boolean.FALSE,
							Boolean.FALSE,
							null,
							null);
			Font font = fontInfo.getFont();
			assertEquals(Font.PLAIN, font.getStyle());
			assertEquals(12, font.getSize());
			if (EnvironmentUtils.IS_WINDOWS) {
				assertEquals("Arial", font.getFamily());
				assertEquals("-Bold -Italic, Arial 12", fontInfo.getText());
			}
			assertEquals(
					"button.getFont().deriveFont(button.getFont().getStyle() & ~java.awt.Font.BOLD & ~java.awt.Font.ITALIC)",
					fontInfo.getSource());
		}
		// +5
		{
			FontInfo fontInfo =
					new DerivedFontInfo(baseFont,
							baseFontSource,
							baseFontClipboardSource,
							null,
							null,
							null,
							+5,
							null);
			Font font = fontInfo.getFont();
			assertEquals(Font.BOLD, font.getStyle());
			assertEquals(12 + 5, font.getSize());
			if (EnvironmentUtils.IS_WINDOWS) {
				assertEquals("Arial", font.getFamily());
				assertEquals("+5, Arial 17 Bold", fontInfo.getText());
			}
			assertEquals(
					"button.getFont().deriveFont(button.getFont().getSize() + 5f)",
					fontInfo.getSource());
		}
		// -5
		{
			FontInfo fontInfo =
					new DerivedFontInfo(baseFont,
							baseFontSource,
							baseFontClipboardSource,
							null,
							null,
							null,
							-5,
							null);
			Font font = fontInfo.getFont();
			assertEquals(Font.BOLD, font.getStyle());
			assertEquals(12 - 5, font.getSize());
			if (EnvironmentUtils.IS_WINDOWS) {
				assertEquals("Arial", font.getFamily());
				assertEquals("-5, Arial 7 Bold", fontInfo.getText());
			}
			assertEquals(
					"button.getFont().deriveFont(button.getFont().getSize() - 5f)",
					fontInfo.getSource());
		}
		// =20
		{
			FontInfo fontInfo =
					new DerivedFontInfo(baseFont,
							baseFontSource,
							baseFontClipboardSource,
							null,
							null,
							null,
							null,
							20);
			Font font = fontInfo.getFont();
			assertEquals(Font.BOLD, font.getStyle());
			assertEquals(20, font.getSize());
			if (EnvironmentUtils.IS_WINDOWS) {
				assertEquals("Arial", font.getFamily());
				assertEquals("20, Arial 20 Bold", fontInfo.getText());
			}
			assertEquals("button.getFont().deriveFont(20f)", fontInfo.getSource());
		}
		// -Bold =20
		{
			FontInfo fontInfo =
					new DerivedFontInfo(baseFont,
							baseFontSource,
							baseFontClipboardSource,
							null,
							Boolean.FALSE,
							null,
							null,
							20);
			Font font = fontInfo.getFont();
			assertEquals(Font.PLAIN, font.getStyle());
			assertEquals(20, font.getSize());
			if (EnvironmentUtils.IS_WINDOWS) {
				assertEquals("Arial", font.getFamily());
				assertEquals("20 -Bold, Arial 20", fontInfo.getText());
			}
			assertEquals(
					"button.getFont().deriveFont(button.getFont().getStyle() & ~java.awt.Font.BOLD, 20f)",
					fontInfo.getSource());
		}
		// -Italic =20
		{
			FontInfo fontInfo =
					new DerivedFontInfo(baseFont,
							baseFontSource,
							baseFontClipboardSource,
							null,
							null,
							Boolean.FALSE,
							null,
							20);
			Font font = fontInfo.getFont();
			assertEquals(Font.BOLD, font.getStyle());
			assertEquals(20, font.getSize());
			if (EnvironmentUtils.IS_WINDOWS) {
				assertEquals("Arial", font.getFamily());
				assertEquals("20 -Italic, Arial 20 Bold", fontInfo.getText());
			}
			assertEquals(
					"button.getFont().deriveFont(button.getFont().getStyle() & ~java.awt.Font.ITALIC, 20f)",
					fontInfo.getSource());
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// FontInfo decoding from source
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_decodeFontInfo_null() throws Exception {
		String text = null;
		String source = "null";
		assertFont(source, text);
	}

	@Test
	public void test_decodeFontInfo_explicit() throws Exception {
		String text = "Arial 15 Bold Italic";
		String source = "new Font(\"Arial\", Font.BOLD | Font.ITALIC, 15)";
		String clipboard =
				"new java.awt.Font(\"Arial\", java.awt.Font.BOLD | java.awt.Font.ITALIC, 15)";
		assertFont(source, text, clipboard);
	}

	@Test
	public void test_decodeFontInfo_UIManager() throws Exception {
		String text = "Button.font, Dialog 12 Bold";
		String source = "javax.swing.UIManager.getFont(\"Button.font\")";
		assertFont(source, text);
	}

	@Test
	public void test_decodeFontInfo_derived_newSize() throws Exception {
		String text = "20, Dialog 20 Bold";
		String source = "button.getFont().deriveFont(20f)";
		assertFont2(source, text);
	}

	@Test
	public void test_decodeFontInfo_derived_plusSize() throws Exception {
		String text = "+5, Dialog 17 Bold";
		String source = "button.getFont().deriveFont(button.getFont().getSize() + 5f)";
		assertFont2(source, text);
	}

	@Test
	public void test_decodeFontInfo_derived_minusSize() throws Exception {
		String text = "-5, Dialog 7 Bold";
		String source = "button.getFont().deriveFont(button.getFont().getSize() - 5f)";
		assertFont2(source, text);
	}

	@Test
	public void test_decodeFontInfo_derived_unknownSize() throws Exception {
		String text = "15, Dialog 15 Bold";
		String source = "button.getFont().deriveFont(5f + 10f)";
		String clipboardSource = "%this%.getFont().deriveFont(15f)";
		assertFont(source, text, clipboardSource);
	}

	@Test
	public void test_decodeFontInfo_derived_plusBold() throws Exception {
		String text = "+Bold, Dialog 12 Bold";
		String source = "button.getFont().deriveFont(button.getFont().getStyle() | java.awt.Font.BOLD)";
		assertFont2(source, text);
	}

	@Test
	public void test_decodeFontInfo_derived_plusItalic() throws Exception {
		String text = "+Italic, Dialog 12 Bold Italic";
		String source =
				"button.getFont().deriveFont(button.getFont().getStyle() | java.awt.Font.ITALIC)";
		assertFont2(source, text);
	}

	@Test
	public void test_decodeFontInfo_derived_plusBoldItalic() throws Exception {
		String text = "+Bold +Italic, Dialog 12 Bold Italic";
		String source =
				"button.getFont().deriveFont(button.getFont().getStyle() | java.awt.Font.BOLD | java.awt.Font.ITALIC)";
		assertFont2(source, text);
	}

	@Test
	public void test_decodeFontInfo_derived_minusBold() throws Exception {
		String text = "-Bold, Dialog 12";
		String source =
				"button.getFont().deriveFont(button.getFont().getStyle() & ~java.awt.Font.BOLD)";
		assertFont2(source, text);
	}

	@Test
	public void test_decodeFontInfo_derived_minusItalic() throws Exception {
		String text = "-Italic, Dialog 12 Bold";
		String source =
				"button.getFont().deriveFont(button.getFont().getStyle() & ~java.awt.Font.ITALIC)";
		assertFont2(source, text);
	}

	@Test
	public void test_decodeFontInfo_derived_minusBoldItalic() throws Exception {
		String text = "-Bold -Italic, Dialog 12";
		String source =
				"button.getFont().deriveFont(button.getFont().getStyle() & ~java.awt.Font.BOLD & ~java.awt.Font.ITALIC)";
		assertFont2(source, text);
	}

	@Test
	public void test_decodeFontInfo_derived_minusBold_plusItalic() throws Exception {
		String text = "-Bold +Italic, Dialog 12 Italic";
		String source =
				"button.getFont().deriveFont(button.getFont().getStyle() & ~java.awt.Font.BOLD | java.awt.Font.ITALIC)";
		assertFont2(source, text);
	}

	@Test
	public void test_decodeFontInfo_derived_plusBold_minusItalic() throws Exception {
		String text = "+Bold -Italic, Dialog 12 Bold";
		String source =
				"button.getFont().deriveFont(button.getFont().getStyle() & ~java.awt.Font.ITALIC | java.awt.Font.BOLD)";
		assertFont2(source, text);
	}

	@Test
	public void test_decodeFontInfo_derived_minusBold_plusSize() throws Exception {
		String text = "+5 -Bold, Dialog 17";
		String source =
				"button.getFont().deriveFont("
						+ "button.getFont().getStyle() & ~java.awt.Font.BOLD, "
						+ "button.getFont().getSize() + 5f)";
		assertFont2(source, text);
	}

	@Test
	public void test_decodeFontInfo_derived_newFamily() throws Exception {
		String text = "*Arial +5 -Bold, Arial 17";
		String source =
				"new java.awt.Font(\"Arial\", "
						+ "button.getFont().getStyle() & ~java.awt.Font.BOLD, "
						+ "button.getFont().getSize() + 5)";
		String clipboardSource = null; // unsupported
		assertFont(source, text, clipboardSource);
	}

	private void assertFont2(String fontSource, String expectedText) throws Exception {
		String expectedClipboard = StringUtils.replace(fontSource, "button.", "%this%.");
		assertFont(fontSource, expectedText, expectedClipboard);
	}

	private void assertFont(String fontSource, String expectedText) throws Exception {
		assertFont(fontSource, expectedText, fontSource);
	}

	private void assertFont(String fontSource, String expectedText, String expectedClipboard)
			throws Exception {
		String fontLine = fontSource != null ? "    button.setFont(" + fontSource + ");" : "";
		ContainerInfo panel =
				parseContainer(
						"public class Test extends JPanel {",
						"  public Test() {",
						"    JButton button = new JButton();",
						fontLine,
						"    add(button);",
						"  }",
						"}");
		ComponentInfo button = panel.getChildrenComponents().get(0);
		// property
		Property property = button.getPropertyByTitle("font");
		if (EnvironmentUtils.IS_WINDOWS) {
			assertEquals(expectedText, getPropertyText(property));
		}
		// clipboard source
		{
			GenericPropertyImpl genericProperty = (GenericPropertyImpl) property;
			String actualClipboard = genericProperty.getClipboardSource();
			if (EnvironmentUtils.IS_WINDOWS) {
				assertEquals(expectedClipboard, actualClipboard);
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Copy/paste
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_copyPaste_null() throws Exception {
		String originalSource = "null";
		String expectedSource = originalSource;
		check_copyPaste(originalSource, expectedSource);
	}

	@Test
	public void test_copyPaste_explicit() throws Exception {
		String originalSource = "new Font(\"Arial\", Font.BOLD | Font.ITALIC, 15)";
		String expectedSource = originalSource;
		check_copyPaste(originalSource, expectedSource);
	}

	@Test
	public void test_copyPaste_derived() throws Exception {
		String originalSource = "myLabel.getFont().deriveFont(20f)";
		String expectedSource = "label.getFont().deriveFont(20f)";
		check_copyPaste(originalSource, expectedSource);
	}

	private void check_copyPaste(String originalSource, String expectedSource) throws Exception {
		String[] lines1 =
			{
					"public class Test extends JPanel {",
					"  public Test() {",
					"    {",
					"      JLabel myLabel = new JLabel();",
					"      myLabel.setFont(" + originalSource + ");",
					"      add(myLabel);",
					"    }",
					"  }",
			"}"};
		ContainerInfo panel = parseContainer(lines1);
		panel.refresh();
		ComponentInfo label = panel.getChildrenComponents().get(0);
		//
		{
			JavaInfoMemento memento = JavaInfoMemento.createMemento(label);
			ComponentInfo newLabel = (ComponentInfo) memento.create(panel);
			((FlowLayoutInfo) panel.getLayout()).add(newLabel, null);
			memento.apply();
		}
		String[] lines =
			{
					"public class Test extends JPanel {",
					"  public Test() {",
					"    {",
					"      JLabel myLabel = new JLabel();",
					"      myLabel.setFont(" + originalSource + ");",
					"      add(myLabel);",
					"    }",
					"    {",
					"      JLabel label = new JLabel();",
					"      label.setFont(" + expectedSource + ");",
					"      add(label);",
					"    }",
					"  }",
			"}"};
		assertEditor(lines);
	}
}
