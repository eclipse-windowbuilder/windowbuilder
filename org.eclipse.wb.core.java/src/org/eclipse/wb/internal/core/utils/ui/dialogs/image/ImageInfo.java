/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.internal.core.utils.ui.dialogs.image;

import org.eclipse.wb.core.model.IImageInfo;

import org.eclipse.swt.graphics.Image;

/**
 * Information about image.
 *
 * @author scheglov_ke
 * @coverage core.ui
 */
public final class ImageInfo implements IImageInfo {
	private final String m_pageId;
	private final Object m_data;
	private final Image m_image;
	private final long m_size;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ImageInfo(String pageId, Object data, Image image, long size) {
		m_pageId = pageId;
		m_data = data;
		m_image = image;
		m_size = size;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getPageId() {
		return m_pageId;
	}

	@Override
	public Object getData() {
		return m_data;
	}

	@Override
	public Image getImage() {
		return m_image;
	}

	@Override
	public long getSize() {
		return m_size;
	}
}
