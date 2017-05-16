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
package org.eclipse.wb.internal.core.databinding.wizards.autobindings;

import org.eclipse.wb.internal.core.databinding.ui.editor.ICompleteListener;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassAndPropertiesConfiguration;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.PropertyAdapter;

import org.eclipse.jdt.ui.wizards.NewTypeWizardPage.ImportsManager;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Composite;

import java.io.InputStream;
import java.util.List;

/**
 * Independent provider from any bindings API.
 *
 * @author lobas_av
 * @coverage bindings.wizard.auto
 */
public interface IAutomaticDatabindingProvider {
  ////////////////////////////////////////////////////////////////////////////
  //
  // SuperClass
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return all super classes for new type.
   */
  String[] getSuperClasses();

  /**
   * @return super classes for initial checked.
   */
  String getInitialSuperClass();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Finish
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@link InputStream} with template for given super class.
   */
  InputStream getTemplateFile(String superClassName);

  /**
   * Generate code.
   *
   * @return the source code with replace template patterns on life values.
   */
  String performSubstitutions(String code, ImportsManager imports) throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Wizard Page
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Sets initial wizard page objects.
   */
  void setCurrentWizardData(AutomaticDatabindingFirstPage firstPage, ICompleteListener pageListener);

  /**
   * Load class with given name. This operation is toolkit dependents and not exist universal way.
   */
  Class<?> loadClass(String className) throws ClassNotFoundException;

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Configure properties {@link CheckboxTableViewer}: add columns, set label provider and etc.
   */
  void configurePropertiesViewer(CheckboxTableViewer viewer);

  /**
   * Add additional settings (value scope, label provider and etc.) to
   * {@link ChooseClassAndPropertiesConfiguration}.
   */
  void configure(ChooseClassAndPropertiesConfiguration configuration);

  /**
   * Create bindings API depended widgets.
   */
  void fillWidgetComposite(Composite widgetComposite);

  /**
   * Handle add/remove property.
   */
  void handlePropertyChecked(PropertyAdapter property, boolean checked);

  /**
   * Initial state for hide/show properties filter.
   */
  boolean getPropertiesViewerFilterInitState();

  /**
   * @return {@link ViewerFilter} for hide/show the properties which can't be bound.
   */
  ViewerFilter getPropertiesViewerFilter();

  /**
   * @return list with properties for given class.
   */
  List<PropertyAdapter> getProperties(Class<?> choosenClass) throws Exception;

  /**
   * Calculate finish state.
   *
   * @return the error message or <code>null</code> otherwise.
   */
  String calculateFinish();
}