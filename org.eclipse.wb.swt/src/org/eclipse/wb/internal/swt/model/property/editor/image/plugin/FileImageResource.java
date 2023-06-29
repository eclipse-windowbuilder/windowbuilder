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
package org.eclipse.wb.internal.swt.model.property.editor.image.plugin;

import org.eclipse.wb.internal.core.utils.ui.dialogs.image.ImageInfo;
import org.eclipse.wb.internal.core.utils.ui.dialogs.image.pages.browse.model.IImageResource;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import java.io.InputStream;

/**
 * Implementation {@link IImageResource} for file image resource into workspace plugin project.
 *
 * @author lobas_av
 * @coverage swt.property.editor.plugin
 */
public final class FileImageResource extends ImageResource {
	private final IFile m_resource;
	private final String m_symbolicName;
	private final String m_imagePath;
	private ImageInfo m_imageInfo;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public FileImageResource(IFile resource, String symbolicName) {
		m_resource = resource;
		m_symbolicName = symbolicName;
		m_imagePath = m_resource.getProjectRelativePath().toOSString().replace('\\', '/');
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IImageResource
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public ImageInfo getImageInfo() {
		if (m_imageInfo == null) {
			try {
				// load image
				Image image;
				try {
					InputStream inputStream = m_resource.getContents(true);
					try {
						image = new Image(Display.getCurrent(), inputStream);
					} finally {
						inputStream.close();
					}
				} catch (Throwable e) {
					return null;
				}
				// add to cache
				m_imageInfo =
						new ImageInfo(PluginFileImagePage.ID,
								new String[]{m_symbolicName, m_imagePath},
								image,
								-1);
			} catch (Throwable e) {
				m_imageInfo = null;
			}
		}
		return m_imageInfo;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IImageElement
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getName() {
		return m_resource.getName();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Internal
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return resource image path.
	 */
	public String getPath() {
		return m_imagePath;
	}

	/**
	 * Disposes {@link Image} in {@link ImageInfo}.
	 */
	@Override
	public void dispose() {
		if (m_imageInfo != null) {
			m_imageInfo.getImage().dispose();
		}
	}
}