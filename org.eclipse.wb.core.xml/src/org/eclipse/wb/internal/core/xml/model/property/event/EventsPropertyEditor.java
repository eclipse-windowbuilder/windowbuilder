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
package org.eclipse.wb.internal.core.xml.model.property.event;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.event.AbstractComplexEventPropertyEditor;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectEventListeners;

import java.util.List;

/**
 * {@link PropertyEditor} for {@link EventsProperty}.
 *
 * @author scheglov_ke
 * @coverage XWT.model.property
 */
final class EventsPropertyEditor extends AbstractComplexEventPropertyEditor {
	////////////////////////////////////////////////////////////////////////////
	//
	// Instance
	//
	////////////////////////////////////////////////////////////////////////////
	public static final EventsPropertyEditor INSTANCE = new EventsPropertyEditor();

	private EventsPropertyEditor() {
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IComplexPropertyEditor
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public AbstractListenerProperty[] getProperties(Property property) throws Exception {
		EventsProperty eventsProperty = (EventsProperty) property;
		XmlObjectInfo javaInfo = eventsProperty.getObject();
		// get from cache or create
		AbstractListenerProperty[] properties =
				(AbstractListenerProperty[]) javaInfo.getArbitraryValue(eventsProperty);
		if (properties == null) {
			properties = createProperties(javaInfo);
			javaInfo.putArbitraryValue(eventsProperty, properties);
		}
		return properties;
	}

	private AbstractListenerProperty[] createProperties(XmlObjectInfo object) throws Exception {
		List<AbstractListenerProperty> properties = Lists.newArrayList();
		object.getBroadcast(XmlObjectEventListeners.class).invoke(object, properties);
		return properties.toArray(new AbstractListenerProperty[properties.size()]);
	}
}
