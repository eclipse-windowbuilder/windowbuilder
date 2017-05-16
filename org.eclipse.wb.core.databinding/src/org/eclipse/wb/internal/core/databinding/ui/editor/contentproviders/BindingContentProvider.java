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
package org.eclipse.wb.internal.core.databinding.ui.editor.contentproviders;

import com.google.common.collect.Sets;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.databinding.Messages;
import org.eclipse.wb.internal.core.databinding.model.IASTObjectInfo2;
import org.eclipse.wb.internal.core.databinding.ui.editor.ICompleteListener;
import org.eclipse.wb.internal.core.databinding.ui.editor.IUiContentProvider;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

import java.util.Set;

/**
 * Content provider for edit binding variable.
 *
 * @author lobas_av
 * @coverage bindings.ui
 */
public final class BindingContentProvider implements IUiContentProvider {
  private final Set<String> m_fields = Sets.newHashSet();
  private final IASTObjectInfo2 m_binding;
  private final JavaInfo m_javaInfoRoot;
  private ExpandableComposite m_expandableComposite;
  private Button m_assignButton;
  private Text m_fieldNameText;
  private String m_fieldName;
  private ICompleteListener m_listener;
  private String m_errorMessage;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public BindingContentProvider(IASTObjectInfo2 binding, JavaInfo javaInfoRoot) {
    m_binding = binding;
    m_javaInfoRoot = javaInfoRoot;
    TypeDeclaration rootNode = JavaInfoUtils.getTypeDeclaration(m_javaInfoRoot);
    for (FieldDeclaration field : rootNode.getFields()) {
      for (VariableDeclarationFragment fragment : DomGenerics.fragments(field)) {
        m_fields.add(fragment.getName().getIdentifier());
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Complete
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setCompleteListener(ICompleteListener listener) {
    m_listener = listener;
  }

  public String getErrorMessage() {
    return m_errorMessage;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // GUI
  //
  ////////////////////////////////////////////////////////////////////////////
  public int getNumberOfControls() {
    return 1;
  }

  public void createContent(final Composite parent, int columns) {
    // expand composite
    m_expandableComposite = new ExpandableComposite(parent, SWT.NONE);
    m_expandableComposite.setText(Messages.BindingContentProvider_binding);
    m_expandableComposite.setExpanded(true);
    GridDataFactory.create(m_expandableComposite).fillH().grabH().spanH(columns);
    m_expandableComposite.addExpansionListener(new IExpansionListener() {
      public void expansionStateChanging(ExpansionEvent e) {
        if (m_expandableComposite.isExpanded()) {
          m_expandableComposite.setText(Messages.BindingContentProvider_bindingDots);
        } else {
          m_expandableComposite.setText(Messages.BindingContentProvider_binding);
        }
      }

      public void expansionStateChanged(ExpansionEvent e) {
        parent.layout();
      }
    });
    // client composite
    Composite clientComposite = new Composite(m_expandableComposite, SWT.NONE);
    GridLayoutFactory.create(clientComposite).columns(2).noMargins();
    m_expandableComposite.setClient(clientComposite);
    // field editor
    m_assignButton = new Button(clientComposite, SWT.CHECK);
    m_assignButton.setText(Messages.BindingContentProvider_assignButton);
    //
    m_fieldNameText = new Text(clientComposite, SWT.SINGLE | SWT.BORDER);
    GridDataFactory.create(m_fieldNameText).fillH().grabH();
    m_fieldNameText.setEnabled(false);
    //
    m_fieldNameText.addModifyListener(new ModifyListener() {
      public void modifyText(ModifyEvent e) {
        calculateAssignComplete();
      }
    });
    m_assignButton.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        m_fieldNameText.setEnabled(m_assignButton.getSelection());
        if (m_assignButton.getSelection()) {
          m_fieldNameText.setFocus();
        }
        calculateAssignComplete();
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Handle
  //
  ////////////////////////////////////////////////////////////////////////////
  private void calculateAssignComplete() {
    if (m_assignButton.getSelection()) {
      String fieldName = m_fieldNameText.getText();
      // check field
      if (fieldName.length() == 0) {
        m_errorMessage = Messages.BindingContentProvider_validateEmptyFieldName;
      } else if (fieldName.equals(m_fieldName) || !m_fields.contains(fieldName)) {
        IStatus status = JavaConventions.validateIdentifier(fieldName, null, null);
        // check status
        if (status.matches(IStatus.ERROR)) {
          m_errorMessage = status.getMessage();
        } else {
          m_errorMessage = null;
        }
      } else {
        m_errorMessage = Messages.BindingContentProvider_validateExistingFieldName;
      }
    } else {
      m_errorMessage = null;
    }
    m_listener.calculateFinish();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Update
  //
  ////////////////////////////////////////////////////////////////////////////
  public void updateFromObject() throws Exception {
    m_fieldName = m_binding.getVariableIdentifier();
    if (m_binding.isField()) {
      m_expandableComposite.setExpanded(true);
      m_expandableComposite.setText(Messages.BindingContentProvider_binding);
      m_assignButton.setSelection(true);
      m_fieldNameText.setText(m_fieldName);
      m_fieldNameText.setEnabled(true);
    }
    calculateAssignComplete();
  }

  public void saveToObject() throws Exception {
    boolean field = m_assignButton.getSelection();
    m_binding.setVariableIdentifier(m_javaInfoRoot, field ? m_fieldNameText.getText() : null, field);
  }
}