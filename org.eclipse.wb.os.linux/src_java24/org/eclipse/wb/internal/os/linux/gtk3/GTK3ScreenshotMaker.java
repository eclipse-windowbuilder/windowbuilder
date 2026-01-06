/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package org.eclipse.wb.internal.os.linux.gtk3;

import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.os.linux.GDK;
import org.eclipse.wb.internal.os.linux.GtkRuntimeException;
import org.eclipse.wb.internal.os.linux.GtkWidget;
import org.eclipse.wb.internal.os.linux.ScreenshotMaker;
import org.eclipse.wb.internal.os.linux.cairo.Cairo;
import org.eclipse.wb.internal.os.linux.cairo.CairoContext;
import org.eclipse.wb.internal.os.linux.cairo.CairoFormat;
import org.eclipse.wb.internal.os.linux.cairo.CairoOperator;
import org.eclipse.wb.internal.os.linux.cairo.CairoRegion;
import org.eclipse.wb.internal.os.linux.cairo.CairoSurface;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

import java.util.function.BiConsumer;

/**
 * Creates a screenshot of a given widget using the GTK3 API.
 */
public class GTK3ScreenshotMaker extends ScreenshotMaker {

	@Override
	protected Image makeShot(Shell shell, BiConsumer<GtkWidget, Image> callback) {
		return traverse(shell, callback);
	}

	private Image traverse(Widget widget, BiConsumer<GtkWidget, Image> callback) {
		Image image = getImageSurface(new GtkWidget(widget), callback);
		if (image == null) {
			return null;
		}
		if (widget instanceof Composite composite) {
			for (Control childWidget : composite.getChildren()) {
				Image childImage = traverse(childWidget, callback);
				if (childImage == null) {
					continue;
				}
				if (callback == null) {
					childImage.dispose();
				}
			}
		}
		return image;
	}

	protected Image getImageSurface(GtkWidget widget, BiConsumer<GtkWidget, Image> callback) {
		GdkWindow window = GTK3.gtk_widget_get_window(widget);
		if (!GDK3.gdk_window_is_visible(window)) {
			// don't deal with unmapped windows
			return null;
		}

		int width = GDK3.gdk_window_get_width(window);
		int height = GDK3.gdk_window_get_height(window);
		// force paint. Note, not all widgets do this completely, known so far is
		// GtkTreeViewer.
		GDK3.gdk_window_process_updates(window, true);
		// take screenshot
		Image image = createImage(window, width, height);
		// get Java code notified
		if (callback != null) {
			callback.accept(widget, image);
		}
		// done
		return image;
	}

	private Image createImage(GdkWindow sourceWindow, int width, int height) {
		// Create the Cairo surface on which the snapshot is drawn on
		CairoSurface targetSurface = Cairo.cairo_image_surface_create(CairoFormat.CAIRO_FORMAT_ARGB32, width, height);
		CairoContext cr = Cairo.cairo_create(targetSurface);
		// Get the visible region of the window
		// Wayland: Trying to take a screenshot of a partially unmapped widget
		// results in a SIGFAULT.
		CairoRegion visibleRegion = GDK3.gdk_window_get_visible_region(sourceWindow);
		// Set the visible region as the clip for the Cairo context
		GDK.gdk_cairo_region(cr, visibleRegion);
		Cairo.cairo_clip(cr);
		// Paint the surface
		GDK3.gdk_cairo_set_source_window(cr, sourceWindow, 0, 0);
		Cairo.cairo_set_operator(cr, CairoOperator.CAIRO_OPERATOR_SOURCE);
		Cairo.cairo_paint(cr);
		// Cleanup
		Cairo.cairo_destroy(cr);
		Cairo.cairo_surface_flush(targetSurface);
		Cairo.cairo_region_destroy(visibleRegion);
		return createImage(targetSurface.segment().address());
	}

	private Image createImage(long imageHandle) {
		Image image = createImage0(imageHandle);
		// BUG in SWT: Image instance is not fully initialized
		// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=382175
		Image newImage = new Image(null, image.getImageData());
		image.dispose();
		return newImage;
	}

	private Image createImage0(long imageHandle) {
		try {
			return (Image) ReflectionUtils.invokeMethod2( //
					Image.class, //
					"gtk_new", //
					Device.class, //
					int.class, //
					long.class, //
					long.class, //
					null, //
					SWT.BITMAP, //
					imageHandle, //
					0);
		} catch (Exception e) {
			throw new GtkRuntimeException(e);
		}
	}

}
