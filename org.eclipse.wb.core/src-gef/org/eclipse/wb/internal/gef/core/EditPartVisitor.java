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
package org.eclipse.wb.internal.gef.core;

import org.eclipse.wb.gef.core.EditPart;

/**
 * Visitor for visiting {@link EditPart} hierarchy.
 *
 * @author lobas_av
 * @coverage gef.core
 */
public class EditPartVisitor {
  /**
   * This method is invoked to check if given {@link EditPart} and its children should be visited.
   */
  public boolean visit(EditPart editPart) {
    return true;
  }

  /**
   * This method is invoked when all children of given {@link EditPart} were visited.
   */
  public void endVisit(EditPart editPart) {
  }
}