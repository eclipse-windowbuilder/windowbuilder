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
package org.eclipse.wb.internal.core.databinding.ui.property;

import org.eclipse.wb.internal.core.databinding.model.IBindingInfo;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;

/**
 * Action for editing single binding.
 *
 * @author lobas_av
 * @coverage bindings.ui.properties
 */
public class BindingAction extends ObjectInfoAction {
  private final Context m_context;
  private final IBindingInfo m_binding;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BindingAction(Context context, IBindingInfo binding) {
    super(context.objectInfo);
    m_context = context;
    m_binding = binding;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ObjectInfoAction
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void runEx() throws Exception {
    AbstractBindingProperty.editBinding(m_context, m_binding);
  }
}