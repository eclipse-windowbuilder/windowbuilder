/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.core.model.property;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.description.helpers.DescriptionPropertiesHelper;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.converter.BooleanArrayConverter;
import org.eclipse.wb.internal.core.model.property.converter.BooleanConverter;
import org.eclipse.wb.internal.core.model.property.converter.BooleanObjectConverter;
import org.eclipse.wb.internal.core.model.property.converter.ByteConverter;
import org.eclipse.wb.internal.core.model.property.converter.ByteObjectConverter;
import org.eclipse.wb.internal.core.model.property.converter.CharacterConverter;
import org.eclipse.wb.internal.core.model.property.converter.DoubleConverter;
import org.eclipse.wb.internal.core.model.property.converter.DoubleObjectConverter;
import org.eclipse.wb.internal.core.model.property.converter.ExpressionConverter;
import org.eclipse.wb.internal.core.model.property.converter.FloatConverter;
import org.eclipse.wb.internal.core.model.property.converter.IntegerArrayConverter;
import org.eclipse.wb.internal.core.model.property.converter.IntegerConverter;
import org.eclipse.wb.internal.core.model.property.converter.IntegerObjectConverter;
import org.eclipse.wb.internal.core.model.property.converter.LocaleConverter;
import org.eclipse.wb.internal.core.model.property.converter.LongConverter;
import org.eclipse.wb.internal.core.model.property.converter.LongObjectConverter;
import org.eclipse.wb.internal.core.model.property.converter.ShortConverter;
import org.eclipse.wb.internal.core.model.property.converter.ShortObjectConverter;
import org.eclipse.wb.internal.core.model.property.converter.StringArrayConverter;
import org.eclipse.wb.internal.core.model.property.converter.StringConverter;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.core.annotations.DisposeProjectAfter;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.JavaCore;

import org.junit.Test;

import java.util.Locale;

/**
 * Tests for standard {@link ExpressionConverter}'s.
 *
 * @author scheglov_ke
 */
public class StandardConvertersTest extends SwingModelTest {
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
	// Tests using property
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_property_boolean() throws Exception {
		check_converter("boolean", Boolean.TRUE, "true");
	}

	@Test
	public void test_property_short() throws Exception {
		check_converter("short", Short.valueOf((short) 123), "(short) 123");
	}

	@Test
	public void test_property_int() throws Exception {
		check_converter("int", Integer.valueOf(123), "123");
	}

	@Test
	public void test_property_float() throws Exception {
		check_converter("float", Float.valueOf(123.4f), "123.4f");
	}

	@Test
	public void test_property_double() throws Exception {
		check_converter("double", Double.valueOf(123.4), "123.4");
	}

	@Test
	public void test_property_String() throws Exception {
		check_converter("String", "abc", "\"abc\"");
	}

	/**
	 * Check that when we set value for property with given type, we have expected source generated.
	 */
	private void check_converter(String typeName, Object value, String expectedSource)
			throws Exception {
		setFileContentSrc(
				"test/Base.java",
				getTestSource(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"public class Base extends JPanel {",
						"  public void setValue(" + typeName + " value) {",
						"  }",
						"}"));
		waitForAutoBuild();
		//
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public final class Test extends Base {",
						"  public Test() {",
						"  }",
						"}");
		Property property = panel.getPropertyByTitle("value");
		property.setValue(value);
		assertEditor(
				"// filler filler filler",
				"public final class Test extends Base {",
				"  public Test() {",
				"    setValue(" + expectedSource + ");",
				"  }",
				"}");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Separate tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_BooleanConverter() throws Exception {
		assertConverterEditor(boolean.class);
		ExpressionConverter converter = BooleanConverter.INSTANCE;
		assertEquals("false", converter.toJavaSource(null, Boolean.FALSE));
		assertEquals("true", converter.toJavaSource(null, Boolean.TRUE));
	}

	@Test
	public void test_CharacterConverter() throws Exception {
		assertConverterEditor(char.class);
		ExpressionConverter converter = CharacterConverter.INSTANCE;
		assertEquals("'0'", converter.toJavaSource(null, Character.valueOf('0')));
	}

	@Test
	public void test_ByteConverter() throws Exception {
		assertConverterEditor(byte.class);
		ExpressionConverter converter = ByteConverter.INSTANCE;
		assertEquals("(byte) 1", converter.toJavaSource(null, Byte.valueOf((byte) 1)));
		assertEquals("(byte) -1", converter.toJavaSource(null, Byte.valueOf((byte) -1)));
	}

	@Test
	public void test_ShortConverter() throws Exception {
		assertConverterEditor(short.class);
		ExpressionConverter converter = ShortConverter.INSTANCE;
		assertEquals("(short) 1", converter.toJavaSource(null, Short.valueOf((short) 1)));
		assertEquals("(short) -1", converter.toJavaSource(null, Short.valueOf((short) -1)));
	}

	@Test
	public void test_IntegerConverter() throws Exception {
		assertConverterEditor(int.class);
		ExpressionConverter converter = IntegerConverter.INSTANCE;
		assertEquals("1", converter.toJavaSource(null, Integer.valueOf(1)));
		assertEquals("-1", converter.toJavaSource(null, Integer.valueOf(-1)));
	}

	@Test
	public void test_LongConverter() throws Exception {
		assertConverterEditor(long.class);
		ExpressionConverter converter = LongConverter.INSTANCE;
		assertEquals("1L", converter.toJavaSource(null, Long.valueOf(1)));
		assertEquals("-1L", converter.toJavaSource(null, Long.valueOf(-1)));
	}

	@Test
	public void test_FloatConverter() throws Exception {
		assertConverterEditor(float.class);
		ExpressionConverter converter = FloatConverter.INSTANCE;
		assertEquals("0.0f", converter.toJavaSource(null, Float.valueOf(0.0f)));
		assertEquals("1.0f", converter.toJavaSource(null, Float.valueOf(1.0f)));
		assertEquals("1.2f", converter.toJavaSource(null, Float.valueOf(1.2f)));
		assertEquals("-1.2f", converter.toJavaSource(null, Float.valueOf(-1.2f)));
	}

	@Test
	public void test_DoubleConverter() throws Exception {
		assertConverterEditor(double.class);
		ExpressionConverter converter = DoubleConverter.INSTANCE;
		// Double value
		assertEquals("0.0", converter.toJavaSource(null, Double.valueOf(0.0)));
		assertEquals("1.0", converter.toJavaSource(null, Double.valueOf(1.0)));
		assertEquals("1.2", converter.toJavaSource(null, Double.valueOf(1.2)));
		assertEquals("-1.2", converter.toJavaSource(null, Double.valueOf(-1.2)));
		// other Number values
		assertEquals("1.0", converter.toJavaSource(null, 1));
		assertEquals("1.0", converter.toJavaSource(null, (byte) 1));
		assertEquals("1.0", converter.toJavaSource(null, (short) 1));
		assertEquals("1.0", converter.toJavaSource(null, (float) 1));
	}

	@Test
	public void test_StringArrayConverter() throws Exception {
		assertConverterEditor(String[].class);
		ExpressionConverter converter = StringArrayConverter.INSTANCE;
		assertEquals("(String[]) null", converter.toJavaSource(null, null));
		assertEquals("new String[] {}", converter.toJavaSource(null, new String[]{}));
		assertEquals("new String[] {\"a\"}", converter.toJavaSource(null, new String[]{"a"}));
		assertEquals(
				"new String[] {\"a\", \"b\"}",
				converter.toJavaSource(null, new String[]{"a", "b"}));
	}

	@Test
	public void test_BooleanArrayConverter() throws Exception {
		assertNotNull(DescriptionPropertiesHelper.getConverterForType(boolean[].class));
		ExpressionConverter converter = BooleanArrayConverter.INSTANCE;
		assertEquals("(boolean[]) null", converter.toJavaSource(null, null));
		assertEquals("new boolean[] {}", converter.toJavaSource(null, new boolean[]{}));
		assertEquals("new boolean[] {true}", converter.toJavaSource(null, new boolean[]{true}));
		assertEquals(
				"new boolean[] {false, true}",
				converter.toJavaSource(null, new boolean[]{false, true}));
	}

	@Test
	public void test_IntegerArrayConverter() throws Exception {
		assertNotNull(DescriptionPropertiesHelper.getConverterForType(int[].class));
		ExpressionConverter converter = IntegerArrayConverter.INSTANCE;
		assertEquals("(int[]) null", converter.toJavaSource(null, null));
		assertEquals("new int[] {}", converter.toJavaSource(null, new int[]{}));
		assertEquals("new int[] {1}", converter.toJavaSource(null, new int[]{1}));
		assertEquals("new int[] {1, 2, 3}", converter.toJavaSource(null, new int[]{1, 2, 3}));
	}

	@Test
	public void test_LocaleConverter() throws Exception {
		assertConverterEditor(Locale.class);
		//
		ExpressionConverter converter = LocaleConverter.INSTANCE;
		assertEquals("(java.util.Locale) null", converter.toJavaSource(null, null));
		assertEquals("java.util.Locale.ENGLISH", converter.toJavaSource(null, Locale.ENGLISH));
		assertEquals(
				"java.util.Locale.CANADA_FRENCH",
				converter.toJavaSource(null, new Locale("fr", "CA")));
		assertEquals("java.util.Locale.US", converter.toJavaSource(null, Locale.US));
		assertEquals("new java.util.Locale(\"ru\")", converter.toJavaSource(null, new Locale("ru")));
		assertEquals(
				"new java.util.Locale(\"ru\", \"RU\")",
				converter.toJavaSource(null, new Locale("ru", "RU")));
		assertEquals(
				"new java.util.Locale(\"no\\tlocale\")",
				converter.toJavaSource(null, new Locale("no\tlocale")));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Boolean as object
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_BooleanObjectConverter_noJava() throws Exception {
		assertConverterEditor(Boolean.class);
		ExpressionConverter converter = BooleanObjectConverter.INSTANCE;
		assertEquals("Boolean.FALSE", converter.toJavaSource(null, false));
		assertEquals("Boolean.TRUE", converter.toJavaSource(null, true));
	}

	@Test
	public void test_BooleanObjectConverter_null() throws Exception {
		ExpressionConverter converter = BooleanObjectConverter.INSTANCE;
		assertEquals("(Boolean) null", converter.toJavaSource(null, null));
	}

	@Test
	public void test_BooleanObjectConverter_forJava5() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		ExpressionConverter converter = BooleanObjectConverter.INSTANCE;
		assertEquals("(Boolean) null", converter.toJavaSource(panel, null));
		assertEquals("false", converter.toJavaSource(panel, false));
		assertEquals("true", converter.toJavaSource(panel, true));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Byte as object
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_ByteObjectConverter_noJava() throws Exception {
		assertConverterEditor(Byte.class);
		ExpressionConverter converter = ByteObjectConverter.INSTANCE;
		assertEquals("Byte.valueOf((byte) 1)", converter.toJavaSource(null, Byte.valueOf((byte) 1)));
		assertEquals("Byte.valueOf((byte) -1)", converter.toJavaSource(null, Byte.valueOf((byte) -1)));
	}

	@Test
	public void test_ByteObjectConverter_null() throws Exception {
		ExpressionConverter converter = ByteObjectConverter.INSTANCE;
		assertEquals("(Byte) null", converter.toJavaSource(null, null));
	}

	@Test
	public void test_ByteObjectConverter_forJava5() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		ExpressionConverter converter = ByteObjectConverter.INSTANCE;
		assertEquals("(byte) 1", converter.toJavaSource(panel, Byte.valueOf((byte) 1)));
		assertEquals("(byte) -1", converter.toJavaSource(panel, Byte.valueOf((byte) -1)));
	}

	@DisposeProjectAfter
	@Test
	public void test_ByteObjectConverter_forJava5_disableBoxing() throws Exception {
		m_javaProject.setOption(JavaCore.COMPILER_PB_AUTOBOXING, "error");
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		ExpressionConverter converter = ByteObjectConverter.INSTANCE;
		assertEquals("Byte.valueOf((byte) 1)", converter.toJavaSource(panel, Byte.valueOf((byte) 1)));
		assertEquals("Byte.valueOf((byte) -1)", converter.toJavaSource(panel, Byte.valueOf((byte) -1)));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Short as object
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_ShortObjectConverter_null() throws Exception {
		ExpressionConverter converter = ShortObjectConverter.INSTANCE;
		assertEquals("(Short) null", converter.toJavaSource(null, null));
	}

	@Test
	public void test_ShortObjectConverter_forJava5() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		ExpressionConverter converter = ShortObjectConverter.INSTANCE;
		assertEquals("(short) 1", converter.toJavaSource(panel, Short.valueOf((short) 1)));
		assertEquals("(short) -1", converter.toJavaSource(panel, Short.valueOf((short) -1)));
	}


	////////////////////////////////////////////////////////////////////////////
	//
	// Integer as object
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_IntegerObjectConverter_noJava() throws Exception {
		assertConverterEditor(Integer.class);
		ExpressionConverter converter = IntegerObjectConverter.INSTANCE;
		assertEquals("Integer.valueOf(1)", converter.toJavaSource(null, Integer.valueOf(1)));
		assertEquals("Integer.valueOf(-1)", converter.toJavaSource(null, Integer.valueOf(-1)));
	}

	@Test
	public void test_IntegerObjectConverter_null() throws Exception {
		ExpressionConverter converter = IntegerObjectConverter.INSTANCE;
		assertEquals("(Integer) null", converter.toJavaSource(null, null));
	}

	@Test
	public void test_IntegerObjectConverter_forJava5() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		ExpressionConverter converter = IntegerObjectConverter.INSTANCE;
		assertEquals("1", converter.toJavaSource(panel, Integer.valueOf(1)));
		assertEquals("-1", converter.toJavaSource(panel, Integer.valueOf(-1)));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Long as object
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_LongObjectConverter_noJava() throws Exception {
		assertConverterEditor(Long.class);
		ExpressionConverter converter = LongObjectConverter.INSTANCE;
		assertEquals("Long.valueOf(1L)", converter.toJavaSource(null, Long.valueOf(1)));
		assertEquals("Long.valueOf(-1L)", converter.toJavaSource(null, Long.valueOf(-1)));
	}

	@Test
	public void test_LongObjectConverter_null() throws Exception {
		ExpressionConverter converter = LongObjectConverter.INSTANCE;
		assertEquals("(Long) null", converter.toJavaSource(null, null));
	}


	@DisposeProjectAfter
	@Test
	public void test_LongObjectConverter_forJava5_disableBoxing() throws Exception {
		m_javaProject.setOption(JavaCore.COMPILER_PB_AUTOBOXING, "error");
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		ExpressionConverter converter = LongObjectConverter.INSTANCE;
		assertEquals("Long.valueOf(1L)", converter.toJavaSource(panel, Long.valueOf(1)));
		assertEquals("Long.valueOf(-1L)", converter.toJavaSource(panel, Long.valueOf(-1)));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Double as object
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_DoubleObjectConverter_noJava() throws Exception {
		assertConverterEditor(Double.class);
		ExpressionConverter converter = DoubleObjectConverter.INSTANCE;
		assertEquals("Double.valueOf(1.0)", converter.toJavaSource(null, Double.valueOf(1)));
		assertEquals("Double.valueOf(-1.0)", converter.toJavaSource(null, Double.valueOf(-1)));
		assertEquals("Double.valueOf(1.2)", converter.toJavaSource(null, Double.valueOf(1.2)));
		assertEquals("Double.valueOf(-2.3)", converter.toJavaSource(null, Double.valueOf(-2.3)));
	}

	@Test
	public void test_DoubleObjectConverter_null() throws Exception {
		ExpressionConverter converter = DoubleObjectConverter.INSTANCE;
		assertEquals("(Double) null", converter.toJavaSource(null, null));
	}

	@Test
	public void test_DoubleObjectConverter_forJava5() throws Exception {
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		ExpressionConverter converter = DoubleObjectConverter.INSTANCE;
		assertEquals("1.2", converter.toJavaSource(panel, Double.valueOf(1.2)));
		assertEquals("-1.2", converter.toJavaSource(panel, Double.valueOf(-1.2)));
	}

	@DisposeProjectAfter
	@Test
	public void test_DoubleObjectConverter_forJava5_disableBoxing() throws Exception {
		m_javaProject.setOption(JavaCore.COMPILER_PB_AUTOBOXING, "error");
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		ExpressionConverter converter = DoubleObjectConverter.INSTANCE;
		assertEquals("Double.valueOf(1.2)", converter.toJavaSource(panel, Double.valueOf(1.2)));
		assertEquals("Double.valueOf(-1.2)", converter.toJavaSource(panel, Double.valueOf(-1.2)));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// String
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_StringConverter() throws Exception {
		assertConverterEditor(String.class);
		ExpressionConverter converter = StringConverter.INSTANCE;
		// null
		assertEquals("(String) null", converter.toJavaSource(null, null));
		// (empty)
		assertEquals("\"\"", converter.toJavaSource(null, ""));
		// abc
		assertEquals("\"abc\"", converter.toJavaSource(null, "abc"));
		check_StringConverter_specialCharacters(null);
	}

	/**
	 * Sometimes source is in charset which can represent national symbols without encoding into "\\u"
	 * form.
	 */
	@Test
	public void test_StringConverter_hasFile_withCharset() throws Exception {
		ExpressionConverter converter = StringConverter.INSTANCE;
		ContainerInfo panel =
				parseContainer(
						"// filler filler filler",
						"public class Test extends JPanel {",
						"  public Test() {",
						"  }",
						"}");
		IFile file = (IFile) panel.getEditor().getModelUnit().getUnderlyingResource();
		String value = "\u0410\u0411\u0412";
		// ISO-8859-1, encoded
		{
			file.setCharset("ISO-8859-1", null);
			String source = converter.toJavaSource(panel, value);
			assertEquals('"' + "\\u0410\\u0411\\u0412" + '"', source);
		}
		// UTF-8, plain
		{
			file.setCharset("UTF-8", null);
			// Russian text
			{
				String source = converter.toJavaSource(panel, value);
				assertEquals('"' + "\u0410\u0411\u0412" + '"', source);
			}
			// special characters
			check_StringConverter_specialCharacters(panel);
		}
		// UTF-16, plain
		{
			file.setCharset("UTF-16", null);
			// Russian text
			{
				String source = converter.toJavaSource(panel, value);
				assertEquals('"' + "\u0410\u0411\u0412" + '"', source);
			}
			// special characters
			check_StringConverter_specialCharacters(panel);
		}
	}

	private static void check_StringConverter_specialCharacters(JavaInfo javaInfo) throws Exception {
		ExpressionConverter converter = StringConverter.INSTANCE;
		// \
		assertEquals("\"\\\\\"", converter.toJavaSource(javaInfo, "\\"));
		// /
		assertEquals("\"/\"", converter.toJavaSource(javaInfo, "/"));
		// \r\n\t
		assertEquals("\"\\r\\n\\t\"", converter.toJavaSource(javaInfo, "\r\n\t"));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Asserts that there are {@link ExpressionConverter} and {@link PropertyEditor} registered for
	 * given type, usually in plugin.xml.
	 */
	private static void assertConverterEditor(Class<?> type) throws Exception {
		assertNotNull(DescriptionPropertiesHelper.getConverterForType(type));
		assertNotNull(DescriptionPropertiesHelper.getEditorForType(type));
	}
}
