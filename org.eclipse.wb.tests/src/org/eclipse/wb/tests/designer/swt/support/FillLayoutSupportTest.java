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
import org.eclipse.wb.internal.swt.support.FillLayoutSupport;

import org.eclipse.swt.SWT;

/**
 * Test for {@link FillLayoutSupport}.
 *
 * @author lobas_av
 */
public class FillLayoutSupportTest extends AbstractSupportTest {
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
	public void test_getType() throws Exception {
		Object layout = getLayoutClass().newInstance();
		assertEquals(SWT.HORIZONTAL, FillLayoutSupport.getType(layout));
		ReflectionUtils.setField(layout, "type", SWT.VERTICAL);
		assertEquals(SWT.VERTICAL, FillLayoutSupport.getType(layout));
	}

	public void test_isHorizontal() throws Exception {
		Object layout = getLayoutClass().newInstance();
		assertTrue(FillLayoutSupport.isHorizontal(layout));
		ReflectionUtils.setField(layout, "type", SWT.VERTICAL);
		assertFalse(FillLayoutSupport.isHorizontal(layout));
	}

	public void test_newInstance() throws Exception {
		Object layout = FillLayoutSupport.newInstance();
		assertNotNull(layout);
		assertSame(getLayoutClass(), layout.getClass());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private Class<?> getLayoutClass() throws Exception {
		return m_lastLoader.loadClass("org.eclipse.swt.layout.FillLayout");
	}
}