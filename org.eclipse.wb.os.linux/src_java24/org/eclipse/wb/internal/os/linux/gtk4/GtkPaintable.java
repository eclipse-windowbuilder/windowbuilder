/*******************************************************************************
 * Copyright (c) 2026 Patrick Ziegler and others.
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
package org.eclipse.wb.internal.os.linux.gtk4;

import java.lang.foreign.MemorySegment;

/**
 * A {@code GdkPaintable} that displays the contents of a widget.
 *
 * {@code GtkWidgetPaintable} will also take care of the widget not being in a
 * state where it can be drawn (like when it isn’t shown) and just draw nothing
 * or where it does not have a size (like when it is hidden) and report no size
 * in that case.
 *
 * Of course, {@code GtkWidgetPaintable} allows you to monitor widgets for size
 * changes by emitting the <a href=
 * "https://docs.gtk.org/gdk4/signal.Paintable.invalidate-size.html">GdkPaintable::invalidate-size</a>
 * signal whenever the size of the widget changes as well as for visual changes
 * by emitting the <a href=
 * "https://docs.gtk.org/gdk4/signal.Paintable.invalidate-contents.html">GdkPaintable::invalidate-contents</a>
 * signal whenever the widget changes.
 *
 * You can use a {@code GtkWidgetPaintable} everywhere a {@code GdkPaintable} is
 * allowed, including using it on a {@code GtkPicture} (or one of its parents)
 * that it was set on itself via gtk_picture_set_paintable(). The paintable will
 * take care of recursion when this happens. If you do this however, ensure that
 * the <a href="gtk_picture_set_paintable">GtkPicture:can-shrink</a> property is
 * set to {@code TRUE} or you might end up with an infinitely growing widget.
 *
 */
public class GtkPaintable extends GdkPaintable {
	public GtkPaintable(MemorySegment segment) {
		super(segment);
	}
}
