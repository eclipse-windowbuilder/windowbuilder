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
package org.eclipse.wb.internal.rcp.databinding.model;

import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassAndPropertiesConfiguration;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassConfiguration;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.PropertyAdapter;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.binding.DataBindManager;
import org.eclipse.wb.internal.rcp.databinding.model.IObservableFactory.Type;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.MapsBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.KnownElementsObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.designer.BeansObservableFactoryInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.designer.TreeBeanAdvisorInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.input.designer.TreeObservableLabelProviderInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.observables.SingleSelectionObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.wizards.autobindings.IAutomaticWizardStub;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.swt.widgets.Composite;

import java.beans.PropertyDescriptor;
import java.util.List;

/**
 * Interface for extend operation with models into sub RCP (EMF and etc.) plugins.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model
 */
public interface IGlobalObservableFactory {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Observable
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@link ObservableInfo} improvement detail observable or <code>null</code> otherwise.
   */
  ObservableInfo createDetailObservable(SingleSelectionObservableInfo masterObservable,
      BindableInfo object,
      Type type) throws Exception;

  /**
   * @return {@link MapsBeanObservableInfo} improvement maps observe or <code>null</code> otherwise.
   */
  MapsBeanObservableInfo createObserveMaps(ObservableInfo inputObservable,
      ObservableInfo domainObservable,
      Class<?> elementType,
      boolean[] useViewerSupport) throws Exception;

  /**
   * @return {@link BeansObservableFactoryInfo} improvement tree observable factory or
   *         <code>null</code> otherwise.
   */
  BeansObservableFactoryInfo createTreeObservableFactory(ObservableInfo inputObservable,
      boolean asList) throws Exception;

  /**
   * @return {@link TreeBeanAdvisorInfo} improvement tree advisor or <code>null</code> otherwise.
   */
  TreeBeanAdvisorInfo createTreeBeanAdvisor(ObservableInfo inputObservable) throws Exception;

  /**
   * @return {@link TreeObservableLabelProviderInfo} improvement tree label provider or
   *         <code>null</code> otherwise.
   */
  TreeObservableLabelProviderInfo createTreeLabelProvider(ObservableInfo inputObservable,
      KnownElementsObservableInfo allElementsObservable) throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // UI
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Configure {@link ChooseClassAndPropertiesConfiguration} for viewer input content provider.
   */
  void configureChooseElementForViewerInput(ObservableInfo inputObservable,
      ChooseClassAndPropertiesConfiguration configuration) throws Exception;

  /**
   * Configure {@link ChooseClassConfiguration} for tree viewer input content provider.
   */
  void configureChooseElementForTreeViewerInput(ObservableInfo inputObservable,
      ChooseClassConfiguration configuration) throws Exception;

  /**
   * Filter given properties (f.e. include only EMF properties).
   */
  void filterElementPropertiesForTreeViewerInput(ObservableInfo inputObservable,
      Class<?> elementType,
      List<PropertyDescriptor> descriptors) throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Automatic Wizard
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Add additional settings (value scope, label provider and etc.) to
   * {@link ChooseClassAndPropertiesConfiguration}.
   */
  void automaticWizardConfigure(ChooseClassAndPropertiesConfiguration configuration);

  /**
   * @return the list of improvement {@link PropertyAdapter} properties for auto wizard or
   *         <code>null</code> otherwise.
   */
  List<PropertyAdapter> automaticWizardGetProperties(IJavaProject javaProject,
      ClassLoader classLoader,
      Class<?> choosenClass) throws Exception;

  /**
   * @return {@link IAutomaticWizardStub} for special configure auto wizard operation or
   *         <code>null</code> otherwise.
   */
  IAutomaticWizardStub automaticWizardCreateStub(IJavaProject javaProject,
      ClassLoader classLoader,
      Class<?> choosenClass) throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Controller
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if do move given {@link IObserveInfo} to controller class or
   *         <code>false</code> otherwise.
   */
  boolean moveBean(IObserveInfo observe,
      AstEditor controllerEditor,
      TypeDeclaration controllerRootNode) throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Preferences
  //
  ////////////////////////////////////////////////////////////////////////////
  void confgureCodeGenerationPreferencePage(Composite parent, DataBindManager bindManager)
      throws Exception;
}