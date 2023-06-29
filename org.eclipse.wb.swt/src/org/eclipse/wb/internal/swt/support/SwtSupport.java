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
package org.eclipse.wb.internal.swt.support;

import org.eclipse.swt.SWT;

/**
 * Stub class for using SWT {@link org.eclipse.swt.SWT} in another {@link ClassLoader}.
 *
 * @author lobas_av
 * @author mitin_aa
 * @coverage swt.support
 */
public class SwtSupport extends AbstractSupport {
	public static final int DEFAULT = SWT.DEFAULT;
	public static final int NONE = SWT.NONE;
	public static final int HORIZONTAL = SWT.HORIZONTAL;
	public static final int VERTICAL = SWT.VERTICAL;
	public static final int BORDER = SWT.BORDER;
	public static final int RIGHT_TO_LEFT = SWT.RIGHT_TO_LEFT;
	public static final int CHECK = SWT.CHECK;
	public static final int RADIO = SWT.RADIO;
	public static final int NORMAL = SWT.NORMAL;
	public static final int BOLD = SWT.BOLD;
	public static final int ITALIC = SWT.ITALIC;
	public static final int BOLD_ITALIC = SWT.BOLD | SWT.ITALIC;
	public static final int BAR = SWT.BAR;
	public static final int POP_UP = SWT.POP_UP;
	public static final int DROP_DOWN = SWT.DROP_DOWN;
	public static final int CASCADE = SWT.CASCADE;
	public static final int SEPARATOR = SWT.SEPARATOR;

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the SWT flag as <code>int</code>.
	 */
	public static int getIntFlag(String name) throws Exception {
		return ((Integer) getFlag(name)).intValue();
	}

	/**
	 * @return the SWT flag as {@link Object}.
	 */
	public static Object getFlag(String name) throws Exception {
		return getSwtClass().getField(name).get(null);
	}

	/**
	 * @return {@link org.eclipse.swt.SWT} {@link Class} loaded from active editor {@link ClassLoader}
	 *         .
	 */
	public static Class<?> getSwtClass() {
		return loadClass("org.eclipse.swt.SWT");
	}
}