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
package org.eclipse.wb.tests.designer.XML.model;

import org.eclipse.wb.internal.core.utils.check.AssertionFailedException;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.creation.ElementCreationSupport;
import org.eclipse.wb.internal.core.xml.model.creation.ILiveCreationSupport;
import org.eclipse.wb.internal.core.xml.model.description.CreationDescription;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;
import org.eclipse.wb.tests.designer.XML.model.description.AbstractCoreTest;

import static org.mockito.Mockito.mock;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.NotImplementedException;
import org.junit.Test;

/**
 * Test for {@link ElementCreationSupport}.
 *
 * @author scheglov_ke
 */
public class ElementCreationSupportTest extends AbstractCoreTest {
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
	// CreationSupport
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for abstract {@link CreationSupport}.
	 */
	@Test
	public void test_CreationSupport() throws Exception {
		CreationSupport creationSupport = mock(CreationSupport.class);
		// no getTitle()
		try {
			creationSupport.getTitle();
			fail();
		} catch (NotImplementedException e) {
		}
		// no getElement()
		try {
			creationSupport.getElement();
			fail();
		} catch (NotImplementedException e) {
		}
		// no getElementMove()
		try {
			creationSupport.getElementMove();
			fail();
		} catch (NotImplementedException e) {
		}
		// no addElement()
		try {
			creationSupport.addElement(null, 0);
			fail();
		} catch (NotImplementedException e) {
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for applying {@link CreationDescription} parameters into {@link XmlObjectInfo}.
	 */
	@Test
	public void test_CreationDescription_withParameters() throws Exception {
		prepareMyComponent(ArrayUtils.EMPTY_STRING_ARRAY, new String[]{
				"  <creation id='withParameters'>",
				"    <source>NA</source>",
				"    <parameter name='name_1'>value_1</parameter>",
				"    <parameter name='name_2'>value_2</parameter>",
				"  </creation>",});
		waitForAutoBuild();
		// parse for context
		parse("<Shell/>");
		// check
		XmlObjectInfo object = createObject("test.MyComponent", "withParameters");
		assertEquals("value_1", XmlObjectUtils.getParameter(object, "name_1"));
		assertEquals("value_2", XmlObjectUtils.getParameter(object, "name_2"));
	}

	/**
	 * Test for {@link ILiveCreationSupport} implementation.
	 */
	@Test
	public void test_ILiveCreationSupport() throws Exception {
		parse("<Shell/>");
		// create CreationSupport
		ILiveCreationSupport creationSupport = new ElementCreationSupport("check", true);
		assertEquals("check true", creationSupport.toString());
		// create "live" CreationSupport
		CreationSupport liveCreationSupport = creationSupport.getLiveComponentCreation();
		assertEquals("check", ReflectionUtils.getFieldString(liveCreationSupport, "m_creationId"));
		assertEquals(true, ReflectionUtils.getFieldBoolean(liveCreationSupport, "m_addAttributes"));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_toString() throws Exception {
		XmlObjectInfo shell =
				parse(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"<Shell text='My text'>",
						"  <Button/>",
						"</Shell>");
		assertEquals(getSourceDQ("<Shell text='My text'>"), shell.getCreationSupport().toString());
	}

	@Test
	public void test_getTitle() throws Exception {
		XmlObjectInfo shell =
				parse(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"<Shell>",
						"  <Button wbp:name='button'/>",
						"</Shell>");
		XmlObjectInfo button = getObjectByName("button");
		assertEquals("Shell", shell.getCreationSupport().getTitle());
		assertEquals("Button", button.getCreationSupport().getTitle());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Delete
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link ElementCreationSupport#delete()}.
	 */
	@Test
	public void test_delete() throws Exception {
		parse(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Button wbp:name='button'/>",
				"</Shell>");
		XmlObjectInfo button = getObjectByName("button");
		//
		assertTrue(button.canDelete());
		button.delete();
		assertXML("<Shell/>");
	}

	/**
	 * Test for {@link ElementCreationSupport#delete()}.
	 */
	@Test
	public void test_delete_withIntermadiateElements() throws Exception {
		parse(
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Shell.layout>",
				"    <FillLayout wbp:name='layout'/>",
				"  </Shell.layout>",
				"</Shell>");
		XmlObjectInfo layout = getObjectByName("layout");
		//
		assertTrue(layout.canDelete());
		layout.delete();
		assertXML("<Shell/>");
	}

	/**
	 * Test for {@link ElementCreationSupport#delete()}.
	 */
	@Test
	public void test_delete_withIntermadiateChildren() throws Exception {
		parse(
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Composite wbp:name='composite'>",
				"    <Button/>",
				"  </Composite>",
				"  <Button text='Other'/>",
				"</Shell>");
		XmlObjectInfo composite = getObjectByName("composite");
		//
		assertTrue(composite.canDelete());
		composite.delete();
		assertXML(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Button text='Other'/>",
				"</Shell>");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// addElement() tag
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link ElementCreationSupport#addElement(DocumentElement, int)}.
	 */
	@Test
	public void test_addElement_standardSWT() throws Exception {
		XmlObjectInfo container = parse("<Shell/>");
		DocumentElement containerElement = container.getCreationSupport().getElement();
		// add
		XmlObjectInfo newObject = createObject("org.eclipse.swt.widgets.Button", "empty");
		newObject.getCreationSupport().addElement(containerElement, 0);
		assertXML(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Button/>",
				"</Shell>");
	}

	/**
	 * Test for {@link ElementCreationSupport#addElement(DocumentElement, int)}.
	 * <p>
	 * Using {@link ElementCreationSupport#ElementCreationSupport()} constructor.
	 */
	@Test
	public void test_addElement_constructorWithoutId() throws Exception {
		XmlObjectInfo container = parse("<Shell/>");
		// add
		DocumentElement containerElement = container.getCreationSupport().getElement();
		XmlObjectInfo newObject =
				XmlObjectUtils.createObject(
						m_lastContext,
						"org.eclipse.swt.widgets.Button",
						new ElementCreationSupport());
		newObject.getCreationSupport().addElement(containerElement, 0);
		assertXML(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Button text='New Button'/>",
				"</Shell>");
	}

	/**
	 * Test for {@link ElementCreationSupport#addElement(DocumentElement, int)}.
	 */
	@Test
	public void test_addElement_noSuchCreationID() throws Exception {
		XmlObjectInfo container = parse("<Shell/>");
		DocumentElement containerElement = container.getCreationSupport().getElement();
		// add
		try {
			XmlObjectInfo newObject = createObject("org.eclipse.swt.widgets.Button", "noSuchID");
			newObject.getCreationSupport().addElement(containerElement, 0);
			fail();
		} catch (AssertionFailedException e) {
		}
	}

	/**
	 * Test for {@link ElementCreationSupport#addElement(DocumentElement, int)}.
	 * <p>
	 * Package has no namespace yet.
	 */
	@Test
	public void test_addElement_inCustomPackage() throws Exception {
		m_getSource_includeStandardNamespaces = false;
		prepareMyComponent(ArrayUtils.EMPTY_STRING_ARRAY);
		XmlObjectInfo container = parse("<Shell/>");
		DocumentElement containerElement = container.getCreationSupport().getElement();
		// add
		XmlObjectInfo newObject = createObject("test.MyComponent");
		newObject.getCreationSupport().addElement(containerElement, 0);
		assertXML(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell xmlns:p1='clr-namespace:test'>",
				"  <p1:MyComponent/>",
				"</Shell>");
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// addElement() attributes
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link ElementCreationSupport#addElement(DocumentElement, int)}.
	 * <p>
	 * Attribute without namespace, just add as is.
	 */
	@Test
	public void test_addElement_attributes_noNamespace() throws Exception {
		prepareMyComponent(new String[]{
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"public void setText(String v) {",
		"}"}, new String[]{
				"<creation>",
				"  <source/>",
				"  <x-attribute name='text' value='My text'/>",
		"</creation>"});
		XmlObjectInfo container = parse("<Shell/>");
		DocumentElement containerElement = container.getCreationSupport().getElement();
		// add
		XmlObjectInfo newObject = createObject("test.MyComponent");
		newObject.getCreationSupport().addElement(containerElement, 0);
		assertXML(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell>",
				"  <t:MyComponent text='My text'/>",
				"</Shell>");
	}

	/**
	 * Test for {@link ElementCreationSupport#addElement(DocumentElement, int)}.
	 * <p>
	 * Attribute with namespace, declare and use it.
	 */
	@Test
	public void test_addElement_attributes_newNamespace() throws Exception {
		prepareMyComponent(new String[]{
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"public void setText(String v) {",
		"}"}, new String[]{
				"<creation>",
				"  <source/>",
				"  <x-attribute ns='http://my.namespace.com' name='text' value='My text'/>",
		"</creation>"});
		XmlObjectInfo container = parse("<Shell/>");
		DocumentElement containerElement = container.getCreationSupport().getElement();
		// add
		XmlObjectInfo newObject = createObject("test.MyComponent");
		newObject.getCreationSupport().addElement(containerElement, 0);
		assertXML(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell xmlns:a1='http://my.namespace.com'>",
				"  <t:MyComponent a1:text='My text'/>",
				"</Shell>");
	}

	/**
	 * Test for {@link ElementCreationSupport#addElement(DocumentElement, int)}.
	 * <p>
	 * Attribute with namespace, which is already declared, so just use it.
	 */
	@Test
	public void test_addElement_attributes_existingNamespace() throws Exception {
		prepareMyComponent(new String[]{
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"public void setText(String v) {",
		"}"}, new String[]{
				"<creation>",
				"  <source/>",
				"  <x-attribute ns='http://my.namespace.com' name='text' value='My text'/>",
		"</creation>"});
		XmlObjectInfo container = parse("<Shell xmlns:myNS='http://my.namespace.com'/>");
		DocumentElement containerElement = container.getCreationSupport().getElement();
		// add
		XmlObjectInfo newObject = createObject("test.MyComponent");
		newObject.getCreationSupport().addElement(containerElement, 0);
		assertXML(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell xmlns:myNS='http://my.namespace.com'>",
				"  <t:MyComponent myNS:text='My text'/>",
				"</Shell>");
	}

	/**
	 * Test for {@link ElementCreationSupport#addElement(DocumentElement, int)}.
	 * <p>
	 * Using "x-content" element.
	 */
	@Test
	public void test_addElement_content() throws Exception {
		prepareMyComponent(new String[]{
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"public void setText(String v) {",
		"}"}, new String[]{
				"<creation>",
				"  <source/>",
				"  <x-content>My content</x-content>",
		"</creation>"});
		XmlObjectInfo container = parse("<Shell/>");
		DocumentElement containerElement = container.getCreationSupport().getElement();
		// add
		XmlObjectInfo newObject = createObject("test.MyComponent");
		newObject.getCreationSupport().addElement(containerElement, 0);
		assertXML(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell>",
				"  <t:MyComponent>My content</t:MyComponent>",
				"</Shell>");
	}
}
