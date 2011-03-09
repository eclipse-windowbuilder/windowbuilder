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
package org.eclipse.wb.internal.swt.model.property.editor.image.plugin;

/**
 * This class configure show plugin filters.
 * 
 * @author lobas_av
 * @coverage swt.property.editor.plugin
 */
public final class FilterConfigurer {
  private boolean m_dirty = true;
  private boolean m_showWorkspacePlugins;
  private boolean m_showRequiredPlugins;
  private boolean m_showUIPlugins;
  private boolean m_showAllPlugins;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FilterConfigurer(boolean showWorkspacePlugins,
      boolean showRequiredPlugins,
      boolean showUIPlugins,
      boolean showAllPlugins) {
    m_showWorkspacePlugins = showWorkspacePlugins;
    m_showRequiredPlugins = showRequiredPlugins;
    m_showUIPlugins = showUIPlugins;
    m_showAllPlugins = showAllPlugins;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Filters
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean showWorkspacePlugins() {
    return m_showWorkspacePlugins;
  }

  public void showWorkspacePlugins(boolean value) {
    m_dirty = m_showWorkspacePlugins != value;
    m_showWorkspacePlugins = value;
  }

  public boolean showRequiredPlugins() {
    return m_showRequiredPlugins;
  }

  public void showRequiredPlugins(boolean value) {
    m_dirty = m_showRequiredPlugins != value;
    m_showRequiredPlugins = value;
  }

  public boolean showUIPlugins() {
    return m_showUIPlugins;
  }

  public void showUIPlugins(boolean value) {
    m_dirty = m_showUIPlugins != value;
    m_showUIPlugins = value;
  }

  public boolean showAllPlugins() {
    return m_showAllPlugins;
  }

  public void showAllPlugins(boolean value) {
    m_dirty = m_showAllPlugins != value;
    m_showAllPlugins = value;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // State
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean isDirty() {
    return m_dirty;
  }

  public void resetState() {
    m_dirty = false;
  }
}