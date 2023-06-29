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
import org.eclipse.wb.internal.swt.support.RowLayoutSupport;

import org.eclipse.swt.SWT;

/**
 * Test for {@link RowLayoutSupport}.
 *
 * @author lobas_av
 */
public class RowLayoutSupportTest extends AbstractSupportTest {
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
		assertEquals(SWT.HORIZONTAL, RowLayoutSupport.getType(layout));
		ReflectionUtils.setField(layout, "type", SWT.VERTICAL);
		assertEquals(SWT.VERTICAL, RowLayoutSupport.getType(layout));
	}

	public void test_isHorizontal() throws Exception {
		Object layout = getLayoutClass().newInstance();
		assertTrue(RowLayoutSupport.isHorizontal(layout));
		ReflectionUtils.setField(layout, "type", SWT.VERTICAL);
		assertFalse(RowLayoutSupport.isHorizontal(layout));
	}

	public void test_createRowData() throws Exception {
		Object data = RowLayoutSupport.createRowData();
		assertNotNull(data);
		assertSame(m_lastLoader.loadClass("org.eclipse.swt.layout.RowData"), data.getClass());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private Class<?> getLayoutClass() throws Exception {
		return m_lastLoader.loadClass("org.eclipse.swt.layout.RowLayout");
	}
}