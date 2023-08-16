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
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.broadcast.GenericPropertySetValue;
import org.eclipse.wb.internal.core.xml.model.description.GenericPropertyDescription;
import org.eclipse.wb.internal.core.xml.model.property.GenericPropertyImpl;
import org.eclipse.wb.internal.core.xml.model.utils.XmlObjectUtils;

/**
 * Helper for setting {@link String} array value in XWT.
 *
 * @author scheglov_ke
 * @coverage XWT.model
 */
public final class XwtStringArraySupport {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public XwtStringArraySupport(XmlObjectInfo rootObject) {
		rootObject.addBroadcastListener(new GenericPropertySetValue() {
			@Override
			public void invoke(GenericPropertyImpl property, Object[] value, boolean[] shouldSetValue)
					throws Exception {
				GenericPropertyDescription description = property.getDescription();
				if (description.getType() == String[].class) {
					XmlObjectInfo object = property.getObject();
					DocumentElement element = object.getCreationSupport().getElement();
					// update
					if (value[0] instanceof String[]) {
						setStringArray(object, element, description.getName(), (String[]) value[0]);
					} else if (value[0] == Property.UNKNOWN_VALUE) {
						removeStringArray(object, element, description.getName());
					}
					// we handled it
					shouldSetValue[0] = false;
				}
			}
		});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Implementation
	//
	////////////////////////////////////////////////////////////////////////////
	private void setStringArray(XmlObjectInfo object,
			DocumentElement element,
			String name,
			String[] values) throws Exception {
		DocumentElement propertyElement = getPropertyElement(element, name);
		// remove old items
		propertyElement.removeChildren();
		// add new items
		String itemTag = XmlObjectUtils.getTagForClass(object, String.class);
		for (String value : values) {
			DocumentElement itemElement = new DocumentElement();
			itemElement.setTag(itemTag);
			propertyElement.addChild(itemElement);
			itemElement.setText(value, false);
		}
	}

	private void removeStringArray(XmlObjectInfo object, DocumentElement element, String name)
			throws Exception {
		DocumentElement propertyElement = getPropertyElement(element, name);
		if (propertyElement != null) {
			propertyElement.remove();
		}
	}

	/**
	 * @return existing or new child {@link DocumentElement} for property with given name.
	 */
	private DocumentElement getPropertyElement(DocumentElement element, String name) {
		String propertyTag = element.getTag() + "." + name;
		// try to find existing element
		{
			DocumentElement propertyElement = element.getChild(propertyTag, true);
			if (propertyElement != null) {
				return propertyElement;
			}
		}
		// add new element
		DocumentElement propertyElement = new DocumentElement();
		propertyElement.setTag(propertyTag);
		element.addChild(propertyElement);
		return propertyElement;
	}
}
