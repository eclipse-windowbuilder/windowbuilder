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
package org.eclipse.wb.internal.core.utils.jdt.core;

import com.google.common.collect.Sets;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;

import org.apache.commons.lang.ArrayUtils;

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
  private final Set<String> m_enclosingResourcePaths = Sets.newHashSet();

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

  public boolean encloses(IJavaElement element) {
    IType type = (IType) element.getAncestor(IJavaElement.TYPE);
    if (type != null) {
      return ArrayUtils.contains(m_subtypes, type);
    }
    return false;
  }

  public IPath[] enclosingProjectsAndJars() {
    return m_hierarchyScope.enclosingProjectsAndJars();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Unused methods
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean includesBinaries() {
    return m_hierarchyScope.includesBinaries();
  }

  public boolean includesClasspaths() {
    return m_hierarchyScope.includesClasspaths();
  }

  public void setIncludesBinaries(boolean includesBinaries) {
    m_hierarchyScope.setIncludesBinaries(includesBinaries);
  }

  public void setIncludesClasspaths(boolean includesClasspaths) {
    m_hierarchyScope.setIncludesClasspaths(includesClasspaths);
  }
}
