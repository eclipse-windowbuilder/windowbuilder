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
package org.eclipse.wb.internal.core.preferences.event;

import org.eclipse.wb.internal.core.model.property.event.EventsProperty;
import org.eclipse.wb.internal.core.model.property.event.IPreferenceConstants;
import org.eclipse.wb.internal.core.preferences.Messages;
import org.eclipse.wb.internal.core.utils.binding.DataBindManager;
import org.eclipse.wb.internal.core.utils.binding.editors.controls.CheckButtonEditor;
import org.eclipse.wb.internal.core.utils.binding.editors.controls.ComboSelectionEditor;
import org.eclipse.wb.internal.core.utils.binding.editors.controls.ComboTextEditor;
import org.eclipse.wb.internal.core.utils.binding.editors.controls.RadioButtonsEditor;
import org.eclipse.wb.internal.core.utils.binding.providers.BooleanPreferenceProvider;
import org.eclipse.wb.internal.core.utils.binding.providers.IntegerPreferenceProvider;
import org.eclipse.wb.internal.core.utils.binding.providers.StringPreferenceProvider;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * {@link PreferencePage} for {@link EventsProperty}.
 *
 * @author scheglov_ke
 * @coverage core.preferences.ui
 */
public abstract class EventsPreferencePage extends PreferencePage
    implements
      IPreferenceConstants,
      IWorkbenchPreferencePage {
  private final IPreferenceStore m_store;
  private final DataBindManager m_bindManager = new DataBindManager();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public EventsPreferencePage(IPreferenceStore preferenceStore) {
    m_store = preferenceStore;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  private Button m_typeAnonymous;
  private Button m_typeInnerClass;
  private Button m_typeInterface;
  private Combo m_innerClassPosition;
  private Button m_createStubMethods;
  private Combo m_stubMethodNameTemplate;
  private Combo m_innerClassNameTemplate;
  private Button m_deleteStubMethods;
  private Button m_finalParameters;
  private Button m_decorateIcon;

  @Override
  protected Control createContents(Composite parent) {
    Composite container = new Composite(parent, SWT.NONE);
    GridLayoutFactory.create(container).noMargins();
    createControls(container);
    return container;
  }

  private void createControls(Composite container) {
    {
      Label eventCodeTypeLabel = new Label(container, SWT.NONE);
      eventCodeTypeLabel.setText(Messages.EventsPreferencePage_typeLabel);
    }
    {
      m_typeAnonymous = new Button(container, SWT.RADIO);
      GridDataFactory.create(m_typeAnonymous).indentH(20);
      m_typeAnonymous.setText(Messages.EventsPreferencePage_typeAnonymous);
    }
    {
      m_typeInnerClass = new Button(container, SWT.RADIO);
      GridDataFactory.create(m_typeInnerClass).indentH(20);
      m_typeInnerClass.setText(Messages.EventsPreferencePage_typeInner);
      m_bindManager.addUpdateEvent(m_typeInnerClass, SWT.Selection);
      //
      m_innerClassPosition = new Combo(container, SWT.READ_ONLY);
      GridDataFactory.create(m_innerClassPosition).grabH().fillH().indentH(40);
      m_innerClassPosition.setItems(new String[]{
          Messages.EventsPreferencePage_typeInnerFirstInClass,
          Messages.EventsPreferencePage_typeInnerLastInClass,
          Messages.EventsPreferencePage_typeInnerBeforeFirstListener,
          Messages.EventsPreferencePage_typeInnerAfterLastListener,});
      UiUtils.setVisibleItemCount(m_innerClassPosition, m_innerClassPosition.getItemCount());
      //
      {
        m_innerClassNameTemplate = new Combo(container, SWT.NONE);
        GridDataFactory.create(m_innerClassNameTemplate).grabH().fill().indentH(40);
        m_innerClassNameTemplate.setToolTipText(getInnerClassNameToolTipText());
        m_innerClassNameTemplate.setItems(new String[]{
            "${Component_name}${Listener_name}",
            "${Component_name}${Listener_className}"});
        UiUtils.setVisibleAll(m_innerClassNameTemplate);
      }
    }
    {
      m_typeInterface = new Button(container, SWT.RADIO);
      GridDataFactory.create(m_typeInterface).indentH(20);
      m_typeInterface.setText(Messages.EventsPreferencePage_typeInterface);
    }
    {
      String toolTipText = getStubEventHandlerMethodNameToolTipText();
      {
        m_createStubMethods = new Button(container, SWT.CHECK);
        m_createStubMethods.setText(Messages.EventsPreferencePage_stubLabel);
        m_createStubMethods.setToolTipText(toolTipText);
        m_bindManager.addUpdateEvent(m_createStubMethods, SWT.Selection);
      }
      {
        m_stubMethodNameTemplate = new Combo(container, SWT.NONE);
        GridDataFactory.create(m_stubMethodNameTemplate).grabH().fill().indentH(20);
        m_stubMethodNameTemplate.setToolTipText(toolTipText);
        m_stubMethodNameTemplate.setItems(new String[]{
            "do_${component_name}_${event_name}",
            "do${Component_name}${Event_name}",
            "handle_${component_name}_${event_name}",
            "handle${Component_name}${Event_name}",
            "${component_name}${Event_name}",
            "${event_name}${Component_name}",
            "${event_name}${Component_name}${Component_class_name}"});
        UiUtils.setVisibleAll(m_stubMethodNameTemplate);
      }
      {
        Label patternsLabel = new Label(container, SWT.WRAP);
        GridDataFactory.create(patternsLabel).grabH().fillH().indentH(20);
        patternsLabel.setToolTipText(toolTipText);
        patternsLabel.setText(Messages.EventsPreferencePage_patternHint1
            + Messages.EventsPreferencePage_patternHint2
            + Messages.EventsPreferencePage_patternHint3
            + Messages.EventsPreferencePage_patternHint4
            + Messages.EventsPreferencePage_patternHint5
            + Messages.EventsPreferencePage_patternHint6);
      }
    }
    {
      m_deleteStubMethods = new Button(container, SWT.CHECK);
      m_deleteStubMethods.setText(Messages.EventsPreferencePage_deleteStubsFlag);
    }
    {
      m_finalParameters = new Button(container, SWT.CHECK);
      m_finalParameters.setText(Messages.EventsPreferencePage_finalParametersFlag);
    }
    {
      m_decorateIcon = new Button(container, SWT.CHECK);
      m_decorateIcon.setText(Messages.EventsPreferencePage_decorateIconFlag);
    }
    // create bindings
    {
      m_bindManager.addUpdateRunnable(new Runnable() {
        public void run() {
          {
            boolean inner = m_typeInnerClass.getSelection();
            m_innerClassPosition.setEnabled(inner);
            m_innerClassNameTemplate.setEnabled(inner);
            m_createStubMethods.setEnabled(!inner);
            m_stubMethodNameTemplate.setEnabled(!inner);
          }
          m_stubMethodNameTemplate.setEnabled(m_createStubMethods.getSelection());
        }
      });
      m_bindManager.bind(new RadioButtonsEditor(container), new IntegerPreferenceProvider(m_store,
          P_CODE_TYPE));
      m_bindManager.bind(
          new ComboSelectionEditor(m_innerClassPosition),
          new IntegerPreferenceProvider(m_store, P_INNER_POSITION));
      m_bindManager.bind(
          new ComboTextEditor(m_innerClassNameTemplate),
          new StringPreferenceProvider(m_store, P_INNER_NAME_TEMPLATE));
      m_bindManager.bind(
          new CheckButtonEditor(m_createStubMethods),
          new BooleanPreferenceProvider(m_store, P_CREATE_STUB));
      m_bindManager.bind(
          new ComboTextEditor(m_stubMethodNameTemplate),
          new StringPreferenceProvider(m_store, P_STUB_NAME_TEMPLATE));
      m_bindManager.bind(
          new CheckButtonEditor(m_deleteStubMethods),
          new BooleanPreferenceProvider(m_store, P_DELETE_STUB));
      m_bindManager.bind(
          new CheckButtonEditor(m_finalParameters),
          new BooleanPreferenceProvider(m_store, P_FINAL_PARAMETERS));
      m_bindManager.bind(
          new CheckButtonEditor(m_decorateIcon),
          new BooleanPreferenceProvider(m_store, P_DECORATE_ICON));
      m_bindManager.performUpdate();
    }
  }

  /**
   * @return the tooltip text for stub methods.
   */
  private static String getStubEventHandlerMethodNameToolTipText() {
    StringBuffer buffer;
    buffer = new StringBuffer();
    buffer.append(Messages.EventsPreferencePage_stubPatternHint1);
    buffer.append(Messages.EventsPreferencePage_stubPatternHint2);
    buffer.append(Messages.EventsPreferencePage_stubPatternHint3);
    buffer.append(Messages.EventsPreferencePage_stubPatternHint4);
    buffer.append(Messages.EventsPreferencePage_stubPatternHint5);
    buffer.append(Messages.EventsPreferencePage_stubPatternHint6);
    buffer.append(Messages.EventsPreferencePage_stubPatternHint7);
    buffer.append(Messages.EventsPreferencePage_stubPatternHint8);
    buffer.append(Messages.EventsPreferencePage_stubPatternHint9);
    buffer.append(Messages.EventsPreferencePage_stubPatternHint10);
    buffer.append(Messages.EventsPreferencePage_stubPatternHint11);
    buffer.append(Messages.EventsPreferencePage_stubPatternHint12);
    return buffer.toString();
  }

  /**
   * @return the tooltip text for inner class name.
   */
  private static String getInnerClassNameToolTipText() {
    StringBuffer buffer;
    buffer = new StringBuffer();
    buffer.append(Messages.EventsPreferencePage_innerPatternHint1);
    buffer.append(Messages.EventsPreferencePage_innerPatternHint2);
    buffer.append(Messages.EventsPreferencePage_innerPatternHint3);
    buffer.append(Messages.EventsPreferencePage_innerPatternHint4);
    buffer.append(Messages.EventsPreferencePage_innerPatternHint5);
    buffer.append(Messages.EventsPreferencePage_innerPatternHint6);
    buffer.append(Messages.EventsPreferencePage_innerPatternHint7);
    buffer.append(Messages.EventsPreferencePage_innerPatternHint8);
    buffer.append(Messages.EventsPreferencePage_innerPatternHint9);
    buffer.append(Messages.EventsPreferencePage_innerPatternHint10);
    buffer.append(Messages.EventsPreferencePage_innerPatternHint11);
    buffer.append(Messages.EventsPreferencePage_innerPatternHint12);
    buffer.append(Messages.EventsPreferencePage_innerPatternHint13);
    buffer.append(Messages.EventsPreferencePage_innerPatternHint14);
    buffer.append(Messages.EventsPreferencePage_innerPatternHint15);
    return buffer.toString();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void performDefaults() {
    super.performDefaults();
    m_bindManager.performDefault();
  }

  @Override
  public boolean performOk() {
    m_bindManager.performCommit();
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initialization
  //
  ////////////////////////////////////////////////////////////////////////////
  public void init(IWorkbench workbench) {
  }
}