/*******************************************************************************
 * Copyright (c) 2011, 2024 Google, Inc. and others.
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
package org.eclipse.wb.tests.gef;

import org.eclipse.wb.draw2d.Layer;
import org.eclipse.wb.gef.core.IEditPartFactory;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.internal.draw2d.IRootFigure;
import org.eclipse.wb.internal.gef.core.AbstractEditPartViewer;
import org.eclipse.wb.internal.gef.core.EditDomain;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.RootEditPart;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Control;

import java.util.Collection;
import java.util.List;

/**
 * @author lobas_av
 *
 */
public class EmptyEditPartViewer extends AbstractEditPartViewer implements IEditPartViewer {
	private final EditDomain m_editDomain = new EditDomain();

	@Override
	public void appendSelection(EditPart part) {
	}

	@Override
	public void deselect(EditPart part) {
	}

	@Override
	public void deselect(List<? extends EditPart> editParts) {
	}

	@Override
	public void deselectAll() {
	}

	@Override
	public org.eclipse.wb.gef.core.EditPart findTargetEditPart(int x,
			int y,
			Collection<? extends EditPart> exclude,
			Conditional conditional) {
		return null;
	}

	@Override
	public org.eclipse.wb.gef.core.EditPart findTargetEditPart(int x,
			int y,
			Collection<? extends EditPart> exclude,
			Conditional conditional,
			String layer) {
		return null;
	}

	@Override
	public Handle findTargetHandle(Point location) {
		return null;
	}

	@Override
	public Handle findTargetHandle(int x, int y) {
		return null;
	}

	@Override
	public Control getControl() {
		return null;
	}

	@Override
	public EditDomain getEditDomain() {
		return m_editDomain;
	}

	@Override
	public IEditPartFactory getEditPartFactory() {
		return null;
	}

	@Override
	public Layer getLayer(String name) {
		return null;
	}

	@Override
	public IRootFigure getRootFigure() {
		return null;
	}

	@Override
	public List<org.eclipse.wb.gef.core.EditPart> getSelectedEditParts() {
		return null;
	}

	@Override
	public void select(EditPart part) {
	}

	@Override
	public void setCursor(Cursor cursor) {
	}

	@Override
	public MenuManager getContextMenu() {
		return null;
	}

	@Override
	public void setContextMenu(MenuManager manager) {
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
	}

	@Override
	public ISelection getSelection() {
		return null;
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
	}

	@Override
	public void setSelection(ISelection selection) {
	}

	@Override
	public int getHOffset() {
		return 0;
	}

	@Override
	public int getVOffset() {
		return 0;
	}

	@Override
	public RootEditPart getRootEditPart() {
		return null;
	}
}