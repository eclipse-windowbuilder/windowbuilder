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
package org.eclipse.wb.internal.gef.core;

import org.eclipse.wb.internal.core.DesignerPlugin;

import org.eclipse.draw2d.Cursors;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageDataProvider;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

/**
 * A shared collection of Cursors.
 *
 * @author lobas_av
 * @coverage gef.core
 */
public class SharedCursors extends Cursors {

	/**
	 * Local cache to store the cursor data for each zoom level.
	 */
	private static final ImageDescriptor CURSOR_AT_100_ZOOM = createDescriptor("icons/cursor@1x.svg"); //$NON-NLS-1$
	private static final ImageDescriptor CURSOR_AT_150_ZOOM = createDescriptor("icons/cursor@1.5x.svg"); //$NON-NLS-1$
	private static final ImageDescriptor CURSOR_AT_200_ZOOM = createDescriptor("icons/cursor@2x.svg"); //$NON-NLS-1$
	private static final ImageDescriptor CURSOR_DESCRIPTOR = ImageDescriptor.createFromImageDataProvider(zoom -> {
		if (zoom < 150) {
			return CURSOR_AT_100_ZOOM.getImageData(100);
		}
		if (zoom < 200) {
			return CURSOR_AT_150_ZOOM.getImageData(100);
		}
		return CURSOR_AT_200_ZOOM.getImageData(100);
	});

	public static final Cursor CURSOR_ADD = createCursor("icons/add_cursor.svg");
	public static final Cursor CURSOR_MOVE = createCursor("icons/move_cursor.svg");
	public static final Cursor CURSOR_NO = createCursor("icons/no_cursor.svg");

	private static Cursor createCursor(String sourceName) {
		if (DesignerPlugin.isSvgSupported()) {
			ImageDescriptor src1 = createDescriptor(sourceName);
			ImageDescriptor src = new DecorationOverlayIcon(src1, CURSOR_DESCRIPTOR, IDecoration.TOP_LEFT) {
				@Override
				// Disabled by default due to https://bugs.eclipse.org/bugs/show_bug.cgi?id=97506
				protected boolean supportsZoomLevel(int zoomLevel) {
					return true;
				}
			};
			return createCursor(src, 0, 0);
		}
		ImageDescriptor src = createDescriptor(sourceName);
		return createCursor(src, 0, 0);
	}

	/**
	 * Creates and returns an image descriptor from the given file. If the file is
	 * an SVG, it will be automatically swapped out with a PNG if not yet supported
	 * by SWT.
	 */
	public static ImageDescriptor createDescriptor(String filename) {
		URL resourceURL = SharedCursors.class.getResource(DesignerPlugin.getEffectiveFileName(filename));
		return ImageDescriptor.createFromURL(resourceURL);
	}

	/**
	 * This method attempts to create the cursor using a constructor introduced in
	 * SWT 3.131.0 that takes an {@link ImageDataProvider}. If this constructor is
	 * not available (SWT versions prior to 3.131.0), it falls back to using the
	 * older constructor that accepts {@link ImageData}.
	 */
	private static Cursor createCursor(ImageDescriptor source, int hotspotX, int hotspotY) {
		try {
			ImageDataProvider provider = zoom -> {
				if (zoom < 150) {
					return source.getImageData(100);
				}
				if (zoom < 200) {
					return source.getImageData(150);
				}
				return source.getImageData(200);
			};
			Constructor<Cursor> ctor = Cursor.class.getConstructor(Device.class, ImageDataProvider.class, int.class,
					int.class);
			return ctor.newInstance(null, provider, hotspotX, hotspotY);
		} catch (NoSuchMethodException e) {
			// SWT version < 3.131.0 (no ImageDataProvider-based constructor)
			return new Cursor(null, source.getImageData(100), hotspotX, hotspotY); // older constructor
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
			throw new RuntimeException("Failed to instantiate Cursor", e); //$NON-NLS-1$
		}
	}
}