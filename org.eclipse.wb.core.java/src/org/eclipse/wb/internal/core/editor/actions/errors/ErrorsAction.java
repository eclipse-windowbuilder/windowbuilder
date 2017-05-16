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
package org.eclipse.wb.internal.core.editor.actions.errors;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.broadcast.ObjectEventListener;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.Messages;

import org.eclipse.jface.action.Action;

import java.util.List;

/**
 * {@link Action} that shows errors during parsing, refresh, etc.
 *
 * @author scheglov_ke
 * @coverage core.editor.action.error
 */
public class ErrorsAction extends Action {
  private final List<IErrorPage> m_pages = Lists.newArrayList();
  private ObjectInfo m_rootObject;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public ErrorsAction() {
    setImageDescriptor(DesignerPlugin.getImageDescriptor("actions/errors/errors.gif"));
    setDisabledImageDescriptor(DesignerPlugin.getImageDescriptor("actions/errors/errors_disabled.gif"));
    setToolTipText(Messages.ErrorsAction_toolTip);
    // add pages
    m_pages.add(new BadNodesRefreshErrorPage());
    m_pages.add(new WarningsErrorPage());
    m_pages.add(new BadNodesParserErrorPage());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets the root {@link ObjectInfo}.
   */
  public void setRoot(ObjectInfo rootObject) {
    m_rootObject = rootObject;
    m_rootObject.addBroadcastListener(new ObjectEventListener() {
      @Override
      public void refreshed() throws Exception {
        update();
      }
    });
    update();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Action
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void run() {
    ErrorsDialog errorsDialog = new ErrorsDialog(DesignerPlugin.getShell(), m_rootObject, m_pages);
    errorsDialog.open();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Updates state.
   */
  private void update() {
    firePropertyChange(ENABLED, null, null);
  }

  @Override
  public boolean isEnabled() {
    for (IErrorPage errorPage : m_pages) {
      errorPage.setRoot(m_rootObject);
      if (errorPage.hasErrors()) {
        return true;
      }
    }
    //
    return false;
  }
}
