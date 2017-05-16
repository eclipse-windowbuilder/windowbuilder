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
package org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassAndPropertiesConfiguration.IPropertiesFilter;

import org.eclipse.jface.viewers.IBaseLabelProvider;

import java.util.List;

/**
 * Configuration for {@link ChooseClassAndPropertiesUiContentProvider}.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public class ChooseClassAndPropertiesConfiguration extends ChooseClassConfiguration {
  private String m_propertiesLabel;
  private boolean m_propertiesMultiChecked;
  private boolean m_showSelectionButtons = true;
  private boolean m_reorderMode;
  private IBaseLabelProvider m_propertiesLabelProvider;
  private LoadedPropertiesCheckedStrategy m_loadedPropertiesCheckedStrategy =
      LoadedPropertiesCheckedStrategy.First;
  private String m_propertiesErrorMessage;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public ChooseClassAndPropertiesConfiguration() {
  }

  public ChooseClassAndPropertiesConfiguration(ChooseClassAndPropertiesConfiguration configuration) {
    super(configuration);
    m_propertiesLabel = configuration.m_propertiesLabel;
    m_propertiesMultiChecked = configuration.m_propertiesMultiChecked;
    m_reorderMode = configuration.m_reorderMode;
    m_propertiesLabelProvider = configuration.m_propertiesLabelProvider;
    m_loadedPropertiesCheckedStrategy = configuration.m_loadedPropertiesCheckedStrategy;
    m_propertiesErrorMessage = configuration.m_propertiesErrorMessage;
    m_propertiesFilters.addAll(configuration.m_propertiesFilters);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties style settings
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the label for properties viewer.
   */
  public final String getPropertiesLabel() {
    return m_propertiesLabel;
  }

  /**
   * Sets the label for properties viewer.
   */
  public final void setPropertiesLabel(String propertiesLabel) {
    m_propertiesLabel = propertiesLabel;
  }

  /**
   * @return on/off state for single or multi checked properties. Default value is
   *         <code>false</code>.
   */
  public final boolean isPropertiesMultiChecked() {
    return m_propertiesMultiChecked;
  }

  /**
   * Sets on/off state for single or multi checked properties.
   */
  public final void setPropertiesMultiChecked(boolean multiChecked) {
    m_propertiesMultiChecked = multiChecked;
  }

  /**
   * @return on/off state for reorder properties.
   */
  public final boolean isReorderMode() {
    return m_reorderMode;
  }

  /**
   * Sets on/off state for reorder properties over DND and "up/down" buttons. If sets
   * <code>false</code> DND and "up/down" buttons don't install.
   */
  public final void setReorderMode(boolean reorderMode) {
    m_reorderMode = reorderMode;
  }

  /**
   * @return on/off state for "Select All" and "Deselect All" buttons.
   */
  public boolean isShowSelectionButtons() {
    return m_showSelectionButtons;
  }

  /**
   * Sets on/off state for "Select All" and "Deselect All" buttons.
   */
  public void setShowSelectionButtons(boolean showSelectionButtons) {
    m_showSelectionButtons = showSelectionButtons;
  }

  /**
   * @return {@link IBaseLabelProvider} for properties viewer.
   */
  public final IBaseLabelProvider getPropertiesLabelProvider() {
    return m_propertiesLabelProvider;
  }

  /**
   * Sets {@link IBaseLabelProvider} for properties viewer.
   */
  public final void setPropertiesLabelProvider(IBaseLabelProvider labelProvider) {
    m_propertiesLabelProvider = labelProvider;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // CheckedStrategy settings
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return {@link LoadedPropertiesCheckedStrategy} strategy for checked properties after loading
   *         class and properties.
   */
  public LoadedPropertiesCheckedStrategy getLoadedPropertiesCheckedStrategy() {
    return m_loadedPropertiesCheckedStrategy;
  }

  /**
   * Sets {@link LoadedPropertiesCheckedStrategy} strategy for checked properties after loading
   * class and properties.
   */
  public void setLoadedPropertiesCheckedStrategy(LoadedPropertiesCheckedStrategy checkedStrategy) {
    m_loadedPropertiesCheckedStrategy = checkedStrategy;
  }

  public static enum LoadedPropertiesCheckedStrategy {
    /**
     * Sets empty checked elements.
     */
    None,
    /**
     * Checked first element.
     */
    First,
    /**
     * Checked last element.
     */
    Last,
    /**
     * Checked all elements.
     */
    All
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Properties filter settings
  //
  ////////////////////////////////////////////////////////////////////////////
  private final List<IPropertiesFilter> m_propertiesFilters = Lists.newArrayList();

  /**
   * Add properties filter.
   */
  public final void addPropertiesFilter(IPropertiesFilter propertiesFilter) {
    m_propertiesFilters.add(propertiesFilter);
  }

  /**
   * Remove properties filter.
   */
  public final void removePropertiesFilter(IPropertiesFilter propertiesFilter) {
    m_propertiesFilters.remove(propertiesFilter);
  }

  /**
   * Filter given properties (f.e. include only collection properties). Use
   * {@link #setPropertiesFilter(IPropertiesFilter)} for set filter or override this method.
   */
  public List<PropertyAdapter> filterProperties(Class<?> choosenClass,
      List<PropertyAdapter> properties) throws Exception {
    if (!m_propertiesFilters.isEmpty()) {
      for (IPropertiesFilter filter : m_propertiesFilters) {
        properties = filter.filterProperties(choosenClass, properties);
      }
    }
    return properties;
  }

  public static interface IPropertiesFilter {
    List<PropertyAdapter> filterProperties(Class<?> choosenClass, List<PropertyAdapter> properties)
        throws Exception;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Error settings
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the error message if properties not checked.
   */
  public final String getPropertiesErrorMessage() {
    return m_propertiesErrorMessage;
  }

  /**
   * Sets the error message if properties not checked.
   */
  public final void setPropertiesErrorMessage(String errorMessage) {
    m_propertiesErrorMessage = errorMessage;
  }
}