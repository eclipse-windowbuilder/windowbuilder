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
package org.eclipse.wb.internal.core.databinding.ui.property;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.JavaInfoAddProperties;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.model.property.Property;

import java.util.List;

/**
 * @author lobas_av
 * @coverage bindings.ui.properties
 */
public abstract class AbstractJavaInfoPropertiesManager {
	private static final String BINDINGS_KEY =
			"Bindings-Property-853b7e9d-af65-4702-b9e4-de524cd066ca";
	protected final IDatabindingsProvider m_provider;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	protected AbstractJavaInfoPropertiesManager(IDatabindingsProvider provider) {
		m_provider = provider;
	}

	public AbstractJavaInfoPropertiesManager(IDatabindingsProvider provider, JavaInfo javaInfoRoot) {
		m_provider = provider;
		javaInfoRoot.addBroadcastListener(new JavaInfoAddProperties() {
			@Override
			public void invoke(JavaInfo javaInfo, List<Property> properties) throws Exception {
				addBindingsProperty(javaInfo, properties);
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Handle
	//
	////////////////////////////////////////////////////////////////////////////
	protected final void addBindingsProperty(ObjectInfo objectInfo, List<Property> properties)
			throws Exception {
		if (isCreateProperty(objectInfo)) {
			AbstractBindingsProperty bindingsProperty =
					(AbstractBindingsProperty) objectInfo.getArbitraryValue(BINDINGS_KEY);
			if (bindingsProperty == null) {
				bindingsProperty = createProperty(objectInfo);
				objectInfo.putArbitraryValue(BINDINGS_KEY, bindingsProperty);
			}
			properties.add(bindingsProperty);
		}
	}

	/**
	 * @return <code>true</code> if for given {@link ObjectInfo} need create properties.
	 */
	protected abstract boolean isCreateProperty(ObjectInfo objectInfo) throws Exception;

	/**
	 * Create {@link AbstractBindingsProperty} for given {@link ObjectInfo}.
	 */
	protected abstract AbstractBindingsProperty createProperty(ObjectInfo objectInfo)
			throws Exception;
}