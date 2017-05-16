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

import org.eclipse.jface.viewers.ICheckable;
import org.eclipse.jface.viewers.StructuredViewer;

/**
 * Universal wrapper for checked Tree and Table viewers.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public interface ICheckboxViewerWrapper {
  /**
   * @return wrapped viewer.
   */
  StructuredViewer getViewer();

  /**
   * @return {@link ICheckable} presentation of wrapped viewer.
   */
  ICheckable getCheckable();

  /**
   * @return the array of checked elements.
   */
  Object[] getCheckedElements();

  /**
   * Sets which nodes are checked in this viewer.
   */
  void setCheckedElements(Object[] elements);

  /**
   * Sets to the given value the checked state for all elements in this viewer.
   */
  void setAllChecked(boolean state);
}