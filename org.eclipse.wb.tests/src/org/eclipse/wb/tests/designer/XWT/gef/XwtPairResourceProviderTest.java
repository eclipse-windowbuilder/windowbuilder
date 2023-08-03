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
package org.eclipse.wb.tests.designer.XWT.gef;

import org.eclipse.wb.internal.xwt.editor.XwtPairResourceProvider;
import org.eclipse.wb.tests.designer.XWT.model.XwtModelTest;

import org.eclipse.core.resources.IFile;

import org.junit.Test;

/**
 * Test for {@link XwtPairResourceProvider}.
 *
 * @author scheglov_ke
 */
public class XwtPairResourceProviderTest extends XwtModelTest {
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
	// Tests
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_unknownExtension() throws Exception {
		IFile file = setFileContentSrc("test/Test.foo", "");
		assertEquals(null, getPair(file));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// XWT -> Java
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_toJava_javaClassAttribute() throws Exception {
		IFile javaFile = setFileContentSrc("foo/Bar.java", "");
		IFile xwtFile = setFileContentSrc("test/Test.xwt", getSource("<Shell x:Class='foo.Bar'/>"));
		//
		assertEquals(javaFile, getPair(xwtFile));
	}

	@Test
	public void test_toJava_sameName() throws Exception {
		IFile javaFile = setFileContentSrc("test/Test.java", "");
		IFile xwtFile = setFileContentSrc("test/Test.xwt", getSource("<Shell/>"));
		//
		assertEquals(javaFile, getPair(xwtFile));
	}

	@Test
	public void test_toJava_no() throws Exception {
		setFileContentSrc("foo/Test.java", "");
		IFile xwtFile = setFileContentSrc("test/Test.xwt", getSource("<Shell/>"));
		//
		assertEquals(null, getPair(xwtFile));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Java -> XWT
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_toXWT_javaClassAttribute_samePackage() throws Exception {
		IFile javaFile = setFileContentSrc("test/Bar.java", "");
		IFile xwtFile = setFileContentSrc("test/Test.xwt", getSource("<Shell x:Class='test.Bar'/>"));
		//
		assertEquals(xwtFile, getPair(javaFile));
	}

	@Test
	public void test_toXWT_sameName() throws Exception {
		IFile javaFile = setFileContentSrc("test/Test.java", "");
		IFile xwtFile = setFileContentSrc("test/Test.xwt", getSource("<Shell/>"));
		//
		assertEquals(xwtFile, getPair(javaFile));
	}

	@Test
	public void test_toXWT_no() throws Exception {
		IFile javaFile = setFileContentSrc("test/Test.java", "");
		setFileContentSrc("foo/Test.xwt", getSource("<Shell/>"));
		//
		assertEquals(null, getPair(javaFile));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private static IFile getPair(IFile file) {
		return XwtPairResourceProvider.INSTANCE.getPair(file);
	}
}