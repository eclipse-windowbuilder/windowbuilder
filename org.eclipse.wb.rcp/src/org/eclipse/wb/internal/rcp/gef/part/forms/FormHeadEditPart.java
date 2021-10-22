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
package org.eclipse.wb.internal.rcp.gef.part.forms;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.internal.rcp.gef.policy.forms.FormHeadLayoutEditPolicy;
import org.eclipse.wb.internal.rcp.model.forms.FormInfo;
import org.eclipse.wb.internal.swt.gef.part.CompositeEditPart;

/**
 * {@link EditPart} for {@link FormInfo#getHead()}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.part
 */
public final class FormHeadEditPart extends CompositeEditPart {
  private final FormInfo m_form;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FormHeadEditPart(FormInfo form) {
    super(form.getHead());
    m_form = form;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Policy
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void refreshEditPolicies() {
    super.refreshEditPolicies();
    installEditPolicy(new FormHeadLayoutEditPolicy(m_form));
  }
}
