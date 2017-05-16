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
package org.eclipse.wb.internal.core.editor;

import org.eclipse.wb.core.controls.flyout.IFlyoutMenuContributor;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;

/**
 * {@link IFlyoutMenuContributor} for structure and palette.
 *
 * @author scheglov_ke
 * @coverage core.editor
 */
public final class DesignerFlyoutMenuContributor implements IFlyoutMenuContributor {
  private final String m_viewId;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public DesignerFlyoutMenuContributor(String viewId) {
    m_viewId = viewId;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IFlyoutMenuContributor
  //
  ////////////////////////////////////////////////////////////////////////////
  public void contribute(IMenuManager manager) {
    manager.add(new Action("Extract as view") {
      @Override
      public void run() {
        ExecutionUtils.runLog(new RunnableEx() {
          public void run() throws Exception {
            DesignerPlugin.getActivePage().showView(m_viewId);
          }
        });
      }
    });
  }
}