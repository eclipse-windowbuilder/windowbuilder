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
package org.eclipse.wb.internal.swt.model.layout.form;

import org.eclipse.wb.core.editor.actions.assistant.ILayoutAssistantPage;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.swt.model.ModelMessages;
import org.eclipse.wb.internal.swt.model.layout.form.actions.PredefinedAnchorsActions;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Layout assistant page for FormLayout automatic mode.
 *
 * @author mitin_aa
 */
public final class LayoutAssistantPageClassic<C extends IControlInfo> extends Composite
    implements
      ILayoutAssistantPage {
  private final List<C> m_selection;
  private final IFormLayoutInfo<C> m_layout;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  @SuppressWarnings("unchecked")
  public LayoutAssistantPageClassic(IFormLayoutInfo<C> layout,
      Composite parent,
      Object selection,
      ArrayList<Object> actions) {
    super(parent, SWT.NONE);
    m_layout = layout;
    if (selection instanceof List<?>) {
      m_selection = (List<C>) selection;
    } else {
      m_selection = Collections.singletonList((C) selection);
    }
    // create UI
    GridLayoutFactory.create(this);
    {
      Group group = new Group(this, SWT.NONE);
      GridDataFactory.create(group).fill().grab();
      group.setText(ModelMessages.LayoutAssistantPageClassic_anchorsGroup);
      GridLayoutFactory.create(group);
      {
        ToolBarManager manager = new ToolBarManager();
        GridDataFactory.create(manager.createControl(group)).fill().grab();
        new PredefinedAnchorsActions<C>(m_layout).contributeActions(m_selection, manager);
        manager.update(true);
      }
    }
    {
      Group group = new Group(this, SWT.NONE);
      GridDataFactory.create(group).fill().grab();
      group.setText(ModelMessages.LayoutAssistantPageClassic_alignmentGroup);
      GridLayoutFactory.create(group);
      ToolBarManager manager = new ToolBarManager();
      GridDataFactory.create(manager.createControl(group)).fill().grab();
      fillAlignmentActions(actions, manager);
      manager.update(true);
    }
  }

  private void fillAlignmentActions(ArrayList<Object> actions, IContributionManager manager) {
    for (Object action : actions) {
      if (action instanceof IContributionItem) {
        manager.add((IContributionItem) action);
      } else if (action instanceof IAction) {
        manager.add((IAction) action);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ILayoutAssistantPage
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean isPageValid() {
    for (C object : m_selection) {
      ObjectInfo parent = object.getParent();
      if (!parent.getChildren().contains(object)) {
        return false;
      }
    }
    return true;
  }

  public void updatePage() {
  }
}