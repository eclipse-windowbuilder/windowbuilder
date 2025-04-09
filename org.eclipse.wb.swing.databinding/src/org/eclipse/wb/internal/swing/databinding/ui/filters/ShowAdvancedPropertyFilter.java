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
package org.eclipse.wb.internal.swing.databinding.ui.filters;

import org.eclipse.wb.internal.core.databinding.model.IObserveDecoration;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.ui.decorate.IObserveDecorator;
import org.eclipse.wb.internal.core.databinding.ui.filter.PropertyFilter;
import org.eclipse.wb.internal.swing.databinding.Activator;
import org.eclipse.wb.internal.swing.databinding.Messages;

import org.eclipse.jface.viewers.Viewer;

/**
 * Show advanced properties filter.
 *
 * @author lobas_av
 * @coverage bindings.swing.ui
 */
public final class ShowAdvancedPropertyFilter extends PropertyFilter {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ShowAdvancedPropertyFilter() {
		super(Messages.ShowAdvancedPropertyFilter_title, Activator.getImageDescriptor("show_advanced.png"));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// PropertyFilter
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean select(Viewer viewer, IObserveInfo propertyObserve) {
		IObserveDecoration observeDecoration = (IObserveDecoration) propertyObserve;
		return observeDecoration.getDecorator() != IObserveDecorator.HIDDEN;
	}
}