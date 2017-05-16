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

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.IObservePresentation;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;

/**
 * Action for editing single binding.
 *
 * @author lobas_av
 * @coverage bindings.ui.properties
 */
public class SingleObserveBindingAction extends ObjectInfoAction {
  private final SingleObserveBindingProperty m_property;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SingleObserveBindingAction(ObjectInfo object, SingleObserveBindingProperty property)
      throws Exception {
    super(object);
    m_property = property;
    //
    IObservePresentation presentation = m_property.getObserveProperty().getPresentation();
    setText(presentation.getText());
    setIcon(presentation.getImage());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ObjectInfoAction
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void runEx() throws Exception {
    m_property.editBinding();
  }
}