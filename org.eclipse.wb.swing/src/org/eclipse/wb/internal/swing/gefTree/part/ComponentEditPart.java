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
package org.eclipse.wb.internal.swing.gefTree.part;

import org.eclipse.wb.core.gefTree.part.JavaEditPart;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

/**
 * {@link EditPart} for {@link ComponentInfo} for GEF Tree.
 * 
 * @author mitin_aa
 * @coverage swing.gefTree.part
 */
public class ComponentEditPart extends JavaEditPart {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ComponentEditPart(ComponentInfo model) {
    super(model);
  }
}
