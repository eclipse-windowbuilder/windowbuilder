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

import org.eclipse.wb.internal.os.linux.Native;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;

/**
 * The Cairo toolkit.
 */
public class Cairo extends Native {
	private static final SymbolLookup CAIRO = SymbolLookup.libraryLookup("libcairo.so.2", Arena.ofAuto());

	private static final MethodHandle cairo_clip = createHandle(CAIRO, "cairo_clip",
			FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));

	private static final MethodHandle cairo_create = createHandle(CAIRO, "cairo_create",
			FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS));

	private static final MethodHandle cairo_destroy = createHandle(CAIRO, "cairo_destroy",
			FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));

	private static final MethodHandle cairo_image_surface_create = createHandle(CAIRO, "cairo_image_surface_create",
			FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));

	private static final MethodHandle cairo_paint = createHandle(CAIRO, "cairo_paint",
			FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));

	private static final MethodHandle cairo_region_destroy = createHandle(CAIRO, "cairo_region_destroy",
			FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));

	private static final MethodHandle cairo_set_operator = createHandle(CAIRO, "cairo_set_operator",
			FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_INT));

	private static final MethodHandle cairo_surface_flush = createHandle(CAIRO, "cairo_surface_flush",
			FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));

	/**
	 * Establishes a new clip region by intersecting the current clip region with
	 * the current path as it would be filled by {@code cairo_fill()} and according
	 * to the current fill rule (see {@code cairo_set_fill_rule()}).
	 *
	 * After {@code cairo_clip()}, the current path will be cleared from the cairo
	 * context.
	 *
	 * The current clip region affects all drawing operations by effectively masking
	 * out any changes to the surface that are outside the current clip region.
	 *
	 * Calling {@code cairo_clip()} can only make the clip region smaller, never
	 * larger. But the current clip is part of the graphics state, so a temporary
	 * restriction of the clip region can be achieved by calling
	 * {@code cairo_clip()} within a {@code cairo_save()}/{@code cairo_restore()}
	 * pair. The only other means of increasing the size of the clip region is
	 * {@code cairo_reset_clip()}.
	 *
	 * @param cr a cairo context
	 */
	public static void cairo_clip(CairoContext cr) {
		runSafe(() -> cairo_clip.invoke(cr.segment()));
	}

	/**
	 * Creates a new {@code cairo_t} with all graphics state parameters set to
	 * default values and with target as a target surface. The target surface should
	 * be constructed with a backend-specific function such as
	 * {@code cairo_image_surface_create()} (or any other
	 * {@code cairo_backend_surface_create()} variant).
	 *
	 * This function references {@code target}, so you can immediately call
	 * {@code cairo_surface_destroy()} on it if you don't need to maintain a
	 * separate reference to it.
	 *
	 * @param target target surface for the context
	 * @return a newly allocated {@code cairo_t} with a reference count of 1. The
	 *         initial reference count should be released with
	 *         {@code cairo_destroy()} when you are done using the cairo_t. This
	 *         function never returns NULL. If memory cannot be allocated, a special
	 *         {@code cairo_t} object will be returned on which
	 *         {@code cairo_status()} returns {@code CAIRO_STATUS_NO_MEMORY}. If you
	 *         attempt to target a surface which does not support writing (such as
	 *         {@code cairo_mime_surface_t}) then a {@code CAIRO_STATUS_WRITE_ERROR}
	 *         will be raised. You can use this object normally, but no drawing will
	 *         be done.
	 */
	public static CairoContext cairo_create(CairoSurface target) {
		MemorySegment handle = (MemorySegment) callSafe(() -> cairo_create.invoke(target.segment()));
		return new CairoContext(handle);
	}

	/**
	 * Decreases the reference count on {@code cr} by one. If the result is zero,
	 * then {@code cr} and all associated resources are freed. See
	 * cairo_reference().
	 *
	 * @param cr a {@code cairo_t}
	 */
	public static void cairo_destroy(CairoContext cr) {
		runSafe(() -> cairo_destroy.invoke(cr.segment()));
	}

	/**
	 * Creates an image surface of the specified format and dimensions. Initially
	 * the surface contents are set to 0. (Specifically, within each pixel, each
	 * color or alpha channel belonging to format will be 0. The contents of bits
	 * within a pixel, but not belonging to the given format are undefined).
	 *
	 * @param format format of pixels in the surface to create
	 * @param width  width of the surface, in pixels
	 * @param height height of the surface, in pixels
	 * @return a pointer to the newly created surface. The caller owns the surface
	 *         and should call {@code cairo_surface_destroy()} when done with it.
	 *         <p>
	 *         This function always returns a valid pointer, but it will return a
	 *         pointer to a "nil" surface if an error such as out of memory occurs.
	 *         You can use {@code cairo_surface_status()} to check for this.
	 *         </p>
	 */
	public static CairoSurface cairo_image_surface_create(CairoFormat format, int width, int height) {
		MemorySegment handle = (MemorySegment) callSafe(() -> cairo_image_surface_create.invoke(format.getValue(), width, height));
		return new CairoSurface(handle);
	}

	/**
	 * A drawing operator that paints the current source everywhere within the
	 * current clip region.
	 *
	 * @param cr a cairo context
	 */
	public static void cairo_paint(CairoContext cr) {
		runSafe(() -> cairo_paint.invoke(cr.segment()));
	}

	/**
	 * Destroys a {@code cairo_region_t} object created with
	 * {@code cairo_region_create()}, {@code cairo_region_copy()}, or or
	 * {@code cairo_region_create_rectangle()}.
	 *
	 * @param region a {@code cairo_region_t}
	 */
	public static void cairo_region_destroy(CairoRegion region) {
		runSafe(() -> cairo_region_destroy.invoke(region.segment()));
	}

	/**
	 * Sets the compositing operator to be used for all drawing operations. See
	 * {@code cairo_operator_t} for details on the semantics of each available
	 * compositing operator.
	 * <p>
	 * The default operator is {@code CAIRO_OPERATOR_OVER}.
	 * </p>
	 *
	 * @param cr a {@code cairo_t}
	 * @param op a compositing operator, specified as a {@code cairo_operator_t}
	 */
	public static void cairo_set_operator(CairoContext cr, CairoOperator op) {
		runSafe(() -> cairo_set_operator.invoke(cr.segment(), op.getValue()));
	}

	/**
	 * Do any pending drawing for the surface and also restore any temporary
	 * modifications cairo has made to the surface's state. This function must be
	 * called before switching from drawing on the surface with cairo to drawing on
	 * it directly with native APIs, or accessing its memory outside of Cairo. If
	 * the surface doesn't support direct access, then this function does nothing.
	 *
	 * @param surface a {@code cairo_surface_t}
	 */
	public static void cairo_surface_flush(CairoSurface surface) {
		runSafe(() -> cairo_surface_flush.invoke(surface.segment()));
	}
}
