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
package org.eclipse.wb.internal.core.xml.model.association;

import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.core.xml.model.utils.ElementTarget;

/**
 * {@link Association} which put child element into "parentTag.propertyName" sub-element.
 *
 * @author scheglov_ke
 * @coverage XML.model.association
 */
public final class PropertyAssociation extends DirectAssociation {
	private final String m_property;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public PropertyAssociation(String property) {
		m_property = property;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Object
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String toString() {
		return "property " + m_property;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Operations
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void add(XmlObjectInfo object, ElementTarget target) throws Exception {
		target = preparePropertyTarget(target);
		super.add(object, target);
	}

	@Override
	public void move(XmlObjectInfo object,
			ElementTarget target,
			XmlObjectInfo oldParent,
			XmlObjectInfo newParent) throws Exception {
		target = preparePropertyTarget(target);
		super.move(object, target, oldParent, newParent);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	private ElementTarget preparePropertyTarget(ElementTarget target) {
		// prepare "property" element
		DocumentElement propertyElement;
		{
			DocumentElement targetElement = target.getElement();
			int targetIndex = target.getIndex();
			// create "property" element
			propertyElement = new DocumentElement();
			propertyElement.setTag(targetElement.getTag() + "." + m_property);
			// add it
			targetElement.addChild(propertyElement, targetIndex);
		}
		// prepare new target
		return new ElementTarget(propertyElement, 0);
	}
}
