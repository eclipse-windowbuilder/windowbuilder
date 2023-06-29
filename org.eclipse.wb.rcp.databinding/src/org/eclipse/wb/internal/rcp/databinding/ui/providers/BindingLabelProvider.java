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
package org.eclipse.wb.internal.rcp.databinding.ui.providers;

import org.eclipse.wb.internal.core.databinding.model.IBindingInfo;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.ui.SwtResourceManager;
import org.eclipse.wb.internal.rcp.databinding.Activator;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.BindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.ListBindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.SetBindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.ValueBindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.AbstractViewerInputBindingInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.IDelayValueProvider;

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
	public static final Image BIND_VALUE_IMAGE = Activator.getImage("bindValue.png");
	private static final Image BIND_LIST_IMAGE = Activator.getImage("bindList.png");
	private static final Image BIND_SET_IMAGE = Activator.getImage("bindSet.png");
	public static final Image CLOCK_DECORATION_IMAGE = Activator.getImage("clock.png");
	public static final BindingLabelProvider INSTANCE = new BindingLabelProvider();

	////////////////////////////////////////////////////////////////////////////
	//
	// ITableLabelProvider
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getColumnText(final Object element, final int column) {
		return ExecutionUtils.runObjectLog(new RunnableObjectEx<String>() {
			@Override
			public String runObject() throws Exception {
				if (element instanceof BindingInfo) {
					return getBindingColumnText((BindingInfo) element, column);
				}
				return getViewerBindingColumnText((AbstractViewerInputBindingInfo) element, column);
			}
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
		Image image = null;
		if (column == 0) {
			// binding
			if (element instanceof ValueBindingInfo) {
				image = BIND_VALUE_IMAGE;
			} else if (element instanceof ListBindingInfo) {
				image = BIND_LIST_IMAGE;
			} else if (element instanceof SetBindingInfo) {
				image = BIND_SET_IMAGE;
			} else if (element instanceof AbstractViewerInputBindingInfo) {
				image = TypeImageProvider.VIEWER_IMAGE;
			} else {
				return null;
			}
			// delay
			if (isDelayBinding(element)) {
				image =
						SwtResourceManager.decorateImage(
								image,
								CLOCK_DECORATION_IMAGE,
								SwtResourceManager.BOTTOM_RIGHT);
			}
		}
		return image;
	}

	private static boolean isDelayBinding(Object element) {
		if (element instanceof BindingInfo) {
			BindingInfo binding = (BindingInfo) element;
			return isDelayObservable(binding.getTargetObservable())
					|| isDelayObservable(binding.getModelObservable());
		}
		if (element instanceof AbstractViewerInputBindingInfo) {
			AbstractViewerInputBindingInfo binding = (AbstractViewerInputBindingInfo) element;
			return isDelayObservable(binding.getInputObservable());
		}
		return false;
	}

	private static boolean isDelayObservable(ObservableInfo observable) {
		if (observable instanceof DetailBeanObservableInfo) {
			DetailBeanObservableInfo detailObservable = (DetailBeanObservableInfo) observable;
			return isDelayObservable(detailObservable.getMasterObservable());
		}
		if (observable instanceof IDelayValueProvider) {
			IDelayValueProvider delayValueProvider = (IDelayValueProvider) observable;
			return delayValueProvider.getDelayValue() != 0;
		}
		return false;
	}
}