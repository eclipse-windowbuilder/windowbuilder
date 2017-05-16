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
package org.eclipse.wb.internal.core.model.property.editor.style.actions;

import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.style.SubStylePropertyImpl;

import org.eclipse.jface.action.IAction;

/**
 * "Checked" action for {@link Property} context menu.
 *
 * @author lobas_av
 * @coverage core.model.property.editor
 */
public final class BooleanStyleAction extends SubStyleAction {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BooleanStyleAction(Property property, SubStylePropertyImpl propertyImpl) {
    super(property, propertyImpl, propertyImpl.getTitle(), IAction.AS_CHECK_BOX);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // SubStyleAction
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Object getActionValue() {
    return isChecked();
  }
}