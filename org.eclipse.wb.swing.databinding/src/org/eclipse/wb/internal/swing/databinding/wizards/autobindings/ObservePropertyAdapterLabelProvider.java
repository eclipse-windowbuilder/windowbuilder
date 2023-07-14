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
package org.eclipse.wb.internal.swing.databinding.wizards.autobindings;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * {@link LabelProvider} for {@link ObservePropertyAdapter}.
 *
 * @author lobas_av
 * @coverage bindings.swing.wizard.auto
 */
public final class ObservePropertyAdapterLabelProvider extends LabelProvider {
	private final ResourceManager m_resourceManager = new LocalResourceManager(JFaceResources.getResources());
	////////////////////////////////////////////////////////////////////////////
	//
	// LabelProvider
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void dispose() {
		super.dispose();
		m_resourceManager.dispose();
	}

	@Override
	public String getText(Object element) {
		ObservePropertyAdapter adapter = (ObservePropertyAdapter) element;
		return adapter.getName();
	}

	@Override
	public Image getImage(Object element) {
		try {
			ObservePropertyAdapter adapter = (ObservePropertyAdapter) element;
			return m_resourceManager.createImage(adapter.getObserve().getPresentation().getImageDescriptor());
		} catch (Throwable e) {
			return null;
		}
	}
}