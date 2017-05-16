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

import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.editor.Messages;
import org.eclipse.wb.internal.core.editor.palette.dialogs.AbstractPaletteElementDialog;
import org.eclipse.wb.internal.core.editor.palette.model.entry.FactoryEntryInfo;
import org.eclipse.wb.internal.core.model.description.factory.FactoryMethodDescription;
import org.eclipse.wb.internal.core.model.description.helpers.FactoryDescriptionHelper;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.dialogfields.BooleanDialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogFieldUtils;
import org.eclipse.wb.internal.core.utils.dialogfields.IStringButtonAdapter;
import org.eclipse.wb.internal.core.utils.dialogfields.StringAreaDialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.StringButtonDialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.StringDialogField;
import org.eclipse.wb.internal.core.utils.jdt.ui.JdtUiUtils;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;
import java.util.Map;

/**
 * Abstract dialog for {@link FactoryEntryInfo}.
 *
 * @author scheglov_ke
 * @coverage core.editor.palette.ui
 */
public abstract class FactoryAbstractDialog extends AbstractPaletteElementDialog {
  private final AstEditor m_editor;
  protected final boolean m_forStatic;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FactoryAbstractDialog(Shell parentShell,
      AstEditor editor,
      boolean forStatic,
      String shellText,
      String titleText) {
    super(parentShell, shellText, titleText, null, Messages.FactoryAbstractDialog_message);
    m_editor = editor;
    m_forStatic = forStatic;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  protected StringDialogField m_nameField;
  protected StringButtonDialogField m_factoryClassField;
  protected StringButtonDialogField m_methodSignatureField;
  protected StringAreaDialogField m_descriptionField;
  protected BooleanDialogField m_visibleField;

  @Override
  protected void createControls(Composite container) {
    m_fieldsContainer = container;
    GridLayoutFactory.create(container).columns(3);
    // name
    {
      m_nameField = new StringDialogField();
      doCreateField(m_nameField, Messages.FactoryAbstractDialog_nameLabel);
    }
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
      m_factoryClassField.setButtonLabel(Messages.FactoryAbstractDialog_classChoose);
      doCreateField(m_factoryClassField, Messages.FactoryAbstractDialog_classLabel);
      m_factoryClassField.getTextControl(null).setEditable(false);
    }
    // method signature
    {
      m_methodSignatureField = new StringButtonDialogField(new IStringButtonAdapter() {
        public void changeControlPressed(DialogField field) {
          try {
            ElementListSelectionDialog dialog =
                new ElementListSelectionDialog(getShell(), new LabelProvider() {
                  @Override
                  public Image getImage(Object element) {
                    return JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_PUBLIC);
                  }

                  @Override
                  public String getText(Object element) {
                    return super.getText(element);
                  }
                });
            dialog.setTitle(Messages.FactoryAbstractDialog_methodDialogTitle);
            dialog.setMessage(Messages.FactoryAbstractDialog_methodDialogMessage);
            // set signatures
            dialog.setElements(m_signaturesMap.keySet().toArray());
            // open dialog
            if (dialog.open() == Window.OK) {
              String signature = (String) dialog.getFirstResult();
              m_methodSignatureField.setText(signature);
              // update presentation
              m_nameField.setText(signature);
              m_descriptionField.setText("");
            }
          } catch (Throwable e) {
            DesignerPlugin.log(e);
          }
        }
      });
      m_methodSignatureField.setButtonLabel(Messages.FactoryAbstractDialog_methodChoose);
      doCreateField(m_methodSignatureField, Messages.FactoryAbstractDialog_methodLabel);
      m_methodSignatureField.getTextControl(null).setEditable(false);
    }
    // description
    {
      m_descriptionField = new StringAreaDialogField(5);
      doCreateField(m_descriptionField, Messages.FactoryAbstractDialog_descriptionLabel);
      GridDataFactory.modify(m_descriptionField.getTextControl(null)).grabV();
    }
    // state
    {
      m_visibleField = new BooleanDialogField();
      doCreateField(m_visibleField, Messages.FactoryAbstractDialog_visibleFlag);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validation
  //
  ////////////////////////////////////////////////////////////////////////////
  private Map<String, FactoryMethodDescription> m_signaturesMap;

  @Override
  protected final String validate() {
    // validate class
    {
      String factoryClassName = m_factoryClassField.getText().trim();
      if (factoryClassName.length() == 0) {
        return Messages.FactoryAbstractDialog_validateEmptyClass;
      }
      // check for existence
      try {
        EditorState state = EditorState.get(m_editor);
        Class<?> factoryClass = state.getEditorLoader().loadClass(factoryClassName);
        m_signaturesMap =
            FactoryDescriptionHelper.getDescriptionsMap(m_editor, factoryClass, m_forStatic);
      } catch (Throwable e) {
        return e.getClass().getName() + ": " + e.getMessage();
      }
      // validate signatures
      if (m_signaturesMap.isEmpty()) {
        return MessageFormat.format(
            Messages.FactoryAbstractDialog_validateNoMethods,
            factoryClassName);
      }
    }
    // validate signature
    {
      String signature = m_methodSignatureField.getText();
      if (signature.length() == 0) {
        return Messages.FactoryAbstractDialog_validateEmptyMethod;
      }
    }
    // validate name
    {
      String name = m_nameField.getText().trim();
      if (name.length() == 0) {
        return Messages.FactoryAbstractDialog_validateEmptyName;
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

  /**
   * @return the description text to use in command, may be <code>null</code> if entered text is
   *         default, so description from metadata should be used.
   */
  protected final String getDescriptionText() {
    String text = m_descriptionField.getText();
    boolean isEmpty = StringUtils.isEmpty(text);
    if (isEmpty) {
      text = null;
    }
    return text;
  }
}
