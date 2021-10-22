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
package org.eclipse.wb.internal.core.xml.editor.actions;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.actions.ActionUtils;
import org.eclipse.wb.internal.core.xml.Messages;
import org.eclipse.wb.internal.core.xml.editor.XmlDesignPage;

import org.eclipse.ui.actions.ActionFactory;

/**
 * Action for reparse/refresh {@link XmlDesignPage}.
 *
 * @author scheglov_ke
 * @coverage XML.editor.action
 */
public final class RefreshAction extends DesignPageAction {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public RefreshAction() {
    // copy presentation
    ActionUtils.copyPresentation(this, ActionFactory.REFRESH);
    // override presentation
    setToolTipText(Messages.RefreshAction_toolTip);
    setImageDescriptor(DesignerPlugin.getImageDescriptor("editor_refresh.png"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // DesignPageAction
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void run(XmlDesignPage designPage) {
    designPage.refreshGEF();
  }
}