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
package org.eclipse.wb.gef.graphical.policies;

import org.eclipse.wb.draw2d.FigureUtils;
import org.eclipse.wb.draw2d.Layer;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.events.IEditPartSelectionListener;
import org.eclipse.wb.gef.core.policies.EditPolicy;
import org.eclipse.wb.gef.core.requests.Request;
import org.eclipse.wb.gef.graphical.handles.Handle;

import java.util.Collections;
import java.util.List;

/**
 * A {@link EditPolicy} that is sensitive to the host's selection.
 * <P>
 * This {@link EditPolicy} adds itself as an {@link IEditPartSelectionListener} so that it can
 * observe selection. When selection or focus changes, the EditPolicy will update itself and call
 * the appropriate methods.
 *
 * @author lobas_av
 * @coverage gef.graphical
 */
public abstract class SelectionEditPolicy extends GraphicalEditPolicy
    implements
      IEditPartSelectionListener {
  private int m_selection = EditPart.SELECTED_NONE;
  private List<Handle> m_staticHandles;
  private List<Handle> m_handles;

  ////////////////////////////////////////////////////////////////////////////
  //
  // EditPolicy
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void activate() {
    super.activate();
    getHost().addSelectionListener(this);
    showStaticHandles();
    selectionChanged(getHost());
  }

  @Override
  public void deactivate() {
    getHost().removeSelectionListener(this);
    selectionChanged(EditPart.SELECTED_NONE);
    hideStaticHandles();
    super.deactivate();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Selection
  //
  ////////////////////////////////////////////////////////////////////////////
  public void selectionChanged(EditPart editPart) {
    selectionChanged(editPart.getSelected());
  }

  private void selectionChanged(int selection) {
    if (m_selection != selection) {
      m_selection = selection;
      //
      if (m_selection == EditPart.SELECTED_NONE) {
        hideSelection();
      } else {
        showSelection();
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handles
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds "static" handles to static_handle layer. This handle's always visible independently of
   * selection.
   */
  protected void showStaticHandles() {
    Layer layer = getLayer(IEditPartViewer.HANDLE_LAYER_STATIC);
    m_staticHandles = createStaticHandles();
    for (Handle handle : m_staticHandles) {
      layer.add(handle);
    }
  }

  /**
   * Removes "static" handles from static_handle layer.
   */
  protected void hideStaticHandles() {
    if (m_staticHandles != null && !m_staticHandles.isEmpty()) {
      Layer layer = getLayer(IEditPartViewer.HANDLE_LAYER_STATIC);
      for (Handle handle : m_staticHandles) {
        layer.remove(handle);
      }
      m_staticHandles = null;
    }
  }

  /**
   * Shows selection. Default implementation adds {@link Handle}s.
   */
  protected void showSelection() {
    hideSelection();
    Layer layer = getLayer(IEditPartViewer.HANDLE_LAYER);
    m_handles = createSelectionHandles();
    for (Handle handle : m_handles) {
      layer.add(handle);
    }
    fire_showSelection();
  }

  /**
   * Hides selection. Default implementation removes {@link Handle}s.
   */
  protected void hideSelection() {
    fire_hideSelection();
    if (m_handles != null) {
      for (Handle handle : m_handles) {
        FigureUtils.removeFigure(handle);
      }
      m_handles = null;
    }
  }

  /**
   * Hides/shows selection, useful when selection presentation should be updated.
   */
  public void refreshSelection() {
    showSelection();
  }

  /**
   * Subclasses must implement to provide the list of "static" handles.
   */
  protected List<Handle> createStaticHandles() {
    return Collections.<Handle>emptyList();
  }

  /**
   * Subclasses must implement to provide the list of handles.
   */
  protected abstract List<Handle> createSelectionHandles();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Request
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean understandsRequest(Request request) {
    return request.getType() == Request.REQ_SELECTION;
  }

  @Override
  public EditPart getTargetEditPart(Request request) {
    return getHost();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Listener
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds {@link ISelectionEditPolicyListener} for listening {@link SelectionEditPolicy} events.
   */
  public void addSelectionPolicyListener(ISelectionEditPolicyListener listener) {
    getEnsureEventTable().addListener(ISelectionEditPolicyListener.class, listener);
  }

  /**
   * Removes {@link ISelectionEditPolicyListener} for listening {@link SelectionEditPolicy} events.
   */
  public void removeSelectionPolicyListener(ISelectionEditPolicyListener listener) {
    getEnsureEventTable().removeListener(ISelectionEditPolicyListener.class, listener);
  }

  /**
   * Notifies {@link ISelectionEditPolicyListener}s that {@link #showSelection()} was executed.
   */
  private void fire_showSelection() {
    List<ISelectionEditPolicyListener> listeners = getListeners(ISelectionEditPolicyListener.class);
    if (listeners != null && !listeners.isEmpty()) {
      for (ISelectionEditPolicyListener listener : listeners) {
        listener.showSelection(this);
      }
    }
  }

  /**
   * Notifies {@link ISelectionEditPolicyListener}s that {@link #hideSelection()} was executed.
   */
  private void fire_hideSelection() {
    List<ISelectionEditPolicyListener> listeners = getListeners(ISelectionEditPolicyListener.class);
    if (listeners != null && !listeners.isEmpty()) {
      for (ISelectionEditPolicyListener listener : listeners) {
        listener.hideSelection(this);
      }
    }
  }
}