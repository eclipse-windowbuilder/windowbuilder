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
package org.eclipse.wb.internal.core.databinding.model;

import org.eclipse.wb.internal.core.databinding.ui.ObserveType;

import java.util.List;

/**
 * Abstract model for any observe object and observe object property.
 *
 * @author lobas_av
 * @coverage bindings.model
 */
public interface IObserveInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Type
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@link ObserveType} for this observe.
   */
  ObserveType getType();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Hierarchy
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the parent of this object.
   */
  IObserveInfo getParent();

  /**
   * @return list of {@link IObserveInfo} children with given {@link ChildrenContext}.
   */
  List<IObserveInfo> getChildren(ChildrenContext context);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@link IObservePresentation} for visual presentation of this object.
   */
  IObservePresentation getPresentation();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Context
  //
  ////////////////////////////////////////////////////////////////////////////
  enum ChildrenContext {
    /**
     * All "object" children.
     */
    ChildrenForMasterTable,
    /**
     * All children properties.
     */
    ChildrenForPropertiesTable
  }
}