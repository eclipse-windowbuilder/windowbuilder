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
package org.eclipse.wb.internal.os.linux.gtk3;

import org.eclipse.wb.internal.os.linux.Cairo;
import org.eclipse.wb.internal.os.linux.GTK;
import org.eclipse.wb.internal.os.linux.GtkAllocation;
import org.eclipse.wb.internal.os.linux.GtkWidget;
import org.eclipse.wb.internal.os.linux.ScreenshotMaker;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import java.lang.foreign.Arena;
/**
 * Creates a screenshot of a given widget using the GTK3 API.
 */
public class GTK3ScreenshotMaker extends ScreenshotMaker {

	@Override
	protected Image getImageSurface(GtkWidget widget) {
		try (Arena arena = Arena.ofConfined()) {
			GtkAllocation allocation = new GtkAllocation(arena);
			GTK.gtk_widget_get_allocation(widget, allocation);
			// Prevent allocation warnings
			GTK3.gtk_widget_get_preferred_size(widget, GtkRequisition.NULL, GtkRequisition.NULL);
			GTK3.gtk_widget_size_allocate(widget, allocation);

			int width = Math.max(1, allocation.width());
			int height = Math.max(1, allocation.height());

			Image image = new Image(Display.getCurrent(), width, height);
			GC gc = new GC(image);
			GTK3.gtk_widget_draw(widget, Cairo.from(gc));
			gc.dispose();
			return image;
		}
	}

}
