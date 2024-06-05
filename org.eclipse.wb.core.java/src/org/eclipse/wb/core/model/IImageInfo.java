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

import org.eclipse.wb.internal.core.utils.ui.dialogs.image.ImageInfo;

import org.eclipse.swt.graphics.Image;

/**
 * Public interface of {@link ImageInfo} used by {@link IImageProcessor}. The
 * image info describes the {@link Image} attached to an
 * {@link IGenericProperty}.
 *
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be subclassed by clients.
 */
public interface IImageInfo {
	/**
	 * @return the id of page that provided this {@link ImageInfo}.
	 */
	String getPageId();

	/**
	 * @return the page specific data abound image, usually string with path.
	 */
	Object getData();

	/**
	 * @return the SWT {@link Image} of this {@link ImageInfo}.
	 */
	Image getImage();

	/**
	 * @return the size of image in bytes.
	 */
	long getSize();
}
