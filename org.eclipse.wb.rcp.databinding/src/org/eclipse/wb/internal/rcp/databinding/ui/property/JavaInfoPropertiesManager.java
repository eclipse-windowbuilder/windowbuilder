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
package org.eclipse.wb.internal.rcp.databinding.ui.property;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.databinding.ui.property.AbstractBindingsProperty;
import org.eclipse.wb.internal.core.databinding.ui.property.AbstractJavaInfoPropertiesManager;
import org.eclipse.wb.internal.core.databinding.ui.property.Context;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.nonvisual.NonVisualBeanContainerInfo;
import org.eclipse.wb.internal.rcp.databinding.Activator;
import org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.JavaInfoReferenceProvider;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.PropertiesSupport;
import org.eclipse.wb.internal.swt.model.jface.viewer.ViewerInfo;

/**
 * @author lobas_av
 * @coverage bindings.rcp.ui.properties
 */
public class JavaInfoPropertiesManager extends AbstractJavaInfoPropertiesManager {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public JavaInfoPropertiesManager(IDatabindingsProvider provider, JavaInfo javaInfoRoot) {
		super(provider, javaInfoRoot);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// AbstractJavaInfoPropertiesManager
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected boolean isCreateProperty(ObjectInfo objectInfo) throws Exception {
		JavaInfo javaInfo = (JavaInfo) objectInfo;
		if (JavaInfoUtils.hasTrueParameter(javaInfo, "databinding.disable")) {
			return false;
		}
		DatabindingsProvider provider = (DatabindingsProvider) m_provider;
		boolean nonVisual =
				!provider.isController() && javaInfo.getParent() instanceof NonVisualBeanContainerInfo;
		return (javaInfo instanceof AbstractComponentInfo || javaInfo instanceof ViewerInfo || nonVisual)
				&& JavaInfoReferenceProvider.getReference(javaInfo) != null
				&& (PropertiesSupport.isObservableInfo(javaInfo) || nonVisual);
	}

	@Override
	protected AbstractBindingsProperty createProperty(ObjectInfo objectInfo) throws Exception {
		return new BindingsProperty(new Context(Activator.getDefault(), m_provider, objectInfo));
	}
}