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
package org.eclipse.wb.internal.core.databinding.ui.providers;

import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo.ChildrenContext;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.apache.commons.lang.ArrayUtils;

import java.util.List;

/**
 * Implementation of {@link ITreeContentProvider} for {@link IObserveInfo}.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public class ObserveTreeContentProvider implements ITreeContentProvider {
  private final ChildrenContext m_context;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ObserveTreeContentProvider(ChildrenContext context) {
    m_context = context;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Input
  //
  ////////////////////////////////////////////////////////////////////////////
  public Object[] getElements(Object input) {
    // case array
    if (input instanceof Object[]) {
      return (Object[]) input;
    }
    // case collection
    if (input instanceof List<?>) {
      List<?> listInput = (List<?>) input;
      return listInput.toArray();
    }
    // case direct object
    if (input instanceof IObserveInfo) {
      return getChildren(input);
    }
    // no input
    return ArrayUtils.EMPTY_OBJECT_ARRAY;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parent/Children
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean hasChildren(Object element) {
    IObserveInfo observe = (IObserveInfo) element;
    return !observe.getChildren(m_context).isEmpty();
  }

  public Object[] getChildren(Object element) {
    IObserveInfo observe = (IObserveInfo) element;
    return observe.getChildren(m_context).toArray();
  }

  public Object getParent(Object element) {
    IObserveInfo observe = (IObserveInfo) element;
    return observe.getParent();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ITreeContentProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public void dispose() {
  }

  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
  }
}