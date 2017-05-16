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
package org.eclipse.wb.internal.core.gef.policy.snapping;

import org.eclipse.wb.core.gef.command.EditCommand;
import org.eclipse.wb.core.model.ObjectInfo;

/**
 * Special command belonging to SnapPoint.
 *
 * @author mitin_aa
 */
public abstract class SnapPointCommand extends EditCommand {
  private SnapPoint m_snapPoint;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SnapPointCommand(ObjectInfo objectInfo, SnapPoint snapPoint) {
    super(objectInfo);
    m_snapPoint = snapPoint;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setSnapPoint(SnapPoint snapPoint) {
    m_snapPoint = snapPoint;
  }

  protected final SnapPoint getSnapPoint() {
    return m_snapPoint;
  }
}
