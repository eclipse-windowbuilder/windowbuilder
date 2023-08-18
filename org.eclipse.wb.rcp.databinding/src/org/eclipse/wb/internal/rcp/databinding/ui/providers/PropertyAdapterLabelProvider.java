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
package org.eclipse.wb.internal.rcp.databinding.ui.providers;

import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.PropertyAdapter;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Implementation of {@link LabelProvider} for {@link PropertyAdapter}.
 *
 * @author lobas_av
 * @coverage bindings.rcp.ui
 */
public final class PropertyAdapterLabelProvider extends LabelProvider {
	private final ResourceManager m_resourceManager = new LocalResourceManager(JFaceResources.getResources());

	////////////////////////////////////////////////////////////////////////////
	//
	// LabelProvider
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getText(Object element) {
		PropertyAdapter adapter = (PropertyAdapter) element;
		return adapter.getName();
	}

	@Override
	public Image getImage(Object element) {
		PropertyAdapter adapter = (PropertyAdapter) element;
		return m_resourceManager.createImageWithDefault(TypeImageProvider.getImageDescriptor(adapter.getType()));
	}

	@Override
	public void dispose() {
		super.dispose();
		m_resourceManager.dispose();
	}
}