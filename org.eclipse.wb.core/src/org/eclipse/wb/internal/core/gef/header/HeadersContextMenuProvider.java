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
package org.eclipse.wb.internal.core.gef.header;

import org.eclipse.wb.core.gef.header.IHeaderMenuProvider;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.internal.gef.core.ContextMenuProvider;
import org.eclipse.wb.internal.gef.core.MultiSelectionContextMenuProvider;

import org.eclipse.jface.action.IMenuManager;

/**
 * {@link ContextMenuProvider} for headers.
 *
 * @author scheglov_ke
 * @coverage core.gef.header
 */
public final class HeadersContextMenuProvider extends MultiSelectionContextMenuProvider {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public HeadersContextMenuProvider(IEditPartViewer viewer) {
    super(viewer);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // MultiSelectionContextMenuProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void buildContextMenu(EditPart editPart, IMenuManager manager) {
    if (editPart instanceof IHeaderMenuProvider) {
      IHeaderMenuProvider headerMenuProvider = (IHeaderMenuProvider) editPart;
      headerMenuProvider.buildContextMenu(manager);
    }
  }
}