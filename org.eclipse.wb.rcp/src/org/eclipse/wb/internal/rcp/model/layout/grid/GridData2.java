/*******************************************************************************
 * Copyright (c) 2011, 2026 Google, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.rcp.model.layout.grid;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Control;

/**
 * <code>GridData</code> is the layout data object associated with <code>GridLayout</code>. To set a
 * <code>GridData</code> object into a control, you use the
 * <code>Control.setLayoutData(Object)</code> method.
 * <p>
 * There are two ways to create a <code>GridData</code> object with certain fields set. The first is
 * to set the fields directly, like this:
 *
 * <pre>
 * 		GridData gridData = new GridData();
 * 		gridData.horizontalAlignment = GridData.FILL;
 * 		gridData.grabExcessHorizontalSpace = true;
 * 		button1.setLayoutData(gridData);
 * </pre>
 *
 * The second is to take advantage of convenience style bits defined by <code>GridData</code>:
 *
 * <pre>
 *      button1.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
 * </pre>
 *
 * </p>
 * <p>
 * NOTE: Do not reuse <code>GridData</code> objects. Every control in a <code>Composite</code> that
 * is managed by a <code>GridLayout</code> must have a unique <code>GridData</code> object. If the
 * layout data for a control in a <code>GridLayout</code> is null at layout time, a unique
 * <code>GridData</code> object is created for it.
 * </p>
 *
 * @see GridLayout2
 * @see Control#setLayoutData
 * @coverage rcp.model.layout.GridLayout.copy
 */
public final class GridData2 {
	final GridData data;
	int cacheWidth = -1, cacheHeight = -1;
	int defaultWhint, defaultHhint, defaultWidth = -1, defaultHeight = -1;
	int currentWhint, currentHhint, currentWidth = -1, currentHeight = -1;

	/**
	 * Constructs a new instance based on the given GridData.
	 *
	 * @param data the source GridData
	 */
	public GridData2(GridData data) {
		this.data = data;
	}

	void computeSize(Control control, int wHint, int hHint, boolean flushCache) {
		if (cacheWidth != -1 && cacheHeight != -1) {
			return;
		}
		if (wHint == data.widthHint && hHint == data.heightHint) {
			if (defaultWidth == -1
					|| defaultHeight == -1
					|| wHint != defaultWhint
					|| hHint != defaultHhint) {
				Point size = control.computeSize(wHint, hHint, flushCache);
				defaultWhint = wHint;
				defaultHhint = hHint;
				defaultWidth = size.x;
				defaultHeight = size.y;
			}
			cacheWidth = defaultWidth;
			cacheHeight = defaultHeight;
			return;
		}
		if (currentWidth == -1 || currentHeight == -1 || wHint != currentWhint || hHint != currentHhint) {
			Point size = control.computeSize(wHint, hHint, flushCache);
			currentWhint = wHint;
			currentHhint = hHint;
			currentWidth = size.x;
			currentHeight = size.y;
		}
		cacheWidth = currentWidth;
		cacheHeight = currentHeight;
	}

	void flushCache() {
		cacheWidth = cacheHeight = -1;
		defaultWidth = defaultHeight = -1;
		currentWidth = currentHeight = -1;
	}

	String getName() {
		String string = getClass().getName();
		int index = string.lastIndexOf('.');
		if (index == -1) {
			return string;
		}
		return string.substring(index + 1, string.length());
	}

	/**
	 * Returns a string containing a concise, human-readable description of the receiver.
	 *
	 * @return a string representation of the GridData object
	 */
	@Override
	public String toString() {
		return data.toString();
	}
}
