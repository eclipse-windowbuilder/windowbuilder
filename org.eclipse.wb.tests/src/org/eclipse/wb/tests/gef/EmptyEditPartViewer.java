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
package org.eclipse.wb.tests.gef;

import org.eclipse.wb.draw2d.Layer;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.IEditPartFactory;
import org.eclipse.wb.gef.core.IEditPartViewer;
import org.eclipse.wb.gef.core.events.IEditPartClickListener;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.internal.draw2d.IRootFigure;
import org.eclipse.wb.internal.gef.core.EditDomain;
import org.eclipse.wb.internal.gef.core.IRootContainer;

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
public class EmptyEditPartViewer implements IEditPartViewer {
  private final EditDomain m_editDomain = new EditDomain();

  public void appendSelection(EditPart part) {
  }

  public void deselect(EditPart part) {
  }

  public void deselect(List<EditPart> editParts) {
  }

  public void deselectAll() {
  }

  public EditPart findTargetEditPart(int x,
      int y,
      Collection<EditPart> exclude,
      IConditional conditional) {
    return null;
  }

  public EditPart findTargetEditPart(int x,
      int y,
      Collection<EditPart> exclude,
      IConditional conditional,
      String layer) {
    return null;
  }

  public Handle findTargetHandle(Point location) {
    return null;
  }

  public Handle findTargetHandle(int x, int y) {
    return null;
  }

  public Control getControl() {
    return null;
  }

  public EditDomain getEditDomain() {
    return m_editDomain;
  }

  public EditPart getEditPartByModel(Object model) {
    return null;
  }

  public IEditPartFactory getEditPartFactory() {
    return null;
  }

  public Layer getLayer(String name) {
    return null;
  }

  public IRootContainer getRootContainer() {
    return null;
  }

  public IRootFigure getRootFigure() {
    return null;
  }

  public List<EditPart> getSelectedEditParts() {
    return null;
  }

  public EditPart getSelectingEditPart() {
    return null;
  }

  public void registerEditPart(EditPart editPart) {
  }

  public void select(EditPart part) {
  }

  public void setCursor(Cursor cursor) {
  }

  public MenuManager getContextMenu() {
    return null;
  }

  public void setContextMenu(MenuManager manager) {
  }

  public void setSelection(List<EditPart> editParts) {
  }

  public void unregisterEditPart(EditPart editPart) {
  }

  public void addSelectionChangedListener(ISelectionChangedListener listener) {
  }

  public ISelection getSelection() {
    return null;
  }

  public void removeSelectionChangedListener(ISelectionChangedListener listener) {
  }

  public void setSelection(ISelection selection) {
  }

  public int getHOffset() {
    return 0;
  }

  public int getVOffset() {
    return 0;
  }

  public EditPart getRootEditPart() {
    return null;
  }

  public void addEditPartClickListener(IEditPartClickListener listener) {
  }

  public void fireEditPartClick(EditPart editPart) {
  }

  public void removeEditPartClickListener(IEditPartClickListener listener) {
  }
}