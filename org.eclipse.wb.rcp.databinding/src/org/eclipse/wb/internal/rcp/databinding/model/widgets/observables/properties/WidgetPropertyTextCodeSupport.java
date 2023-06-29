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
package org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.properties;

import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetPropertyBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.TextSwtObservableInfo;

import java.util.Iterator;
import java.util.List;

/**
 * Model for observable object <code>WidgetProperties.text(...)</code>.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public class WidgetPropertyTextCodeSupport extends WidgetPropertiesCodeSupport {
	private final int[] m_parseEvents;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	public WidgetPropertyTextCodeSupport() {
		this(null);
	}

	public WidgetPropertyTextCodeSupport(int[] events) {
		super("observeText");
		m_parseEvents = events;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Parser
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected ObservableInfo createObservable(WidgetBindableInfo bindableWidget,
			WidgetPropertyBindableInfo bindableProperty,
			int delayValue) throws Exception {
		Assert.isNotNull(m_parseEvents);
		TextSwtObservableInfo observable =
				new TextSwtObservableInfo(bindableWidget, bindableProperty, m_parseEvents);
		observable.setDelayValue(delayValue);
		return observable;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Code generation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected String getSourceCode(ObservableInfo observable) throws Exception {
		TextSwtObservableInfo textObservable = (TextSwtObservableInfo) observable;
		List<String> updateEvents = textObservable.getUpdateEvents();
		//
		if (updateEvents.size() == 0) {
			return super.getSourceCode(observable);
		}
		return "org.eclipse.jface.databinding.swt.typed.WidgetProperties.text("
		+ getEventsSourceCode(updateEvents)
		+ ")";
	}

	@Override
	public String getSourceCode() throws Exception {
		if (m_parseEvents.length == 0) {
			return super.getSourceCode();
		}
		List<String> updateEvents = TextSwtObservableInfo.getEventsSources(m_parseEvents);
		return "org.eclipse.jface.databinding.swt.typed.WidgetProperties.text("
		+ getEventsSourceCode(updateEvents)
		+ ")";
	}

	private String getEventsSourceCode(List<String> updateEvents) {
		int size = updateEvents.size();
		StringBuffer events = new StringBuffer();
		if (size == 1) {
			events.append("org.eclipse.swt." + updateEvents.get(0));
		} else {
			events.append("new int[]{");
			for (Iterator<String> I = updateEvents.iterator(); I.hasNext();) {
				events.append("org.eclipse.swt." + I.next());
				if (I.hasNext()) {
					events.append(", ");
				}
			}
			events.append("}");
		}
		return events.toString();
	}
}