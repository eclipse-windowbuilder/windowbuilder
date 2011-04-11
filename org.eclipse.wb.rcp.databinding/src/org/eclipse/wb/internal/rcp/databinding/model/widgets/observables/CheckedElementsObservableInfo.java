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

import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders.ChooseClassConfiguration;
import org.eclipse.wb.internal.rcp.databinding.DatabindingsProvider;
import org.eclipse.wb.internal.rcp.databinding.Messages;
import org.eclipse.wb.internal.rcp.databinding.model.BindableInfo;
import org.eclipse.wb.internal.rcp.databinding.model.context.BindingUiContentProviderContext;
import org.eclipse.wb.internal.rcp.databinding.ui.contentproviders.CheckedElementsUiContentProvider;

import org.apache.commons.lang.ClassUtils;

import java.text.MessageFormat;
import java.util.List;

/**
 * Model for observable object <code>ViewersObservables.observeCheckedElements(...)</code>.
 * 
 * @author lobas_av
 * @coverage bindings.rcp.model.widgets
 */
public final class CheckedElementsObservableInfo extends ViewerObservableInfo {
  private Class<?> m_elementType;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public CheckedElementsObservableInfo(BindableInfo bindableWidget) throws Exception {
    this(bindableWidget, (Class<?>) null);
  }

  public CheckedElementsObservableInfo(BindableInfo bindableWidget, Class<?> elementType)
      throws Exception {
    super(bindableWidget, "observeCheckedElements");
    m_elementType = elementType;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ObservableInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean canShared() {
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public Class<?> getElementType() {
    return m_elementType;
  }

  public void setElementType(Class<?> elementType) {
    m_elementType = elementType;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getPresentationText() throws Exception {
    String presentationElementType =
        m_elementType == null ? "?????" : ClassUtils.getShortClassName(m_elementType);
    return getBindableObject().getPresentation().getTextForBinding()
        + ".checkedElements("
        + presentationElementType
        + ".class)";
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
    //
    ChooseClassConfiguration configuration = new ChooseClassConfiguration();
    configuration.setDialogFieldLabel(Messages.CheckedElementsObservableInfo_label);
    configuration.setValueScope("beans");
    configuration.setChooseInterfaces(true);
    configuration.setEmptyClassErrorMessage(MessageFormat.format(
        Messages.CheckedElementsObservableInfo_emptyMessage,
        context.getDirection()));
    configuration.setErrorMessagePrefix(MessageFormat.format(
        Messages.CheckedElementsObservableInfo_errorPrefix,
        context.getDirection()));
    //
    providers.add(new CheckedElementsUiContentProvider(configuration, this, provider));
  }
}