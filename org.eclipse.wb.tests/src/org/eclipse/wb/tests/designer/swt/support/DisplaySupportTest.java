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
package org.eclipse.wb.tests.designer.swt.support;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swt.support.DisplaySupport;

import org.eclipse.swt.SWT;

import org.junit.Test;

/**
 * Test for {@link DisplaySupport}.
 *
 * @author lobas_av
 */
public class DisplaySupportTest extends AbstractSupportTest {
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
	public void test_getDisplayClass() throws Exception {
		assertSame(getDisplayClass(), DisplaySupport.getDisplayClass());
	}

	@Test
	public void test_getDefault() throws Exception {
		assertSame(getDefaultDisplay(), DisplaySupport.getDefault());
	}

	@Test
	public void test_getCurrent() throws Exception {
		assertSame(getCurrentDisplay(), DisplaySupport.getCurrent());
	}

	@Test
	public void test_getSystemColor() throws Exception {
		assertEquals(
				ReflectionUtils.invokeMethod(getCurrentDisplay(), "getSystemColor(int)", SWT.COLOR_RED),
				DisplaySupport.getSystemColor(SWT.COLOR_RED));
		assertEquals(
				ReflectionUtils.invokeMethod(getCurrentDisplay(), "getSystemColor(int)", SWT.COLOR_BLACK),
				DisplaySupport.getSystemColor(SWT.COLOR_BLACK));
		assertEquals(
				ReflectionUtils.invokeMethod(getCurrentDisplay(), "getSystemColor(int)", SWT.COLOR_WHITE),
				DisplaySupport.getSystemColor(SWT.COLOR_WHITE));
	}

	@Test
	public void test_getSystemFont() throws Exception {
		assertSame(
				ReflectionUtils.invokeMethod(getDefaultDisplay(), "getSystemFont()"),
				DisplaySupport.getSystemFont());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private Class<?> getDisplayClass() throws Exception {
		return m_lastLoader.loadClass("org.eclipse.swt.widgets.Display");
	}

	private Object getDefaultDisplay() throws Exception {
		return ReflectionUtils.invokeMethod(getDisplayClass(), "getDefault()");
	}

	private Object getCurrentDisplay() throws Exception {
		return ReflectionUtils.invokeMethod(getDisplayClass(), "getCurrent()");
	}
}