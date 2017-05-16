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
package org.eclipse.wb.internal.core.utils.gef;

import com.google.common.collect.Lists;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.apache.commons.lang.ArrayUtils;

import java.util.List;

/**
 * Implementation of {@link ITreeContentProvider} for GEF {@link IEditPartViewer}.
 *
 * @author scheglov_ke
 * @coverage gef.core
 */
public final class EditPartsContentProvider implements ITreeContentProvider {
  private final IEditPartViewer m_viewer;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public EditPartsContentProvider(IEditPartViewer viewer) {
    m_viewer = viewer;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IStructuredContentProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public Object[] getElements(Object inputElement) {
    Object input = m_viewer.getRootContainer().getContent().getModel();
    return new Object[]{input};
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ITreeContentProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean hasChildren(Object parentElement) {
    return getChildren(parentElement).length != 0;
  }

  public Object[] getChildren(Object parentElement) {
    EditPart parentEditPart = m_viewer.getEditPartByModel(parentElement);
    if (parentEditPart != null) {
      List<Object> children = Lists.newArrayList();
      for (EditPart editPart : parentEditPart.getChildren()) {
        children.add(editPart.getModel());
      }
      return children.toArray();
    }
    return ArrayUtils.EMPTY_OBJECT_ARRAY;
  }

  public Object getParent(Object element) {
    EditPart editPart = m_viewer.getEditPartByModel(element);
    if (editPart != null) {
      return editPart.getParent().getModel();
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IContentProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public void dispose() {
  }

  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
  }
}
