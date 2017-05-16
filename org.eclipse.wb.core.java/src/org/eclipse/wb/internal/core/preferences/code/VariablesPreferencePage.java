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

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.description.ToolkitDescription;
import org.eclipse.wb.internal.core.model.variable.NamesManager;
import org.eclipse.wb.internal.core.model.variable.NamesManager.ComponentNameDescription;
import org.eclipse.wb.internal.core.preferences.IPreferenceConstants;
import org.eclipse.wb.internal.core.preferences.Messages;
import org.eclipse.wb.internal.core.preferences.bind.AbstractBindingPreferencesPage;
import org.eclipse.wb.internal.core.utils.binding.DataBindManager;
import org.eclipse.wb.internal.core.utils.ui.AbstractBindingComposite;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.TabFactory;
import org.eclipse.wb.internal.core.utils.ui.TableFactory;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import java.text.MessageFormat;
import java.util.List;

/**
 * {@link PreferencePage} for variables options.
 *
 * @author scheglov_ke
 * @coverage core.preferences.ui
 */
public abstract class VariablesPreferencePage extends AbstractBindingPreferencesPage
    implements
      IPreferenceConstants {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public VariablesPreferencePage(ToolkitDescription toolkit) {
    super(toolkit);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected AbstractBindingComposite createBindingComposite(Composite parent) {
    return new ContentsComposite(parent, m_bindManager, m_toolkit);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Contents
  //
  ////////////////////////////////////////////////////////////////////////////
  private static class ContentsComposite extends AbstractBindingComposite {
    private final ToolkitDescription m_toolkit;

    public ContentsComposite(Composite parent,
        DataBindManager bindManager,
        ToolkitDescription toolkit) {
      super(parent, bindManager, toolkit.getPreferences());
      m_toolkit = toolkit;
      GridLayoutFactory.create(this).noMargins();
      //
      TabFolder tabFolder = new TabFolder(this, SWT.NONE);
      GridDataFactory.create(tabFolder).grab().fill();
      {
        Composite composite =
            TabFactory.item(tabFolder).text(Messages.VariablesPreferencePage_typeSpecificTab).composite();
        createTypeSpecificPage(composite);
      }
      {
        Composite composite =
            TabFactory.item(tabFolder).text(Messages.VariablesPreferencePage_autoRenameTab).composite();
        createAutoRenamePage(composite);
      }
      {
        Composite composite =
            TabFactory.item(tabFolder).text(Messages.VariablesPreferencePage_miscTab).composite();
        createMiscellaneousPage(composite);
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Validation
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected String validate() throws Exception {
      // template
      {
        String template = getString(P_VARIABLE_TEXT_TEMPLATE);
        String message = NamesManager.validate(template);
        if (message != null) {
          return MessageFormat.format(
              Messages.VariablesPreferencePage_validateVariableNameTemplate,
              message);
        }
      }
      // continue
      return super.validate();
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Auto rename
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Creates page for auto rename on "text" property change.
     */
    private void createAutoRenamePage(Composite parent) {
      GridLayoutFactory.create(parent).columns(2).equalColumns();
      // mode
      {
        new Label(parent, SWT.NONE).setText(Messages.VariablesPreferencePage_arLabel);
        // control
        Combo modeCombo = new Combo(parent, SWT.READ_ONLY);
        GridDataFactory.create(modeCombo).grabH().fillH();
        modeCombo.setItems(new String[]{
            Messages.VariablesPreferencePage_arAlways,
            Messages.VariablesPreferencePage_arDefault,
            Messages.VariablesPreferencePage_arNever});
        // bind
        bindSelection(modeCombo, P_VARIABLE_TEXT_MODE);
      }
      // template
      {
        new Label(parent, SWT.NONE).setText(Messages.VariablesPreferencePage_arPatternLabel);
        // control
        Combo templateCombo = new Combo(parent, SWT.NONE);
        GridDataFactory.create(templateCombo).grabH().fillH();
        templateCombo.setItems(new String[]{
            "${text}${class_name}",
            "${class_acronym}${text}",
            "${class_name}${text}"});
        // bind
        bindString(templateCombo, P_VARIABLE_TEXT_TEMPLATE);
      }
      // words limit
      {
        Composite composite = new Composite(parent, SWT.NONE);
        GridDataFactory.create(composite).spanH(2).fillH();
        GridLayoutFactory.create(composite).columns(3).noMargins();
        // initial text
        new Label(composite, SWT.NONE).setText(Messages.VariablesPreferencePage_arLimitWords1);
        // control
        {
          Text wordsText = new Text(composite, SWT.BORDER);
          GridDataFactory.create(wordsText).hintHC(3);
          bindInteger(wordsText, P_VARIABLE_TEXT_WORDS_LIMIT);
        }
        // final text
        new Label(composite, SWT.NONE).setText(Messages.VariablesPreferencePage_arLimitWords2);
      }
      // template parts hint
      {
        Label templateHint = new Label(parent, SWT.WRAP);
        GridDataFactory.create(templateHint).spanH(2);
        templateHint.setText(Messages.VariablesPreferencePage_arHint1
            + Messages.VariablesPreferencePage_arHint2
            + Messages.VariablesPreferencePage_arHint3
            + Messages.VariablesPreferencePage_arHint4
            + Messages.VariablesPreferencePage_arHint5);
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Miscellaneous
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Creates page for miscellaneous variable preferences.
     */
    private void createMiscellaneousPage(Composite parent) {
      GridLayoutFactory.create(parent).columns(2).equalColumns();
      // variable in component
      checkButton(
          parent,
          Messages.VariablesPreferencePage_miscRememberName,
          IPreferenceConstants.P_VARIABLE_IN_COMPONENT);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Type specific
    //
    ////////////////////////////////////////////////////////////////////////////
    private List<ComponentNameDescription> m_descriptions;
    private TableViewer m_namesViewer;

    /**
     * Creates page for type specific variable options.
     */
    private void createTypeSpecificPage(Composite parent) {
      GridLayoutFactory.create(parent).columns(2);
      // viewer
      {
        m_namesViewer = new TableViewer(parent, SWT.BORDER | SWT.SINGLE | SWT.FULL_SELECTION);
        GridDataFactory.create(m_namesViewer.getTable()).grab().fill();
        Table table = m_namesViewer.getTable();
        // columns
        {
          TableFactory tableFactory = TableFactory.modify(m_namesViewer);
          tableFactory.linesVisible(true).headerVisible(true);
          tableFactory.newColumn().text(Messages.VariablesPreferencePage_tsClassColumn).widthC(35);
          tableFactory.newColumn().text(Messages.VariablesPreferencePage_tsDefaultNameColumn).widthC(
              20);
          tableFactory.newColumn().text(Messages.VariablesPreferencePage_tsAcronymColumn).widthC(15);
          tableFactory.newColumn().text(Messages.VariablesPreferencePage_tsAsFieldColumn).widthC(15);
        }
        // providers
        m_namesViewer.setContentProvider(new ArrayContentProvider());
        m_namesViewer.setLabelProvider(new NamesLabelProvider());
        // editing
        m_namesViewer.setColumnProperties(new String[]{"class", "name", "acronym", "asField"});
        m_namesViewer.setCellEditors(new CellEditor[]{
            null,
            new TextCellEditor(table),
            new TextCellEditor(table),
            new CheckboxCellEditor(table),});
        m_namesViewer.setCellModifier(new NamesCellModifier());
      }
      // buttons
      {
        Composite buttonsComposite = new Composite(parent, SWT.NONE);
        GridDataFactory.create(buttonsComposite).grabV().alignVT();
        GridLayoutFactory.create(buttonsComposite).noMargins();
        // add button
        {
          Button addButton = new Button(buttonsComposite, SWT.NONE);
          GridDataFactory.create(addButton).fillH();
          addButton.setText(Messages.VariablesPreferencePage_tsAddButton);
          // operation
          addButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
              InputDialog inputDialog =
                  new InputDialog(getShell(),
                      Messages.VariablesPreferencePage_tsAddTitle,
                      Messages.VariablesPreferencePage_tsAddMessage,
                      "",
                      null);
              if (inputDialog.open() == Window.OK) {
                String className = inputDialog.getValue();
                ComponentNameDescription description =
                    NamesManager.getDefaultNameDescription(className);
                m_descriptions.add(description);
                m_namesViewer.add(description);
                m_namesViewer.setSelection(new StructuredSelection(description));
              }
            }
          });
        }
        // remove button
        {
          final Button removeButton = new Button(buttonsComposite, SWT.NONE);
          GridDataFactory.create(removeButton).fillH();
          removeButton.setText(Messages.VariablesPreferencePage_tsRemove);
          // operation
          removeButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
              ComponentNameDescription description =
                  (ComponentNameDescription) ((IStructuredSelection) m_namesViewer.getSelection()).getFirstElement();
              if (MessageDialog.openConfirm(
                  getShell(),
                  Messages.VariablesPreferencePage_tsRemoveTitle,
                  MessageFormat.format(
                      Messages.VariablesPreferencePage_tsRemoveMessage,
                      description.getClassName()))) {
                m_descriptions.remove(description);
                m_namesViewer.remove(description);
              }
            }
          });
          // enable/disable
          removeButton.setEnabled(false);
          m_namesViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
              removeButton.setEnabled(!event.getSelection().isEmpty());
            }
          });
        }
      }
      // set initial descriptions
      m_descriptions = NamesManager.getNameDescriptions(m_toolkit, false);
      m_namesViewer.setInput(m_descriptions);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Type specific names: label provider
    //
    ////////////////////////////////////////////////////////////////////////////
    private static final Image IMAGE_FALSE =
        DesignerPlugin.getImage("preferences/boolean_false.png");
    private static final Image IMAGE_TRUE = DesignerPlugin.getImage("preferences/boolean_true.png");

    /**
     * Implementation of {@link ITableLabelProvider} for {@link ComponentNameDescription}.
     */
    private static class NamesLabelProvider extends LabelProvider implements ITableLabelProvider {
      public Image getColumnImage(Object element, int columnIndex) {
        ComponentNameDescription description = (ComponentNameDescription) element;
        if (columnIndex == 3) {
          return description.isAsField() ? IMAGE_TRUE : IMAGE_FALSE;
        }
        return null;
      }

      public String getColumnText(Object element, int columnIndex) {
        ComponentNameDescription description = (ComponentNameDescription) element;
        if (columnIndex == 0) {
          return description.getClassName();
        }
        if (columnIndex == 1) {
          return description.getName();
        }
        if (columnIndex == 2) {
          return description.getAcronym();
        }
        return null;
      }
    }
    ////////////////////////////////////////////////////////////////////////////
    //
    // Type specific names: modifier
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Implementation of {@link ICellModifier} for {@link ComponentNameDescription}.
     */
    private class NamesCellModifier implements ICellModifier {
      public Object getValue(Object element, String property) {
        ComponentNameDescription description = (ComponentNameDescription) element;
        if ("name".equals(property)) {
          return description.getName();
        }
        if ("acronym".equals(property)) {
          return description.getAcronym();
        }
        if ("asField".equals(property)) {
          return description.isAsField() ? Boolean.TRUE : Boolean.FALSE;
        }
        return null;
      }

      public boolean canModify(Object element, String property) {
        return "name".equals(property) || "acronym".equals(property) || "asField".equals(property);
      }

      public void modify(Object element, String property, Object value) {
        // prepare description
        ComponentNameDescription description;
        {
          if (element instanceof Item) {
            element = ((Item) element).getData();
          }
          description = (ComponentNameDescription) element;
        }
        // update description
        try {
          if ("name".equals(property)) {
            description.setName((String) value);
          }
          if ("acronym".equals(property)) {
            description.setAcronym((String) value);
          }
          if ("asField".equals(property)) {
            description.setAsField(((Boolean) value).booleanValue());
          }
          // refresh to display new values
          m_namesViewer.refresh(description);
        } catch (Throwable e) {
          DesignerPlugin.log(e);
        }
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // State
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean performOk() {
      try {
        NamesManager.setNameDescriptions(m_toolkit, m_descriptions);
      } catch (Throwable e) {
        DesignerPlugin.log(e);
        MessageDialog.openError(
            getShell(),
            Messages.VariablesPreferencePage_setErrorTitle,
            Messages.VariablesPreferencePage_setErrorMessage);
        return false;
      }
      return super.performOk();
    }

    @Override
    public void performDefaults() {
      {
        m_descriptions = NamesManager.getNameDescriptions(m_toolkit, true);
        m_namesViewer.setInput(m_descriptions);
      }
      super.performDefaults();
    }
  }
}
