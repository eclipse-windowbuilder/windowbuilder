/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

import java.util.Collection;

/**
 * Helper for association {@link Class} with {@link Image}.
 *
 * @author lobas_av
 * @coverage bindings.rcp.ui
 */
public final class TypeImageProvider {
	public static final Image OBJECT_IMAGE = Activator.getImage("types/Object2.png");
	public static final Image VIEWER_IMAGE = Activator.getImage("types/viewer.png");
	public static final Image VIEWER_COLLECTION_IMAGE =
			Activator.getImage("types/viewer_collection.png");
	public static final Image STRING_IMAGE = Activator.getImage("types/String2.png");
	public static final Image BOOLEAN_IMAGE = Activator.getImage("types/Boolean4.png");
	public static final Image NUMBER_IMAGE = Activator.getImage("types/Number2.png");
	public static final Image IMAGE_IMAGE = Activator.getImage("types/Image2.png");
	public static final Image COLOR_IMAGE = Activator.getImage("types/Color2.png");
	public static final Image FONT_IMAGE = Activator.getImage("types/Font2.png");
	public static final Image ARRAY_IMAGE = Activator.getImage("types/Array.png");
	public static final Image COLLECTION_IMAGE = Activator.getImage("types/Collection.png");
	public static final Image DIRECT_IMAGE = Activator.getImage("types/Direct.png");
	public static final Image METHOD_IMAGE = Activator.getImage("method.png");

	////////////////////////////////////////////////////////////////////////////
	//
	// Image
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return {@link Image} association with given {@link Class}.
	 */
	public static Image getImage(Class<?> type) {
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