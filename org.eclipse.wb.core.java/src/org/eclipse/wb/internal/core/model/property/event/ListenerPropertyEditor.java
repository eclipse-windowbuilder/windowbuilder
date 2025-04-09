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
package org.eclipse.wb.internal.core.model.property.event;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;

import java.util.List;

/**
 * Implementation of {@link PropertyEditor} for {@link ListenerProperty}.
 *
 * @author scheglov_ke
 * @coverage core.model.property.events
 */
final class ListenerPropertyEditor extends AbstractComplexEventPropertyEditor {
	private final ListenerInfo m_listener;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ListenerPropertyEditor(ListenerInfo listener) {
		m_listener = listener;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IComplexPropertyEditor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public ListenerMethodProperty[] getProperties(Property property) throws Exception {
		JavaInfo javaInfo = ((ListenerProperty) property).getJavaInfo();
		List<ListenerMethodInfo> methods = m_listener.getMethods();
		ListenerMethodProperty[] properties = new ListenerMethodProperty[methods.size()];
		for (int i = 0; i < methods.size(); i++) {
			ListenerMethodInfo method = methods.get(i);
			properties[i] = new ListenerMethodProperty(javaInfo, m_listener, method, properties);
		}
		return properties;
	}
}
