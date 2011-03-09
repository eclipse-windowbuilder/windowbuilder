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
package org.eclipse.wb.internal.rcp.databinding.preferences;

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.utils.binding.DataBindManager;
import org.eclipse.wb.internal.core.utils.binding.editors.controls.CheckButtonEditor;
import org.eclipse.wb.internal.core.utils.binding.editors.controls.ComboTextEditor;
import org.eclipse.wb.internal.core.utils.binding.editors.controls.RadioButtonsEditor;
import org.eclipse.wb.internal.core.utils.binding.providers.BooleanPreferenceProvider;
import org.eclipse.wb.internal.core.utils.binding.providers.IntegerPreferenceProvider;
import org.eclipse.wb.internal.core.utils.binding.providers.StringPreferenceProvider;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.jdt.ui.JdtUiUtils;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.rcp.databinding.Activator;
import org.eclipse.wb.internal.rcp.databinding.model.DataBindingsRootInfo;
import org.eclipse.wb.internal.rcp.databinding.model.GlobalFactoryHelper;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Code generation preference page.
 * 
 * @author lobas_av
 * @coverage bindings.rcp.preferences
 */
public final class DbCodeGenerationPreferencePage extends PreferencePage
    implements
      IWorkbenchPreferencePage,
      IPreferenceConstants {
  private static final String[] TITLES = {"public", "protected", "private", "default"};
  //
  private final DataBindManager m_bindManager = new DataBindManager();
  private final IPreferenceStore m_store = Activator.getStore();
  private Button m_addInvokeButton;
  private Button m_assignToFieldButton;
  private Button m_addToCompositeConstructorButton;
  private Button m_dontUseDeprecatedMethodsButton;
  private Button m_addTryCatch;
  private Button m_generateCodeForVersion13Button;
  private Button m_useViewerSupportButton;
  private Combo m_updateValueStrategyCombo;
  private Combo m_updateListStrategyCombo;
  private Combo m_updateSetStrategyCombo;
  private Button[] m_accessButtons;
  private SourceViewer m_previewViewer;

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Control createContents(Composite parent) {
    final Composite container = new Composite(parent, SWT.NONE);
    GridLayoutFactory.create(container);
    //
    m_addInvokeButton = new Button(container, SWT.CHECK);
    GridDataFactory.create(m_addInvokeButton).fillH().grabH();
    m_addInvokeButton.setText("Add initDataBindings() invocation to GUI creation method");
    //
    m_assignToFieldButton = new Button(container, SWT.CHECK);
    GridDataFactory.create(m_assignToFieldButton).fillH().grabH();
    m_assignToFieldButton.setText("Assign return value of initDataBindings() invocation to field");
    //
    m_addToCompositeConstructorButton = new Button(container, SWT.CHECK);
    GridDataFactory.create(m_addToCompositeConstructorButton).fillH().grabH();
    m_addToCompositeConstructorButton.setText("Add initDataBindings() invocation to Composite constructor");
    //
    m_dontUseDeprecatedMethodsButton = new Button(container, SWT.CHECK);
    GridDataFactory.create(m_dontUseDeprecatedMethodsButton).fillH().grabH();
    m_dontUseDeprecatedMethodsButton.setText("Don't use deprecated methods of BeansObservables");
    //
    m_addTryCatch = new Button(container, SWT.CHECK);
    GridDataFactory.create(m_addTryCatch).fillH().grabH();
    m_addTryCatch.setText("Enclosing databinding code in a try-catch block");
    //
    m_generateCodeForVersion13Button = new Button(container, SWT.CHECK);
    GridDataFactory.create(m_generateCodeForVersion13Button).fillH().grabH();
    m_generateCodeForVersion13Button.setText("Generate observables code for version 1.3 (over properties)");
    //
    m_useViewerSupportButton = new Button(container, SWT.CHECK);
    GridDataFactory.create(m_useViewerSupportButton).fillH().grabH();
    m_useViewerSupportButton.setText("Use ViewerSupport for generate JFace Viewer bindings");
    //
    try {
      GlobalFactoryHelper.confgureCodeGenerationPreferencePage(container, m_bindManager);
    } catch (Throwable e) {
      DesignerPlugin.log(e);
    }
    //
    Group strategiesComposite = new Group(container, SWT.NONE);
    GridLayoutFactory.create(strategiesComposite).columns(2);
    GridDataFactory.create(strategiesComposite).fillH().grabH();
    strategiesComposite.setText("Default value for new strategy object");
    //
    new Label(strategiesComposite, SWT.NONE).setText("UpdateValuesStrategy:");
    m_updateValueStrategyCombo = new Combo(strategiesComposite, SWT.BORDER | SWT.READ_ONLY);
    GridDataFactory.create(m_updateValueStrategyCombo).fillH().grabH();
    m_updateValueStrategyCombo.setItems(new String[]{
        "POLICY_UPDATE",
        "POLICY_NEVER",
        "POLICY_ON_REQUEST",
        "POLICY_CONVERT"});
    //
    new Label(strategiesComposite, SWT.NONE).setText("UpdateListStrategy:");
    m_updateListStrategyCombo = new Combo(strategiesComposite, SWT.BORDER | SWT.READ_ONLY);
    GridDataFactory.create(m_updateListStrategyCombo).fillH().grabH();
    m_updateListStrategyCombo.setItems(new String[]{
        "POLICY_UPDATE",
        "POLICY_NEVER",
        "POLICY_ON_REQUEST"});
    //
    new Label(strategiesComposite, SWT.NONE).setText("UpdateSetStrategy:");
    m_updateSetStrategyCombo = new Combo(strategiesComposite, SWT.BORDER | SWT.READ_ONLY);
    GridDataFactory.create(m_updateSetStrategyCombo).fillH().grabH();
    m_updateSetStrategyCombo.setItems(new String[]{
        "POLICY_UPDATE",
        "POLICY_NEVER",
        "POLICY_ON_REQUEST"});
    // access block
    Label accessLabel = new Label(container, SWT.NONE);
    GridDataFactory.create(accessLabel).fillH().grabH();
    accessLabel.setText("Generate initDataBindings() with access:");
    //
    m_accessButtons = new Button[TITLES.length];
    for (int i = 0; i < TITLES.length; i++) {
      Button button = new Button(container, SWT.RADIO);
      GridDataFactory.create(button).indentH(20).fillH().grabH();
      button.setText(TITLES[i]);
      m_accessButtons[i] = button;
    }
    // preview block
    Label previewLabel = new Label(container, SWT.NONE);
    GridDataFactory.create(previewLabel).fillH().grabH();
    previewLabel.setText("Preview:");
    //
    ExecutionUtils.runRethrow(new RunnableEx() {
      public void run() throws Exception {
        m_previewViewer = JdtUiUtils.createJavaSourceViewer(container, SWT.BORDER | SWT.V_SCROLL);
      }
    });
    GridDataFactory.create(m_previewViewer.getControl()).hintVC(16).grab().fill();
    // create bindings
    m_bindManager.bind(
        new CheckButtonEditor(m_addInvokeButton),
        new BooleanPreferenceProvider(m_store, ADD_INVOKE_INITDB_TO_GUI));
    m_bindManager.bind(
        new CheckButtonEditor(m_assignToFieldButton),
        new BooleanPreferenceProvider(m_store, ADD_INITDB_TO_FIELD));
    m_bindManager.bind(
        new CheckButtonEditor(m_addToCompositeConstructorButton),
        new BooleanPreferenceProvider(m_store, ADD_INVOKE_INITDB_TO_COMPOSITE_CONSTRUCTOR));
    m_bindManager.bind(
        new CheckButtonEditor(m_dontUseDeprecatedMethodsButton),
        new BooleanPreferenceProvider(m_store, DONT_USE_DEPRECATED_METHODS));
    m_bindManager.bind(new CheckButtonEditor(m_addTryCatch), new BooleanPreferenceProvider(m_store,
        INITDB_TRY_CATCH));
    m_bindManager.bind(
        new CheckButtonEditor(m_generateCodeForVersion13Button),
        new BooleanPreferenceProvider(m_store, GENERATE_CODE_FOR_VERSION_1_3));
    m_bindManager.bind(
        new CheckButtonEditor(m_useViewerSupportButton),
        new BooleanPreferenceProvider(m_store, USE_VIEWER_SUPPORT));
    m_bindManager.bind(
        new ComboTextEditor(m_updateValueStrategyCombo),
        new StringPreferenceProvider(m_store, UPDATE_VALUE_STRATEGY_DEFAULT));
    m_bindManager.bind(
        new ComboTextEditor(m_updateListStrategyCombo),
        new StringPreferenceProvider(m_store, UPDATE_LIST_STRATEGY_DEFAULT));
    m_bindManager.bind(
        new ComboTextEditor(m_updateSetStrategyCombo),
        new StringPreferenceProvider(m_store, UPDATE_SET_STRATEGY_DEFAULT));
    m_bindManager.bind(
        new RadioButtonsEditor(m_accessButtons),
        new IntegerPreferenceProvider(m_store, INITDB_GENERATE_ACCESS));
    // additional listeners
    m_bindManager.addUpdateEvent(m_addInvokeButton, SWT.Selection);
    m_bindManager.addUpdateEvent(m_assignToFieldButton, SWT.Selection);
    m_bindManager.addUpdateEvent(m_addTryCatch, SWT.Selection);
    m_bindManager.addUpdateEvent(m_generateCodeForVersion13Button, SWT.Selection);
    for (int i = 0; i < m_accessButtons.length; i++) {
      m_bindManager.addUpdateEvent(m_accessButtons[i], SWT.Selection);
    }
    m_bindManager.addUpdateRunnable(new Runnable() {
      public void run() {
        boolean state = m_addInvokeButton.getSelection();
        m_assignToFieldButton.setEnabled(state);
        m_addToCompositeConstructorButton.setEnabled(state);
        boolean selection13 = m_generateCodeForVersion13Button.getSelection();
        if (!selection13) {
          if (m_useViewerSupportButton.getSelection()) {
            m_useViewerSupportButton.setSelection(false);
          }
        }
        m_useViewerSupportButton.setEnabled(selection13);
        updatePreview();
      }
    });
    // initial update
    m_bindManager.performUpdate();
    //
    return container;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Preview
  //
  ////////////////////////////////////////////////////////////////////////////
  protected void updatePreview() {
    // prepare state
    boolean addState = m_addInvokeButton.getSelection();
    boolean fieldState = m_assignToFieldButton.getSelection();
    // prepare source
    final StringBuffer source = new StringBuffer("public class Foo extends ... {\n");
    // field code
    if (addState && fieldState) {
      source.append("\n  private DataBindingContext m_bindingContext;\n");
    }
    // create GUI method
    source.append("\n  CreateGUI() {\n    ...\n");
    if (addState && fieldState) {
      source.append("    m_bindingContext = initDataBindings();\n");
    } else if (addState && !fieldState) {
      source.append("    initDataBindings();\n");
    }
    source.append("  }\n\n  ");
    // access code
    int access = 1;
    for (int i = 0; i < m_accessButtons.length; i++) {
      if (m_accessButtons[i].getSelection()) {
        access = i;
        break;
      }
    }
    source.append(DataBindingsRootInfo.ACCESS_VALUES[access]);
    // initDB method
    source.append("DataBindingContext initDataBindings() {\n");
    boolean addTryCatch = m_addTryCatch.getSelection();
    if (addTryCatch) {
      source.append("   try {\n");
    }
    source.append("    ...\n");
    if (m_generateCodeForVersion13Button.getSelection()) {
      source.append("    WidgetProperties.text(SWT.Modify).observe(m_text);\n");
    } else {
      source.append("    SWTObservables.observeText(m_text, SWT.Modify);\n");
    }
    source.append("    ...\n");
    if (addTryCatch) {
      source.append("   } catch(Throwable e) {}\n");
    }
    source.append("  }\n\n}");
    // set preview source
    ExecutionUtils.runRethrow(new RunnableEx() {
      public void run() throws Exception {
        JdtUiUtils.setJavaSourceForViewer(m_previewViewer, source.toString());
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // State
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean performOk() {
    m_bindManager.performCommit();
    return super.performOk();
  }

  @Override
  protected void performDefaults() {
    m_bindManager.performDefault();
    super.performDefaults();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initialization
  //
  ////////////////////////////////////////////////////////////////////////////
  public void init(IWorkbench workbench) {
  }
}