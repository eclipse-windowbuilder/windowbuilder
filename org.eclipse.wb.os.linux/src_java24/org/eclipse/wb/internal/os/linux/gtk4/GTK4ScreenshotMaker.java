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

import org.eclipse.wb.internal.os.linux.GtkWidget;
import org.eclipse.wb.internal.os.linux.OS;
import org.eclipse.wb.internal.os.linux.ScreenshotMaker;
import org.eclipse.wb.internal.os.linux.cairo.CairoContext;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import java.lang.foreign.MemorySegment;
import java.util.function.BiConsumer;

public class GTK4ScreenshotMaker extends ScreenshotMaker {

	@Override
	protected Image getImageSurface(GtkWidget widget, BiConsumer<GtkWidget, Image> callback) {
		GtkPaintable widgetPaintable = GTK4.gtk_widget_paintable_new(widget);
		if (MemorySegment.NULL.equals(widgetPaintable.segment())) {
			return null;
		}

		int width = GTK4.gtk_widget_get_width(widget);
		int height = GTK4.gtk_widget_get_height(widget);

		Image image = new Image(Display.getCurrent(), width, height);

		GtkSnapshot snapshot = GTK4.gtk_snapshot_new();
		if (MemorySegment.NULL.equals(snapshot.segment())) {
			return image;
		}

		GC gc = new GC(image);

		try {
			GTK4.gdk_paintable_snapshot(widgetPaintable, snapshot, width, height);

			GskRenderNode renderNode = GTK4.gtk_snapshot_free_to_node(snapshot);
			snapshot = null; // freed by gtk_snapshot_free_to_node

			if (MemorySegment.NULL.equals(renderNode.segment())) {
				return image;
			}

			try {
				GTK4.gsk_render_node_draw(renderNode, CairoContext.from(gc));
				GTK4.gsk_render_node_unref(renderNode);
			} finally {
				gc.dispose();
			}

			return image;
		} finally {
			gc.dispose();

			callback.accept(widget, image);
			if (snapshot != null) {
				OS.g_object_unref(snapshot);
			}

			OS.g_object_unref(widgetPaintable);
		}
	}
}
