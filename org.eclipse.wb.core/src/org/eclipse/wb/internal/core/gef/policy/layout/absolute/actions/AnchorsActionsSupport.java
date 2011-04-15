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
package org.eclipse.wb.internal.core.gef.policy.layout.absolute.actions;

import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.draw2d.IPositionConstants;
import org.eclipse.wb.internal.core.gef.GefMessages;
import org.eclipse.wb.internal.core.gef.policy.snapping.PlacementsSupport;
import org.eclipse.wb.internal.core.model.util.ObjectInfoAction;

import org.eclipse.jface.action.IContributionManager;

public class AnchorsActionsSupport {
  private final PlacementsSupport m_placementsSupport;
  private final IActionImageProvider m_imageProvider;

  public AnchorsActionsSupport(PlacementsSupport placementsSupport,
      IActionImageProvider imageProvider) {
    m_placementsSupport = placementsSupport;
    m_imageProvider = imageProvider;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Actions
  //
  ////////////////////////////////////////////////////////////////////////////
  public void fillAnchorsActions(IContributionManager manager,
      IAbstractComponentInfo widget,
      boolean isHorizontal) {
    if (isHorizontal) {
      manager.add(new SetAlignmentAction(widget,
          GefMessages.AnchorsActionsSupport_leftAlignment,
          "h/menu/left.gif",
          IPositionConstants.LEFT));
      manager.add(new SetAlignmentAction(widget,
          GefMessages.AnchorsActionsSupport_rightAlignment,
          "h/menu/right.gif",
          IPositionConstants.RIGHT));
      manager.add(new MakeResizeableAction(widget,
          GefMessages.AnchorsActionsSupport_makeResizableHorizontal,
          "h/menu/both.gif",
          isHorizontal));
    } else {
      manager.add(new SetAlignmentAction(widget,
          GefMessages.AnchorsActionsSupport_topAlignment,
          "v/menu/top.gif",
          IPositionConstants.TOP));
      manager.add(new SetAlignmentAction(widget,
          GefMessages.AnchorsActionsSupport_bottomAlignment,
          "v/menu/bottom.gif",
          IPositionConstants.BOTTOM));
      manager.add(new MakeResizeableAction(widget,
          GefMessages.AnchorsActionsSupport_makeResizableVertical,
          "v/menu/both.gif",
          isHorizontal));
    }
  }

  private final class SetAlignmentAction extends ObjectInfoAction {
    private final int m_alignment;
    private final IAbstractComponentInfo m_widget;

    private SetAlignmentAction(IAbstractComponentInfo widget,
        String text,
        String imageName,
        int alignment) {
      super(widget.getUnderlyingModel(), text, m_imageProvider.getActionImage(imageName));
      m_widget = widget;
      m_alignment = alignment;
    }

    @Override
    protected void runEx() throws Exception {
      m_placementsSupport.setAlignment(m_widget, m_alignment);
    }
  }
  private final class MakeResizeableAction extends ObjectInfoAction {
    private final boolean m_isHorizontal;
    private final IAbstractComponentInfo m_widget;

    private MakeResizeableAction(IAbstractComponentInfo widget,
        String text,
        String imageName,
        boolean isHorizontal) {
      super(widget.getUnderlyingModel(), text, m_imageProvider.getActionImage(imageName));
      m_widget = widget;
      m_isHorizontal = isHorizontal;
    }

    @Override
    protected void runEx() throws Exception {
      m_placementsSupport.setResizeable(m_widget, m_isHorizontal);
    }
  }
}
