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
import org.eclipse.wb.internal.core.databinding.ui.editor.IPageListener;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassAndPropertiesConfiguration;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassAndPropertiesRouter;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassAndPropertiesUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.LabelUiContentProvider;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.Messages;
import org.eclipse.wb.internal.rcp.databinding.model.GlobalFactoryHelper;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.DetailBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.observables.MapsBeanObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.DataBindingContextInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetPropertyBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.ui.contentproviders.InputElementUiContentProvider;

import java.util.List;

/**
 * Model for binding input to <code>JFace</code> viewer (exclude tree viewer).
 * 
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public final class ViewerInputBindingInfo extends AbstractViewerInputBindingInfo {
  private ObservableCollectionContentProviderInfo m_contentProvider;
  private ObservableMapLabelProviderInfo m_labelProvider;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public ViewerInputBindingInfo(WidgetBindableInfo viewerBindable) throws Exception {
    super(viewerBindable);
  }

  public ViewerInputBindingInfo(WidgetBindableInfo viewerBindable,
      WidgetPropertyBindableInfo viewerBindableProperty) throws Exception {
    super(viewerBindable, viewerBindableProperty);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setContentProvider(ObservableCollectionContentProviderInfo contentProvider) {
    m_contentProvider = contentProvider;
  }

  public ObservableMapLabelProviderInfo getLabelProvider() {
    return m_labelProvider;
  }

  public void setLabelProvider(ObservableMapLabelProviderInfo labelProvider) {
    m_labelProvider = labelProvider;
    m_labelProvider.setBinding(this);
  }

  @Override
  public void setDefaultProviders(boolean asList, Class<?> elementType, boolean useViewerSupport)
      throws Exception {
    boolean[] useViewerSupports = {useViewerSupport};
    // create content provider
    if (asList) {
      m_contentProvider = new ObservableListContentProviderInfo();
    } else {
      m_contentProvider = new ObservableSetContentProviderInfo();
    }
    // create content provider accessor
    KnownElementsObservableInfo knownElementsObservable =
        new KnownElementsObservableInfo(m_contentProvider);
    // create label provider
    MapsBeanObservableInfo observeMaps =
        GlobalFactoryHelper.createObserveMaps(
            m_inputObservable,
            knownElementsObservable,
            elementType,
            useViewerSupports);
    if (observeMaps == null) {
      observeMaps = new MapsBeanObservableInfo(knownElementsObservable, elementType, null);
    }
    setLabelProvider(new ObservableMapLabelProviderInfo(observeMaps));
    //
    if (useViewerSupports[0]) {
      setCodeSupport(new ViewerCodeSupport(this));
    }
  }

  @Override
  public Class<?> getElementType() {
    return m_labelProvider == null
        ? super.getElementType()
        : m_labelProvider.getMapsObservable().getElementType();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void createContentProviders(List<IUiContentProvider> providers,
      IPageListener listener,
      DatabindingsProvider provider) throws Exception {
    // configure page
    listener.setTitle(Messages.ViewerInputBindingInfo_title);
    listener.setMessage(Messages.ViewerInputBindingInfo_message);
    //
    providers.add(new LabelUiContentProvider(Messages.ViewerInputBindingInfo_viewerLabel,
        m_viewerBindable.getPresentation().getTextForBinding()));
    providers.add(new LabelUiContentProvider(Messages.ViewerInputBindingInfo_inputLabel,
        m_inputObservable.getPresentationText()));
    m_labelProvider.createContentProviders(providers, provider, true);
    // element type editor
    ChooseClassAndPropertiesConfiguration configuration =
        new ChooseClassAndPropertiesConfiguration();
    configuration.setDialogFieldLabel(Messages.ViewerInputBindingInfo_elementClassLabel);
    configuration.setValueScope("beans");
    configuration.setChooseInterfaces(true);
    configuration.setEmptyClassErrorMessage(Messages.ViewerInputBindingInfo_elementClassEmptyMessage);
    configuration.setErrorMessagePrefix(Messages.ViewerInputBindingInfo_elementClassErrorPrefix);
    configuration.setPropertiesLabel(Messages.ViewerInputBindingInfo_elementClassPropertiesLabel);
    configuration.setPropertiesMultiChecked(true);
    configuration.setReorderMode(true);
    configuration.setShowSelectionButtons(false);
    configuration.setLoadedPropertiesCheckedStrategy(ChooseClassAndPropertiesConfiguration.LoadedPropertiesCheckedStrategy.None);
    configuration.setPropertiesErrorMessage(Messages.ViewerInputBindingInfo_elementClassPropertiesErrorMessage);
    //
    if (m_inputObservable instanceof DetailBeanObservableInfo) {
      configuration.setDialogFieldEnabled(false);
      configuration.setValueScope("beans0");
      m_inputObservable.createContentProviders(providers, null, provider);
    }
    //
    GlobalFactoryHelper.configureChooseElementForViewerInput(m_inputObservable, configuration);
    //
    InputElementUiContentProvider inputUIContentProvider =
        new InputElementUiContentProvider(configuration, this);
    //
    if (m_inputObservable instanceof DetailBeanObservableInfo) {
      new ChooseClassAndPropertiesRouter((ChooseClassAndPropertiesUiContentProvider) providers.get(providers.size() - 1),
          inputUIContentProvider);
    }
    //
    providers.add(inputUIContentProvider);
    //
    super.createContentProviders(providers, listener, provider);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getPresentationText() throws Exception {
    return super.getPresentationText()
        + "("
        + m_contentProvider.getPresentationText()
        + ", "
        + m_labelProvider.getPresentationText()
        + ")";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Code generation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void addSourceCode(DataBindingContextInfo context,
      List<String> lines,
      CodeGenerationSupport generationSupport) throws Exception {
    addSourceCode(context, lines, generationSupport, m_contentProvider, m_labelProvider);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parser
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void postParse() throws Exception {
    super.postParse();
    Assert.isNotNull(m_contentProvider);
    Assert.isNotNull(m_labelProvider);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visiting
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void accept(AstObjectInfoVisitor visitor) throws Exception {
    accept(visitor, m_contentProvider, m_labelProvider);
  }
}