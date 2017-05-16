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
package org.eclipse.wb.internal.core.model.menu;

import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import org.eclipse.swt.graphics.Image;

/**
 * Common interface for any menu object.
 *
 * @author mitin_aa
 * @author scheglov_ke
 * @coverage core.model.menu
 */
public interface IMenuObjectInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Model
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Usually we should register menu {@link EditPart}'s with original toolkit models, such as
   * {@link IAbstractComponentInfo}. At same time we don't want expose
   * {@link IAbstractComponentInfo} to the generic menu implementation.
   *
   * @return the model that should be associated with this {@link IMenuObjectInfo}.
   */
  Object getModel();
  /**
   * @return the toolkit model, such as {@link ObjectInfo}.
   */
  Object getToolkitModel();
  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete listener
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds {@link IMenuObjectListener}.
   */
  void addListener(IMenuObjectListener listener);
  /**
   * Removes {@link IMenuObjectListener}.
   */
  void removeListener(IMenuObjectListener listener);
  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Image} to show as presentation, may be <code>null</code>. If
   *         <code>null</code>, then only {@link #getBounds()} will be used to select area on
   *         parent's presentation.
   */
  Image getImage();
  /**
   * @return the location/size of this object presentation on parent's presentation.
   */
  Rectangle getBounds();
  ////////////////////////////////////////////////////////////////////////////
  //
  // Validation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if this {@link IMenuObjectInfo} can be moved inside of its parent.
   */
  boolean canMove();
  /**
   * @return <code>true</code> if this {@link IMenuObjectInfo} can be moved on different parent.
   */
  boolean canReparent();
  ////////////////////////////////////////////////////////////////////////////
  //
  // Policy
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Performs edit operation using given {@link RunnableEx}.
   */
  void executeEdit(RunnableEx runnable);
  /**
   * @return the {@link IMenuPolicy} for validating and performing operations.
   */
  IMenuPolicy getPolicy();
}
