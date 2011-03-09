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
package org.eclipse.wb.internal.swt.model.layout;

import org.eclipse.wb.core.model.IObjectInfo;
import org.eclipse.wb.internal.swt.model.widgets.ICompositeInfo;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.swt.widgets.Layout;

import java.util.List;

/**
 * Interface model for SWT {@link Layout}.
 * 
 * @author scheglov_ke
 * @coverage swt.model.layout
 */
public interface ILayoutInfo<C extends IControlInfo> extends IObjectInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link ICompositeInfo} that contains this {@link ILayoutInfo}.
   */
  ICompositeInfo getComposite();

  /**
   * @return <code>true</code> if this {@link ILayoutInfo} is active. For example implicit
   *         {@link ILayoutInfo}'s replaced by "real" {@link ILayoutInfo} is inactive.
   */
  boolean isActive();

  /**
   * @return <code>true</code> if given {@link Object} is managed by this {@link ILayoutInfo}.
   */
  boolean isManagedObject(Object o);

  /**
   * @return the {@link IControlInfo} that are managed by this {@link ILayoutInfo}. This excludes
   *         for example indirectly exposed {@link IControlInfo}'s.
   */
  List<C> getControls();

  /**
   * @return {@link ILayoutDataInfo} associated with given {@link IControlInfo}.
   */
  ILayoutDataInfo getLayoutData2(IControlInfo control);
}