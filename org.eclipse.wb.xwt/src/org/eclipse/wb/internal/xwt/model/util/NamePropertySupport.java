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
package org.eclipse.wb.internal.xwt.model.util;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.category.PropertyCategory;
import org.eclipse.wb.internal.core.model.property.editor.string.StringPropertyEditor;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.broadcast.XmlObjectAddProperties;
import org.eclipse.wb.internal.core.xml.model.property.XmlProperty;
import org.eclipse.wb.internal.xwt.model.jface.ViewerInfo;
import org.eclipse.wb.internal.xwt.model.widgets.WidgetInfo;

import java.util.List;

/**
 * Support for special "Name" property for XWT.
 *
 * @author scheglov_ke
 * @coverage XWT.model
 */
public final class NamePropertySupport {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public NamePropertySupport(XmlObjectInfo rootObject) {
		rootObject.addBroadcastListener(new XmlObjectAddProperties() {
			@Override
			public void invoke(XmlObjectInfo object, List<Property> properties) throws Exception {
				if (object instanceof WidgetInfo || object instanceof ViewerInfo) {
					Property property = getNameProperty(object);
					properties.add(property);
				}
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	////////////////////////////////////////////////////////////////////////////
	private Property getNameProperty(XmlObjectInfo object) {
		Property property = (Property) object.getArbitraryValue(this);
		if (property == null) {
			property = new XmlProperty(object, "Name", StringPropertyEditor.INSTANCE) {
				@Override
				public boolean isModified() throws Exception {
					return getValue() != Property.UNKNOWN_VALUE;
				}

				@Override
				public Object getValue() throws Exception {
					String name = NameSupport.getName(m_object);
					return name != null ? name : Property.UNKNOWN_VALUE;
				}

				@Override
				protected void setValueEx(Object value) throws Exception {
					String name = value instanceof String ? (String) value : null;
					NameSupport.setName(m_object, name);
				}
			};
			property.setCategory(PropertyCategory.system(4));
			object.putArbitraryValue(this, property);
		}
		return property;
	}
}
