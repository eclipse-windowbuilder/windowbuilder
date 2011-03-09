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
package org.eclipse.wb.internal.ercp.gefTree;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartFactory;
import org.eclipse.wb.internal.ercp.gefTree.part.CaptionedControlEditPart;
import org.eclipse.wb.internal.ercp.gefTree.part.ListBoxEditPart;
import org.eclipse.wb.internal.ercp.model.widgets.mobile.CaptionedControlInfo;
import org.eclipse.wb.internal.ercp.model.widgets.mobile.ListBoxInfo;

/**
 * Implementation of {@link IEditPartFactory} for eRCP.
 * 
 * @author scheglov_ke
 * @coverage ercp.gefTree
 */
public final class EditPartFactory implements IEditPartFactory {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IEditPartFactory
  //
  ////////////////////////////////////////////////////////////////////////////
  public EditPart createEditPart(EditPart context, Object model) {
    if (model instanceof CaptionedControlInfo) {
      return new CaptionedControlEditPart((CaptionedControlInfo) model);
    }
    if (model instanceof ListBoxInfo) {
      return new ListBoxEditPart((ListBoxInfo) model);
    }
    // not found
    return null;
  }
}