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
package org.eclipse.wb.gef.core;

import org.eclipse.wb.draw2d.Figure;
import org.eclipse.wb.draw2d.Layer;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.gef.core.events.IEditPartClickListener;
import org.eclipse.wb.gef.graphical.handles.Handle;
import org.eclipse.wb.internal.draw2d.IRootFigure;
import org.eclipse.wb.internal.gef.core.EditDomain;
import org.eclipse.wb.internal.gef.core.IRootContainer;

import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

import java.util.Collection;
import java.util.List;

/**
 * @author lobas_av
 * @coverage gef.core
 */
public interface IEditPartViewer extends ISelectionProvider {
  /**
   * The layer directly below {@link #PRIMARY_LAYER}.
   */
  String PRIMARY_LAYER_SUB_1 = "Primary Layer Sub 1";
  /**
   * Identifies the layer containing the primary pieces of the application.
   */
  String PRIMARY_LAYER = "Primary Layer";
  /**
   * The layer directly below {@link #HANDLE_LAYER}.
   */
  String HANDLE_LAYER_SUB_1 = "Handle Layer Sub 1";
  /**
   * The layer directly below {@link #HANDLE_LAYER}.
   */
  String HANDLE_LAYER_SUB_2 = "Handle Layer Sub 2";
  /**
   * Identifies the layer containing handles, which are typically editing decorations that appear on
   * top of any model representations.
   */
  String HANDLE_LAYER = "Handle Layer";
  /**
   * The layer directly below {@link #HANDLE_LAYER}.
   */
  String HANDLE_LAYER_STATIC = "Handle Layer Static";
  /**
   * The layer directly above {@link #FEEDBACK_LAYER}.
   */
  String FEEDBACK_LAYER_ABV_1 = "Feedback Layer Abv 1";
  /**
   * The layer directly below {@link #FEEDBACK_LAYER}.
   */
  String FEEDBACK_LAYER_SUB_1 = "Feedback Layer Sub 1";
  /**
   * The layer directly below {@link #FEEDBACK_LAYER}.
   */
  String FEEDBACK_LAYER_SUB_2 = "Feedback Layer Sub 2";
  /**
   * The layer containing feedback, which generally temporary visuals that appear on top of all
   * other visuals.
   */
  String FEEDBACK_LAYER = "Feedback Layer";
  /**
   * The layer containing feedback figures that can accept clicks.
   */
  String CLICKABLE_LAYER = "Clickable Layer";
  /**
   * Identifies the layer containing the menu.
   */
  String MENU_PRIMARY_LAYER = "Menu Primary Layer";
  /**
   * The special layer containing {@link Handle}'s for menu.
   */
  String MENU_HANDLE_LAYER = "Menu Handle Layer";
  /**
   * The special layer containing static {@link Handle}'s for menu.
   */
  String MENU_HANDLE_LAYER_STATIC = "Menu Handle Layer static";
  /**
   * The special layer containing feedback for menu.
   */
  String MENU_FEEDBACK_LAYER = "Menu Feedback Layer";
  /**
   * The most top layer.
   */
  String TOP_LAYER = "Top Layer";

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns the SWT <code>Control</code> for this viewer.
   */
  Control getControl();

  /**
   * @return viewer horizontal scroll offset.
   */
  int getHOffset();

  /**
   * @return viewer vertical scroll offset.
   */
  int getVOffset();

  /**
   * Returns the {@link IRootContainer}.
   */
  IRootContainer getRootContainer();

  /**
   * Returns root {@link EditPart}.
   */
  EditPart getRootEditPart();

  /**
   * Returns root {@link Figure} use for access to {@link Layer}'s.
   */
  IRootFigure getRootFigure();

  /**
   * Returns the layer identified by the <code>name</code> given in the input.
   */
  Layer getLayer(String name);

  /**
   * Get factory for creating new EditParts.
   */
  IEditPartFactory getEditPartFactory();

  /**
   * Register given {@link EditPart} into this viewer.
   */
  void registerEditPart(EditPart editPart);

  /**
   * Unregister given {@link EditPart} into this viewer.
   */
  void unregisterEditPart(EditPart editPart);

  /**
   * Returns {@link EditPart} register into this viewer associate given model.
   */
  EditPart getEditPartByModel(Object model);

  /**
   * Returns the {@link EditDomain EditDomain} to which this viewer belongs.
   */
  EditDomain getEditDomain();

  /**
   * Set the Cursor.
   */
  void setCursor(Cursor cursor);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Context menu
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the context {@link MenuManager} for this viewer.
   */
  MenuManager getContextMenu();

  /**
   * Sets the context {@link MenuManager} for this viewer. The {@link MenuManager} will be asked to
   * create a {@link Menu}, which will be used as the context menu for this viewer's control.
   */
  void setContextMenu(MenuManager manager);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Selection
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Appends the specified <code>{@link EditPart}</code> to the viewer's <i>selection</i>. The
   * {@link EditPart} becomes the new primary selection.
   */
  void appendSelection(EditPart part);

  /**
   * Replaces the current selection with the specified <code>{@link EditPart EditParts}</code>.
   */
  void setSelection(List<EditPart> editParts);

  /**
   * Replaces the current selection with the specified <code>{@link EditPart}</code>. That part
   * becomes the primary selection.
   */
  void select(EditPart part);

  /**
   * Removes the specified <code>{@link EditPart}</code> from the current selection. The last
   * EditPart in the new selection is made {@link EditPart#SELECTED_PRIMARY primary}.
   */
  void deselect(EditPart part);

  /**
   * Removes the specified <code>{@link List}</code> of <code>{@link EditPart}</code>'s from the
   * current selection. The last EditPart in the new selection is made
   * {@link EditPart#SELECTED_PRIMARY primary}.
   */
  void deselect(List<EditPart> editParts);

  /**
   * Deselects all EditParts.
   */
  void deselectAll();

  /**
   * Returns an unmodifiable <code>List</code> containing zero or more selected editparts. This list
   * may be empty. This list can be modified indirectly by calling other methods on the viewer.
   */
  List<EditPart> getSelectedEditParts();

  /**
   * Returns the {@link EditPart} which is being selected during selection listeners firing. IOW,
   * this will allow to track where the selection goes during deselecting another EditPart. After
   * all selection listeners fired this method returns <code>null</code>.
   */
  EditPart getSelectingEditPart();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Click
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds a listener on click {@link EditPart}.
   */
  void addEditPartClickListener(IEditPartClickListener listener);

  /**
   * Removes a listener on click {@link EditPart}.
   */
  void removeEditPartClickListener(IEditPartClickListener listener);

  /**
   * Notify listeners on click given {@link EditPart}.
   */
  void fireEditPartClick(EditPart editPart);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Finding
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Returns <code>null</code> or the <code>{@link EditPart}</code> at the specified location, using
   * the given exclusion set and conditional.
   */
  EditPart findTargetEditPart(int x,
      int y,
      final Collection<EditPart> exclude,
      final IConditional conditional);

  /**
   * Returns <code>null</code> or the <code>{@link EditPart}</code> at the specified location on
   * specified given layer, using the given exclusion set and conditional.
   */
  EditPart findTargetEditPart(int x,
      int y,
      final Collection<EditPart> exclude,
      final IConditional conditional,
      String layer);

  /**
   * @return the <code>{@link Handle}</code> at the specified location.
   */
  Handle findTargetHandle(Point location);

  /**
   * Returns the <code>{@link Handle}</code> at the specified location <code>(x, y)</code>. Returns
   * <code>null</code> if no handle exists at the given location <code>(x, y)</code>.
   */
  Handle findTargetHandle(int x, int y);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Finding Inner class
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * An object which evaluates an {@link EditPart} for an arbitrary property. Conditionals are used
   * when querying a viewer for an {@link EditPart}.
   */
  interface IConditional {
    /**
     * Returns <code>true</code> if the editPart meets this condition.
     */
    boolean evaluate(EditPart editPart);
  }
}