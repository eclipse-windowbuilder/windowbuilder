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

import com.google.common.collect.Sets;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.reference.IReferenceProvider;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.utils.xml.DocumentAttribute;
import org.eclipse.wb.internal.core.utils.xml.DocumentElement;
import org.eclipse.wb.internal.core.utils.xml.DocumentModelVisitor;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.rcp.databinding.xwt.model.beans.BeansObserveTypeContainer;

import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Set;
import java.util.UUID;

/**
 *
 * @author lobas_av
 *
 */
public class XmlObjectReferenceProvider implements IReferenceProvider {
	private final XmlObjectInfo m_objectInfo;
	private final String m_defaultReference;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public XmlObjectReferenceProvider(XmlObjectInfo objectInfo) {
		m_objectInfo = objectInfo;
		m_defaultReference = UUID.randomUUID().toString();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// IReferenceProvider
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getReference() throws Exception {
		return StringUtils.defaultString(getName(m_objectInfo), m_defaultReference);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	public static String getName(ObjectInfo objectInfo) throws Exception {
		Property property = objectInfo.getPropertyByTitle("Name");
		if (property == null) {
			return null;
		}
		Object value = property.getValue();
		return value == Property.UNKNOWN_VALUE ? null : (String) value;
	}

	public static void generateName(XmlObjectInfo objectInfo) throws Exception {
		if (getName(objectInfo) == null) {
			final Set<String> variables = Sets.newHashSet();
			//
			DocumentElement rootElement = objectInfo.getElement().getRoot();
			final String[] xName = new String[1];
			//
			for (DocumentAttribute attribute : rootElement.getDocumentAttributes()) {
				String name = attribute.getName();
				String value = attribute.getValue();
				if (name.startsWith(BeansObserveTypeContainer.NAMESPACE_KEY)) {
					if (value.equals("http://www.eclipse.org/xwt")) {
						xName[0] = name.substring(BeansObserveTypeContainer.NAMESPACE_KEY.length()) + ":Name";
						break;
					}
				}
			}
			//
			rootElement.accept(new DocumentModelVisitor() {
				@Override
				public void visit(DocumentAttribute attribute) {
					if (attribute.getName().equalsIgnoreCase(xName[0])) {
						variables.add(attribute.getValue());
					}
				}
			});
			//
			String baseVariable =
					StringUtils.uncapitalize(ClassUtils.getShortClassName(objectInfo.getDescription().getComponentClass()));
			String variable = baseVariable;
			int variableIndex = 1;
			// ensure unique
			while (variables.contains(variable)) {
				variable = baseVariable + "_" + Integer.toString(variableIndex++);
			}
			//
			Property property = objectInfo.getPropertyByTitle("Name");
			property.setValue(variable);
		}
	}
}