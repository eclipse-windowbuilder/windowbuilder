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

import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.xml.model.creation.TagCreationSupport;
import org.eclipse.wb.tests.designer.XML.model.description.AbstractCoreTest;

/**
 * Test for {@link TagCreationSupport}.
 *
 * @author scheglov_ke
 */
public class TagCreationSupportTest extends AbstractCoreTest {
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
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public void test_toString_hasElement() throws Exception {
		XmlObjectInfo shell =
				parse(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"<Shell text='My text'>",
						"  <Button/>",
						"</Shell>");
		DocumentElement element = shell.getElement();
		CreationSupport creationSupport = new TagCreationSupport(element);
		assertSame(element, creationSupport.getElement());
		assertEquals(getSourceDQ("<Shell text='My text'>"), creationSupport.toString());
	}

	public void test_toString_hasTag() throws Exception {
		CreationSupport creationSupport = new TagCreationSupport("myTag");
		assertEquals("myTag", creationSupport.toString());
	}

	public void test_getTitle() throws Exception {
		XmlObjectInfo shell =
				parse(
						"// filler filler filler filler filler",
						"// filler filler filler filler filler",
						"<Shell>",
						"  <Button wbp:name='button'/>",
						"</Shell>");
		XmlObjectInfo button = getObjectByName("button");
		{
			DocumentElement element = shell.getElement();
			CreationSupport creationSupport = new TagCreationSupport(element);
			assertEquals("Shell", creationSupport.getTitle());
		}
		{
			DocumentElement element = button.getElement();
			CreationSupport creationSupport = new TagCreationSupport(element);
			assertEquals("Button", creationSupport.getTitle());
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Delete
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link TagCreationSupport#delete()}.
	 */
	public void test_delete() throws Exception {
		parse(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell>",
				"  <Button wbp:name='button'/>",
				"</Shell>");
		XmlObjectInfo button = getObjectByName("button");
		//
		CreationSupport creationSupport = new TagCreationSupport(button.getElement());
		creationSupport.setObject(button);
		creationSupport.delete();
		assertXML("<Shell/>");
	}

	/**
	 * Test for {@link TagCreationSupport#delete()}.
	 */
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
		CreationSupport creationSupport = new TagCreationSupport(layout.getElement());
		creationSupport.setObject(layout);
		creationSupport.delete();
		assertXML("<Shell/>");
	}

	/**
	 * Test for {@link TagCreationSupport#delete()}.
	 */
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
		CreationSupport creationSupport = new TagCreationSupport(composite.getElement());
		creationSupport.setObject(composite);
		creationSupport.delete();
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
	 * Test for {@link TagCreationSupport#addElement(DocumentElement, int)}.
	 */
	public void test_addElement() throws Exception {
		XmlObjectInfo shell = parse("<Shell/>");
		DocumentElement shellElement = shell.getCreationSupport().getElement();
		// add
		CreationSupport creationSupport = new TagCreationSupport("myTag");
		creationSupport.addElement(shellElement, 0);
		assertXML(
				"// filler filler filler filler filler",
				"// filler filler filler filler filler",
				"<Shell>",
				"  <myTag/>",
				"</Shell>");
	}
}
