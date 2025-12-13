/*******************************************************************************
 * Copyright (c) 2025 Patrick Ziegler and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Patrick Ziegler - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.os.linux.cairo;

public enum CairoFormat {
	CAIRO_FORMAT_INVALID(-1),
	CAIRO_FORMAT_ARGB32(0),
	CAIRO_FORMAT_RGB24(1),
	CAIRO_FORMAT_A8(2),
	CAIRO_FORMAT_A1(3),
	CAIRO_FORMAT_RGB16_565(4),
	CAIRO_FORMAT_RGB30(5),
	CAIRO_FORMAT_RGB96F(6),
	CAIRO_FORMAT_RGBA128F(7);

	private final int value;

	private CairoFormat(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
