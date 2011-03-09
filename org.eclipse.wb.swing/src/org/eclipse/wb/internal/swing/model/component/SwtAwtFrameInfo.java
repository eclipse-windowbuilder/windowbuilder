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
package org.eclipse.wb.internal.swing.model.component;

import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.swing.utils.SwingUtils;

/**
 * Special model for SWT_AWT frame.
 * 
 * @author mitin_aa
 * @coverage swing.model
 */
public class SwtAwtFrameInfo extends ContainerInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SwtAwtFrameInfo(AstEditor editor,
      ComponentDescription description,
      CreationSupport creationSupport) throws Exception {
    super(editor, description, creationSupport);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Hierarchy
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean isSwingRoot() {
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh 
  //
  ////////////////////////////////////////////////////////////////////////////	
  @Override
  protected void refresh_fetch() throws Exception {
    SwingUtils.runLaterAndWait(new RunnableEx() {
      public void run() throws Exception {
        runRefreshFetch();
      }
    });
  }

  private void runRefreshFetch() throws Exception {
    super.refresh_fetch();
  }
}
