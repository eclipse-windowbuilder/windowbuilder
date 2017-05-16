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
package org.eclipse.wb.internal.core.preferences.code;

import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.model.description.ToolkitDescriptionJava;
import org.eclipse.wb.internal.core.model.generation.GenerationDescription;
import org.eclipse.wb.internal.core.model.generation.GenerationPropertiesComposite;
import org.eclipse.wb.internal.core.model.generation.GenerationSettings;
import org.eclipse.wb.internal.core.model.generation.preview.GenerationPreview;
import org.eclipse.wb.internal.core.model.generation.statement.StatementGeneratorDescription;
import org.eclipse.wb.internal.core.model.variable.description.VariableSupportDescription;
import org.eclipse.wb.internal.core.preferences.Messages;
import org.eclipse.wb.internal.core.utils.binding.DataBindManager;
import org.eclipse.wb.internal.core.utils.binding.IDataProvider;
import org.eclipse.wb.internal.core.utils.binding.editors.controls.CheckButtonEditor;
import org.eclipse.wb.internal.core.utils.binding.editors.controls.ComboTextEditor;
import org.eclipse.wb.internal.core.utils.binding.providers.BooleanPreferenceProvider;
import org.eclipse.wb.internal.core.utils.binding.providers.StringPreferenceProvider;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.jdt.ui.JdtUiUtils;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Map;

/**
 * {@link PreferencePage} for code generation.
 *
 * @author scheglov_ke
 * @coverage core.preferences.ui
 */
public abstract class CodeGenerationPreferencePage extends PreferencePage
    implements
      IWorkbenchPreferencePage {
  private final DataBindManager m_bindManager = new DataBindManager();
  private final GenerationSettings m_settings;
  private final IPreferenceStore m_preferences;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public CodeGenerationPreferencePage(ToolkitDescriptionJava toolkit) {
    m_settings = toolkit.getGenerationSettings();
    m_preferences = toolkit.getPreferences();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected Control createContents(Composite parent) {
    Composite contents = new Composite(parent, SWT.NONE);
    GridLayoutFactory.create(contents).noMargins();
    //
    {
      Button deduceSettingsButton = new Button(contents, SWT.CHECK);
      deduceSettingsButton.setText(Messages.CodeGenerationPreferencePage_deduceFlag);
      m_bindManager.bind(
          new CheckButtonEditor(deduceSettingsButton),
          new BooleanPreferenceProvider(m_preferences, GenerationSettings.P_DEDUCE_SETTINGS),
          true);
    }
    // forced method for new statements/children
    {
      Composite composite = new Composite(contents, SWT.NONE);
      GridDataFactory.create(composite).grabH().fill();
      GridLayoutFactory.create(composite).columns(2).noMargins();
      // controls
      new Label(composite, SWT.NONE).setText(Messages.CodeGenerationPreferencePage_forcedMethod);
      Combo methodCombo = new Combo(composite, SWT.BORDER);
      methodCombo.setItems(getMethodForCombo());
      // bind
      m_bindManager.bind(
          new ComboTextEditor(methodCombo),
          new StringPreferenceProvider(m_preferences, GenerationSettings.P_FORCED_METHOD),
          true);
    }
    createDefaultsComposite(contents);
    //
    return contents;
  }

  protected String[] getMethodForCombo() {
    return new String[]{"createContents", "initialize", "jbInit", "initComponents", "initGUI"};
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Defaults
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates GUI for default variable/statement types.
   */
  private void createDefaultsComposite(Composite parent) {
    Group container = new Group(parent, SWT.NONE);
    GridDataFactory.create(container).grab().fill();
    GridLayoutFactory.create(container).columns(2).equalColumns().noMargins();
    container.setText(Messages.CodeGenerationPreferencePage_defaultGroup);
    //
    createVariableComposite(container);
    createStatementComposite(container);
    // bind
    {
      // variable
      m_bindManager.bind(
          new GenerationDescriptionEditor(m_variablesTabFolder),
          new IDataProvider() {
            public void setValue(Object value) {
              m_settings.setVariable((VariableSupportDescription) value);
            }

            public Object getValue(boolean def) {
              return def ? m_settings.getDefaultVariable() : m_settings.getVariable();
            }
          },
          true);
      // statement
      showCompatibleStatements();
      m_bindManager.bind(
          new GenerationDescriptionEditor(m_statementsTabFolder),
          new IDataProvider() {
            public void setValue(Object value) {
              m_settings.setStatement((StatementGeneratorDescription) value);
            }

            public Object getValue(boolean def) {
              return def ? m_settings.getDefaultStatement() : m_settings.getStatement();
            }
          },
          true);
    }
    // create preview (here, after binding)
    createPreviewComposite(container);
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Variable
  //
  ////////////////////////////////////////////////////////////////////////////
  private TabFolder m_variablesTabFolder;

  /**
   * Creates GUI for {@link VariableSupportDescription}.
   */
  private void createVariableComposite(Composite parent) {
    Composite container = new Composite(parent, SWT.NONE);
    GridDataFactory.create(container).grabH().fill();
    GridLayoutFactory.create(container);
    // title
    new Label(container, SWT.NONE).setText(Messages.CodeGenerationPreferencePage_variableLabel);
    // tab folder
    {
      m_variablesTabFolder = new TabFolder(container, SWT.NONE);
      GridDataFactory.create(m_variablesTabFolder).grab().fill();
      // create tab's
      for (VariableSupportDescription description : m_settings.getVariables()) {
        TabItem tabItem = new TabItem(m_variablesTabFolder, SWT.NONE);
        tabItem.setText(description.getName());
        tabItem.setData(description);
        //
        Composite composite = getDescriptionComposite(description, m_variablesTabFolder);
        tabItem.setControl(composite);
      }
    }
    // hint
    {
      Label hintLabel = new Label(container, SWT.WRAP);
      GridDataFactory.create(hintLabel).hintHC(50);
      hintLabel.setText(Messages.CodeGenerationPreferencePage_hintVariableSelectingTab);
    }
    // selection listener
    m_variablesTabFolder.addListener(SWT.Selection, new Listener() {
      public void handleEvent(Event event) {
        showCompatibleStatements();
      }
    });
  }

  /**
   * @return the selected {@link VariableSupportDescription}
   */
  private VariableSupportDescription getSelectedVariable() {
    TabItem tabItem = m_variablesTabFolder.getSelection()[0];
    return (VariableSupportDescription) tabItem.getData();
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Statement
  //
  ////////////////////////////////////////////////////////////////////////////
  private TabFolder m_statementsTabFolder;
  private StatementGeneratorDescription[] m_currentStatementDescriptions;

  /**
   * Creates GUI for {@link StatementGeneratorDescription}.
   */
  private void createStatementComposite(Composite parent) {
    Composite container = new Composite(parent, SWT.NONE);
    GridDataFactory.create(container).grabH().fill();
    GridLayoutFactory.create(container);
    // title
    new Label(container, SWT.NONE).setText(Messages.CodeGenerationPreferencePage_statementLabel);
    // tab folder
    {
      m_statementsTabFolder = new TabFolder(container, SWT.NONE);
      GridDataFactory.create(m_statementsTabFolder).grab().fill().hintVC(8);
    }
    // hint
    {
      Label hintLabel = new Label(container, SWT.WRAP);
      GridDataFactory.create(hintLabel).hintHC(50);
      hintLabel.setText(Messages.CodeGenerationPreferencePage_hintStatementSelectingTab);
    }
  }

  /**
   * Shows the list of {@link StatementGeneratorDescription} compatible with given
   * {@link VariableSupportDescription}.
   */
  private void showCompatibleStatements() {
    VariableSupportDescription variableDescription = getSelectedVariable();
    // prepare new statements to show
    StatementGeneratorDescription[] newStatements;
    {
      newStatements = m_settings.getStatements(variableDescription);
      if (Arrays.equals(m_currentStatementDescriptions, newStatements)) {
        return;
      }
      m_currentStatementDescriptions = newStatements;
    }
    // dispose existing tab's
    for (TabItem tabItem : m_statementsTabFolder.getItems()) {
      tabItem.getControl().setSize(0, 0);
      tabItem.dispose();
    }
    // create tab's
    for (StatementGeneratorDescription description : newStatements) {
      TabItem tabItem = new TabItem(m_statementsTabFolder, SWT.NONE);
      tabItem.setText(description.getName());
      tabItem.setData(description);
      //
      Composite composite = getDescriptionComposite(description, m_statementsTabFolder);
      tabItem.setControl(composite);
    }
    // update preview
    updatePreview();
  }

  /**
   * @return the selected {@link StatementGeneratorDescription}.
   */
  private StatementGeneratorDescription getSelectedStatement() {
    return (StatementGeneratorDescription) m_statementsTabFolder.getSelection()[0].getData();
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Preview
  //
  ////////////////////////////////////////////////////////////////////////////
  private SourceViewer m_previewViewer;
  /**
   * {@link Listener} for updates in {@link GenerationPropertiesComposite} of variable/statement.
   */
  private final Listener m_descriptionPropertiesListener = new Listener() {
    public void handleEvent(Event event) {
      m_descriptionPropertiesRunnable.run();
    }
  };
  /**
   * {@link Runnable} for updates in {@link GenerationPropertiesComposite} of variable/statement.
   */
  private final Runnable m_descriptionPropertiesRunnable = new Runnable() {
    public void run() {
      Display.getCurrent().asyncExec(new Runnable() {
        public void run() {
          updatePreview();
        }
      });
    }
  };

  /**
   * Creates GUI for variable/statement combination preview.
   */
  private void createPreviewComposite(Group container) {
    // create GUI
    {
      final Composite previewComposite = new Composite(container, SWT.NONE);
      GridDataFactory.create(previewComposite).spanH(2).grab().fill();
      GridLayoutFactory.create(previewComposite);
      // label
      new Label(previewComposite, SWT.NONE).setText(Messages.CodeGenerationPreferencePage_previewLabel);
      // source viewer
      {
        m_previewViewer =
            JdtUiUtils.createJavaSourceViewer(previewComposite, SWT.BORDER | SWT.V_SCROLL);
        GridDataFactory.create(m_previewViewer.getControl()).hintVC(16).grab().fill();
      }
    }
    // update preview on variable/statement selection
    {
      m_variablesTabFolder.addListener(SWT.Selection, m_descriptionPropertiesListener);
      m_statementsTabFolder.addListener(SWT.Selection, m_descriptionPropertiesListener);
      updatePreview();
    }
  }

  /**
   * Updates preview for variable/statement combination.
   */
  private void updatePreview() {
    if (m_previewViewer == null) {
      return;
    }
    ExecutionUtils.runRethrow(new RunnableEx() {
      public void run() throws Exception {
        // prepare descriptions
        VariableSupportDescription variableDescription = getSelectedVariable();
        StatementGeneratorDescription statementDescription = getSelectedStatement();
        // prepare properties for descriptions
        GenerationPropertiesComposite variableComposite =
            getDescriptionPropertiesComposite(variableDescription);
        GenerationPropertiesComposite statementComposite =
            getDescriptionPropertiesComposite(statementDescription);
        // do update
        GenerationPreview preview =
            m_settings.getPreview(variableDescription, statementDescription);
        if (preview != null) {
          JdtUiUtils.setJavaSourceForViewer(
              m_previewViewer,
              preview.getPreview(variableComposite, statementComposite));
        } else {
          JdtUiUtils.setJavaSourceForViewer(
              m_previewViewer,
              Messages.CodeGenerationPreferencePage_noPreviewMessage);
        }
      }
    });
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Map<GenerationDescription, Composite> m_descriptionComposites = Maps.newHashMap();
  private final Map<GenerationDescription, GenerationPropertiesComposite> m_descriptionPropertyComposites =
      Maps.newHashMap();

  /**
   * @return the {@link Composite} for displaying/editing {@link GenerationDescription} properties.
   */
  private Composite getDescriptionComposite(GenerationDescription description, Composite parent) {
    Composite wrapper = m_descriptionComposites.get(description);
    if (wrapper == null) {
      wrapper = new Composite(parent, SWT.NONE);
      GridLayoutFactory.create(wrapper);
      {
        Label descriptionLabel = new Label(wrapper, SWT.WRAP);
        GridDataFactory.create(descriptionLabel).grabH().fillH().hintHC(50);
        descriptionLabel.setText(MessageFormat.format(
            Messages.CodeGenerationPreferencePage_descriptionLabel,
            description.getDescription()));
      }
      {
        GenerationPropertiesComposite composite =
            description.createPropertiesComposite(wrapper, m_bindManager, m_preferences);
        GridDataFactory.create(composite).grab().fill();
        m_descriptionPropertyComposites.put(description, composite);
        composite.addUpdateListener(m_descriptionPropertiesRunnable);
      }
      m_descriptionComposites.put(description, wrapper);
    }
    return wrapper;
  }

  /**
   * @return the existing {@link GenerationPropertiesComposite} for given
   *         {@link GenerationDescription}.
   */
  private GenerationPropertiesComposite getDescriptionPropertiesComposite(GenerationDescription description) {
    GenerationPropertiesComposite composite = m_descriptionPropertyComposites.get(description);
    Assert.isNotNull(composite);
    return composite;
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
    showCompatibleStatements();
    super.performDefaults();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IWorkbenchPreferencePage
  //
  ////////////////////////////////////////////////////////////////////////////
  public void init(IWorkbench workbench) {
  }
}