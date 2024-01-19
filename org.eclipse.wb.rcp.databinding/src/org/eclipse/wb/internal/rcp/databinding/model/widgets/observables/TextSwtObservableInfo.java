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
package org.eclipse.wb.internal.rcp.databinding.model.widgets.observables;

import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.model.context.BindingUiContentProviderContext;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetPropertyBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.ui.contentproviders.SwtTextEventsUiContentProvider;

import org.eclipse.swt.SWT;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Model for observable object <code>SWTObservables.observeText(Control, int)</code>.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public final class TextSwtObservableInfo extends SwtObservableInfo {
	private static final int[] VALID_UPDATE_EVENT_TYPES = {
			SWT.Modify,
			SWT.FocusOut,
			SWT.DefaultSelection,
			SWT.None};
	public static final String[] TEXT_EVENTS = {
			"SWT.Modify",
			"SWT.FocusOut",
			"SWT.DefaultSelection",
	"SWT.NONE"};
	private List<String> m_updateEvents;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	public TextSwtObservableInfo(WidgetBindableInfo bindableWidget,
			WidgetPropertyBindableInfo bindableProperty) {
		this(bindableWidget, bindableProperty, new int[]{SWT.Modify});
	}

	public TextSwtObservableInfo(WidgetBindableInfo bindableWidget,
			WidgetPropertyBindableInfo bindableProperty,
			int updateEvent) {
		this(bindableWidget, bindableProperty, new int[]{updateEvent});
	}

	public TextSwtObservableInfo(WidgetBindableInfo bindableWidget,
			WidgetPropertyBindableInfo bindableProperty,
			int[] updateEvents) {
		super(bindableWidget, bindableProperty);
		Assert.isNotNull(updateEvents);
		m_updateEvents = getEventsSources(updateEvents);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public List<String> getUpdateEvents() {
		return new ArrayList<>(m_updateEvents);
	}

	public void setUpdateEvents(List<String> updateEvents) {
		m_updateEvents = new ArrayList<>(updateEvents);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getPresentationText() throws Exception {
		String presentationText = super.getPresentationText();
		if (!m_updateEvents.isEmpty()) {
			presentationText += "(" + StringUtils.join(m_updateEvents, ", ") + ")";
		}
		return presentationText;
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
		providers.add(new SwtTextEventsUiContentProvider(this));
		super.createContentProviders(providers, context, provider);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utilities
	//
	////////////////////////////////////////////////////////////////////////////
	public static List<String> getEventsSources(int[] updateEvents) {
		List<String> updateEventStrings = new ArrayList<>();
		if (updateEvents.length == 1) {
			int updateEventTypeIndex = ArrayUtils.indexOf(VALID_UPDATE_EVENT_TYPES, updateEvents[0]);
			Assert.isTrue(updateEventTypeIndex >= 0
					&& updateEventTypeIndex < VALID_UPDATE_EVENT_TYPES.length);
			updateEventStrings.add(TEXT_EVENTS[updateEventTypeIndex]);
		} else {
			for (int i = 0; i < updateEvents.length; i++) {
				int eventValue = updateEvents[i];
				int updateEventTypeIndex = ArrayUtils.indexOf(VALID_UPDATE_EVENT_TYPES, eventValue);
				Assert.isTrue(eventValue != SWT.NONE
						&& updateEventTypeIndex >= 0
						&& updateEventTypeIndex < VALID_UPDATE_EVENT_TYPES.length);
				updateEventStrings.add(TEXT_EVENTS[updateEventTypeIndex]);
			}
		}
		return updateEventStrings;
	}
}