/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package org.eclipse.wb.internal.core.utils.jdt.core;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;

import org.apache.commons.lang3.ArrayUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * Implementation of {@link IJavaSearchScope} that returns all sub-types of {@link IType}.
 *
 * @author scheglov_ke
 * @coverage core.util.jdt
 */
public final class SubtypesScope implements IJavaSearchScope {
	private final IType[] m_subtypes;
	private final IJavaSearchScope m_hierarchyScope;
	private final Set<String> m_enclosingResourcePaths = new HashSet<>();

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor
	//
	////////////////////////////////////////////////////////////////////////////
	public SubtypesScope(IType type) throws JavaModelException {
		m_hierarchyScope = SearchEngine.createHierarchyScope(type);
		m_subtypes = type.newTypeHierarchy(null).getAllSubtypes(type);
		for (IType subType : m_subtypes) {
			IResource resource = subType.getUnderlyingResource();
			if (resource != null) {
				m_enclosingResourcePaths.add(resource.getFullPath().toString());
			} else {
				m_enclosingResourcePaths.add(subType.getFullyQualifiedName().replace('.', '/') + ".class");
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Enclosing
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean encloses(String resourcePath) {
		// prepare name of class, without leading "jar" path
		String classPath = resourcePath;
		{
			int index = classPath.indexOf("|");
			if (index != -1) {
				classPath = classPath.substring(index + 1);
			}
		}
		// check in prepared resource
		return m_enclosingResourcePaths.contains(classPath);
	}

	@Override
	public boolean encloses(IJavaElement element) {
		IType type = (IType) element.getAncestor(IJavaElement.TYPE);
		if (type != null) {
			return ArrayUtils.contains(m_subtypes, type);
		}
		return false;
	}

	@Override
	public IPath[] enclosingProjectsAndJars() {
		return m_hierarchyScope.enclosingProjectsAndJars();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Unused methods
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@Deprecated
	public boolean includesBinaries() {
		return m_hierarchyScope.includesBinaries();
	}

	@Override
	@Deprecated
	public boolean includesClasspaths() {
		return m_hierarchyScope.includesClasspaths();
	}

	@Override
	@Deprecated
	public void setIncludesBinaries(boolean includesBinaries) {
		m_hierarchyScope.setIncludesBinaries(includesBinaries);
	}

	@Override
	@Deprecated
	public void setIncludesClasspaths(boolean includesClasspaths) {
		m_hierarchyScope.setIncludesClasspaths(includesClasspaths);
	}
}
