/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.internal.gef.tree;

import org.eclipse.wb.gef.tree.TreeEditPart;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;
import org.eclipse.wb.internal.gef.core.AbstractEditPartViewer;
import org.eclipse.wb.internal.gef.core.EditDomain;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author lobas_av
 * @coverage gef.tree
 */
public class TreeViewer extends AbstractEditPartViewer {
	private final Tree m_tree;
	private final RootEditPart m_rootEditPart;
	private final TreeEventManager m_eventManager;

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructors
	//
	////////////////////////////////////////////////////////////////////////////
	public TreeViewer(Composite parent, int style) {
		this(new Tree(parent, style));
	}

	public TreeViewer(Tree tree) {
		m_tree = tree;
		// handle SWT events
		m_eventManager = new TreeEventManager(m_tree, this);
		// create root EditPart
		m_rootEditPart = new RootEditPart();
		m_rootEditPart.setViewer(this);
		m_rootEditPart.activate();
		setRootEditPart(m_rootEditPart);
		// handle selection events
		synchronizeSelection();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link Tree} control for this viewer.
	 */
	public Tree getTree() {
		return m_tree;
	}

	/**
	 * Returns the SWT <code>Control</code> for this viewer.
	 */
	@Override
	public Control getControl() {
		return m_tree;
	}

	/**
	 * @return viewer horizontal scroll offset.
	 */
	@Override
	public int getHOffset() {
		return 0;
	}

	/**
	 * @return viewer vertical scroll offset.
	 */
	@Override
	public int getVOffset() {
		return 0;
	}

	/**
	 * Returns root {@link EditPart}.
	 */
	@Override
	public RootEditPart getRootEditPart() {
		return m_rootEditPart;
	}

	/**
	 * Sets the <code>{@link EditDomain}</code> for this viewer. The Viewer will route all mouse and
	 * keyboard events to the {@link EditDomain}.
	 */
	@Override
	public void setEditDomain(EditDomain domain) {
		super.setEditDomain(domain);
		m_eventManager.setDomain(domain);
	}

	/**
	 * Set the Cursor.
	 */
	@Override
	public void setCursor(Cursor cursor) {
		m_tree.setCursor(cursor);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Expansion
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Collapses all items in underlying {@link Tree}.
	 */
	public void collapseAll() {
		UiUtils.collapseAll(m_tree);
	}

	/**
	 * Expands all items in underlying {@link Tree}.
	 */
	public void expandAll() {
		UiUtils.expandAll(m_tree);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Selection
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Adds listeners for synchronizing selection between this {@link TreeViewer} and underlying
	 * {@link Tree} widget.
	 */
	private void synchronizeSelection() {
		final boolean[] inTreeSelectionListener = new boolean[1];
		// listener for Tree widget selection
		m_tree.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// prepare selected EditPart's
				EditPart[] selection;
				{
					TreeItem[] items = m_tree.getSelection();
					selection = new EditPart[items.length];
					for (int i = 0; i < selection.length; i++) {
						selection[i] = (EditPart) items[i].getData();
					}
				}
				// set selection in viewer
				try {
					inTreeSelectionListener[0] = true;
					setSelection(new StructuredSelection(selection));
				} finally {
					inTreeSelectionListener[0] = false;
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		// listener for this viewer selection
		addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (!inTreeSelectionListener[0]) {
					setSelectionToTreeWidget();
				}
			}
		});
	}

	/**
	 * Applies existing selection from this {@link TreeViewer} to underlying {@link Tree} widget.
	 */
	public void setSelectionToTreeWidget() {
		// prepare selected TreeItem's
		List<TreeItem> treeItems = new ArrayList<>();
		for (EditPart editPart : getSelectedEditParts()) {
			TreeEditPart treeEditPart = (TreeEditPart) editPart;
			treeItems.add(treeEditPart.getWidget());
		}
		// set selection in tree
		m_tree.setSelection(treeItems.toArray(new TreeItem[treeItems.size()]));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Finding
	//
	////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns <code>null</code> or the <code>{@link EditPart}</code> at the specified location, using
	 * the given exclusion set and conditional.
	 */
	@Override
	public EditPart findObjectAtExcluding(Point location,
			Collection<IFigure> exclude,
			Conditional conditional) {
		// simple check location
		Rectangle clientArea = m_tree.getClientArea();
		if (location.x < 0 || location.y < 0 || location.x > clientArea.width || location.y > clientArea.height) {
			return null;
		}
		// find EditPart
		EditPart result = null;
		TreeItem item = m_tree.getItem(new org.eclipse.swt.graphics.Point(location.x, location.y));
		if (item == null) {
			result = m_rootEditPart;
		} else {
			result = (EditPart) item.getData();
		}
		// apply conditional
		while (result != null) {
			if (conditional == null || conditional.evaluate(result)) {
				return result;
			}
			result = result.getParent();
		}
		return null;
	}

	@Override
	public EditPart findObjectAtExcluding(Point location,
			Collection<IFigure> exclude,
			Conditional conditional,
			String layer) {
		return null;
	}
}