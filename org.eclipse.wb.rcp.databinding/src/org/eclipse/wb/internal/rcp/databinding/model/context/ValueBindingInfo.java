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
package org.eclipse.wb.internal.rcp.databinding.model.context;

import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.strategies.UpdateValueStrategyInfo;

/**
 * Model for <code>DataBindingContext.bindValue()</code>.
 * 
 * @author lobas_av
 * @coverage bindings.rcp.model.context
 */
public final class ValueBindingInfo extends BindingInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public ValueBindingInfo(ObservableInfo target, ObservableInfo model) {
    this(target, model, null, null);
  }

  public ValueBindingInfo(ObservableInfo target,
      ObservableInfo model,
      UpdateValueStrategyInfo targetStrategy,
      UpdateValueStrategyInfo modelStrategy) {
    super(target, model);
    m_targetStrategy = targetStrategy == null ? new UpdateValueStrategyInfo() : targetStrategy;
    m_modelStrategy = modelStrategy == null ? new UpdateValueStrategyInfo() : modelStrategy;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Code generation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getBindingMethod() {
    return "bindValue";
  }
}