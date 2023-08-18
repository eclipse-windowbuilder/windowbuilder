/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.rcp.databinding.ui.providers;

import org.eclipse.wb.internal.rcp.databinding.Activator;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

import java.util.Collection;

/**
 * Helper for association {@link Class} with {@link ImageDescriptor}.
 *
 * @author lobas_av
 * @coverage bindings.rcp.ui
 */
public final class TypeImageProvider {
	public static final ImageDescriptor OBJECT_IMAGE = Activator.getImageDescriptor("types/Object2.png");
	public static final ImageDescriptor VIEWER_IMAGE = Activator.getImageDescriptor("types/viewer.png");
	public static final ImageDescriptor VIEWER_COLLECTION_IMAGE =
			Activator.getImageDescriptor("types/viewer_collection.png");
	public static final ImageDescriptor STRING_IMAGE = Activator.getImageDescriptor("types/String2.png");
	public static final ImageDescriptor BOOLEAN_IMAGE = Activator.getImageDescriptor("types/Boolean4.png");
	public static final ImageDescriptor NUMBER_IMAGE = Activator.getImageDescriptor("types/Number2.png");
	public static final ImageDescriptor IMAGE_IMAGE = Activator.getImageDescriptor("types/Image2.png");
	public static final ImageDescriptor COLOR_IMAGE = Activator.getImageDescriptor("types/Color2.png");
	public static final ImageDescriptor FONT_IMAGE = Activator.getImageDescriptor("types/Font2.png");
	public static final ImageDescriptor ARRAY_IMAGE = Activator.getImageDescriptor("types/Array.png");
	public static final ImageDescriptor COLLECTION_IMAGE = Activator.getImageDescriptor("types/Collection.png");
	public static final ImageDescriptor DIRECT_IMAGE = Activator.getImageDescriptor("types/Direct.png");
	public static final ImageDescriptor METHOD_IMAGE = Activator.getImageDescriptor("method.png");

	////////////////////////////////////////////////////////////////////////////
	//
	// Image
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return {@link ImageDescriptor} association with given {@link Class}.
	 */
	public static ImageDescriptor getImageDescriptor(Class<?> type) {
		// unknown type accept as object
		if (type == null) {
			return OBJECT_IMAGE;
		}
		// string
		if (type == String.class || type == byte.class || type == char.class) {
			return STRING_IMAGE;
		}
		// boolean
		if (type == boolean.class || type == Boolean.class) {
			return BOOLEAN_IMAGE;
		}
		// arithmetic
		if (type == int.class
				|| type == short.class
				|| type == long.class
				|| type == float.class
				|| type == double.class) {
			return NUMBER_IMAGE;
		}
		// array
		if (type.isArray()) {
			return ARRAY_IMAGE;
		}
		// Collection
		if (Collection.class.isAssignableFrom(type)) {
			return COLLECTION_IMAGE;
		}
		// SWT image
		if (type == Image.class) {
			return IMAGE_IMAGE;
		}
		// SWT color
		if (type == Color.class) {
			return COLOR_IMAGE;
		}
		// SWT font
		if (type == Font.class) {
			return FONT_IMAGE;
		}
		// other accept as object
		return OBJECT_IMAGE;
	}
}