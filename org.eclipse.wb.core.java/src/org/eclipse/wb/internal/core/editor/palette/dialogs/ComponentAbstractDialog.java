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
package org.eclipse.wb.internal.core.editor.palette.dialogs;

import org.eclipse.wb.core.editor.palette.model.entry.ComponentEntryInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.Messages;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.description.helpers.ComponentDescriptionHelper;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.dialogfields.BooleanDialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogFieldUtils;
import org.eclipse.wb.internal.core.utils.dialogfields.IStringButtonAdapter;
import org.eclipse.wb.internal.core.utils.dialogfields.StringAreaDialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.StringButtonDialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.StringDialogField;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.jdt.ui.JdtUiUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import org.apache.commons.lang.StringUtils;

/**
 * Abstract dialog for {@link ComponentEntryInfo}.
 *
 * @author scheglov_ke
 * @coverage core.editor.palette.ui
 */
public abstract class ComponentAbstractDialog extends AbstractPaletteElementDialog {
  private final AstEditor m_editor;
  private ComponentDescription m_componentDescription;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ComponentAbstractDialog(Shell parentShell,
      AstEditor editor,
      String shellText,
      String titleText) {
    super(parentShell, shellText, titleText, null, Messages.ComponentAbstractDialog_message);
    m_editor = editor;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  protected StringDialogField m_idField;
  protected StringDialogField m_nameField;
  protected StringButtonDialogField m_classField;
  protected StringAreaDialogField m_descriptionField;
  protected BooleanDialogField m_visibleField;

  @Override
  protected void createControls(Composite container) {
    m_fieldsContainer = container;
    GridLayoutFactory.create(container).columns(3);
    // id
    {
      m_idField = new StringDialogField();
      m_idField.setEditable(false);
      doCreateField(m_idField, Messages.ComponentAbstractDialog_idLabel);
    }
    // name
    {
      m_nameField = new StringDialogField();
      doCreateField(m_nameField, Messages.ComponentAbstractDialog_nameLabel);
      m_nameField.setFocus();
    }
    // class
    {
      m_classField = new StringButtonDialogField(new IStringButtonAdapter() {
        public void changeControlPressed(DialogField field) {
          try {
            String componentClassName =
                JdtUiUtils.selectTypeName(getShell(), m_editor.getJavaProject());
            if (componentClassName != null) {
              m_classField.setText(componentClassName);
              prepareComponentDescription();
              m_nameField.setText(CodeUtils.getShortClass(componentClassName));
              if (m_componentDescription != null) {
                m_descriptionField.setText(m_componentDescription.getDescription());
              } else {
                m_descriptionField.setText(componentClassName);
              }
            }
          } catch (Throwable e) {
            DesignerPlugin.log(e);
          }
        }
      });
      m_classField.setButtonLabel(Messages.ComponentAbstractDialog_classButton);
      doCreateField(m_classField, Messages.ComponentAbstractDialog_classLabel);
      m_classField.getTextControl(null).setEditable(false);
    }
    // description
    {
      m_descriptionField = new StringAreaDialogField(5);
      doCreateField(m_descriptionField, Messages.ComponentAbstractDialog_descriptionLabel);
      GridDataFactory.modify(m_descriptionField.getTextControl(null)).grabV();
    }
    // state
    {
      m_visibleField = new BooleanDialogField();
      doCreateField(m_visibleField, Messages.ComponentAbstractDialog_visibleFlag);
    }
  }

  @Override
  protected void okPressed() {
    prepareComponentDescription();
    super.okPressed();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected final String validate() {
    // validate class
    {
      String className = m_classField.getText().trim();
      if (className.length() == 0) {
        return Messages.ComponentAbstractDialog_validateEmptyClass;
      }
      // check for existence
      try {
        EditorState state = EditorState.get(m_editor);
        state.getEditorLoader().loadClass(className);
      } catch (Throwable e) {
        return e.getClass().getName() + ": " + e.getMessage();
      }
    }
    // validate name
    {
      String name = m_nameField.getText().trim();
      if (name.length() == 0) {
        return Messages.ComponentAbstractDialog_validateEmptyName;
      }
    }
    // OK
    return null;
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
  protected final void doCreateField(DialogField dialogField, String labelText) {
    dialogField.setLabelText(labelText);
    dialogField.setDialogFieldListener(m_validateListener);
    DialogFieldUtils.fillControls(m_fieldsContainer, dialogField, 3, 60);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ComponentDescription utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Tries to load {@link ComponentDescription}, may be set <code>null</code>.
   */
  private void prepareComponentDescription() {
    m_componentDescription = null;
    try {
      String componentClassName = m_classField.getText();
      m_componentDescription =
          ComponentDescriptionHelper.getDescription(m_editor, componentClassName);
    } catch (Throwable e) {
    }
  }

  /**
   * @return the description text to use in command, may be <code>null</code> if entered text is
   *         default, so description from metadata should be used.
   */
  protected String getDescriptionText() {
    String text = m_descriptionField.getText();
    boolean isEmpty = StringUtils.isEmpty(text);
    boolean isClassName = m_classField.getText().equals(text);
    boolean isSameAsMetadata =
        m_componentDescription != null && text.equals(m_componentDescription.getDescription());
    if (isEmpty || isClassName || isSameAsMetadata) {
      text = null;
    }
    return text;
  }
}
