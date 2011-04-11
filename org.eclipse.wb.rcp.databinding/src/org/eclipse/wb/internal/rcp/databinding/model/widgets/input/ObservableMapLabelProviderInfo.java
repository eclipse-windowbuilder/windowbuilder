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
package org.eclipse.wb.internal.rcp.databinding.model.widgets.input;

import org.eclipse.wb.internal.core.databinding.model.AstObjectInfoVisitor;
import org.eclipse.wb.internal.core.databinding.model.CodeGenerationSupport;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassConfiguration;
import org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.Messages;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.MapsBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.ui.contentproviders.SimpleClassUiContentProvider;

import org.eclipse.core.databinding.observable.map.IObservableMap;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Model for {@link org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider}.
 * 
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public final class ObservableMapLabelProviderInfo extends AbstractLabelProviderInfo {
  private static final String MAP_PROVIDER_CLASS =
      "org.eclipse.jface.databinding.viewers.ObservableMapLabelProvider";
  private final MapsBeanObservableInfo m_mapsObservable;
  private AbstractViewerInputBindingInfo m_binding;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public ObservableMapLabelProviderInfo(MapsBeanObservableInfo mapsObservable) {
    this(MAP_PROVIDER_CLASS, mapsObservable);
  }

  public ObservableMapLabelProviderInfo(String className, MapsBeanObservableInfo mapsObservable) {
    super(className);
    m_mapsObservable = mapsObservable;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public MapsBeanObservableInfo getMapsObservable() {
    return m_mapsObservable;
  }

  public void setBinding(AbstractViewerInputBindingInfo binding) {
    m_binding = binding;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Create {@link IUiContentProvider} content providers for edit this model.
   */
  @Override
  public void createContentProviders(List<IUiContentProvider> providers,
      DatabindingsProvider provider,
      boolean useClear) {
    ChooseClassConfiguration configuration = new ChooseClassConfiguration();
    configuration.setDialogFieldLabel(Messages.ObservableMapLabelProviderInfo_title);
    configuration.setValueScope(MAP_PROVIDER_CLASS);
    if (useClear) {
      configuration.setClearValue(MAP_PROVIDER_CLASS);
      configuration.setBaseClassNames(
          MAP_PROVIDER_CLASS,
          "org.eclipse.jface.databinding.viewers.ObservableMapCellLabelProvider");
      configuration.setConstructorsParameters(
          new Class[]{IObservableMap[].class},
          new Class[]{IObservableMap.class});
    } else {
      configuration.setBaseClassName("org.eclipse.jface.viewers.IBaseLabelProvider");
    }
    configuration.setEmptyClassErrorMessage(Messages.ObservableMapLabelProviderInfo_emptyMessage);
    configuration.setErrorMessagePrefix(Messages.ObservableMapLabelProviderInfo_errorPrefix);
    //
    SimpleClassUiContentProvider contentProvider =
        new SimpleClassUiContentProvider(configuration, this);
    contentProvider.getDialogField().setEnabled(m_binding.getCodeSupport() == null);
    providers.add(contentProvider);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getPresentationText() throws Exception {
    return "ObservableMaps[" + StringUtils.join(m_mapsObservable.getProperties(), ", ") + "]";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Code generation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getSourceCode(List<String> lines, CodeGenerationSupport generationSupport)
      throws Exception {
    generationSupport.addSourceCode(m_mapsObservable, lines);
    return "new " + m_className + "(" + m_mapsObservable.getVariableIdentifier() + ")";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visiting
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void accept(AstObjectInfoVisitor visitor) throws Exception {
    super.accept(visitor);
    m_mapsObservable.accept(visitor);
  }
}