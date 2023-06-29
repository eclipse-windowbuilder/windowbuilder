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
package org.eclipse.wb.internal.swing.databinding.model.properties;

import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo.ChildrenContext;
import org.eclipse.wb.internal.swing.databinding.model.ObserveInfo;
import org.eclipse.wb.internal.swing.databinding.model.beans.ObjectPropertyObserveInfo;
import org.eclipse.wb.internal.swing.databinding.model.generic.GenericUtils;
import org.eclipse.wb.internal.swing.databinding.model.generic.IGenericType;
import org.eclipse.wb.internal.swing.databinding.parser.DatabindingParser;

import java.util.List;

/**
 * Model for {@link org.jdesktop.beansbinding.ObjectProperty}.
 *
 * @author lobas_av
 * @coverage bindings.swing.model.properties
 */
public final class ObjectPropertyInfo extends PropertyInfo {
	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public ObjectPropertyInfo(IGenericType sourceObjectType) {
		super(sourceObjectType, sourceObjectType);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public ObserveInfo getObserveProperty(ObserveInfo observeObject) throws Exception {
		for (IObserveInfo observe : observeObject.getChildren(ChildrenContext.ChildrenForPropertiesTable)) {
			if (observe instanceof ObjectPropertyObserveInfo) {
				return (ObserveInfo) observe;
			}
		}
		return null;
	}

	@Override
	public boolean canShared(PropertyInfo property) {
		if (property instanceof ObjectPropertyInfo) {
			return !DatabindingParser.useGenerics
					|| m_sourceObjectType.getFullTypeName().equals(
							property.getSourceObjectType().getFullTypeName());
		}
		return false;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Code generation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void addSourceCode(List<String> lines, CodeGenerationSupport generationSupport)
			throws Exception {
		if (getVariableIdentifier() == null) {
			setVariableIdentifier(generationSupport.generateLocalName(
					m_sourceObjectType.getSimpleTypeName(),
					"ObjectProperty"));
		}
		if (generationSupport.useGenerics()) {
			lines.add("org.jdesktop.beansbinding.ObjectProperty"
					+ GenericUtils.getTypesSource(m_sourceObjectType)
					+ " "
					+ getVariableIdentifier()
					+ " = org.jdesktop.beansbinding.ObjectProperty.create();");
		} else {
			lines.add("org.jdesktop.beansbinding.Property "
					+ getVariableIdentifier()
					+ " = org.jdesktop.beansbinding.ObjectProperty.create();");
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getPresentationText(IObserveInfo observeObject,
			IObserveInfo observeProperty,
			boolean full) throws Exception {
		return observeObject.getPresentation().getTextForBinding();
	}
}