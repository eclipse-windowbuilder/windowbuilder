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

  @Override
  public void appendSelection(EditPart part) {
  }

  @Override
  public void deselect(EditPart part) {
  }

  @Override
  public void deselect(List<EditPart> editParts) {
  }

  @Override
  public void deselectAll() {
  }

  @Override
  public EditPart findTargetEditPart(int x,
      int y,
      Collection<EditPart> exclude,
      IConditional conditional) {
    return null;
  }

  @Override
  public EditPart findTargetEditPart(int x,
      int y,
      Collection<EditPart> exclude,
      IConditional conditional,
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
  public EditPart getEditPartByModel(Object model) {
    return null;
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
  public IRootContainer getRootContainer() {
    return null;
  }

  @Override
  public IRootFigure getRootFigure() {
    return null;
  }

  @Override
  public List<EditPart> getSelectedEditParts() {
    return null;
  }

  @Override
  public EditPart getSelectingEditPart() {
    return null;
  }

  @Override
  public void registerEditPart(EditPart editPart) {
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
  public void setSelection(List<EditPart> editParts) {
  }

  @Override
  public void unregisterEditPart(EditPart editPart) {
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
  public EditPart getRootEditPart() {
    return null;
  }

  @Override
  public void addEditPartClickListener(IEditPartClickListener listener) {
  }

  @Override
  public void fireEditPartClick(EditPart editPart) {
  }

  @Override
  public void removeEditPartClickListener(IEditPartClickListener listener) {
  }
}