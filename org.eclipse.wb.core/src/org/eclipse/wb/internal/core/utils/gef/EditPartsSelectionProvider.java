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
package org.eclipse.wb.internal.core.utils.gef;

import com.google.common.collect.Lists;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.internal.draw2d.events.EventTable;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;

import java.util.Iterator;
import java.util.List;

/**
 * Implementation of {@link ISelectionProvider} for models of {@link EditPart}'s in GEF
 * {@link IEditPartViewer}.
 *
 * @author scheglov_ke
 * @coverage gef.core
 */
public final class EditPartsSelectionProvider implements ISelectionProvider {
  private final IEditPartViewer m_viewer;
  private final EventTable m_eventTable = new EventTable();
  private final ISelectionChangedListener m_selectionListener = new ISelectionChangedListener() {
    public void selectionChanged(SelectionChangedEvent event) {
      fireSelectionChanged();
    }
  };

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public EditPartsSelectionProvider(IEditPartViewer viewer) {
    m_viewer = viewer;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public ISelection getSelection() {
    List<Object> models = Lists.newArrayList();
    for (EditPart editPart : m_viewer.getSelectedEditParts()) {
      models.add(editPart.getModel());
    }
    return new StructuredSelection(models);
  }

  public void setSelection(ISelection selection) {
    // prepare EditPart's for model selection
    List<EditPart> editParts = Lists.newArrayList();
    for (Iterator<?> I = ((StructuredSelection) selection).iterator(); I.hasNext();) {
      Object model = I.next();
      EditPart editPart = m_viewer.getEditPartByModel(model);
      if (editPart != null) {
        editParts.add(editPart);
      }
    }
    // set selection in viewer
    m_viewer.setSelection(editParts);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Listeners
  //
  ////////////////////////////////////////////////////////////////////////////
  public void addSelectionChangedListener(ISelectionChangedListener listener) {
    m_eventTable.addListener(ISelectionChangedListener.class, listener);
    if (m_eventTable.getListeners(ISelectionChangedListener.class).size() == 1) {
      m_viewer.addSelectionChangedListener(m_selectionListener);
    }
  }

  public void removeSelectionChangedListener(ISelectionChangedListener listener) {
    m_eventTable.removeListener(ISelectionChangedListener.class, listener);
    if (m_eventTable.getListeners(ISelectionChangedListener.class).isEmpty()) {
      m_viewer.removeSelectionChangedListener(m_selectionListener);
    }
  }

  /**
   * Notifies subscribers of this {@link ISelectionProvider}.
   */
  private void fireSelectionChanged() {
    // prepare event
    SelectionChangedEvent event;
    {
      ISelection selection = getSelection();
      event = new SelectionChangedEvent(this, selection);
    }
    // notify subscribers
    for (ISelectionChangedListener listener : m_eventTable.getListeners(ISelectionChangedListener.class)) {
      listener.selectionChanged(event);
    }
  }
}
