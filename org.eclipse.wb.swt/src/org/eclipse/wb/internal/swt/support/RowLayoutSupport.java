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
package org.eclipse.wb.internal.swt.support;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.swt.SWT;

/**
 * Stub class for using SWT {@link org.eclipse.swt.layout.RowLayout}'s in another
 * {@link ClassLoader}.
 *
 * @author lobas_av
 * @coverage swt.support
 */
public class RowLayoutSupport extends AbstractSupport {
	////////////////////////////////////////////////////////////////////////////
	//
	// RowLayout
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Create new {@link org.eclipse.swt.layout.RowData}.
	 */
	public static Object createRowData() throws Exception {
		return loadClass("org.eclipse.swt.layout.RowData").newInstance();
	}

	/**
	 * @return value of field <code>type</code>.
	 */
	public static int getType(final Object layout) {
		return ExecutionUtils.runObjectLog(() -> ReflectionUtils.getFieldInt(layout, "type"), SWT.HORIZONTAL);
	}

	/**
	 * @return <code>true</code> if this layout is horizontal (type == SWT.HORIZONTAL).
	 */
	public static boolean isHorizontal(Object layout) {
		return getType(layout) == SWT.HORIZONTAL;
	}
}