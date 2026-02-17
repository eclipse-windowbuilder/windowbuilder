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
 * {@code CairoOperator} is used to set the compositing operator for all cairo
 * drawing operations.
 *
 * The default operator is {@link #CAIRO_OPERATOR_OVER}.
 *
 * The operators marked as unbounded modify their destination even outside of
 * the mask layer (that is, their effect is not bound by the mask layer).
 * However, their effect can still be limited by way of clipping.
 *
 * To keep things simple, the operator descriptions here document the behavior
 * for when both source and destination are either fully transparent or fully
 * opaque. The actual implementation works for translucent layers too. For a
 * more detailed explanation of the effects of each operator, including the
 * mathematical definitions, see <a href=
 * "https://cairographics.org/operators/">https://cairographics.org/operators/</a>.
 */
public enum CairoOperator {
	/**
	 * clear destination layer (bounded) (Since 1.0)
	 */
	CAIRO_OPERATOR_CLEAR(0),
	/**
	 * replace destination layer (bounded) (Since 1.0)
	 */
	CAIRO_OPERATOR_SOURCE(1),
	/**
	 * draw source layer on top of destination layer (bounded) (Since 1.0)
	 */
	CAIRO_OPERATOR_OVER(2),
	/**
	 * draw source where there was destination content (unbounded) (Since 1.0)
	 */
	CAIRO_OPERATOR_IN(3),
	/**
	 * draw source where there was no destination content (unbounded) (Since 1.0)
	 */
	CAIRO_OPERATOR_OUT(4),
	/**
	 * draw source on top of destination content and only there (Since 1.0)
	 */
	CAIRO_OPERATOR_ATOP(5),
	/**
	 * ignore the source (Since 1.0)
	 */
	CAIRO_OPERATOR_DEST(6),
	/**
	 * draw destination on top of source (Since 1.0)
	 */
	CAIRO_OPERATOR_DEST_OVER(7),
	/**
	 * leave destination only where there was source content (unbounded) (Since 1.0)
	 */
	CAIRO_OPERATOR_DEST_IN(8),
	/**
	 * leave destination only where there was no source content (Since 1.0)
	 */
	CAIRO_OPERATOR_DEST_OUT(9),
	/**
	 * leave destination on top of source content and only there (unbounded) (Since
	 * 1.0)
	 */
	CAIRO_OPERATOR_DEST_ATOP(10),
	/**
	 * source and destination are shown where there is only one of them (Since 1.0)
	 */
	CAIRO_OPERATOR_XOR(11),
	/**
	 * source and destination layers are accumulated (Since 1.0)
	 */
	CAIRO_OPERATOR_ADD(12),
	/**
	 * like over, but assuming source and dest are disjoint geometries (Since 1.0)
	 */
	CAIRO_OPERATOR_SATURATE(13),
	/**
	 * source and destination layers are multiplied. This causes the result to be at
	 * least as dark as the darker inputs. (Since 1.10)
	 */
	CAIRO_OPERATOR_MULTIPLY(14),
	/**
	 * source and destination are complemented and multiplied. This causes the
	 * result to be at least as light as the lighter inputs. (Since 1.10)
	 */
	CAIRO_OPERATOR_SCREEN(15),
	/**
	 * multiplies or screens, depending on the lightness of the destination color.
	 * (Since 1.10)
	 */
	CAIRO_OPERATOR_OVERLAY(16),
	/**
	 * replaces the destination with the source if it is darker, otherwise keeps the
	 * source. (Since 1.10)
	 */
	CAIRO_OPERATOR_DARKEN(17),
	/**
	 * replaces the destination with the source if it is lighter, otherwise keeps
	 * the source. (Since 1.10)
	 */
	CAIRO_OPERATOR_LIGHTEN(18),
	/**
	 * brightens the destination color to reflect the source color. (Since 1.10)
	 */
	CAIRO_OPERATOR_COLOR_DODGE(19),
	/**
	 * darkens the destination color to reflect the source color. (Since 1.10)
	 */
	CAIRO_OPERATOR_COLOR_BURN(20),
	/**
	 * Multiplies or screens, dependent on source color. (Since 1.10)
	 */
	CAIRO_OPERATOR_HARD_LIGHT(21),
	/**
	 * Darkens or lightens, dependent on source color. (Since 1.10)
	 */
	CAIRO_OPERATOR_SOFT_LIGHT(22),
	/**
	 * Takes the difference of the source and destination color. (Since 1.10)
	 */
	CAIRO_OPERATOR_DIFFERENCE(23),
	/**
	 * Produces an effect similar to difference, but with lower contrast. (Since
	 * 1.10)
	 */
	CAIRO_OPERATOR_EXCLUSION(24),
	/**
	 * Creates a color with the hue of the source and the saturation and luminosity
	 * of the target. (Since 1.10)
	 */
	CAIRO_OPERATOR_HSL_HUE(25),
	/**
	 * Creates a color with the saturation of the source and the hue and luminosity
	 * of the target. Painting with this mode onto a gray area produces no change.
	 * (Since 1.10)
	 */
	CAIRO_OPERATOR_HSL_SATURATION(26),
	/**
	 * Creates a color with the hue and saturation of the source and the luminosity
	 * of the target. This preserves the gray levels of the target and is useful for
	 * coloring monochrome images or tinting color images. (Since 1.10)
	 */
	CAIRO_OPERATOR_HSL_COLOR(27),
	/**
	 * Creates a color with the luminosity of the source and the hue and saturation
	 * of the target. This produces an inverse effect to
	 * {@link CAIRO_OPERATOR_HSL_COLOR} . (Since 1.10)
	 */
	CAIRO_OPERATOR_HSL_LUMINOSITY(28);

	private final int value;

	private CairoOperator(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
