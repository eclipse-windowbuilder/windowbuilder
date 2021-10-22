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
package org.eclipse.wb.internal.rcp.databinding.model.widgets.observables;

import org.eclipse.wb.internal.core.databinding.model.IObserveInfo;
import org.eclipse.wb.internal.core.databinding.model.IObserveInfo.ChildrenContext;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.Messages;
import org.eclipse.wb.internal.rcp.databinding.model.BindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.ObservableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.beans.IMasterDetailProvider;
import org.eclipse.wb.internal.rcp.databinding.model.context.BindingUiContentProviderContext;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.PropertiesSupport;
import org.eclipse.wb.internal.rcp.databinding.model.widgets.bindables.WidgetBindableInfo;
import org.eclipse.wb.internal.rcp.databinding.ui.contentproviders.SwtDelayUiContentProvider;

import java.util.List;

/**
 * Model for observable object <code>ViewersObservables.observeSingleSelection(...)</code>.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public final class SingleSelectionObservableInfo extends ViewerObservableInfo
    implements
      IMasterDetailProvider,
      IDelayValueProvider {
  private final boolean m_isViewer;
  private int m_delayValue;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public SingleSelectionObservableInfo(BindableInfo bindableWidget, BindableInfo bindableProperty)
      throws Exception {
    super(bindableWidget, bindableProperty);
    m_isViewer = isViewer(m_bindableWidget);
  }

  public SingleSelectionObservableInfo(BindableInfo bindableWidget) throws Exception {
    super(bindableWidget, "observeSingleSelection");
    m_isViewer = isViewer(m_bindableWidget);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public boolean isViewer() {
    return m_isViewer;
  }

  private static final boolean isViewer(BindableInfo bindableObject) throws Exception {
    if (bindableObject instanceof WidgetBindableInfo) {
      WidgetBindableInfo bindableWidget = (WidgetBindableInfo) bindableObject;
      return bindableWidget.getClassLoader().loadClass("org.eclipse.jface.viewers.Viewer").isAssignableFrom(
          bindableWidget.getObjectType());
    }
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IMasterDetailProvider
  //
  ////////////////////////////////////////////////////////////////////////////
  public ObservableInfo getMasterObservable() throws Exception {
    BindableInfo bindableObject = getBindableObject();
    //
    for (IObserveInfo property : bindableObject.getChildren(ChildrenContext.ChildrenForPropertiesTable)) {
      if (PropertiesSupport.DETAIL_SINGLE_SELECTION_NAME.equals(property.getPresentation().getText())) {
        SingleSelectionObservableInfo observableInfo =
            new SingleSelectionObservableInfo(bindableObject, (BindableInfo) property);
        observableInfo.setDelayValue(m_delayValue);
        if (m_codeSupport != null) {
          observableInfo.setCodeSupport(m_codeSupport);
        }
        return observableInfo;
      }
    }
    //
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // DelayValue
  //
  ////////////////////////////////////////////////////////////////////////////
  public final int getDelayValue() {
    return m_delayValue;
  }

  public final void setDelayValue(int delayValue) {
    Assert.isTrue(delayValue >= 0);
    m_delayValue = delayValue;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void createContentProviders(List<IUiContentProvider> providers,
      BindingUiContentProviderContext context,
      DatabindingsProvider provider) throws Exception {
    super.createContentProviders(providers, context, provider);
    if (m_isViewer) {
      providers.add(new SwtDelayUiContentProvider(this,
          Messages.SingleSelectionObservableInfo_viewerDelay));
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getPresentationText() throws Exception {
    return getBindableObject().getPresentation().getTextForBinding() + ".selection";
  }
}