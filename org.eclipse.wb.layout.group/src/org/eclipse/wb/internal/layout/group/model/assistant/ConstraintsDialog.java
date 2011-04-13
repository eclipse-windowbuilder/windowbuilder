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
package org.eclipse.wb.internal.layout.group.model.assistant;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.internal.layout.group.Messages;
import org.eclipse.wb.internal.layout.group.model.IGroupLayoutInfo;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * Just for display {@link GroupLayoutSpacesPage}.
 * 
 * @author mitin_aa
 */
public final class ConstraintsDialog extends Dialog {
  private final AbstractComponentInfo m_component;
  private final IGroupLayoutInfo m_layout;
  private GroupLayoutSpacesPage m_groupLayoutSpacesPage;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ConstraintsDialog(Shell parentShell,
      IGroupLayoutInfo layout,
      AbstractComponentInfo component) {
    super(parentShell);
    m_layout = layout;
    m_component = component;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Contents
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected final Control createContents(Composite parent) {
    Composite container = (Composite) super.createDialogArea(parent);
    container.setLayout(new FillLayout());
    m_groupLayoutSpacesPage = new GroupLayoutSpacesPage(container, m_layout, m_component);
    return container;
  }

  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(Messages.ConstraintsDialog_title);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Update
  //
  ////////////////////////////////////////////////////////////////////////////
  public final void updateUI() {
    m_groupLayoutSpacesPage.updatePage();
  }
}
