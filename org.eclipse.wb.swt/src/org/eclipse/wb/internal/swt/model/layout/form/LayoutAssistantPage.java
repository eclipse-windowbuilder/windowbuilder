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

import com.google.common.collect.Lists;

import org.eclipse.wb.core.editor.actions.assistant.ILayoutAssistantPage;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.internal.core.gef.policy.layout.absolute.actions.AnchorsActionsSupport;
import org.eclipse.wb.internal.core.gef.policy.layout.absolute.actions.IActionImageProvider;
import org.eclipse.wb.internal.core.gef.policy.snapping.PlacementsSupport;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;
import org.eclipse.wb.internal.core.utils.GenericsUtils;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.swt.gef.policy.layout.form.FormLayoutEditPolicy;
import org.eclipse.wb.internal.swt.model.widgets.IControlInfo;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
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
public final class LayoutAssistantPage<C extends IControlInfo> extends Composite
    implements
      ILayoutAssistantPage {
  private final List<C> m_selection;
  private final IFormLayoutInfo<C> m_layout;
  private final PlacementsSupport m_placementsSupport;
  private final IActionImageProvider m_imageProvider = new IActionImageProvider() {
    public Image getActionImage(String imagePath) {
      return FormLayoutInfoImplAutomatic.getImage(imagePath);
    }
  };

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  @SuppressWarnings("unchecked")
  public LayoutAssistantPage(IFormLayoutInfo<C> layout,
      PlacementsSupport placementsSupport,
      Composite parent,
      Object selection) {
    super(parent, SWT.NONE);
    m_layout = layout;
    m_placementsSupport = placementsSupport;
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
      group.setText("Anchors");
      GridLayoutFactory.create(group);
      {
        ToolBarManager manager = new ToolBarManager();
        GridDataFactory.create(manager.createControl(group)).fill().grab();
        fillAnchorsActions(manager);
        manager.update(true);
      }
      {
        ToolBarManager manager = new ToolBarManager();
        GridDataFactory.create(manager.createControl(group)).fill().grab();
        fillComplexAnchorsActions(manager);
        manager.update(true);
      }
    }
    {
      Group group = new Group(this, SWT.NONE);
      GridDataFactory.create(group).fill().grab();
      group.setText("Alignment");
      GridLayoutFactory.create(group);
      ToolBarManager manager = new ToolBarManager();
      GridDataFactory.create(manager.createControl(group)).fill().grab();
      fillAlignmentActions(manager);
      manager.update(true);
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

  ////////////////////////////////////////////////////////////////////////////
  //
  // Actions
  //
  ////////////////////////////////////////////////////////////////////////////
  private void fillAnchorsActions(IContributionManager manager) {
    C widget = m_selection.size() == 1 ? (C) m_selection.get(0) : null;
    new AnchorsActionsSupport(m_placementsSupport, m_imageProvider).fillAnchorsActions(
        manager,
        widget,
        false);
    new AnchorsActionsSupport(m_placementsSupport, m_imageProvider).fillAnchorsActions(
        manager,
        widget,
        true);
  }

  private void fillAlignmentActions(final IContributionManager manager) {
    ArrayList<Object> actions = Lists.newArrayList();
    new FormLayoutEditPolicy.FormLayoutAlignmentActionsSupport<C>(m_layout, m_placementsSupport).addAlignmentActions(
        GenericsUtils.<ObjectInfo>cast(m_selection),
        actions);
    for (Object action : actions) {
      if (action instanceof IContributionItem) {
        manager.add((IContributionItem) action);
      } else if (action instanceof IAction) {
        manager.add((IAction) action);
      }
    }
  }

  private void fillComplexAnchorsActions(IContributionManager manager) {
    manager.add(new SetCornerAnchorsAction(m_selection,
        "Set Top-Left",
        "assistant/top_left.gif",
        IPositionConstants.LEFT | IPositionConstants.TOP));
    manager.add(new SetCornerAnchorsAction(m_selection,
        "Set Top-Right",
        "assistant/top_right.gif",
        IPositionConstants.TOP | IPositionConstants.RIGHT));
    manager.add(new SetCornerAnchorsAction(m_selection,
        "Set Bottom-Left",
        "assistant/bottom_left.gif",
        IPositionConstants.LEFT | IPositionConstants.BOTTOM));
    manager.add(new SetCornerAnchorsAction(m_selection,
        "Set Bottom-Right",
        "assistant/bottom_right.gif",
        IPositionConstants.BOTTOM | IPositionConstants.RIGHT));
    manager.add(new Separator());
    manager.add(new SetCornerAnchorsAction(m_selection,
        "Set Left-Right-Top",
        "assistant/top_left_right.gif",
        IPositionConstants.LEFT | IPositionConstants.TOP | IPositionConstants.RIGHT));
    manager.add(new SetCornerAnchorsAction(m_selection,
        "Set Left-Right-Bottom",
        "assistant/bottom_left_right.gif",
        IPositionConstants.LEFT | IPositionConstants.BOTTOM | IPositionConstants.RIGHT));
    manager.add(new SetCornerAnchorsAction(m_selection,
        "Set Top-Bottom-Left",
        "assistant/top_bottom_left.gif",
        IPositionConstants.TOP | IPositionConstants.LEFT | IPositionConstants.BOTTOM));
    manager.add(new SetCornerAnchorsAction(m_selection,
        "Set Top-Bottom-Right",
        "assistant/top_bottom_right.gif",
        IPositionConstants.BOTTOM | IPositionConstants.RIGHT | IPositionConstants.TOP));
    manager.add(new SetCornerAnchorsAction(m_selection,
        "Set All",
        "assistant/top_bottom_left_right.gif",
        IPositionConstants.BOTTOM
            | IPositionConstants.RIGHT
            | IPositionConstants.TOP
            | IPositionConstants.LEFT));
  }

  private final class SetCornerAnchorsAction extends ObjectInfoAction {
    private final int m_alignment;
    private final List<C> m_widgets;

    private SetCornerAnchorsAction(List<C> selection, String text, String imageName, int alignment) {
      super(m_layout.getUnderlyingModel(), text, m_imageProvider.getActionImage(imageName));
      m_widgets = selection;
      m_alignment = alignment;
    }

    @Override
    protected void runEx() throws Exception {
      for (C widget : m_widgets) {
        if ((m_alignment & IPositionConstants.LEFT) != 0
            && (m_alignment & IPositionConstants.RIGHT) != 0) {
          m_placementsSupport.setResizeable(widget, true);
        } else {
          checkAlignment(widget, IPositionConstants.LEFT);
          checkAlignment(widget, IPositionConstants.RIGHT);
        }
        if ((m_alignment & IPositionConstants.TOP) != 0
            && (m_alignment & IPositionConstants.BOTTOM) != 0) {
          m_placementsSupport.setResizeable(widget, false);
        } else {
          checkAlignment(widget, IPositionConstants.TOP);
          checkAlignment(widget, IPositionConstants.BOTTOM);
        }
      }
    }

    private void checkAlignment(C widget, int alignment) throws Exception {
      if ((m_alignment & alignment) != 0) {
        m_placementsSupport.setAlignment(widget, alignment);
      }
    }
  }
}