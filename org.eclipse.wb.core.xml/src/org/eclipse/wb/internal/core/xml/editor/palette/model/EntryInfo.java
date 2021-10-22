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
package org.eclipse.wb.internal.core.xml.editor.palette.model;

import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.internal.core.xml.model.EditorContext;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.swt.graphics.Image;

/**
 * Model of entry on palette.
 *
 * @author scheglov_ke
 * @coverage XML.editor.palette
 */
public abstract class EntryInfo extends AbstractElementInfo {
  protected IEditPartViewer m_editPartViewer;
  protected XmlObjectInfo m_rootJavaInfo;
  protected EditorContext m_context;
  protected IJavaProject m_javaProject;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Initializes this {@link EntryInfo}, prepares it for future calls of methods.
   *
   * @return <code>true</code> if this {@link EntryInfo} is successfully activated.
   */
  public boolean initialize(IEditPartViewer editPartViewer, XmlObjectInfo rootObject) {
    m_editPartViewer = editPartViewer;
    m_rootJavaInfo = rootObject;
    m_context = rootObject.getContext();
    m_javaProject = m_context.getJavaProject();
    return true;
  }

  /**
   * Sometimes we want to show entry, but don't allow to select it.
   *
   * @return <code>true</code> if this {@link EntryInfo} is enabled.
   */
  public boolean isEnabled() {
    return true;
  }

  /**
   * @return the icon for visual.
   */
  public abstract Image getIcon();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Activation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Performs operation when user selects this entry on palette.
   *
   * @param reload
   *          is <code>true</code> if entry should be automatically reloaded after successful using.
   *
   * @return <code>true</code> if entry was successfully activated.
   */
  public abstract boolean activate(boolean reload);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link IPaletteSite} for palette of this entry.
   */
  protected final IPaletteSite getSite() {
    return IPaletteSite.Helper.getSite(m_rootJavaInfo);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Category
  //
  ////////////////////////////////////////////////////////////////////////////
  private CategoryInfo m_category;

  /**
   * @return the parent {@link CategoryInfo}.
   */
  public final CategoryInfo getCategory() {
    return m_category;
  }

  /**
   * Sets the parent {@link CategoryInfo}.
   */
  public final void setCategory(CategoryInfo category) {
    m_category = category;
  }
}
