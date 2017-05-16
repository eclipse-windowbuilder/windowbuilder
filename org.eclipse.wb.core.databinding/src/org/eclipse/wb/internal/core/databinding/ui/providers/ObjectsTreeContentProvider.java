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

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import org.apache.commons.lang.ArrayUtils;

import java.util.Collection;

/**
 * Implementation of {@link ITreeContentProvider} for {@link ObjectInfo}.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public class ObjectsTreeContentProvider implements ITreeContentProvider {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Input
  //
  ////////////////////////////////////////////////////////////////////////////
  public Object[] getElements(Object input) {
    // case collection
    if (input instanceof Collection<?>) {
      Collection<?> inputCollection = (Collection<?>) input;
      return inputCollection.toArray();
    }
    // case array
    if (input instanceof Object[]) {
      return (Object[]) input;
    }
    // case direct object
    if (input instanceof ObjectInfo) {
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
  public Object getParent(Object element) {
    if (element instanceof ObjectInfo) {
      ObjectInfo info = (ObjectInfo) element;
      return info.getParent();
    }
    return null;
  }

  public boolean hasChildren(Object element) {
    if (element instanceof ObjectInfo) {
      // prepare info
      ObjectInfo info = (ObjectInfo) element;
      // prepare presentation
      final IObjectPresentation presentation = info.getPresentation();
      if (presentation != null) {
        // check children
        return ExecutionUtils.runObjectLog(new RunnableObjectEx<Boolean>() {
          public Boolean runObject() throws Exception {
            return presentation.isVisible() && !presentation.getChildrenTree().isEmpty();
          }
        }, false);
      }
    }
    return false;
  }

  public Object[] getChildren(Object element) {
    if (element instanceof ObjectInfo) {
      // prepare info
      ObjectInfo info = (ObjectInfo) element;
      // prepare presentation
      final IObjectPresentation presentation = info.getPresentation();
      if (presentation != null) {
        // get children
        return ExecutionUtils.runObjectLog(new RunnableObjectEx<Object[]>() {
          public Object[] runObject() throws Exception {
            return presentation.getChildrenTree().toArray();
          }
        }, ArrayUtils.EMPTY_OBJECT_ARRAY);
      }
    }
    return ArrayUtils.EMPTY_OBJECT_ARRAY;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Life cycle
  //
  ////////////////////////////////////////////////////////////////////////////
  public void dispose() {
  }

  public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
  }
}