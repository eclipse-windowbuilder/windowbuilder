/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *    Marcel du Preez - Nullcheck added to setSelection method
 *******************************************************************************/
package org.eclipse.wb.internal.gef.core;

import org.eclipse.wb.gef.core.IEditPartFactory;
import org.eclipse.wb.gef.core.IEditPartViewer;

import org.eclipse.core.runtime.Assert;
import org.eclipse.draw2d.EventListenerList;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.RootEditPart;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author lobas_av
 * @author mitin_aa
 * @coverage gef.core
 */
public abstract class AbstractEditPartViewer extends org.eclipse.gef.ui.parts.AbstractEditPartViewer implements IEditPartViewer {
	private EditDomain m_domain;
	private IEditPartFactory m_factory;
	private MenuManager m_contextMenu;
	private List<EditPart> m_selectionList = new ArrayList<>();
	private EventListenerList m_eventTable;
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
	 * Get factory for creating new EditParts.
	 */
	@Override
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
	 * Returns the {@link EditDomain EditDomain} to which this viewer belongs.
	 */
	@Override
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
		RootEditPart rootEditPart = getRootEditPart();
		EditPart contentEditPart = m_factory.createEditPart(rootEditPart, model);
		rootEditPart.setContents(contentEditPart);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Context menu
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public MenuManager getContextMenu() {
		return m_contextMenu;
	}

	@Override
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
	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		getEnsureEventTable().addListener(ISelectionChangedListener.class, listener);
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		getEnsureEventTable().removeListener(ISelectionChangedListener.class, listener);
	}

	/**
	 * Returns an {@link ISelection} containing a list of one or more {@link EditPart}. Whenever
	 * {@link #getSelectedEditParts()} returns an empty list, the <i>contents</i> editpart is returned
	 * as the current selection.
	 */
	@Override
	public ISelection getSelection() {
		if (m_selectionList.isEmpty()) {
			EditPart content = getRootEditPart().getContents();
			if (content != null) {
				return new StructuredSelection(content);
			}
		}
		return new StructuredSelection(m_selectionList);
	}

	@Override
	protected void fireSelectionChanged() {
		Iterator<ISelectionChangedListener> listeners = getListeners(ISelectionChangedListener.class);
		if (listeners != null) {
			SelectionChangedEvent event = new SelectionChangedEvent(this, getSelection());
			listeners.forEachRemaining(listener -> listener.selectionChanged(event));
		}
	}

	/**
	 * Appends the specified <code>{@link EditPart}</code> to the viewer's <i>selection</i>. The
	 * {@link EditPart} becomes the new primary selection.
	 */
	@Override
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
	@Override
	public void setSelection(ISelection selection) {
		try {
			@SuppressWarnings("unchecked")
			List<EditPart> editParts = ((StructuredSelection) selection).toList();
			if (!editParts.isEmpty()) {
				m_selecting = editParts.get(0);
			}
			internalDeselectAll();
			//
			for (Iterator<EditPart> I = editParts.iterator(); I.hasNext();) {
				EditPart part = I.next();
				if(part != null) {
					m_selectionList.add(part);
					m_selecting = part;
					if (I.hasNext()) {
						part.setSelected(EditPart.SELECTED);
					} else {
						part.setSelected(EditPart.SELECTED_PRIMARY);
					}
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
	@Override
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
	@Override
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
	@Override
	public void deselect(List<? extends EditPart> editParts) {
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
	@Override
	public void deselectAll() {
		internalDeselectAll();
		fireSelectionChanged();
	}

	private void internalDeselectAll() {
		List<EditPart> selectionList = m_selectionList;
		m_selectionList = new ArrayList<>();
		for (EditPart part : selectionList) {
			part.setSelected(EditPart.SELECTED_NONE);
		}
	}

	/**
	 * Returns an unmodifiable <code>List</code> containing zero or more selected {@link EditPart}'s.
	 * This list may be empty. This list can be modified indirectly by calling other methods on the
	 * viewer.
	 */
	@Override
	public List<? extends EditPart> getSelectedEditParts() {
		return m_selectionList;
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Events
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Access to <code>{@link EventListenerList}</code> use lazy creation mechanism.
	 */
	private EventListenerList getEnsureEventTable() {
		if (m_eventTable == null) {
			m_eventTable = new EventListenerList();
		}
		return m_eventTable;
	}

	/**
	 * Return all registers listeners for given class or <code>null</code>.
	 */
	private <T extends Object> Iterator<T> getListeners(Class<T> listenerClass) {
		return m_eventTable == null ? null : m_eventTable.getListeners(listenerClass);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// GEF
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public org.eclipse.wb.gef.core.EditPart findObjectAtExcluding(Point location, Collection<IFigure> exclusionSet, Conditional conditional) {
		return null;
	}

	@Override
	public Control createControl(Composite parent) {
		return null;
	}
}