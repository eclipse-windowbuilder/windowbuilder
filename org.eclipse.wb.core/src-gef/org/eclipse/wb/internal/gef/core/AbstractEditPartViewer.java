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
package org.eclipse.wb.internal.gef.core;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartFactory;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.events.IEditPartClickListener;
import org.eclipse.wb.internal.draw2d.events.EventTable;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author lobas_av
 * @author mitin_aa
 * @coverage gef.core
 */
public abstract class AbstractEditPartViewer implements IEditPartViewer {
  private/*final*/IRootContainer m_rootEditPart;
  private EditDomain m_domain;
  private IEditPartFactory m_factory;
  private final Map<Object, EditPart> m_modelToEditPart = Maps.newHashMap();
  private MenuManager m_contextMenu;
  private List<EditPart> m_selectionList = Lists.newArrayList();
  private EventTable m_eventTable;
  /**
   * The EditPart which is being selected in selection process.
   */
  private EditPart m_selecting;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns the {@link IRootContainer}.
   */
  public IRootContainer getRootContainer() {
    return m_rootEditPart;
  }

  protected final void setRootEditPart(IRootContainer rootEditPart) {
    Assert.isTrue(m_rootEditPart == null);
    m_rootEditPart = rootEditPart;
  }

  /**
   * Get factory for creating new EditParts.
   */
  public IEditPartFactory getEditPartFactory() {
    return m_factory;
  }

  /**
   * Set factory for creating new EditParts.
   */
  public void setEditPartFactory(IEditPartFactory factory) {
    m_factory = factory;
  }

  /**
   * Register given {@link EditPart} into this viewer.
   */
  public void registerEditPart(EditPart editPart) {
    m_modelToEditPart.put(editPart.getModel(), editPart);
  }

  /**
   * Unregister given {@link EditPart} into this viewer.
   */
  public void unregisterEditPart(EditPart editPart) {
    Object model = editPart.getModel();
    Object registerPart = m_modelToEditPart.get(model);
    /*
     * check editPart because during refreshChildren firstly add new child,
     * example (old model, new EditPart) after remove old child (old model, old EditPart)
     */
    if (registerPart == editPart) {
      m_modelToEditPart.remove(model);
    }
  }

  /**
   * Returns {@link EditPart} register into this viewer associate given model.
   */
  public EditPart getEditPartByModel(Object model) {
    return m_modelToEditPart.get(model);
  }

  /**
   * Returns the {@link EditDomain EditDomain} to which this viewer belongs.
   */
  public EditDomain getEditDomain() {
    return m_domain;
  }

  /**
   * Sets the <code>{@link EditDomain}</code> for this viewer. The Viewer will route all mouse and
   * keyboard events to the {@link EditDomain}.
   */
  public void setEditDomain(EditDomain domain) {
    m_domain = domain;
  }

  /**
   * Set input model for this viewer.
   */
  public void setInput(Object model) {
    EditPart contentEditPart = m_factory.createEditPart((EditPart) m_rootEditPart, model);
    m_rootEditPart.setContent(contentEditPart);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Context menu
  //
  ////////////////////////////////////////////////////////////////////////////
  public MenuManager getContextMenu() {
    return m_contextMenu;
  }

  public void setContextMenu(MenuManager menu) {
    // dispose old menu
    if (m_contextMenu != null && m_contextMenu != menu) {
      m_contextMenu.dispose();
    }
    // remember new
    m_contextMenu = menu;
    // create new menu
    Control control = getControl();
    Menu menuWidget = m_contextMenu.createContextMenu(control);
    if (menuWidget.getShell() == control.getShell()) {
      control.setMenu(menuWidget);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Selection
  //
  ////////////////////////////////////////////////////////////////////////////
  public void addSelectionChangedListener(ISelectionChangedListener listener) {
    getEnsureEventTable().addListener(ISelectionChangedListener.class, listener);
  }

  public void removeSelectionChangedListener(ISelectionChangedListener listener) {
    getEnsureEventTable().removeListener(ISelectionChangedListener.class, listener);
  }

  /**
   * Returns an {@link ISelection} containing a list of one or more {@link EditPart}. Whenever
   * {@link #getSelectedEditParts()} returns an empty list, the <i>contents</i> editpart is returned
   * as the current selection.
   */
  public ISelection getSelection() {
    if (m_selectionList.isEmpty()) {
      EditPart content = m_rootEditPart.getContent();
      if (content != null) {
        return new StructuredSelection(content);
      }
    }
    return new StructuredSelection(m_selectionList);
  }

  @SuppressWarnings("unchecked")
  public void setSelection(ISelection selection) {
    if (selection instanceof IStructuredSelection) {
      IStructuredSelection structuredSelection = (IStructuredSelection) selection;
      setSelection(structuredSelection.toList());
    }
  }

  private void fireSelectionChanged() {
    List<ISelectionChangedListener> listeners = getListeners(ISelectionChangedListener.class);
    if (listeners != null && !listeners.isEmpty()) {
      SelectionChangedEvent event = new SelectionChangedEvent(this, getSelection());
      for (ISelectionChangedListener listener : listeners) {
        listener.selectionChanged(event);
      }
    }
  }

  /**
   * Appends the specified <code>{@link EditPart}</code> to the viewer's <i>selection</i>. The
   * {@link EditPart} becomes the new primary selection.
   */
  public void appendSelection(EditPart part) {
    Assert.isNotNull(part);
    if (!m_selectionList.isEmpty()) {
      EditPart primary = m_selectionList.get(m_selectionList.size() - 1);
      if (primary != part) {
        try {
          m_selecting = part;
          primary.setSelected(EditPart.SELECTED);
        } finally {
          m_selecting = null;
        }
      }
    }
    //
    try {
      m_selecting = part;
      m_selectionList.remove(part);
      m_selectionList.add(part);
      part.setSelected(EditPart.SELECTED_PRIMARY);
    } finally {
      m_selecting = null;
    }
    //
    fireSelectionChanged();
  }

  /**
   * Replaces the current selection with the specified <code>{@link EditPart EditParts}</code>.
   */
  public void setSelection(List<EditPart> editParts) {
    try {
      if (!editParts.isEmpty()) {
        m_selecting = editParts.get(0);
      }
      internalDeselectAll();
      //
      for (Iterator<EditPart> I = editParts.iterator(); I.hasNext();) {
        EditPart part = I.next();
        Assert.isNotNull(part);
        m_selectionList.add(part);
        m_selecting = part;
        if (I.hasNext()) {
          part.setSelected(EditPart.SELECTED);
        } else {
          part.setSelected(EditPart.SELECTED_PRIMARY);
        }
      }
    } finally {
      m_selecting = null;
    }
    //
    fireSelectionChanged();
  }

  /**
   * Replaces the current selection with the specified <code>{@link EditPart}</code>. That part
   * becomes the primary selection.
   */
  public void select(EditPart part) {
    Assert.isNotNull(part);
    if (m_selectionList.size() != 1 || m_selectionList.get(0) != part) {
      try {
        m_selectionList.add(part);
        m_selecting = part;
        internalDeselectAll();
        appendSelection(part);
      } finally {
        m_selecting = null;
      }
    }
  }

  /**
   * Removes the specified <code>{@link EditPart}</code> from the current selection. The last
   * EditPart in the new selection is made {@link EditPart#SELECTED_PRIMARY primary}.
   */
  public void deselect(EditPart part) {
    Assert.isNotNull(part);
    m_selectionList.remove(part);
    part.setSelected(EditPart.SELECTED_NONE);
    //
    if (!m_selectionList.isEmpty()) {
      EditPart primary = m_selectionList.get(m_selectionList.size() - 1);
      primary.setSelected(EditPart.SELECTED_PRIMARY);
    }
    //
    fireSelectionChanged();
  }

  /**
   * Removes the specified <code>{@link List}</code> of <code>{@link EditPart}</code>'s from the
   * current selection. The last EditPart in the new selection is made
   * {@link EditPart#SELECTED_PRIMARY primary}.
   */
  public void deselect(List<EditPart> editParts) {
    for (EditPart part : editParts) {
      Assert.isNotNull(part);
      m_selectionList.remove(part);
      part.setSelected(EditPart.SELECTED_NONE);
    }
    //
    if (!m_selectionList.isEmpty()) {
      EditPart primary = m_selectionList.get(m_selectionList.size() - 1);
      primary.setSelected(EditPart.SELECTED_PRIMARY);
    }
    //
    fireSelectionChanged();
  }

  /**
   * Deselects all EditParts.
   */
  public void deselectAll() {
    internalDeselectAll();
    fireSelectionChanged();
  }

  private void internalDeselectAll() {
    List<EditPart> selectionList = m_selectionList;
    m_selectionList = Lists.newArrayList();
    for (EditPart part : selectionList) {
      part.setSelected(EditPart.SELECTED_NONE);
    }
  }

  /**
   * Returns an unmodifiable <code>List</code> containing zero or more selected {@link EditPart}'s.
   * This list may be empty. This list can be modified indirectly by calling other methods on the
   * viewer.
   */
  public List<EditPart> getSelectedEditParts() {
    return m_selectionList;
  }

  /**
   * @return The EditPart which is being selected in selection process.
   */
  public EditPart getSelectingEditPart() {
    return m_selecting;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Click
  //
  ////////////////////////////////////////////////////////////////////////////
  public void addEditPartClickListener(IEditPartClickListener listener) {
    getEnsureEventTable().addListener(IEditPartClickListener.class, listener);
  }

  public void removeEditPartClickListener(IEditPartClickListener listener) {
    getEnsureEventTable().removeListener(IEditPartClickListener.class, listener);
  }

  public void fireEditPartClick(EditPart editPart) {
    List<IEditPartClickListener> listeners = getListeners(IEditPartClickListener.class);
    if (listeners != null && !listeners.isEmpty()) {
      for (IEditPartClickListener listener : listeners) {
        listener.clickNotify(editPart);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Events
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Access to <code>{@link EventTable}</code> use lazy creation mechanism.
   */
  private EventTable getEnsureEventTable() {
    if (m_eventTable == null) {
      m_eventTable = new EventTable();
    }
    return m_eventTable;
  }

  /**
   * Return all registers listeners for given class or <code>null</code>.
   */
  private <T extends Object> List<T> getListeners(Class<T> listenerClass) {
    return m_eventTable == null ? null : m_eventTable.getListeners(listenerClass);
  }
}