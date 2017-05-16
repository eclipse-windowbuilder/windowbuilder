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
package org.eclipse.wb.internal.core.preferences;

import org.eclipse.wb.internal.core.UiMessages;
import org.eclipse.wb.internal.core.model.description.LayoutDescription;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.model.description.helpers.LayoutDescriptionHelper;
import org.eclipse.wb.internal.core.preferences.bind.AbstractBindingPreferencesPage;
import org.eclipse.wb.internal.core.utils.binding.DataBindManager;
import org.eclipse.wb.internal.core.utils.binding.IDataEditor;
import org.eclipse.wb.internal.core.utils.binding.providers.StringPreferenceProvider;
import org.eclipse.wb.internal.core.utils.ui.AbstractBindingComposite;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Main {@link PreferencePage} for Layout Support.
 *
 * @author scheglov_ke
 * @coverage core.preferences.ui
 */
public abstract class LayoutsPreferencePage extends AbstractBindingPreferencesPage {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LayoutsPreferencePage(ToolkitDescription toolkit) {
    super(toolkit);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected AbstractBindingComposite createBindingComposite(Composite parent) {
    return new ContentsComposite(parent, m_bindManager, m_preferences);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Contents
  //
  ////////////////////////////////////////////////////////////////////////////
  protected class ContentsComposite extends AbstractBindingComposite {
    public ContentsComposite(Composite parent,
        DataBindManager bindManager,
        IPreferenceStore preferences) {
      super(parent, bindManager, preferences);
      GridLayoutFactory.create(this).noMargins().columns(2);
      // default layout
      {
        new Label(this, SWT.NONE).setText(UiMessages.LayoutsPreferencePage_defaultLayout);
        final Combo layoutCombo = new Combo(this, SWT.READ_ONLY);
        GridDataFactory.create(layoutCombo).grabH().fillH();
        UiUtils.setVisibleItemCount(layoutCombo, 15);
        // prepare layouts
        final List<LayoutDescription> layouts = LayoutDescriptionHelper.get(m_toolkit);
        Collections.sort(layouts, new Comparator<LayoutDescription>() {
          public int compare(LayoutDescription layout_1, LayoutDescription layout_2) {
            return layout_1.getName().compareTo(layout_2.getName());
          }
        });
        // add items for layouts
        {
          layoutCombo.add(UiMessages.LayoutsPreferencePage_implicitLayout);
          for (LayoutDescription layoutDescription : layouts) {
            layoutCombo.add(layoutDescription.getName());
          }
        }
        // bind
        m_bindManager.bind(new IDataEditor() {
          public void setValue(Object value) {
            String id = (String) value;
            // implicit layout
            if (StringUtils.isEmpty(id)) {
              layoutCombo.select(0);
              return;
            }
            // find layout by id
            for (int index = 0; index < layouts.size(); index++) {
              LayoutDescription layout = layouts.get(index);
              if (layout.getId().equals(id)) {
                layoutCombo.select(1 + index);
              }
            }
          }

          public Object getValue() {
            int index = layoutCombo.getSelectionIndex();
            if (index <= 0) {
              // implicit layout
              return null;
            } else {
              LayoutDescription layout = layouts.get(index - 1);
              return layout.getId();
            }
          }
        }, new StringPreferenceProvider(m_preferences, IPreferenceConstants.P_LAYOUT_DEFAULT), true);
      }
      // boolean preferences
      checkButton(
          this,
          2,
          UiMessages.LayoutsPreferencePage_inheritLayout,
          IPreferenceConstants.P_LAYOUT_OF_PARENT);
    }
  }
}