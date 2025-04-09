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
package org.eclipse.wb.internal.rcp.databinding.model.widgets.input.designer;

import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassConfiguration;
import org.eclipse.wb.internal.core.databinding.utils.CoreUtils;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.TreeStructureAdvisorInfo;

import java.util.List;

/**
 * Model for {@link org.eclipse.wb.rcp.databinding.TreeBeanAdvisor}.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public class TreeBeanAdvisorInfo extends TreeStructureAdvisorInfo {
	private static final String ADVISOR_CLASS = "org.eclipse.wb.rcp.databinding.TreeBeanAdvisor";
	private Class<?> m_elementType;
	private String m_parentProperty;
	private String m_childrenProperty;
	private String m_hasChildrenProperty;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public TreeBeanAdvisorInfo() {
		super(ADVISOR_CLASS);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public void setElementType(Class<?> elementType) {
		m_elementType = elementType;
	}

	public Class<?> getElementType() {
		return m_elementType;
	}

	public String getParentProperty() {
		return m_parentProperty;
	}

	public void setParentProperty(String parentProperty) throws Exception {
		m_parentProperty = parentProperty;
	}

	public String getChildrenProperty() {
		return m_childrenProperty;
	}

	public void setChildrenProperty(String childrenProperty) throws Exception {
		m_childrenProperty = childrenProperty;
	}

	public String getHasChildrenProperty() {
		return m_hasChildrenProperty;
	}

	public void setHasChildrenProperty(String hasChildrenProperty) throws Exception {
		m_hasChildrenProperty = hasChildrenProperty;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Editing
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void configure(ChooseClassConfiguration configuration) {
		configuration.setValueScope(ADVISOR_CLASS);
		configuration.setClearValue(ADVISOR_CLASS);
		configuration.setBaseClassName(ADVISOR_CLASS);
		configuration.setConstructorParameters(new Class[]{
				Class.class,
				String.class,
				String.class,
				String.class});
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Presentation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getPresentationText() throws Exception {
		return CoreUtils.joinStrings(", ", m_parentProperty, m_childrenProperty, m_hasChildrenProperty);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Code generation
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void addSourceCode(List<String> lines) throws Exception {
		lines.add(ADVISOR_CLASS
				+ " "
				+ getVariableIdentifier()
				+ " = new "
				+ m_className
				+ "("
				+ CoreUtils.getClassName(m_elementType)
				+ ".class, "
				+ CoreUtils.getDefaultString(m_parentProperty, "\"", "null")
				+ ", "
				+ CoreUtils.getDefaultString(m_childrenProperty, "\"", "null")
				+ ", "
				+ CoreUtils.getDefaultString(m_hasChildrenProperty, "\"", "null")
				+ ");");
	}
}