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
package org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders;

import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckable;
import org.eclipse.jface.viewers.StructuredViewer;

/**
 * Implementation {@link ICheckboxViewerWrapper} for {@link CheckboxTreeViewer}.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public class CheckboxTreeViewerWrapper implements ICheckboxViewerWrapper {
  private final CheckboxTreeViewer m_viewer;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CheckboxTreeViewerWrapper(CheckboxTreeViewer viewer) {
    m_viewer = viewer;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ICheckboxViewerWrapper
  //
  ////////////////////////////////////////////////////////////////////////////
  public StructuredViewer getViewer() {
    return m_viewer;
  }

  public ICheckable getCheckable() {
    return m_viewer;
  }

  public Object[] getCheckedElements() {
    return m_viewer.getCheckedElements();
  }

  public void setCheckedElements(Object[] elements) {
    m_viewer.setCheckedElements(elements);
  }

  public void setAllChecked(boolean state) {
    m_viewer.setAllChecked(state);
  }
}