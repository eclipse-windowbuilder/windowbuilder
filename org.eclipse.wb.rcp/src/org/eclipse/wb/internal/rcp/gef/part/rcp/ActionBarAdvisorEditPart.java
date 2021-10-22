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
package org.eclipse.wb.internal.rcp.gef.part.rcp;

import org.eclipse.wb.core.gef.part.AbstractComponentEditPart;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.internal.rcp.model.rcp.ActionBarAdvisorInfo;

/**
 * {@link EditPart} for {@link ActionBarAdvisorInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.part
 */
public final class ActionBarAdvisorEditPart extends AbstractComponentEditPart {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ActionBarAdvisorEditPart(ActionBarAdvisorInfo advisor) {
    super(advisor);
  }
}
