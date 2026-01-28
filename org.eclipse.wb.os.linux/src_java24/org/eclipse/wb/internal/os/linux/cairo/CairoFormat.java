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

/**
 * {@code cairo_format_t} is used to identify the memory format of image data.
 *
 * New entries may be added in future versions.
 */
public enum CairoFormat {
	/**
	 * no such format exists or is supported.
	 */
	CAIRO_FORMAT_INVALID(-1),
	/**
	 * each pixel is a 32-bit quantity, with alpha in the upper 8 bits, then red,
	 * then green, then blue. The 32-bit quantities are stored native-endian.
	 * Pre-multiplied alpha is used. (That is, 50% transparent red is 0x80800000,
	 * not 0x80ff0000.) (Since 1.0)
	 */
	CAIRO_FORMAT_ARGB32(0),
	/**
	 * each pixel is a 32-bit quantity, with the upper 8 bits unused. Red, Green,
	 * and Blue are stored in the remaining 24 bits in that order. (Since 1.0)
	 */
	CAIRO_FORMAT_RGB24(1),
	/**
	 * each pixel is a 8-bit quantity holding an alpha value. (Since 1.0)
	 */
	CAIRO_FORMAT_A8(2),
	/**
	 * each pixel is a 1-bit quantity holding an alpha value. Pixels are packed
	 * together into 32-bit quantities. The ordering of the bits matches the
	 * endianness of the platform. On a big-endian machine, the first pixel is in
	 * the uppermost bit, on a little-endian machine the first pixel is in the
	 * least-significant bit. (Since 1.0)
	 */
	CAIRO_FORMAT_A1(3),
	/**
	 * each pixel is a 16-bit quantity with red in the upper 5 bits, then green in
	 * the middle 6 bits, and blue in the lower 5 bits. (Since 1.2)
	 */
	CAIRO_FORMAT_RGB16_565(4),
	/**
	 * like RGB24 but with 10bpc. (Since 1.12)
	 */
	CAIRO_FORMAT_RGB30(5),
	/**
	 * 3 floats, R, G, B. (Since 1.17.2)
	 */
	CAIRO_FORMAT_RGB96F(6),
	/**
	 * 4 floats, R, G, B, A. (Since 1.17.2)
	 */
	CAIRO_FORMAT_RGBA128F(7);

	private final int value;

	private CairoFormat(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
