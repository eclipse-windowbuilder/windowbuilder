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
package org.eclipse.wb.internal.rcp.databinding.xwt.model.widgets;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.rcp.databinding.xwt.model.ObserveTypeContainer;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author lobas_av
 *
 */
public final class WidgetsObserveTypeContainer extends ObserveTypeContainer {
	private List<WidgetBindableInfo> m_observables = Collections.emptyList();

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public WidgetsObserveTypeContainer() {
		super(ObserveType.WIDGETS, true, false);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IObserveInfo
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void synchronizeObserves() throws Exception {
		for (WidgetBindableInfo widget : m_observables) {
			widget.update();
		}
	}

	@Override
	public void createObservables(XmlObjectInfo xmlObjectRoot) throws Exception {
		m_observables = Lists.newArrayList();
		m_observables.add(new WidgetBindableInfo(xmlObjectRoot, null));
	}

	@Override
	public List<IObserveInfo> getObservables() {
		return CoreUtils.cast(m_observables);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public WidgetBindableInfo resolve(XmlObjectInfo xmlObjectInfo) {
		return m_observables.get(0).resolve(xmlObjectInfo);
	}

	public WidgetBindableInfo resolve(DocumentElement element) {
		return m_observables.get(0).resolve(element);
	}

	public WidgetBindableInfo resolve(String reference) throws Exception {
		return (WidgetBindableInfo) m_observables.get(0).resolveReference(reference);
	}
}