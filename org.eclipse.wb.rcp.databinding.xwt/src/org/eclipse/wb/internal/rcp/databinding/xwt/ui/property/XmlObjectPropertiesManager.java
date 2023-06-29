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
package org.eclipse.wb.internal.rcp.databinding.xwt.ui.property;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.databinding.ui.property.AbstractBindingsProperty;
import org.eclipse.wb.internal.core.databinding.ui.property.Context;
import org.eclipse.wb.internal.core.databinding.xml.ui.AbstracXmlObjectPropertiesManager;
import org.eclipse.wb.internal.core.xml.model.AbstractComponentInfo;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;
import org.eclipse.wb.internal.rcp.databinding.xwt.Activator;
import org.eclipse.wb.internal.rcp.databinding.xwt.DatabindingsProvider;
import org.eclipse.wb.internal.xwt.model.jface.ViewerInfo;

/**
 *
 * @author lobas_av
 *
 */
public class XmlObjectPropertiesManager extends AbstracXmlObjectPropertiesManager {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public XmlObjectPropertiesManager(DatabindingsProvider provider) {
		super(provider, provider.getXmlObjectRoot());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// AbstractJavaInfoPropertiesManager
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected boolean isCreateProperty(ObjectInfo objectInfo) throws Exception {
		// temporary disabled bindings property for Layout_Info, LayoutData_Info & etc.
		// FIXME if (objectInfo instanceof XMLObject_Info) {
		if (objectInfo instanceof AbstractComponentInfo || objectInfo instanceof ViewerInfo) {
			XmlObjectInfo xmlObjectInfo = (XmlObjectInfo) objectInfo;
			return !XmlObjectUtils.hasTrueParameter(xmlObjectInfo, "databinding.disable");
		}
		return false;
	}

	@Override
	protected AbstractBindingsProperty createProperty(ObjectInfo objectInfo) throws Exception {
		return new BindingsProperty(new Context(Activator.getDefault(), m_provider, objectInfo));
	}
}