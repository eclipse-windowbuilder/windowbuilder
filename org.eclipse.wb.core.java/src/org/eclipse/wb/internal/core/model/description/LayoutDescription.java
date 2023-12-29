/*******************************************************************************
 * Copyright (c) 2011, 2023 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.core.model.description;

import org.eclipse.wb.core.databinding.xsd.component.Component;
import org.eclipse.wb.core.databinding.xsd.component.ContextFactory;
import org.eclipse.wb.core.databinding.xsd.component.Creation;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.palette.model.entry.LibraryInfo;
import org.eclipse.wb.internal.core.model.description.helpers.DescriptionHelper;
import org.eclipse.wb.internal.core.model.description.resource.ResourceInfo;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jdt.core.IJavaProject;

import org.apache.commons.lang.StringUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;

/**
 * Description for layout manager existing in toolkit.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public final class LayoutDescription {
	private final ToolkitDescription m_toolkit;
	private final String m_id;
	private final String m_name;
	private final String m_layoutClassName;
	private final String m_creationId;
	private String m_source;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public LayoutDescription(ToolkitDescription toolkit, IConfigurationElement element) {
		m_toolkit = toolkit;
		m_id = ExternalFactoriesHelper.getRequiredAttribute(element, "id");
		m_name = ExternalFactoriesHelper.getRequiredAttribute(element, "name");
		m_layoutClassName = ExternalFactoriesHelper.getRequiredAttribute(element, "class");
		// creation id
		{
			String creationId = element.getAttribute("creationId");
			m_creationId = StringUtils.isEmpty(creationId) ? (String) null : creationId;
		}
		// source
		{
			String source = element.getAttribute("source");
			if (StringUtils.isEmpty(source)) {
				try {
					loadDescription();
				} catch (Exception e) {
					DesignerPlugin.log(e);
				}
				m_source = m_source != null ? m_source : "new " + m_layoutClassName + "()";
			} else {
				m_source = source;
			}
		}
		addLibraries(element);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	public ToolkitDescription getToolkit() {
		return m_toolkit;
	}

	/**
	 * @return the unique id of this {@link LayoutDescription}.
	 */
	public String getId() {
		return m_id;
	}

	/**
	 * @return the name of this layout manager, to display for user.
	 */
	public String getName() {
		return m_name;
	}

	/**
	 * @return the name of layout manager class.
	 */
	public String getLayoutClassName() {
		return m_layoutClassName;
	}

	/**
	 * @return the source of expression for <code>setLayout()</code> invocation.
	 */
	public String getSourceFull() {
		return m_source;
	}

	public String getSourceSmart() {
		String shortClass = CodeUtils.getShortClass(m_layoutClassName);
		return StringUtils.replace(m_source, m_layoutClassName, shortClass);
	}

	/**
	 * @return the Creation Id of layout manager class.
	 */
	public String getCreationId() {
		return m_creationId;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Libraries
	//
	////////////////////////////////////////////////////////////////////////////
	private final List<LibraryInfo> m_libraries = new ArrayList<>();

	/**
	 * Adds new {@link LibraryInfo} to ensure.
	 */
	private void addLibraries(IConfigurationElement componentElement) {
		for (IConfigurationElement libraryElement : componentElement.getChildren("library")) {
			m_libraries.add(new LibraryInfo(libraryElement));
		}
	}

	/**
	 * Ensures all {@link LibraryInfo}'s.
	 */
	public void ensureLibraries(IJavaProject javaProject) throws Exception {
		for (LibraryInfo library : m_libraries) {
			library.ensure(javaProject);
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utilities
	//
	////////////////////////////////////////////////////////////////////////////
	private void loadDescription() throws Exception {
		// prepare resource
		String resourcePath = m_layoutClassName.replace('.', '/') + ".wbp-component.xml";
		ResourceInfo resourceInfo = DescriptionHelper.getResourceInfo(resourcePath, m_toolkit.getId());
		if (resourceInfo == null) {
			DesignerPlugin.log("Not found resource " + m_layoutClassName.replace('.', '/') + ".wbp-component.xml"
					+ " in bundle " + m_toolkit.getId());
			return;
		}
		// do parse
		try (InputStream is = resourceInfo.getURL().openStream();) {
			JAXBContext context = ContextFactory.createContext();
			Unmarshaller unmarshaller = context.createUnmarshaller();
			Component component = (Component) unmarshaller.unmarshal(is);
			List<Creation> creations = component.getCreation();
			for (Creation creation : creations) {
				if (m_creationId != null && m_creationId.equals(creation.getId())) {
					m_source = creation.getSource();
				}
			}
		}
	}
}
