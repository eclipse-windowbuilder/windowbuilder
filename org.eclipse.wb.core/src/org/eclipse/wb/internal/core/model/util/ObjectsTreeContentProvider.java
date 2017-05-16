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
package org.eclipse.wb.internal.core.model.util;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import org.eclipse.wb.core.model.ObjectInfo;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import java.util.List;

/**
 * Implementation of {@link ITreeContentProvider} for {@link ObjectInfo}.
 *
 * @author scheglov_ke
 * @coverage core.model.util
 */
public final class ObjectsTreeContentProvider implements ITreeContentProvider {
  private final Predicate<ObjectInfo> m_predicate;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ObjectsTreeContentProvider(Predicate<ObjectInfo> predicate) {
    m_predicate = predicate;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Input
  //
  ////////////////////////////////////////////////////////////////////////////
  public Object[] getElements(Object inputElement) {
    if (inputElement instanceof Object[]) {
      return (Object[]) inputElement;
    } else {
      return getChildren(inputElement);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Children
  //
  ////////////////////////////////////////////////////////////////////////////
  public Object[] getChildren(Object parentElement) {
    List<ObjectInfo> children = ((ObjectInfo) parentElement).getChildren();
    Iterable<ObjectInfo> filtered = Iterables.filter(children, m_predicate);
    return Iterables.toArray(filtered, ObjectInfo.class);
  }

  public boolean hasChildren(Object element) {
    return getChildren(element).length != 0;
  }

  public Object getParent(Object element) {
    return ((ObjectInfo) element).getParent();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
  }

  public void dispose() {
  }
}
