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
package org.eclipse.wb.internal.core.databinding.model;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.core.databinding.ui.editor.IPageListener;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.filter.PropertyFilter;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.ToolBar;

import java.util.List;

/**
 * Independent provider from any bindings API.
 *
 * @author lobas_av
 * @coverage bindings.model
 */
public interface IDatabindingsProvider {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Bindings
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Configure binding {@link TableViewer}: add columns, set label provider and etc.
   */
  void configureBindingViewer(IDialogSettings settings, TableViewer viewer);

  /**
   * @return list of all {@link IBindingInfo} for current compilation unit.
   */
  List<IBindingInfo> getBindings();

  /**
   * @return text for visual presentation of given {@link IBindingInfo} object.
   */
  String getBindingPresentationText(IBindingInfo binding) throws Exception;

  /**
   * Show source code for given {@link IBindingInfo}.
   */
  void gotoDefinition(IBindingInfo binding);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Types
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return list of all {@link ObserveType} for current Databindings API.
   */
  List<ObserveType> getTypes();

  /**
   * @return the start showing {@link ObserveType} page for "target".
   */
  ObserveType getTargetStartType();

  /**
   * @return the start showing {@link ObserveType} page for "model".
   */
  ObserveType getModelStartType();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Observes
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * XXX
   */
  IBaseLabelProvider createPropertiesViewerLabelProvider(TreeViewer viewer);

  /**
   * @return list of all {@link PropertyFilter} for property based {@link IObserveInfo}.
   */
  List<PropertyFilter> getObservePropertyFilters();

  /**
   * @return list of all {@link IObserveInfo} with given {@link ObserveType} for current compilation
   *         unit.
   */
  List<IObserveInfo> getObserves(ObserveType type);

  /**
   * Update all observes (fields, JavaInfo, etc.) maybe more elements is added or removed.
   */
  void synchronizeObserves() throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  List<IUiContentProvider> getContentProviders(IBindingInfo binding, IPageListener listener)
      throws Exception;

  /**
   * @return <code>true</code> if given set of objects "target" + "property" and "model" +
   *         "property" accept union to binding otherwise <code>false</code>.
   */
  boolean validate(IObserveInfo target,
      IObserveInfo targetProperty,
      IObserveInfo model,
      IObserveInfo modelProperty) throws Exception;

  /**
   * Create {@link IBindingInfo} for given set of objects "target" + "property" and "model" +
   * "property".
   */
  IBindingInfo createBinding(IObserveInfo target,
      IObserveInfo targetProperty,
      IObserveInfo model,
      IObserveInfo modelProperty) throws Exception;

  /**
   * Add new {@link IBindingInfo} object.
   */
  void addBinding(IBindingInfo binding);

  /**
   * Handle edit given {@link IBindingInfo} object.
   */
  void editBinding(IBindingInfo binding);

  /**
   * Delete given {@link IBindingInfo} object.
   */
  void deleteBinding(IBindingInfo binding);

  /**
   * Delete all bindings.
   */
  void deleteAllBindings();

  /**
   * XXX
   */
  void deleteBindings(JavaInfo javaInfo) throws Exception;

  /**
   * XXX
   */
  boolean canMoveBinding(IBindingInfo binding, int targetIndex, boolean upDown);

  /**
   * XXX
   */
  void moveBinding(IBindingInfo binding, int sourceIndex, int targetIndex, boolean upDown);

  /**
   * XXX
   */
  void setBindingPage(Object bindingPage);

  /**
   * XXX
   */
  void refreshDesigner();

  /**
   * XXX
   */
  void fillExternalBindingActions(ToolBar toolBar, Menu contextMenu);

  /**
   * XXX
   */
  void saveEdit();
}