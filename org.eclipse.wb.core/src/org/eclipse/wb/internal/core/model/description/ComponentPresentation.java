/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
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
package org.eclipse.wb.internal.core.model.description;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;

import java.io.ByteArrayInputStream;

/**
 * Information for component on palette.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class ComponentPresentation {
	private final String m_key;
	private final String m_toolkitId;
	private final String m_name;
	private final String m_description;
	private byte[] m_iconBytes;
	private ImageDescriptor m_icon;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ComponentPresentation(String key,
			String toolkitId,
			String name,
			String description,
			byte[] iconBytes) {
		m_key = key;
		m_toolkitId = toolkitId;
		m_name = name;
		m_description = description;
		m_iconBytes = iconBytes;
	}

	public ComponentPresentation(String key,
			String toolkitId,
			String name,
			String description,
			ImageDescriptor icon) {
		m_key = key;
		m_toolkitId = toolkitId;
		m_name = name;
		m_description = description;
		m_icon = icon;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public String getKey() {
		return m_key;
	}

	public String getToolkitId() {
		return m_toolkitId;
	}

	public String getName() {
		return m_name;
	}

	public String getDescription() {
		return m_description;
	}

	public ImageDescriptor getIcon() {
		if (m_icon == null) {
			if (m_iconBytes != null) {
				ImageData imageData = new ImageData(new ByteArrayInputStream(m_iconBytes));
				m_icon = ImageDescriptor.createFromImageDataProvider(zoom -> zoom == 100 ? imageData : null);
			}
		}
		return m_icon;
	}
}
