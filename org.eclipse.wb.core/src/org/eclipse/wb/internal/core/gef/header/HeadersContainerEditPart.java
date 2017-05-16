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
import org.eclipse.wb.core.gef.header.IHeadersProvider;
import org.eclipse.wb.core.gef.policy.selection.EmptySelectionEditPolicy;
import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.geometry.Dimension;
import org.eclipse.wb.draw2d.geometry.Rectangle;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.GraphicalEditPart;
import org.eclipse.wb.internal.gef.graphical.GraphicalViewer;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Control;

import java.util.Collections;
import java.util.List;

/**
 * Implementation of {@link EditPart} for displaying headers for selection in main
 * {@link GraphicalViewer}.
 *
 * @author scheglov_ke
 * @coverage core.gef.header
 */
public final class HeadersContainerEditPart extends GraphicalEditPart
    implements
      IHeaderMenuProvider {
  private final GraphicalViewer m_viewer;
  private final boolean m_horizontal;
  private IHeadersProvider m_headersProvider;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public HeadersContainerEditPart(GraphicalViewer viewer, boolean horizontal) {
    m_viewer = viewer;
    m_horizontal = horizontal;
    //
    m_viewer.addSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event) {
        refreshHeaders();
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Policy
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void createEditPolicies() {
    super.createEditPolicies();
    installEditPolicy(EditPolicy.SELECTION_ROLE, new EmptySelectionEditPolicy());
  }

  @Override
  public void performRequest(Request request) {
    super.performRequest(request);
    if (request.getType() == Request.REQ_OPEN) {
      if (m_headersProvider != null) {
        m_headersProvider.handleDoubleClick(m_horizontal);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Figure
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Figure createFigure() {
    return new Figure();
  }

  @Override
  protected void refreshVisuals() {
    GraphicalViewer viewer = (GraphicalViewer) getViewer();
    if (!viewer.getControl().isDisposed()) {
      // prepare viewer size
      org.eclipse.swt.graphics.Point size = viewer.getControl().getSize();
      // prepare main viewer size
      Dimension mainSize = m_viewer.getLayer(IEditPartViewer.PRIMARY_LAYER).getSize();
      // set bounds
      if (m_horizontal) {
        getFigure().setBounds(new Rectangle(0, 0, mainSize.width, size.y));
      } else {
        getFigure().setBounds(new Rectangle(0, 0, size.x, mainSize.height));
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Children
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public List<?> getModelChildren() {
    if (m_headersProvider != null) {
      return m_headersProvider.getHeaders(m_horizontal);
    }
    return Collections.emptyList();
  }

  @Override
  protected EditPart createEditPart(Object model) {
    return m_headersProvider.createHeaderEditPart(m_horizontal, model);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Refresh
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Updates current {@link IHeadersProvider} and headers.
   */
  public void refreshHeaders() {
    final Control viewerControl = ((GraphicalViewer) getViewer()).getControl();
    if (!viewerControl.isDisposed()) {
      m_headersProvider = getHeadersProvider();
      doRefreshHeaders();
    }
  }

  /**
   * Updates current {@link IHeadersProvider} and headers.
   */
  private void doRefreshHeaders() {
    installEditPolicy(EditPolicy.LAYOUT_ROLE, null);
    // refresh children
    refresh();
    for (EditPart editPart : getChildren()) {
      editPart.refresh();
      ((GraphicalEditPart) editPart).getFigure().repaint();
    }
    // install LayoutEditPolicy
    if (m_headersProvider != null) {
      installEditPolicy(
          EditPolicy.LAYOUT_ROLE,
          m_headersProvider.getContainerLayoutPolicy(m_horizontal));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link IHeadersProvider} for current selection in {@link #m_viewer}.
   */
  private IHeadersProvider getHeadersProvider() {
    // prepare selected EditPart
    EditPart selectedEditPart;
    {
      List<EditPart> selectedEditParts = m_viewer.getSelectedEditParts();
      if (selectedEditParts.size() != 1) {
        return null;
      }
      selectedEditPart = selectedEditParts.get(0);
      if (selectedEditPart == null) {
        return null;
      }
    }
    // get provider from container of selected EditPart
    {
      EditPart containerEditPart = selectedEditPart.getParent();
      if (containerEditPart != null) {
        IHeadersProvider headersProvider = getHeadersProvider(containerEditPart);
        if (headersProvider != null) {
          return headersProvider;
        }
      }
    }
    // get provider from selected EditPart itself (it also may be container)
    {
      IHeadersProvider headersProvider = getHeadersProvider(selectedEditPart);
      if (headersProvider != null) {
        return headersProvider;
      }
    }
    // no provider
    return null;
  }

  /**
   * @return the {@link IHeadersProvider} implemented by one of the {@link EditPolicy} of given
   *         {@link EditPart}.
   */
  private static IHeadersProvider getHeadersProvider(EditPart editPart) {
    // find policy that implements IHeadersProvider
    for (EditPolicy editPolicy : editPart.getEditPolicies()) {
      if (editPolicy.isActive() && editPolicy instanceof IHeadersProvider) {
        return (IHeadersProvider) editPolicy;
      }
    }
    // not found
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IHeaderMenuProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public void buildContextMenu(IMenuManager manager) {
    if (m_headersProvider != null) {
      m_headersProvider.buildContextMenu(manager, m_horizontal);
    }
  }
}
