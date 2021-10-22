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

import org.eclipse.wb.core.gef.part.AbstractComponentEditPart;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.internal.rcp.model.forms.MasterDetailsBlockInfo;

/**
 * {@link EditPart} for {@link MasterDetailsBlockInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.gef.part
 */
public final class MasterDetailsBlockEditPart extends AbstractComponentEditPart {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public MasterDetailsBlockEditPart(MasterDetailsBlockInfo block) {
    super(block);
  }
}
