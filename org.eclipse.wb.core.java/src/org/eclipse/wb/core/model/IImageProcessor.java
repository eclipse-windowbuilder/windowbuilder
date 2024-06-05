/*******************************************************************************
 * Copyright (c) 2024 DSA GmbH, Aachen and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    DSA GmbH, Aachen - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.core.model;

import org.eclipse.wb.core.editor.icon.AbstractClasspathImageProcessor;
import org.eclipse.wb.core.editor.icon.AbstractFileImageProcessor;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.ImageInfo;

/**
 * This interface is used for accessing and modifying the the images that are
 * attached to generic properties. Clients may contribute custom processors via
 * the {@code org.eclipse.wb.core.java.imageProcessor} extension point.
 *
 * @noimplement This interface is not intended to be implemented by clients. Use
 *              either {@link AbstractClasspathImageProcessor} or
 *              {@link AbstractFileImageProcessor}.
 * @noextend This interface is not intended to be subclassed by clients.
 */
public interface IImageProcessor {
	/**
	 * @return the id of page that provided this {@link ImageInfo}.
	 */
	String getPageId();

	/**
	 * This method is called by the {@link PropertyTable} to get a human-readable
	 * representation of the attached image.
	 *
	 * @param property the image property.
	 * @param value    the array with single element, processor can change it.
	 * @return {@code true}, if the value was set by this processor.
	 */
	boolean process(IGenericProperty property, String[] value);

	/**
	 * This method is called by the {@link PropertyTable} before the image dialog is
	 * opened. It extracts the image path from the human-readable text.
	 *
	 * @param property the image property.
	 * @param text     the human-readable representation of the attached image.
	 * @param value    the array with single element, processor can change it.
	 * @return {@code true}, if the value was set by this processor.
	 */
	boolean preOpen(IGenericProperty property, String text, Object[] value);

	/**
	 * This method is called by the {@link PropertyTable} after the image dialog has
	 * been closed. It generates the Java code that is added to the widget.
	 *
	 * @param property  the image property.
	 * @param imageInfo the image that was selected in the dialog.
	 * @param value     the array with single element, processor can change it.
	 * @return {@code true}, if the value was set by this processor.
	 */
	boolean postOpen(IGenericProperty property, IImageInfo imageInfo, String[] value);
}
