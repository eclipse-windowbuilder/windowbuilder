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
package org.eclipse.wb.internal.css.editors.multi;

import org.eclipse.wb.internal.core.utils.binding.editors.controls.DefaultControlActionsManager;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.ui.texteditor.IWorkbenchActionDefinitionIds;

/**
 * Manager for installing/unistalling global handlers for {@link StyledText} actions commands.
 * 
 * @author sablin_aa
 * @author mitin_aa
 * @coverage CSS.editor
 */
final class StyledTextControlActionsManager extends DefaultControlActionsManager {
  private final StyledText m_text;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public StyledTextControlActionsManager(final StyledText text) {
    super(text);
    m_text = text;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handlers
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected IHandler getHandlerFor(String actionName) {
    if (actionName.equalsIgnoreCase(IWorkbenchActionDefinitionIds.COPY)) {
      return COPY_HANDLER;
    }
    if (actionName.equalsIgnoreCase(IWorkbenchActionDefinitionIds.CUT)) {
      return CUT_HANDLER;
    }
    if (actionName.equalsIgnoreCase(IWorkbenchActionDefinitionIds.PASTE)) {
      return PASTE_HANDLER;
    }
    return super.getHandlerFor(actionName);
  };

  @Override
  protected void selectAllExecuted() {
    m_text.selectAll();
  }

  private final IHandler COPY_HANDLER = new AbstractHandler() {
    public Object execute(ExecutionEvent event) throws ExecutionException {
      m_text.copy();
      return null;
    }

    @Override
    public boolean isEnabled() {
      return true;
    }

    @Override
    public boolean isHandled() {
      return true;
    }
  };
  private final IHandler CUT_HANDLER = new AbstractHandler() {
    public Object execute(ExecutionEvent event) throws ExecutionException {
      m_text.cut();
      return null;
    }

    @Override
    public boolean isEnabled() {
      return true;
    }

    @Override
    public boolean isHandled() {
      return true;
    }
  };
  private final IHandler PASTE_HANDLER = new AbstractHandler() {
    public Object execute(ExecutionEvent event) throws ExecutionException {
      m_text.paste();
      return null;
    }

    @Override
    public boolean isEnabled() {
      return true;
    }

    @Override
    public boolean isHandled() {
      return true;
    }
  };
}
