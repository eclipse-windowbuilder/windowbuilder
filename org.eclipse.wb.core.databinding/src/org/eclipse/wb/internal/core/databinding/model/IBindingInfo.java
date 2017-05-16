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

/**
 * Abstract model for any binding describing "target" + "target property" and "model" +
 * "model property".
 *
 * @author lobas_av
 * @coverage bindings.model
 */
public interface IBindingInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Target
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@link IObserveInfo} the "target" model.
   */
  IObserveInfo getTarget();

  /**
   * @return {@link IObserveInfo} the "target property" model.
   */
  IObserveInfo getTargetProperty();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Model
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@link IObserveInfo} the "model" model.
   */
  IObserveInfo getModel();

  /**
   * @return {@link IObserveInfo} the "model property" model.
   */
  IObserveInfo getModelProperty();
}