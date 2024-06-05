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
package org.eclipse.wb.core.editor.icon;

import org.eclipse.wb.core.model.IGenericProperty;
import org.eclipse.wb.core.model.IImageInfo;
import org.eclipse.wb.internal.core.editor.icon.AbstractImageProcessor;
import org.eclipse.wb.internal.core.model.property.table.PropertyTable;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.ClasspathImagePage;

/**
 * Abstract base class for all image processors that access files via the
 * classpath (e.g. images inside jars). May be sub-classed by clients.
 */
public abstract class AbstractClasspathImageProcessor extends AbstractImageProcessor {

	public AbstractClasspathImageProcessor() {
		super("Classpath: ");
	}

	@Override
	public final String getPageId() {
		return ClasspathImagePage.ID;
	}

	@Override
	public final boolean postOpen(IGenericProperty property, IImageInfo imageInfo, String[] value) {
		if (getPageId().equals(imageInfo.getPageId())) {
			String path = "/" + imageInfo.getData();
			return postOpen(property, path, value);
		}
		return false;
	}

	/**
	 *
	 * This method is called by the {@link PropertyTable} after the image dialog has
	 * been closed. It generates the Java code that is added to the widget.
	 *
	 * @param property the image property.
	 * @param path     the image path.
	 * @param value    the array with single element, processor can change it.
	 * @return {@code true}, if the value was set by this processor.
	 */
	public abstract boolean postOpen(IGenericProperty property, String path, String[] value);
}
