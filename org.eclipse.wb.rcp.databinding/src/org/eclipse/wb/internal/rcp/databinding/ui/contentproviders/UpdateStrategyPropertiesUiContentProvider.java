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
package org.eclipse.wb.internal.rcp.databinding.ui.contentproviders;

import org.eclipse.wb.internal.core.databinding.ui.UiUtils;
import org.eclipse.wb.internal.core.databinding.ui.editor.ICompleteListener;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.rcp.databinding.Activator;
import org.eclipse.wb.internal.rcp.databinding.Messages;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Content provider container for edit update strategy properties: converter and validators.
 *
 * @author lobas_av
 * @coverage bindings.rcp.ui
 */
public final class UpdateStrategyPropertiesUiContentProvider implements IUiContentProvider {
  //
  private final List<IUiContentProvider> m_providers = new ArrayList<>();
  private ExpandableComposite m_expandableComposite;
  private final String m_settingKey;
  private final String m_captionPrefix;
  private final IDialogSettings m_settings;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public UpdateStrategyPropertiesUiContentProvider(String direction) {
    m_settingKey = direction + " strategy expanded state";
    if ("Target".equals(direction)) {
      m_captionPrefix = Messages.UpdateStrategyPropertiesUiContentProvider_Target2Model + " ";
    } else if ("Model".equals(direction)) {
      m_captionPrefix = Messages.UpdateStrategyPropertiesUiContentProvider_Model2Target + " ";
    } else {
      m_captionPrefix = StringUtils.EMPTY;
    }
    IDialogSettings mainSettings = Activator.getDefault().getDialogSettings();
    m_settings = UiUtils.getSettings(mainSettings, getClass().getName());
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Add sub {@link IUiContentProvider} content provider to container.
   */
  public void addProvider(IUiContentProvider provider) {
    m_providers.add(provider);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Complete
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setCompleteListener(ICompleteListener listener) {
    for (IUiContentProvider provider : m_providers) {
      provider.setCompleteListener(listener);
    }
  }

  public String getErrorMessage() {
    for (IUiContentProvider provider : m_providers) {
      String errorMessage = provider.getErrorMessage();
      if (errorMessage != null) {
        return errorMessage;
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  public int getNumberOfControls() {
    return 1;
  }

  public void createContent(final Composite parent, int columns) {
    // create expandable composite
    m_expandableComposite = new ExpandableComposite(parent, SWT.NONE);
    m_expandableComposite.setExpanded(true);
    GridDataFactory.create(m_expandableComposite).fillH().grabH().spanH(columns);
    m_expandableComposite.addExpansionListener(new ExpansionAdapter() {
      @Override
      public void expansionStateChanged(ExpansionEvent e) {
        compositeExpansionStateChanging();
        parent.layout();
      }
    });
    // calculate columns
    int subColumns = 0;
    for (IUiContentProvider provider : m_providers) {
      subColumns = Math.max(subColumns, provider.getNumberOfControls());
    }
    // create sub content providers
    Composite clientComposite = new Composite(m_expandableComposite, SWT.NONE);
    GridLayoutFactory.create(clientComposite).columns(subColumns).noMargins();
    m_expandableComposite.setClient(clientComposite);
    //
    for (IUiContentProvider provider : m_providers) {
      provider.createContent(clientComposite, subColumns);
    }
    compositeExpansionStateChanging();
  }

  private void compositeExpansionStateChanging() {
    m_settings.put(m_settingKey, !m_expandableComposite.isExpanded());
    if (m_expandableComposite.isExpanded()) {
      m_expandableComposite.setText(
          m_captionPrefix + Messages.UpdateStrategyPropertiesUiContentProvider_strategyProperties);
    } else {
      m_expandableComposite.setText(
          m_captionPrefix
              + Messages.UpdateStrategyPropertiesUiContentProvider_strategyPropertiesDots);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Update
  //
  ////////////////////////////////////////////////////////////////////////////
  public void updateFromObject() throws Exception {
    for (IUiContentProvider provider : m_providers) {
      provider.updateFromObject();
    }
  }

  public void saveToObject() throws Exception {
    for (IUiContentProvider provider : m_providers) {
      provider.saveToObject();
    }
  }
}