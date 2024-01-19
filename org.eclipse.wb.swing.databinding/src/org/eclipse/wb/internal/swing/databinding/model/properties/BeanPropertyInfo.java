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

import org.eclipse.wb.internal.core.databinding.model.AstObjectInfoVisitor;
import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo.ChildrenContext;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.swing.databinding.model.ObserveInfo;
import org.eclipse.wb.internal.swing.databinding.model.generic.GenericUtils;
import org.eclipse.wb.internal.swing.databinding.model.generic.IGenericType;
import org.eclipse.wb.internal.swing.databinding.parser.DatabindingParser;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Model for {@link org.jdesktop.beansbinding.BeanProperty}.
 *
 * @author lobas_av
 * @coverage bindings.swing.model.properties
 */
public final class BeanPropertyInfo extends PropertyInfo {
	private final PropertyInfo m_baseProperty;
	private String m_path;
	private String[] m_properties;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public BeanPropertyInfo(IGenericType sourceObjectType,
			IGenericType valueType,
			PropertyInfo baseProperty,
			String path) {
		super(sourceObjectType, valueType);
		m_baseProperty = baseProperty;
		setPath(path);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public String getPath() {
		return m_path;
	}

	public void setPath(String path) {
		Assert.isNotNull(path);
		m_path = path;
		m_properties = StringUtils.split(m_path, '.');
	}

	@Override
	public ObserveInfo getObserveProperty(ObserveInfo observeObject) throws Exception {
		// configure observe object account with base property
		if (m_baseProperty != null) {
			Assert.instanceOf(BeanPropertyInfo.class, m_baseProperty);
			observeObject = m_baseProperty.getObserveProperty(observeObject);
			if (observeObject == null) {
				return null;
			}
		}
		// find property
		for (String property : m_properties) {
			boolean found = false;
			for (ObserveInfo observeProperty : CoreUtils.<ObserveInfo>cast(observeObject.getChildren(ChildrenContext.ChildrenForPropertiesTable))) {
				if (observeProperty.isRepresentedBy(property)) {
					observeObject = observeProperty;
					found = true;
					break;
				}
			}
			if (!found) {
				return null;
			}
		}
		return observeObject;
	}

	@Override
	public boolean canShared(PropertyInfo property) {
		if (property instanceof BeanPropertyInfo) {
			// check generic types
			if (DatabindingParser.useGenerics) {
				if (!m_sourceObjectType.getFullTypeName().equals(
						property.getSourceObjectType().getFullTypeName())) {
					return false;
				}
				if (!m_valueType.getFullTypeName().equals(property.getValueType().getFullTypeName())) {
					return false;
				}
			}
			// check path
			BeanPropertyInfo beanProperty = (BeanPropertyInfo) property;
			if (!m_path.equals(beanProperty.getPath())) {
				return false;
			}
			// check base property
			if (m_baseProperty != null
					&& beanProperty.m_baseProperty == null
					|| m_baseProperty == null
					&& beanProperty.m_baseProperty != null) {
				return false;
			}
			if (m_baseProperty != null && beanProperty.m_baseProperty != null) {
				return m_baseProperty.canShared(beanProperty.m_baseProperty);
			}
			// shared
			return true;
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
		// configure variable
		if (getVariableIdentifier() == null) {
			setVariableIdentifier(generationSupport.generateLocalName(
					m_sourceObjectType.getSimpleTypeName(),
					"BeanProperty"));
		}
		// handle base property
		String base = "";
		if (m_baseProperty != null) {
			generationSupport.addSourceCode(m_baseProperty, lines);
			base = m_baseProperty.getVariableIdentifier() + ", ";
		}
		// add source code
		if (generationSupport.useGenerics()) {
			lines.add("org.jdesktop.beansbinding.BeanProperty"
					+ GenericUtils.getTypesSource(m_sourceObjectType, m_valueType)
					+ " "
					+ getVariableIdentifier()
					+ " = org.jdesktop.beansbinding.BeanProperty.create("
					+ base
					+ "\""
					+ m_path
					+ "\");");
		} else {
			lines.add("org.jdesktop.beansbinding.Property "
					+ getVariableIdentifier()
					+ " = org.jdesktop.beansbinding.BeanProperty.create("
					+ base
					+ "\""
					+ m_path
					+ "\");");
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Visiting
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public void accept(AstObjectInfoVisitor visitor) throws Exception {
		super.accept(visitor);
		if (m_baseProperty != null) {
			m_baseProperty.accept(visitor);
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
		if (full && observeObject == null && observeProperty == null) {
			String presentationText = "";
			if (m_baseProperty != null) {
				presentationText = m_baseProperty.getPresentationText(null, null, true) + ".";
			}
			return presentationText + m_path;
		}
		if (full && m_path.length() > 0) {
			String basePresentationText = "";
			if (m_baseProperty != null) {
				basePresentationText = m_baseProperty.getPresentationText(null, null, true) + ".";
			}
			return observeObject.getPresentation().getTextForBinding()
					+ "."
					+ basePresentationText
					+ m_path;
		}
		return super.getPresentationText(observeObject, observeProperty, full);
	}
}