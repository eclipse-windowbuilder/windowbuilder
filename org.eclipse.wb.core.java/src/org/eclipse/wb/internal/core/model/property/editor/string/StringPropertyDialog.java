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
package org.eclipse.wb.internal.core.model.property.editor.string;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.ModelMessages;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.nls.NlsSupport;
import org.eclipse.wb.internal.core.nls.edit.IEditableSource;
import org.eclipse.wb.internal.core.nls.edit.IEditableSupport;
import org.eclipse.wb.internal.core.nls.model.AbstractSource;
import org.eclipse.wb.internal.core.nls.model.LocaleInfo;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;
import org.eclipse.wb.internal.core.utils.ui.dialogs.ResizableDialog;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * {@link Dialog} for editing value in {@link StringPropertyEditor}.
 *
 * @author scheglov_ke
 * @coverage core.model.property.editor
 */
public final class StringPropertyDialog extends ResizableDialog {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Final fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private final Property m_property;
  private final JavaInfo m_component;
  private final GenericProperty m_genericProperty;
  private final NlsSupport m_support;
  private final IEditableSupport m_editableSupport;
  private final LocaleInfo m_locale;
  private final AbstractSource m_initialSource;
  ////////////////////////////////////////////////////////////////////////////
  //
  // State fields
  //
  ////////////////////////////////////////////////////////////////////////////
  private IEditableSource m_selectedEditSource;
  private String m_selectedKey;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public StringPropertyDialog(Shell parentShell, Property property) throws Exception {
    super(parentShell, DesignerPlugin.getDefault());
    m_property = property;
    if (m_property instanceof GenericProperty) {
      m_genericProperty = (GenericProperty) m_property;
      m_component = m_genericProperty.getJavaInfo();
      // NLSSupport
      m_support = NlsSupport.get(m_component);
      m_editableSupport = m_support.getEditable();
      m_locale = AbstractSource.getLocaleInfo(m_component);
      // source
      {
        Expression expression = m_genericProperty.getExpression();
        if (expression != null) {
          m_initialSource = NlsSupport.getSource(expression);
          m_selectedEditSource = m_editableSupport.getEditableSource(m_initialSource);
          // selected source/key
          if (m_selectedEditSource != null) {
            m_selectedKey = m_initialSource.getKey(expression);
          }
        } else {
          m_initialSource = null;
        }
      }
    } else {
      m_genericProperty = null;
      m_component = null;
      m_support = null;
      m_editableSupport = null;
      m_locale = null;
      //
      m_initialSource = null;
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  private Text m_valueText;
  private Composite m_nlsComposite;
  private Button m_nlsButton;
  private Text m_nlsSourceText;
  private Text m_nlsKeyText;

  @Override
  public void create() {
    super.create();
    // show initial source/key
    updateSourceKey();
    m_valueText.selectAll();
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite area = (Composite) super.createDialogArea(parent);
    // value
    {
      m_valueText = new Text(area, SWT.BORDER | SWT.MULTI | SWT.WRAP);
      GridDataFactory.create(m_valueText).grab().hintC(80, 8).fill();
      // initial value
      ExecutionUtils.runLog(new RunnableEx() {
        public void run() throws Exception {
          Object value = m_property.getValue();
          if (value instanceof String) {
            m_valueText.setText((String) value);
          }
        }
      });
      // handle Ctrl+Enter as OK
      m_valueText.addKeyListener(new KeyAdapter() {
        @Override
        public void keyPressed(KeyEvent e) {
          if (e.stateMask == SWT.CTRL && e.keyCode == SWT.CR) {
            okPressed();
          }
        }
      });
    }
    // localization
    if (m_genericProperty != null) {
      Group group = new Group(area, SWT.NONE);
      GridDataFactory.create(group).fill();
      GridLayoutFactory.create(group);
      group.setText(ModelMessages.StringPropertyDialog_localizationGroup);
      // flag of using existing NLS key
      {
        m_nlsButton = new Button(group, SWT.CHECK);
        m_nlsButton.setText(ModelMessages.StringPropertyDialog_localizationUseExisting);
        m_nlsButton.setEnabled(!m_editableSupport.getEditableSources().isEmpty());
        // initial state
        ExecutionUtils.runLog(new RunnableEx() {
          public void run() throws Exception {
            m_nlsButton.setSelection(m_support.isExternalized(m_genericProperty.getExpression()));
          }
        });
        // add selection listener
        m_nlsButton.addListener(SWT.Selection, new Listener() {
          public void handleEvent(Event event) {
            UiUtils.changeControlEnable(m_nlsComposite, m_nlsButton.getSelection());
            updateSourceKey();
          }
        });
      }
      // NLS container
      {
        m_nlsComposite = new Composite(group, SWT.NONE);
        GridDataFactory.create(m_nlsComposite).grab().fill();
        GridLayoutFactory.create(m_nlsComposite).columns(3).noMargins();
        // source
        {
          new Label(m_nlsComposite, SWT.NONE).setText(ModelMessages.StringPropertyDialog_localizationSourceLabel);
          //
          m_nlsSourceText = new Text(m_nlsComposite, SWT.BORDER | SWT.READ_ONLY);
          GridDataFactory.create(m_nlsSourceText).grabH().fillH();
        }
        // browse for source/key
        {
          Button browseButton = new Button(m_nlsComposite, SWT.NONE);
          GridDataFactory.create(browseButton).hintHU(50).spanV(2).fill();
          browseButton.setText(ModelMessages.StringPropertyDialog_localizationBrowse);
          // add selection listener
          browseButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
              StringPropertyKeyDialog keyDialog =
                  new StringPropertyKeyDialog(getShell(),
                      m_editableSupport,
                      m_selectedEditSource,
                      m_selectedKey,
                      m_locale);
              if (keyDialog.open() == OK) {
                m_selectedEditSource = keyDialog.getSelectedSource();
                m_selectedKey = keyDialog.getSelectedKey();
                updateSourceKey();
              }
            }
          });
        }
        // key
        {
          new Label(m_nlsComposite, SWT.NONE).setText(ModelMessages.StringPropertyDialog_localizationKeyLabel);
          //
          m_nlsKeyText = new Text(m_nlsComposite, SWT.BORDER | SWT.READ_ONLY);
          GridDataFactory.create(m_nlsKeyText).grabH().fillH();
        }
      }
      // update enable state for NLS container
      UiUtils.changeControlEnable(m_nlsComposite, m_nlsButton.getSelection());
    }
    //
    return area;
  }

  /**
   * Shows source/key in {@link Control}'s.
   */
  private void updateSourceKey() {
    if (m_genericProperty != null) {
      if (m_nlsButton.getSelection() && m_selectedKey != null) {
        m_nlsSourceText.setText(m_selectedEditSource.getLongTitle());
        m_nlsKeyText.setText(m_selectedKey);
        String value = m_selectedEditSource.getValue(m_locale, m_selectedKey);
        if (value != null) {
          m_valueText.setText(value);
        }
      }
      // update OK button
      {
        Button okButton = getButton(IDialogConstants.OK_ID);
        if (m_nlsButton.getSelection()) {
          okButton.setEnabled(m_selectedKey != null);
        } else {
          okButton.setEnabled(true);
        }
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Shell
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void configureShell(Shell newShell) {
    super.configureShell(newShell);
    newShell.setText(ModelMessages.StringPropertyDialog_title);
  }

  @Override
  protected void okPressed() {
    final String value = m_valueText.getText();
    if (m_component != null) {
      ExecutionUtils.run(m_component, new RunnableEx() {
        public void run() throws Exception {
          if (m_genericProperty != null) {
            // replace with StringLiteral
            if (!m_nlsButton.getSelection() && m_initialSource != null) {
              m_initialSource.replace_toStringLiteral(m_genericProperty, value);
              return;
            }
            // use different key in different source
            if (m_nlsButton.getSelection()) {
              AbstractSource selectedSource =
                  m_support.getAttachedSource(m_editableSupport, m_selectedEditSource);
              selectedSource.useKey(m_genericProperty, m_selectedKey);
              if (isValueDifferentThanInSource()) {
                m_property.setValue(value);
              }
              return;
            }
          }
          m_property.setValue(value);
        }

        /**
         * @return <code>true</code> if value in NLS source and in editor is not same, so user
         *         changed it and we should update NLS source.
         */
        private boolean isValueDifferentThanInSource() {
          String valueInSource = m_selectedEditSource.getValue(m_locale, m_selectedKey);
          return !value.equals(valueInSource);
        }
      });
    } else {
      ExecutionUtils.runLog(new RunnableEx() {
        public void run() throws Exception {
          m_property.setValue(value);
        }
      });
    }
    // close dialog
    super.okPressed();
  }
}
