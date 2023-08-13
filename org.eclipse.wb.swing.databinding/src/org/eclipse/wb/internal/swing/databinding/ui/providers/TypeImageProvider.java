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
package org.eclipse.wb.internal.swing.databinding.ui.providers;

import org.eclipse.wb.internal.swing.databinding.Activator;

import org.eclipse.jface.resource.ImageDescriptor;

import java.util.Collection;

/**
 * Helper for association {@link Class} with {@link ImageDescriptor}.
 *
 * @author lobas_av
 * @coverage bindings.swing.ui
 */
public final class TypeImageProvider {
	public static final ImageDescriptor OBJECT_IMAGE = Activator.getImageDescriptor("types/Object.png");
	public static final ImageDescriptor STRING_IMAGE = Activator.getImageDescriptor("types/String.png");
	public static final ImageDescriptor BOOLEAN_IMAGE = Activator.getImageDescriptor("types/Boolean.png");
	public static final ImageDescriptor NUMBER_IMAGE = Activator.getImageDescriptor("types/Number.png");
	public static final ImageDescriptor IMAGE_IMAGE = Activator.getImageDescriptor("types/Image.png");
	public static final ImageDescriptor COLOR_IMAGE = Activator.getImageDescriptor("types/Color.png");
	public static final ImageDescriptor FONT_IMAGE = Activator.getImageDescriptor("types/Font.png");
	public static final ImageDescriptor ARRAY_IMAGE = Activator.getImageDescriptor("types/Array.png");
	public static final ImageDescriptor COLLECTION_IMAGE = Activator.getImageDescriptor("types/Collection.png");
	public static final ImageDescriptor EL_PROPERTY_IMAGE = Activator.getImageDescriptor("el_property2.gif");
	public static final ImageDescriptor OBJECT_PROPERTY_IMAGE = Activator.getImageDescriptor("SelfObject.png");

	////////////////////////////////////////////////////////////////////////////
	//
	// Image
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return {@link ImageDescriptor} association with given {@link Class}.
	 */
	public static ImageDescriptor getImage(Class<?> type) {
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
		// AWT image
		if (type == java.awt.Image.class || type == javax.swing.Icon.class) {
			return IMAGE_IMAGE;
		}
		// AWT color
		if (type == java.awt.Color.class) {
			return COLOR_IMAGE;
		}
		// AWT font
		if (type == java.awt.Font.class) {
			return FONT_IMAGE;
		}
		// other accept as object
		return OBJECT_IMAGE;
	}
}