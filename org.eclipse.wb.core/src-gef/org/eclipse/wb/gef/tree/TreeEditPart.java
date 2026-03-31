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
package org.eclipse.wb.gef.tree;

import org.eclipse.wb.gef.core.tools.Tool;
import org.eclipse.wb.internal.gef.tree.tools.DoubleClickEditPartTracker;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

import java.util.List;

/**
 * @author lobas_av
 * @coverage gef.tree
 */
public abstract class TreeEditPart extends org.eclipse.wb.gef.core.EditPart implements org.eclipse.gef.TreeEditPart {
	private TreeItem m_widget;
	private boolean m_expandedShouldRestore;
	private boolean m_expanded;

	////////////////////////////////////////////////////////////////////////////
	//
	// Widget
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public TreeItem getWidget() {
		return m_widget;
	}

	@Override
	public void setWidget(Widget widget) {
		m_widget = (TreeItem) widget;
		//
		List<? extends EditPart> children = getChildren();
		if (m_widget == null) {
			for (EditPart editPart : children) {
				TreeEditPart treePart = (TreeEditPart) editPart;
				treePart.setWidget(null);
			}
		} else {
			m_widget.setData(this);
			m_widget.addDisposeListener(new DisposeListener() {
				@Override
				public void widgetDisposed(DisposeEvent e) {
					m_expandedShouldRestore = true;
					m_expanded = m_widget.getExpanded();
				}
			});
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// EditPart
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void addChildVisual(EditPart childPart, int index) {
		TreeEditPart treePart = (TreeEditPart) childPart;
		treePart.setWidget(new TreeItem(getWidget(), SWT.NONE, index));
	}

	@Override
	protected void removeChildVisual(EditPart childPart) {
		TreeEditPart treePart = (TreeEditPart) childPart;
		if (treePart.getWidget() != null) {
			treePart.getWidget().dispose();
			treePart.setWidget(null);
		}
	}

	@Override
	protected void updateChildVisual(org.eclipse.wb.gef.core.EditPart childPart, int index) {
		TreeEditPart treePart = (TreeEditPart) childPart;
		if (treePart.getWidget() == null) {
			treePart.setWidget(new TreeItem(getWidget(), SWT.NONE, index));
		}
	}

	@Override
	public void refresh() {
		super.refresh();
		if (m_expandedShouldRestore) {
			m_expandedShouldRestore = false;
			TreeItem widget = getWidget();
			if (widget != null && !widget.isDisposed()) {
				widget.setExpanded(m_expanded);
			}
		}
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Policy
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	protected void createEditPolicies() {
		//installEditPolicy("TreeToolAdapterEditPolicy", new TreeToolAdapterEditPolicy());
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// DragTracking
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	public Tool getDragTracker(Request request) {
		return new DoubleClickEditPartTracker(this);
	}
}