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
package org.eclipse.wb.swt.widgets.baseline;

import org.eclipse.swt.widgets.Control;

import java.lang.reflect.Field;

/**
 * Implementation for Cocoa SWT baseline.
 *
 * @author mitin_aa
 */
final class CocoaBaseline extends Baseline {
	static {
		Library.loadLibrary("baseline-cocoa");
	}

	@Override
	public int fetchBaseline(Control control, int width, int height) {
		try {
			int baseline = _fetchBaseline(getID(control, "view"));
			if (baseline != -1) {
				return baseline;
			}
		} catch (Throwable e) {
			// ignore errors
		}
		return super.fetchBaseline(control, width, height);
	}

	/**
	 * @return the Cocoa id field.
	 * @throws Exception
	 */
	private long getID(Control control, String name) throws Exception {
		Field handleField = Control.class.getDeclaredField(name);
		handleField.setAccessible(true);
		Object handleValue = handleField.get(control);
		Field idField = handleValue.getClass().getField("id");
		idField.setAccessible(true);
		Number idValue = (Number) idField.get(handleValue);
		return idValue.longValue();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// native
	//
	////////////////////////////////////////////////////////////////////////////
	private native int _fetchBaseline(long widgetHandle);
}
