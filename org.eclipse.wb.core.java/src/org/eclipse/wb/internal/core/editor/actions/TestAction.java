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
package org.eclipse.wb.internal.core.editor.actions;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.DesignPage;
import org.eclipse.wb.internal.core.model.TopBoundsSupport;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;

import org.eclipse.jface.action.Action;

/**
 * Action for displaying {@link AbstractComponentInfo} for test/preview.
 *
 * @author scheglov_ke
 * @coverage core.editor.action
 */
public final class TestAction extends Action {
  private ObjectInfo m_rootObject;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TestAction() {
    setText("Test/Preview...");
    setToolTipText("Quickly test/preview the window without compiling or running it");
    setImageDescriptor(DesignerPlugin.getImageDescriptor("test.png"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets the root {@link ObjectInfo} on {@link DesignPage}.
   */
  public void setRoot(ObjectInfo rootObject) {
    m_rootObject = rootObject;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Action
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void run() {
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        ExecutionUtils.runDesignTime(new RunnableEx() {
          public void run() throws Exception {
            run0();
          }
        });
      }
    });
  }

  private void run0() throws Exception {
    if (m_rootObject instanceof AbstractComponentInfo) {
      AbstractComponentInfo component = (AbstractComponentInfo) m_rootObject;
      TopBoundsSupport topSupport = component.getTopBoundsSupport();
      boolean doReparse = topSupport.show();
      // refresh because user can change something
      if (doReparse) {
        m_rootObject.refresh();
      }
    }
  }
}