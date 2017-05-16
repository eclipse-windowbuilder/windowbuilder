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
package org.eclipse.wb.internal.core.model.description.helpers;

import org.eclipse.wb.internal.core.model.description.resource.IDescriptionVersionsProvider;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaProject;

import java.net.URL;
import java.util.List;

/**
 * {@link ILoadingContext} for {@link EditorState}.
 *
 * @author scheglov_ke
 * @coverage core.model.description
 */
public class EditorStateLoadingContext implements ILoadingContext {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Factory
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link ILoadingContext} to use for given {@link EditorState}.
   */
  public static ILoadingContext get(EditorState state) {
    String key = ILoadingContext.class.getName();
    ILoadingContext context = (ILoadingContext) state.getEditor().getGlobalValue(key);
    if (context == null) {
      context = new EditorStateLoadingContext(state);
      state.getEditor().putGlobalValue(key, context);
    }
    return context;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final EditorState m_state;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  private EditorStateLoadingContext(EditorState state) {
    m_state = state;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ILoadingContext
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getToolkitId() {
    return m_state.getToolkitId();
  }

  public URL getResource(String name) throws Exception {
    // try editor class loader
    {
      URL resource = m_state.getEditorLoader().getResource(name);
      if (resource != null) {
        return resource;
      }
    }
    // try "wbp-meta" of IJavaProject
    {
      URL resource = getResource(m_state.getEditor().getJavaProject(), name);
      if (resource != null) {
        return resource;
      }
    }
    // not found
    return null;
  }

  public List<IDescriptionVersionsProvider> getDescriptionVersionsProviders() {
    return m_state.getDescriptionVersionsProviders();
  }

  public Object getGlobalValue(String key) {
    return m_state.getEditor().getGlobalValue(key);
  }

  public void putGlobalValue(String key, Object value) {
    m_state.getEditor().putGlobalValue(key, value);
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
      return file.getLocation().toFile().toURL();
    }
    return null;
  }
}
