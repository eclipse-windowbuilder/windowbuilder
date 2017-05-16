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
package org.eclipse.wb.internal.core.editor.palette;

import org.eclipse.wb.internal.core.editor.Messages;
import org.eclipse.wb.internal.core.model.description.CreationDescription.TypeParameterDescription;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.jdt.ui.JdtUiUtils;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.UiUtils;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;

/**
 * {@link Composite} provides selecting argument for type parameter of creating generic component.
 *
 * @author sablin_aa
 * @coverage core.editor.palette.ui
 */
public class TypeParameterComposite extends Composite {
  private final TypeParameterDescription m_typeParameter;
  private final IJavaProject m_javaProject;
  private final Label m_label;
  private final Text m_text;
  private final Button m_button;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TypeParameterComposite(Composite parent,
      int style,
      IJavaProject javaProject,
      TypeParameterDescription typeParameter) {
    super(parent, style);
    GridLayoutFactory.create(this).columns(3);
    m_typeParameter = typeParameter;
    m_javaProject = javaProject;
    // label
    m_label = new Label(this, SWT.NONE);
    GridDataFactory.create(m_label).alignHR();
    m_label.setText(m_typeParameter.getTitle() + ":");
    // text
    m_text = new Text(this, SWT.BORDER);
    GridDataFactory.create(m_text).grabH().fillH();
    m_text.setText(m_typeParameter.getTypeName());
    // button
    m_button = new Button(this, SWT.NONE);
    m_button.setText("...");
    m_button.addSelectionListener(new SelectionAdapter() {
      @Override
      public void widgetSelected(SelectionEvent e) {
        ExecutionUtils.runLog(new RunnableEx() {
          public void run() throws Exception {
            chooseType();
          }
        });
      }
    });
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public String getArgument() {
    String argumentText = m_text.getText();
    return StringUtils.isEmpty(argumentText) ? m_typeParameter.getTypeName() : argumentText;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Event handlers
  //
  ////////////////////////////////////////////////////////////////////////////
  private void chooseType() throws Exception {
    IType selectedType = JdtUiUtils.selectType(getShell(), m_javaProject);
    // cancel
    if (selectedType == null) {
      return;
    }
    // should be subtype of "parameter"
    {
      String parameterTypeName = m_typeParameter.getTypeName();
      IType parameterType = m_javaProject.findType(parameterTypeName);
      ITypeHierarchy hierarchy = selectedType.newSupertypeHierarchy(null);
      if (!hierarchy.contains(parameterType)) {
        String message =
            MessageFormat.format(
                Messages.TypeParameterComposite_subTypeMessage,
                selectedType.getFullyQualifiedName(),
                parameterTypeName);
        UiUtils.openError(getShell(), Messages.TypeParameterComposite_subTypeTitle, message);
        return;
      }
    }
    // OK
    m_text.setText(selectedType.getFullyQualifiedName());
  }
}
