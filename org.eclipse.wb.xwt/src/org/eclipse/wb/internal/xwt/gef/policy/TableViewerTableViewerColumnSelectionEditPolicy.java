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
package org.eclipse.wb.internal.xwt.gef.policy;

import org.eclipse.wb.core.gef.policy.layout.generic.AbstractColumnSelectionEditPolicy;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;
import org.eclipse.wb.internal.xwt.model.jface.TableViewerColumnInfo;

/**
 * {@link SelectionEditPolicy} for {@link TableViewerColumnInfo}.
 * 
 * @author scheglov_ke
 * @coverage XWT.gef.policy
 */
public final class TableViewerTableViewerColumnSelectionEditPolicy
    extends
      AbstractColumnSelectionEditPolicy {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TableViewerTableViewerColumnSelectionEditPolicy(TableViewerColumnInfo column) {
    super(column);
  }
}