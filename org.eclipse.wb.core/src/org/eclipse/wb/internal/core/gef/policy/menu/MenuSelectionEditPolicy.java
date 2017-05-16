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
package org.eclipse.wb.internal.core.gef.policy.menu;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.IColorConstants;
import org.eclipse.wb.draw2d.Layer;
import org.eclipse.wb.draw2d.border.LineBorder;
import org.eclipse.wb.draw2d.events.IAncestorListener;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.gef.graphical.policies.SelectionEditPolicy;

import java.util.Collections;
import java.util.List;

/**
 * A selection policy for every menu object.
 *
 * @author mitin_aa
 * @coverage core.gef.menu
 */
public final class MenuSelectionEditPolicy extends SelectionEditPolicy {
  private Figure m_selectionFigure;
  private IAncestorListener m_ancestorListener;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handles
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<Handle> createSelectionHandles() {
    return Collections.emptyList();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Selection
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void showSelection() {
    hideSelection();
    m_selectionFigure = new Figure();
    m_selectionFigure.setBorder(new LineBorder(IColorConstants.menuBackgroundSelected));
    updateFeedbackBounds();
    m_ancestorListener = new IAncestorListener() {
      public void ancestorMoved(Figure ancestor) {
        updateFeedbackBounds();
      }
    };
    getHostFigure().addAncestorListener(m_ancestorListener);
    addFeedback(m_selectionFigure);
  }

  @Override
  protected void hideSelection() {
    if (m_selectionFigure != null) {
      getHostFigure().removeAncestorListener(m_ancestorListener);
      removeFeedback(m_selectionFigure);
      m_selectionFigure = null;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Feedbacks
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Layer getFeedbackLayer() {
    return getLayer(IEditPartViewer.MENU_FEEDBACK_LAYER);
  }

  /**
   * Updates feedback figure bounds in ancestor listener.
   */
  private void updateFeedbackBounds() {
    Rectangle selectionBounds = getHostFigure().getBounds().getCopy();
    FigureUtils.translateFigureToFigure(getHostFigure(), m_selectionFigure, selectionBounds);
    m_selectionFigure.setBounds(selectionBounds);
  }
}
