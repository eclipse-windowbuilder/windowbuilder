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
package org.eclipse.wb.internal.rcp.databinding.ui.providers;

import org.eclipse.wb.internal.core.databinding.model.IBindingInfo;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.rcp.databinding.Activator;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.BindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.ListBindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.SetBindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.ValueBindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.AbstractViewerInputBindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.IDelayValueProvider;

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
 * Implementation of {@link ITableLabelProvider} for {@link IBindingInfo}.
 *
 * @author lobas_av
 * @coverage bindings.rcp.ui
 */
public final class BindingLabelProvider extends LabelProvider implements ITableLabelProvider {
	public static final ImageDescriptor BIND_VALUE_IMAGE = Activator.getImageDescriptor("bindValue.png");
	private static final ImageDescriptor BIND_LIST_IMAGE = Activator.getImageDescriptor("bindList.png");
	private static final ImageDescriptor BIND_SET_IMAGE = Activator.getImageDescriptor("bindSet.png");
	public static final ImageDescriptor CLOCK_DECORATION_IMAGE = Activator.getImageDescriptor("clock.png");
	private final ResourceManager m_resourceManager = new LocalResourceManager(JFaceResources.getResources());

	@Override
	public void dispose() {
		super.dispose();
		m_resourceManager.dispose();
	}

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
			if (element instanceof BindingInfo) {
				return getBindingColumnText((BindingInfo) element, column);
			}
			return getViewerBindingColumnText((AbstractViewerInputBindingInfo) element, column);
		}, "<exception, see log>");
	}

	/**
	 * Column text for simple bindings.
	 */
	private static String getBindingColumnText(BindingInfo binding, int column) throws Exception {
		switch (column) {
		case 1 :
			// target
			return binding.getTargetObservable().getPresentationText();
		case 2 :
			// model
			return binding.getModelObservable().getPresentationText();
		case 3 :
			// target strategy
			return binding.getTargetStrategy().getPresentationText();
		case 4 :
			// model strategy
			return binding.getModelStrategy().getPresentationText();
		case 5 :
			// binding
			return binding.getVariableIdentifier();
		default :
			return null;
		}
	}

	/**
	 * Column text for viewer input bindings.
	 */
	private static String getViewerBindingColumnText(AbstractViewerInputBindingInfo binding,
			int column) throws Exception {
		switch (column) {
		case 1 :
			// target
			return binding.getPresentationText();
		case 2 :
			// model
			return binding.getInputObservable().getPresentationText();
		case 5 :
			// binding
			return binding.getVariableIdentifier();
		default :
			return null;
		}
	}

	@Override
	public Image getColumnImage(Object element, int column) {
		if (column == 0) {
			return m_resourceManager.createImageWithDefault(getIcon(element));
		}
		return null;
	}

	public static ImageDescriptor getIcon(Object element) {
		ImageDescriptor imageDescriptor = null;
		// binding
		if (element instanceof ValueBindingInfo) {
			imageDescriptor = BIND_VALUE_IMAGE;
		} else if (element instanceof ListBindingInfo) {
			imageDescriptor = BIND_LIST_IMAGE;
		} else if (element instanceof SetBindingInfo) {
			imageDescriptor = BIND_SET_IMAGE;
		} else if (element instanceof AbstractViewerInputBindingInfo) {
			imageDescriptor = TypeImageProvider.VIEWER_IMAGE;
		} else {
			return null;
		}
		// delay
		if (isDelayBinding(element)) {
			imageDescriptor = new DecorationOverlayIcon(imageDescriptor, CLOCK_DECORATION_IMAGE,
					IDecoration.BOTTOM_RIGHT);
		}
		return imageDescriptor;
	}

	private static boolean isDelayBinding(Object element) {
		if (element instanceof BindingInfo binding) {
			return isDelayObservable(binding.getTargetObservable())
					|| isDelayObservable(binding.getModelObservable());
		}
		if (element instanceof AbstractViewerInputBindingInfo binding) {
			return isDelayObservable(binding.getInputObservable());
		}
		return false;
	}

	private static boolean isDelayObservable(ObservableInfo observable) {
		if (observable instanceof DetailBeanObservableInfo detailObservable) {
			return isDelayObservable(detailObservable.getMasterObservable());
		}
		if (observable instanceof IDelayValueProvider delayValueProvider) {
			return delayValueProvider.getDelayValue() != 0;
		}
		return false;
	}
}