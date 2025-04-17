/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
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
package org.eclipse.wb.internal.rcp.databinding.model.widgets.observables;

import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.Messages;
import org.eclipse.wb.internal.rcp.databinding.model.BindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.BindingUiContentProviderContext;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetPropertyBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.ui.contentproviders.SwtDelayUiContentProvider;

import java.util.List;

/**
 * Model for observable objects <code>SWTObservables.observeXXX(Control)</code>.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public class SwtObservableInfo extends ObservableInfo implements IDelayValueProvider {
	protected final WidgetBindableInfo m_bindableWidget;
	protected final WidgetPropertyBindableInfo m_bindableProperty;
	private int m_delayValue;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SwtObservableInfo(WidgetBindableInfo bindableWidget,
			WidgetPropertyBindableInfo bindableProperty) {
		m_bindableWidget = bindableWidget;
		m_bindableProperty = bindableProperty;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// ObservableInfo
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public final BindableInfo getBindableObject() {
		return m_bindableWidget;
	}

	@Override
	public final BindableInfo getBindableProperty() {
		return m_bindableProperty;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// DelayValue
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public final int getDelayValue() {
		return m_delayValue;
	}

	@Override
	public final void setDelayValue(int delayValue) {
		Assert.isTrue(delayValue >= 0);
		m_delayValue = delayValue;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Editing
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void createContentProviders(List<IUiContentProvider> providers,
			BindingUiContentProviderContext context,
			DatabindingsProvider provider) throws Exception {
		providers.add(new SwtDelayUiContentProvider(this, Messages.SwtObservableInfo_swtDelay));
	}
}