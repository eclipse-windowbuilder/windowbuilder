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

import com.google.common.collect.Lists;

import org.eclipse.wb.core.editor.actions.assistant.ILayoutAssistantPage;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.layout.group.Messages;
import org.eclipse.wb.internal.layout.group.model.AlignmentsSupport;
import org.eclipse.wb.internal.layout.group.model.IGroupLayoutInfo;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.ToolBar;

import org.apache.commons.collections.CollectionUtils;
import org.netbeans.modules.form.layoutdesign.LayoutConstants;

import java.util.List;

/**
 * Layout assistant page for aligning components.
 * 
 * @author mitin_aa
 */
public final class GroupLayoutAlignmentPage extends Composite
    implements
      ILayoutAssistantPage,
      LayoutConstants {
  private final IGroupLayoutInfo m_layout;
  private final List<ObjectInfo> m_objects;
  private ToolBarManager m_toolBarManager;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GroupLayoutAlignmentPage(Composite parent,
      IGroupLayoutInfo layout,
      List<ObjectInfo> objects) {
    super(parent, SWT.NONE);
    // fields
    m_layout = layout;
    m_objects = objects;
    // UI
    GridLayoutFactory.create(this).noMargins().noSpacing();
    // put everything into a top group
    Group topGroup = new Group(this, SWT.NONE);
    GridDataFactory.create(topGroup).grab().fill();
    GridLayoutFactory.create(topGroup);
    topGroup.setText(Messages.GroupLayoutAlignmentPage_alignmentGroup);
    {
      m_toolBarManager = new ToolBarManager(SWT.FLAT);
      ToolBar toolBar = m_toolBarManager.createControl(topGroup);
      GridDataFactory.create(toolBar).alignHC();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ILayoutAssistantPage
  //
  ////////////////////////////////////////////////////////////////////////////
  public void updatePage() {
    m_toolBarManager.removeAll();
    List<ObjectInfo> sel = Lists.newArrayList();
    List<Object> actions = Lists.newArrayList();
    CollectionUtils.addAll(sel, m_objects.iterator());
    new AlignmentsSupport(m_layout).addAlignmentActions(sel, actions);
    for (Object action : actions) {
      if (action instanceof IContributionItem) {
        m_toolBarManager.add((IContributionItem) action);
      } else if (action instanceof IAction) {
        m_toolBarManager.add((IAction) action);
      }
    }
    m_toolBarManager.update(true);
  }

  public boolean isPageValid() {
    return true;
  }
}
