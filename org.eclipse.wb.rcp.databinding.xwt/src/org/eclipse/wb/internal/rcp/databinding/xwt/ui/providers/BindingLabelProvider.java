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
package org.eclipse.wb.internal.rcp.databinding.xwt.ui.providers;

import static org.eclipse.wb.internal.rcp.databinding.ui.providers.BindingLabelProvider.BIND_VALUE_IMAGE;
import static org.eclipse.wb.internal.rcp.databinding.ui.providers.BindingLabelProvider.CLOCK_DECORATION_IMAGE;

import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.rcp.databinding.xwt.model.BindingInfo;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

/**
 *
 * @author lobas_av
 *
 */
public class BindingLabelProvider extends LabelProvider implements ITableLabelProvider {
	private final ResourceManager m_resourceManager = new LocalResourceManager(JFaceResources.getResources());

	////////////////////////////////////////////////////////////////////////////
	//
	// ITableLabelProvider
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getColumnText(final Object element, final int column) {
		return getText(element, column);
	}

	public static String getText(final Object element, final int column) {
		return ExecutionUtils.runObjectLog(() -> {
			BindingInfo binding = (BindingInfo) element;
			switch (column) {
			case 1 :
				// target
				return binding.getTargetPresentationText();
			case 2 :
				// model
				return binding.getModelPresentationText();
			case 3 :
				// mode
				return BindingInfo.MODES[binding.getMode()];
			default :
				return null;
			}
		}, "<exception, see log>");
	}

	@Override
	public Image getColumnImage(Object element, int column) {
		if (column == 0) {
			return m_resourceManager.createImageWithDefault(getIcon(element));
		}
		return null;
	}

	public static ImageDescriptor getIcon(Object element) {
		// binding
		ImageDescriptor imageDescriptor = BIND_VALUE_IMAGE;
		// delay
		if (isDelayBinding(element)) {
			imageDescriptor = new DecorationOverlayIcon(imageDescriptor, CLOCK_DECORATION_IMAGE, IDecoration.BOTTOM_RIGHT);
		}
		return imageDescriptor;
	}

	private static boolean isDelayBinding(Object element) {
		// XXX
		return false;
	}

	@Override
	public void dispose() {
		super.dispose();
		m_resourceManager.dispose();
	}
}