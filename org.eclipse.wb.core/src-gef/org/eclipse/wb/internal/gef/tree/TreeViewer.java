/*******************************************************************************
 * Copyright (c) 2011, 2026 Google, Inc. and others.
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

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.TreeEditPart;
import org.eclipse.gef.editparts.RootTreeEditPart;
import org.eclipse.gef.ui.parts.AbstractEditPartViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
	private TreeEventManager m_eventManager;

	public TreeViewer() {
		// create root EditPart
		RootTreeEditPart m_rootEditPart = new RootTreeEditPart();
		setRootEditPart(m_rootEditPart);
	}

	@Override
	public Control createControl(Composite parent) {
		// create widget
		Tree tree = new Tree(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		setControl(tree);
		return tree;
	}

	@Override
	protected void hookControl() {
		if (getControl() == null) {
			return;
		}
		// handle SWT events
		m_eventManager = new TreeEventManager(getControl(), this);
		// handle selection events
		synchronizeSelection();
		((RootTreeEditPart) getRootEditPart()).setWidget(getControl());
		super.hookControl();
	}

	@Override
	protected void unhookControl() {
		super.unhookControl();
		((RootTreeEditPart) getRootEditPart()).setWidget(null);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Access
	//
	////////////////////////////////////////////////////////////////////////////

	@Override
	public Tree getControl() {
		return (Tree) super.getControl();
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
		getControl().addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// prepare selected EditPart's
				EditPart[] selection;
				{
					TreeItem[] items = getControl().getSelection();
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
		addSelectionChangedListener(event -> {
			if (!inTreeSelectionListener[0]) {
				setSelectionToTreeWidget();
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
			treeItems.add((TreeItem) treeEditPart.getWidget());
		}
		// set selection in tree
		getControl().setSelection(treeItems.toArray(new TreeItem[treeItems.size()]));
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
			// TODO Draw2D - Typify once lower bound is 3.22
			@SuppressWarnings("rawtypes") Collection exclude,
			Conditional conditional) {
		// simple check location
		Rectangle clientArea = getControl().getClientArea();
		if (location.x < 0 || location.y < 0 || location.x > clientArea.width || location.y > clientArea.height) {
			return null;
		}
		// find EditPart
		EditPart result = null;
		TreeItem item = getControl().getItem(new org.eclipse.swt.graphics.Point(location.x, location.y));
		if (item == null) {
			result = getRootEditPart();
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
}