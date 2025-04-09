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
package org.eclipse.wb.internal.core.databinding.ui.providers;

import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.IObservePresentation;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 * Implementation of {@link LabelProvider} for {@link IObserveInfo}.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public class ObserveLabelProvider extends LabelProvider {
	private final ResourceManager m_resourceManager = new LocalResourceManager(JFaceResources.getResources());
	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	private static IObservePresentation getPresentation(Object element) {
		IObserveInfo observe = (IObserveInfo) element;
		return observe.getPresentation();
	}

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
	public String getText(final Object element) {
		return ExecutionUtils.runObjectLog(() -> getPresentation(element).getText(), "<exception, see log>");
	}

	@Override
	public Image getImage(final Object element) {
		return ExecutionUtils.runObjectLog(() -> m_resourceManager.createImage(getPresentation(element).getImageDescriptor()), null);
	}
}