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
package org.eclipse.wb.internal.core.editor.palette.dialogs.factory;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.editor.palette.model.CategoryInfo;
import org.eclipse.wb.core.editor.palette.model.PaletteInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.Messages;
import org.eclipse.wb.internal.core.editor.palette.command.Command;
import org.eclipse.wb.internal.core.editor.palette.command.factory.FactoryAddCommand;
import org.eclipse.wb.internal.core.editor.palette.dialogs.AbstractPaletteDialog;
import org.eclipse.wb.internal.core.editor.palette.model.entry.FactoryEntryInfo;
import org.eclipse.wb.internal.core.editor.palette.model.entry.StaticFactoryEntryInfo;
import org.eclipse.wb.internal.core.model.description.factory.FactoryMethodDescription;
import org.eclipse.wb.internal.core.model.description.helpers.FactoryDescriptionHelper;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.dialogfields.CheckedListDialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.ComboDialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogFieldUtils;
import org.eclipse.wb.internal.core.utils.dialogfields.IListAdapter;
import org.eclipse.wb.internal.core.utils.dialogfields.IStringButtonAdapter;
import org.eclipse.wb.internal.core.utils.dialogfields.ListDialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.StringButtonDialogField;
import org.eclipse.wb.internal.core.utils.jdt.ui.JdtUiUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

/**
 * Abstract dialog for adding multiple {@link FactoryEntryInfo} for factory class.
 *
 * @author scheglov_ke
 * @coverage core.editor.palette.ui
 */
public final class FactoriesAddDialog extends AbstractPaletteDialog {
  private final AstEditor m_editor;
  private final PaletteInfo m_palette;
  private final CategoryInfo m_initialCategory;
  private final boolean m_forStatic;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FactoriesAddDialog(Shell parentShell,
      AstEditor editor,
      PaletteInfo palette,
      CategoryInfo initialCategory,
      boolean forStatic) {
    super(parentShell, forStatic
        ? Messages.FactoriesAddDialog_shellTitleStatic
        : Messages.FactoriesAddDialog_shellTitleInstance, forStatic
        ? Messages.FactoriesAddDialog_titleStatic
        : Messages.FactoriesAddDialog_titleInstance, null, Messages.FactoriesAddDialog_message);
    m_editor = editor;
    m_palette = palette;
    m_initialCategory = initialCategory;
    m_forStatic = forStatic;
    setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  private StringButtonDialogField m_factoryClassField;
  private CheckedListDialogField m_signaturesField;
  private ComboDialogField m_categoryField;

  @Override
  protected void createControls(Composite container) {
    m_fieldsContainer = container;
    GridLayoutFactory.create(container).columns(3);
    // factory class
    {
      m_factoryClassField = new StringButtonDialogField(new IStringButtonAdapter() {
        public void changeControlPressed(DialogField field) {
          try {
            String factoryClassName =
                JdtUiUtils.selectTypeName(getShell(), m_editor.getJavaProject());
            if (factoryClassName != null) {
              m_factoryClassField.setText(factoryClassName);
            }
          } catch (Throwable e) {
            DesignerPlugin.log(e);
          }
        }
      });
      m_factoryClassField.setButtonLabel(Messages.FactoriesAddDialog_classChoose);
      doCreateField(m_factoryClassField, Messages.FactoriesAddDialog_classLabel);
      m_factoryClassField.getTextControl(null).setEditable(false);
    }
    // method signatures
    {
      m_signaturesField =
          new CheckedListDialogField(new IListAdapter() {
            public void selectionChanged(ListDialogField field) {
            }

            public void doubleClicked(ListDialogField field) {
            }

            public void customButtonPressed(ListDialogField field, int index) {
            }
          }, new String[]{
              Messages.FactoriesAddDialog_selectAllButton,
              Messages.FactoriesAddDialog_deselectAllButton}, new LabelProvider());
      m_signaturesField.setCheckAllButtonIndex(0);
      m_signaturesField.setUncheckAllButtonIndex(1);
      doCreateField(m_signaturesField, Messages.FactoriesAddDialog_methodsLabel);
    }
    // category
    {
      m_categoryField = createCategoryField(m_palette, m_initialCategory);
      doCreateField(m_categoryField, Messages.FactoriesAddDialog_categoryLabel);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validation
  //
  ////////////////////////////////////////////////////////////////////////////
  private String m_factoryClassName;

  @Override
  protected String validate() {
    // validate class
    {
      String factoryClassName = m_factoryClassField.getText().trim();
      if (factoryClassName.length() == 0) {
        return Messages.FactoriesAddDialog_validateEmptyClass;
      }
      // check for existence
      if (!factoryClassName.equals(m_factoryClassName)) {
        m_factoryClassName = factoryClassName;
        Class<?> factoryClass;
        try {
          EditorState state = EditorState.get(m_editor);
          factoryClass = state.getEditorLoader().loadClass(factoryClassName);
          // validate signatures
          {
            Map<String, FactoryMethodDescription> signaturesMap =
                FactoryDescriptionHelper.getDescriptionsMap(m_editor, factoryClass, m_forStatic);
            // set signatures
            m_signaturesField.setElements(signaturesMap.keySet());
            // validate
            if (signaturesMap.isEmpty()) {
              return MessageFormat.format(
                  Messages.FactoriesAddDialog_validateNoMethods,
                  factoryClassName);
            }
          }
        } catch (Throwable e) {
          return e.getClass().getName() + ": " + e.getMessage();
        }
      }
    }
    // validate signatures selection
    if (m_signaturesField.getCheckedElements().isEmpty()) {
      return Messages.FactoriesAddDialog_validateNoMethodSelection;
    }
    // OK
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Commands
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Command}'s for adding {@link StaticFactoryEntryInfo}'s.
   */
  public List<Command> getCommands() {
    CategoryInfo category = m_palette.getCategories().get(m_categoryField.getSelectionIndex());
    //
    List<?> signatures = m_signaturesField.getCheckedElements();
    List<Command> commands = Lists.newArrayList();
    for (int i = 0; i < signatures.size(); i++) {
      String signature = (String) signatures.get(i);
      //
      String id = "custom_" + System.currentTimeMillis() + "_" + i;
      String name = signature;
      String description = null;
      //
      commands.add(new FactoryAddCommand(id,
          name,
          description,
          true,
          m_factoryClassName,
          signature,
          m_forStatic,
          category));
    }
    //
    return commands;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private Composite m_fieldsContainer;

  /**
   * Configures given {@link DialogField} for specific of this dialog.
   */
  protected void doCreateField(DialogField dialogField, String labelText) {
    dialogField.setLabelText(labelText);
    dialogField.setDialogFieldListener(m_validateListener);
    DialogFieldUtils.fillControls(m_fieldsContainer, dialogField, 3, 60);
  }
}
