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
package org.eclipse.wb.internal.swt.model.layout.form;

import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.internal.swt.model.layout.ILayoutInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.core.resources.IResource;

/**
 * Interface for FormLayout model.
 *
 * @author mitin_aa
 * @coverage swt.model.layout.form
 */
public interface IFormLayoutInfo<C extends IControlInfo> extends ILayoutInfo<C> {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Model
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the implementation of the FormLayout support.
   */
  FormLayoutInfoImpl<C> getImpl();

  /**
   * @return the size of the host container.
   */
  Dimension getContainerSize();

  /**
   * Moves a Control into a host Composite.
   */
  void commandMove(C control, C nextControl) throws Exception;

  /**
   * Adds a Control into a host Composite.
   */
  void commandCreate(C control, C nextControl) throws Exception;

  /**
   * Binds Control's <code>side</code> to the <b>left</b> side of the container with given
   * <code>offset</code>.
   */
  void setAttachmentOffset(C control, int side, int offset) throws Exception;

  /**
   * Anchors the control at current place to the parent with given sides. If side omitted and if
   * relative, then anchors the missing side to percent, otherwise deletes attachment.
   */
  void setQuickAnchors(C control, int sides, boolean relative) throws Exception;

  /**
   * Anchors the control at current place to the parent with given control's side and container's
   * side.
   */
  void anchorToParent(C control, int controlSide, int parentSide) throws Exception;

  /**
   * Anchors the control at current place by the given side to the percentage value.
   */
  void anchorToParentAsPercent(C control, int controlSide) throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Misc
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the instance of class maintaining FormLayout preferences. May not return
   *         <code>null</code>.
   */
  FormLayoutPreferences<C> getPreferences();

  /**
   * @return underlying resource for the editing class.
   */
  IResource getUnderlyingResource() throws Exception;
}
