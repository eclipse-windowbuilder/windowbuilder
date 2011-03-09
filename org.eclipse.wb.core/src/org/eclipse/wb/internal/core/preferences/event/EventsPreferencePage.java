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
      eventCodeTypeLabel.setText("Event code generation:");
    }
    {
      m_typeAnonymous = new Button(container, SWT.RADIO);
      GridDataFactory.create(m_typeAnonymous).indentH(20);
      m_typeAnonymous.setText("Create &anonymous class");
    }
    {
      m_typeInnerClass = new Button(container, SWT.RADIO);
      GridDataFactory.create(m_typeInnerClass).indentH(20);
      m_typeInnerClass.setText("Create &inner class");
      m_bindManager.addUpdateEvent(m_typeInnerClass, SWT.Selection);
      //
      m_innerClassPosition = new Combo(container, SWT.READ_ONLY);
      GridDataFactory.create(m_innerClassPosition).grabH().fillH().indentH(40);
      m_innerClassPosition.setItems(new String[]{
          "First body declaration in class",
          "Last body declaration in class",
          "Before first inner listener, or as first body declaration in class",
          "After last inner listener, or as last body declaration in class",});
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
      m_typeInterface.setText("Implement &listener interface in parent class");
    }
    {
      String toolTipText = getStubEventHandlerMethodNameToolTipText();
      {
        m_createStubMethods = new Button(container, SWT.CHECK);
        m_createStubMethods.setText("&Create stub event handler methods named:");
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
        patternsLabel.setText("Available pattern parts are:\r\n"
            + "${component_name} - the name of the component to which\r\n"
            + "        the handler is being attached.\r\n"
            + "${component_class_name} - the name of the class of the\r\n"
            + "        component to which the handler is being attached.\r\n"
            + "${event_name} - the name of the event being handled.");
      }
    }
    {
      m_deleteStubMethods = new Button(container, SWT.CHECK);
      m_deleteStubMethods.setText("Delete stub event handler methods on component delete");
    }
    {
      m_finalParameters = new Button(container, SWT.CHECK);
      m_finalParameters.setText("Declare parameters in event handlers as \"final\"");
    }
    {
      m_decorateIcon = new Button(container, SWT.CHECK);
      m_decorateIcon.setText("Show icon decorator for components with events");
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
    buffer.append("Use this field to change the pattern used to generate\n");
    buffer.append("the name of the method that will be invoked from within\n");
    buffer.append("the event handler. The pattern can include the template\n");
    buffer.append("variables (names enclosed between \"${\" and \"}\")\n");
    buffer.append("shown below. Capitalizing the name of any variable will\n");
    buffer.append("cause the value of the variable to be capitalized\n");
    buffer.append("before it is inserted into the resulting text.\n\n");
    buffer.append("component_name - the name of the component to which the\n");
    buffer.append("    handler is being attached\n");
    buffer.append("component_class_name - the name of the class of the\n");
    buffer.append("    component to which the handler is being attached\n");
    buffer.append("event_name - the name of the event being handled");
    return buffer.toString();
  }

  /**
   * @return the tooltip text for inner class name.
   */
  private static String getInnerClassNameToolTipText() {
    StringBuffer buffer;
    buffer = new StringBuffer();
    buffer.append("Use this field to change the pattern used to generate\n");
    buffer.append("the name of the inner class.\n");
    buffer.append("\n");
    buffer.append("The pattern can include the template\n");
    buffer.append("variables (names enclosed between \"${\" and \"}\")\n");
    buffer.append("shown below. Capitalizing the name of any variable will\n");
    buffer.append("cause the value of the variable to be capitalized\n");
    buffer.append("before it is inserted into the resulting text.\n");
    buffer.append("\n");
    buffer.append("component_name - the name of the component to which the\n");
    buffer.append("    handler is being attached\n");
    buffer.append("component_className - the name of the class of the\n");
    buffer.append("    component to which the handler is being attached\n");
    buffer.append("listener_name - the name of the event being handled\n");
    buffer.append("listener_className - the name of the event listener");
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