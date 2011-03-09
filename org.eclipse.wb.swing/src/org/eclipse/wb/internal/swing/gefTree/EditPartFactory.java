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
package org.eclipse.wb.internal.swing.gefTree;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartFactory;
import org.eclipse.wb.internal.swing.gefTree.part.ComponentEditPart;
import org.eclipse.wb.internal.swing.gefTree.part.ContainerEditPart;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

/**
 * Implementation of {@link IEditPartFactory} for Swing.
 * 
 * @author mitin_aa
 * @coverage swing.gefTree
 */
public final class EditPartFactory implements IEditPartFactory {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IEditPartFactory
  //
  ////////////////////////////////////////////////////////////////////////////
  public EditPart createEditPart(EditPart context, Object model) {
    // components
    if (model instanceof ContainerInfo) {
      return new ContainerEditPart((ContainerInfo) model);
    } else if (model instanceof ComponentInfo) {
      return new ComponentEditPart((ComponentInfo) model);
    }
    // unknown
    return null;
  }
}
