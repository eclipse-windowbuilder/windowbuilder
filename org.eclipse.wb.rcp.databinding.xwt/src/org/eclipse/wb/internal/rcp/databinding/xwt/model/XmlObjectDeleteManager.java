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
package org.eclipse.wb.internal.rcp.databinding.xwt.model;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.IBindingInfo;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.JavaInfoDeleteManager;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.rcp.databinding.xwt.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.xwt.model.widgets.WidgetBindableInfo;

import java.util.List;

/**
 *
 * @author lobas_av
 *
 */
public class XmlObjectDeleteManager extends JavaInfoDeleteManager {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public XmlObjectDeleteManager(DatabindingsProvider provider) {
		super(provider, provider.getXmlObjectRoot());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	//
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected boolean accept(ObjectInfo object) throws Exception {
		return object instanceof XmlObjectInfo;
	}

	@Override
	protected void deleteBinding(IBindingInfo binding, List<IBindingInfo> bindings) throws Exception {
		m_provider.deleteBinding(binding);
	}

	@Override
	protected boolean equals(ObjectInfo object, String javaInfoReference, IObserveInfo iobserve)
			throws Exception {
		if (iobserve instanceof WidgetBindableInfo widget) {
			return object == widget.getXMLObjectInfo();
		}
		return false;
	}

	@Override
	protected String getReference(ObjectInfo object) throws Exception {
		return "";
	}
}