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
package org.eclipse.wb.internal.draw2d;

import org.eclipse.wb.draw2d.Figure;

/**
 * Visitor for visiting {@link Figure} hierarchy.
 *
 * @author lobas_av
 * @coverage gef.draw2d
 */
public class FigureVisitor {
  /**
   * This method is invoked to check if given {@link Figure} and its children should be visited.
   */
  public boolean visit(Figure figure) {
    return true;
  }

  /**
   * This method is invoked when all children of given {@link Figure} were visited.
   */
  public void endVisit(Figure figure) {
  }
}