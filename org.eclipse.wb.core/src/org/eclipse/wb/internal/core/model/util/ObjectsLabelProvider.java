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
package org.eclipse.wb.internal.core.model.util;

import org.eclipse.wb.core.model.ObjectInfo;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Implementation of {@link LabelProvider} for {@link ObjectInfo}.
 *
 * @author scheglov_ke
 * @coverage core.model.util
 */
public final class ObjectsLabelProvider extends LabelProvider {
	private ResourceManager m_resourceManager = new LocalResourceManager(JFaceResources.getResources());

	@Override
	public void dispose() {
		super.dispose();
		m_resourceManager.dispose();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// LabelProvider
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Image getImage(final Object element) {
		ImageDescriptor imageDescriptor = ObjectInfo.getImageDescriptor((ObjectInfo) element);
		return imageDescriptor == null ? null : m_resourceManager.createImage(imageDescriptor);
	}

	@Override
	public String getText(final Object element) {
		return ObjectInfo.getText((ObjectInfo) element);
	}
}
