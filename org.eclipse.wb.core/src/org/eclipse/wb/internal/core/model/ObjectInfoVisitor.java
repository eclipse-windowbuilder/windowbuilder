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
package org.eclipse.wb.internal.core.model;

import org.eclipse.wb.core.model.ObjectInfo;

/**
 * Visitor for visiting {@link ObjectInfo} hierarchy.
 *
 * @author scheglov_ke
 * @coverage core.model
 */
public class ObjectInfoVisitor {
  /**
   * This method is invoked to check if given {@link ObjectInfo} and its children should be visited.
   */
  public boolean visit(ObjectInfo objectInfo) throws Exception {
    return true;
  }

  /**
   * This method is invoked when all children of given {@link ObjectInfo} were visited.
   */
  public void endVisit(ObjectInfo objectInfo) throws Exception {
  }
}
