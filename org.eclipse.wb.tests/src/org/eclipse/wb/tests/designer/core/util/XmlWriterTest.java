/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.core.util;

import org.eclipse.wb.internal.core.utils.XmlWriter;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import org.junit.Test;

import java.io.DataOutputStream;
import java.io.StringWriter;

/**
 * Tests for {@link XmlWriter}.
 *
 * @author mitin_aa
 */
public class XmlWriterTest extends DesignerTestCase {
	private static final String lineSeparator = System.getProperty("line.separator");

	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Testing just create empty XML.
	 */
	@Test
	public void test_constructor() throws Exception {
		StringWriter stringWriter = new StringWriter();
		XmlWriter writer = new XmlWriter(stringWriter);
		writer.close();
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>", stringWriter.toString());
	}

	/**
	 * Create XML, open and close tag.
	 */
	@Test
	public void test_open_tag() throws Exception {
		StringWriter stringWriter = new StringWriter();
		XmlWriter writer = new XmlWriter(stringWriter);
		writer.openTag("root");
		writer.openTag("tag1");
		writer.closeTag();
		writer.closeTag();
		writer.close();
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ lineSeparator
				+ "<root>"
				+ lineSeparator
				+ "\t<tag1></tag1>"
				+ lineSeparator
				+ "</root>"
				+ lineSeparator, stringWriter.toString());
	}

	/**
	 * Open tag for adding attribute, close tag.
	 */
	@Test
	public void test_open_tag_for_attribute() throws Exception {
		StringWriter stringWriter = new StringWriter();
		XmlWriter writer = new XmlWriter(stringWriter);
		writer.beginTag("tag1");
		writer.closeTag();
		writer.close();
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ lineSeparator
				+ "<tag1/>"
				+ lineSeparator, stringWriter.toString());
	}

	/**
	 * Adding several tags with attributes (without tag value).
	 */
	@Test
	public void test_write_several_tags_with_attributes() throws Exception {
		StringWriter stringWriter = new StringWriter();
		XmlWriter writer = new XmlWriter(stringWriter);
		writer.openTag("root");
		writer.beginTag("tag1");
		writer.writeAttribute("attr1", "attrValue1");
		writer.closeTag();
		writer.beginTag("tag2");
		writer.writeAttribute("attr2", "attrValue2");
		writer.closeTag();
		writer.closeTag();
		writer.close();
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ lineSeparator
				+ "<root>"
				+ lineSeparator
				+ "\t<tag1 attr1=\"attrValue1\"/>"
				+ lineSeparator
				+ "\t<tag2 attr2=\"attrValue2\"/>"
				+ lineSeparator
				+ "</root>"
				+ lineSeparator, stringWriter.toString());
	}

	/**
	 * Open tag for adding attribute, add attribute, close tag.
	 */
	@Test
	public void test_open_tag_write_attribute() throws Exception {
		StringWriter stringWriter = new StringWriter();
		XmlWriter writer = new XmlWriter(stringWriter);
		writer.beginTag("tag1");
		writer.writeAttribute("attr1", "attrValue1");
		writer.closeTag();
		writer.close();
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ lineSeparator
				+ "<tag1 attr1=\"attrValue1\"/>"
				+ lineSeparator, stringWriter.toString());
	}

	/**
	 * Use {@link XmlWriter#writeAttribute(String, String)} when attribute value has non-Latin
	 * characters.
	 */
	@Test
	public void test_writeAttribute_withSpecialCharacters() throws Exception {
		StringWriter stringWriter = new StringWriter();
		XmlWriter writer = new XmlWriter(stringWriter);
		writer.beginTag("tag");
		writer.writeAttribute("attr", "abc\n\u0410<>'&");
		writer.closeTag();
		writer.close();
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ lineSeparator
				+ "<tag attr=\"abc&#10;А&lt;&gt;&apos;&amp;\"/>"
				+ lineSeparator, stringWriter.toString());
	}

	/**
	 * Open tag for adding attribute, add attribute, end tag, close tag.
	 */
	@Test
	public void test_open_tag_write_attribute_endTag() throws Exception {
		StringWriter stringWriter = new StringWriter();
		XmlWriter writer = new XmlWriter(stringWriter);
		writer.beginTag("tag1");
		writer.writeAttribute("attr1", "attrValue1");
		writer.endTag();
		writer.closeTag();
		writer.close();
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ lineSeparator
				+ "<tag1 attr1=\"attrValue1\"></tag1>"
				+ lineSeparator, stringWriter.toString());
	}

	/**
	 * Testing convenience method which adds tag, writes its value and closes tag.
	 */
	@Test
	public void test_open_close_tag_with_value() throws Exception {
		StringWriter stringWriter = new StringWriter();
		XmlWriter writer = new XmlWriter(stringWriter);
		writer.write("tag1", "value1");
		writer.close();
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ lineSeparator
				+ "<tag1>value1</tag1>"
				+ lineSeparator, stringWriter.toString());
	}

	/**
	 * Testing writing nested tags.
	 */
	@Test
	public void test_write_nested_tags() throws Exception {
		StringWriter stringWriter = new StringWriter();
		XmlWriter writer = new XmlWriter(stringWriter);
		writer.openTag("tag1");
		writer.openTag("tag2");
		writer.openTag("tag3");
		writer.closeTag();
		writer.closeTag();
		writer.closeTag();
		writer.close();
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ lineSeparator
				+ "<tag1>"
				+ lineSeparator
				+ "\t<tag2>"
				+ lineSeparator
				+ "\t\t<tag3></tag3>"
				+ lineSeparator
				+ "\t</tag2>"
				+ lineSeparator
				+ "</tag1>"
				+ lineSeparator, stringWriter.toString());
	}

	/**
	 * Testing writing CDATA.
	 */
	@Test
	public void test_write_cdata() throws Exception {
		StringWriter stringWriter = new StringWriter();
		XmlWriter writer = new XmlWriter(stringWriter);
		writer.openTag("tag1");
		writer.openTag("tag2");
		final DataOutputStream dataOutputStream = new DataOutputStream(writer.streamCDATA());
		dataOutputStream.writeBytes("cdata contents");
		dataOutputStream.close();
		writer.closeTag();
		writer.closeTag();
		writer.close();
		assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ lineSeparator
				+ "<tag1>"
				+ lineSeparator
				+ "\t<tag2><![CDATA["
				+ lineSeparator
				+ "cdata contents]]></tag2>"
				+ lineSeparator
				+ "</tag1>"
				+ lineSeparator, stringWriter.toString());
	}
}
