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
package org.eclipse.wb.internal.core.xml.model;

import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.model.description.helpers.ILoadingContext;
import org.eclipse.wb.internal.core.model.description.resource.IDescriptionVersionsProvider;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaProject;

import java.net.URL;
import java.util.List;
import java.util.Map;

/**
 * {@link ILoadingContext} for XML.
 *
 * @author scheglov_ke
 * @coverage XML.model
 */
public final class XmlLoadingContext implements ILoadingContext {
	private final EditorContext m_editorContext;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public XmlLoadingContext(EditorContext editorContext) {
		m_editorContext = editorContext;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public String getToolkitId() {
		return m_editorContext.getToolkit().getId();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Resources
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public URL getResource(String name) throws Exception {
		// try editor class loader
		{
			URL resource = m_editorContext.getClassLoader().getResource(name);
			if (resource != null) {
				return resource;
			}
		}
		// try "wbp-meta" of IJavaProject
		{
			URL resource = getResource(m_editorContext.getJavaProject(), name);
			if (resource != null) {
				return resource;
			}
		}
		// not found
		return null;
	}

	@Override
	public List<IDescriptionVersionsProvider> getDescriptionVersionsProviders() {
		return m_editorContext.getDescriptionVersionsProviders();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Global values
	//
	////////////////////////////////////////////////////////////////////////////
	private final Map<String, Object> m_globalMap = Maps.newHashMap();

	@Override
	public Object getGlobalValue(String key) {
		return m_globalMap.get(key);
	}

	@Override
	public void putGlobalValue(String key, Object value) {
		m_globalMap.put(key, value);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Utils
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link URL} with <code>*.wbp-component.xml</code> file for given name, in
	 *         {@link IJavaProject} itself or any required project.
	 */
	private static URL getResource(IJavaProject javaProject, String name) throws Exception {
		List<IFile> files = ProjectUtils.findFiles(javaProject, "wbp-meta/" + name);
		if (!files.isEmpty()) {
			IFile file = files.get(0);
			return file.getLocation().toFile().toURI().toURL();
		}
		return null;
	}
}
