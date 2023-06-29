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
import org.eclipse.wb.internal.core.xml.model.utils.NamespacesHelper;
import org.eclipse.wb.tests.designer.XML.model.description.AbstractCoreTest;

/**
 * Test for {@link NamespacesHelper}.
 *
 * @author scheglov_ke
 */
public class NamespacesHelperTest extends AbstractCoreTest {
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
	// getURI()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link NamespacesHelper#getURI(String)}.
	 */
	public void test_getURI_no() throws Exception {
		XmlObjectInfo shell = parse("<Shell/>");
		DocumentElement element = shell.getElement();
		//
		String uri = NamespacesHelper.getURI(element, "noSuchName");
		assertEquals(null, uri);
	}

	/**
	 * Test for {@link NamespacesHelper#getURI(String)}.
	 */
	public void test_getURI_has() throws Exception {
		XmlObjectInfo shell = parse("<Shell xmlns:p='someURI'/>");
		DocumentElement element = shell.getElement();
		//
		String uri = NamespacesHelper.getURI(element, "p");
		assertEquals("someURI", uri);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// getName()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link NamespacesHelper#getName(String)}.
	 */
	public void test_getName_no() throws Exception {
		XmlObjectInfo shell = parse("<Shell/>");
		DocumentElement element = shell.getElement();
		//
		String name = NamespacesHelper.getName(element, "noSuchURI");
		assertEquals(null, name);
	}

	/**
	 * Test for {@link NamespacesHelper#getName(String)}.
	 */
	public void test_getName_has() throws Exception {
		XmlObjectInfo shell = parse("<Shell xmlns:myName='someURI'/>");
		DocumentElement element = shell.getElement();
		//
		String name = NamespacesHelper.getName(element, "someURI");
		assertEquals("myName", name);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ensureName()
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Test for {@link NamespacesHelper#ensureName(String, String)}.
	 */
	public void test_ensureName_existing() throws Exception {
		XmlObjectInfo shell = parse("<Shell xmlns:nm='someURI'/>");
		DocumentElement element = shell.getElement();
		//
		String name = NamespacesHelper.ensureName(element, "someURI", "nm");
		assertEquals("nm", name);
		assertXML("<Shell xmlns:nm='someURI'/>");
	}

	/**
	 * Test for {@link NamespacesHelper#ensureName(String, String)}.
	 */
	public void test_ensureName_new() throws Exception {
		XmlObjectInfo shell = parse("<Shell/>");
		DocumentElement element = shell.getElement();
		//
		String name = NamespacesHelper.ensureName(element, "someURI", "nm");
		assertEquals("nm1", name);
		assertXML("<Shell xmlns:nm1='someURI'/>");
	}

	/**
	 * Test for {@link NamespacesHelper#ensureName(String, String)}.
	 */
	public void test_ensureName_newConflict() throws Exception {
		XmlObjectInfo shell = parse("<Shell xmlns:nm1='existingURI'/>");
		DocumentElement element = shell.getElement();
		//
		String name = NamespacesHelper.ensureName(element, "someURI", "nm");
		assertEquals("nm2", name);
		assertXML("<Shell xmlns:nm1='existingURI' xmlns:nm2='someURI'/>");
	}
}