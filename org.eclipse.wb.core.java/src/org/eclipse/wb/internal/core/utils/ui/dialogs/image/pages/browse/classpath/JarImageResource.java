/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
package org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.classpath;

import org.eclipse.wb.internal.core.utils.ui.dialogs.image.ImageInfo;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.model.IImageResource;

import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.graphics.Image;

/**
 * implementation of {@link IImageResource} for single file in jar.
 *
 * @author scheglov_ke
 * @coverage core.ui
 */
final class JarImageResource extends AbstractJarImageElement implements IImageResource {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public JarImageResource(JarImageContainer jarContainer, IPath entryPath) {
		super(jarContainer, entryPath);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IImageElement
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Image getImage() {
		ImageInfo imageInfo = getImageInfo();
		return imageInfo != null ? imageInfo.getImage() : null;
	}

	@Override
	public String getName() {
		return m_entryPath.lastSegment();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IImageResource
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public ImageInfo getImageInfo() {
		return m_jarContainer.getImage(m_entryPath.toOSString().replace('\\', '/'));
	}
}
