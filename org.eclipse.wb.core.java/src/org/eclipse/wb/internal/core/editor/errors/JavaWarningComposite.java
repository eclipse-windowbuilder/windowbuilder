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
package org.eclipse.wb.internal.core.editor.errors;

import org.eclipse.wb.internal.core.editor.actions.RefreshAction;
import org.eclipse.wb.internal.core.editor.actions.SwitchAction;

import org.eclipse.swt.widgets.Composite;

/**
 * Implementation for Java-related UI.
 *
 * @author scheglov_ke
 * @author mitin_aa
 * @coverage core.editor.errors
 */
public class JavaWarningComposite extends WarningComposite {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public JavaWarningComposite(Composite parent, int style) {
    super(parent, style);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void doRefresh() {
    new RefreshAction().run();
  }

  @Override
  protected void doShowSource(int sourcePosition) {
    SwitchAction.showSource(sourcePosition);
  }
}
