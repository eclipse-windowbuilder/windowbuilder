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
package org.eclipse.wb.internal.core.nls.bundle.pure.field;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.nls.Messages;
import org.eclipse.wb.internal.core.nls.bundle.AbstractBundleSource;
import org.eclipse.wb.internal.core.nls.bundle.AbstractBundleSourceNewComposite;
import org.eclipse.wb.internal.core.nls.edit.IEditableSource;
import org.eclipse.wb.internal.core.utils.dialogfields.StringDialogField;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

import java.text.MessageFormat;

/**
 * Composite for creating new source.
 *
 * @author scheglov_ke
 * @coverage core.nls.ui
 */
public class FieldSourceNewComposite extends AbstractBundleSourceNewComposite {
  private StringDialogField m_fieldNameField;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FieldSourceNewComposite(Composite parent, int style, JavaInfo root) {
    super(parent, style, root);
    // create GUI controls
    createFieldGroup();
    createPropertyGroup();
    // initialize fields
    m_fieldNameField.setText("BUNDLE");
    initializePropertyGroup();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Field group
  //
  ////////////////////////////////////////////////////////////////////////////
  private void createFieldGroup() {
    Group fieldGroup = new Group(this, SWT.NONE);
    fieldGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
    fieldGroup.setLayout(new GridLayout(3, false));
    fieldGroup.setText(Messages.FieldSourceNewComposite_fieldGroup);
    // create field name field
    {
      m_fieldNameField = new StringDialogField();
      m_fieldNameField.setDialogFieldListener(m_validateListener);
      m_fieldNameField.setLabelText(Messages.FieldSourceNewComposite_fieldNameLabel);
      createTextFieldControls(fieldGroup, m_fieldNameField, 3);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Validation
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String KEY_FIELD_NAME = "KEY_FIELD_NAME";

  @Override
  protected void validateAll() {
    validateFieldName();
    super.validateAll();
  }

  private void validateFieldName() {
    final String fieldName = m_fieldNameField.getText();
    // check that there are no field or variable with same name
    {
      // check, may be there is such name in CU
      final boolean hasSuchName[] = new boolean[1];
      m_editor.getAstUnit().accept(new ASTVisitor() {
        @Override
        public void endVisit(SimpleName node) {
          hasSuchName[0] |= node.getIdentifier().equals(fieldName);
        }
      });
      // set status
      if (hasSuchName[0]) {
        setInvalid(
            KEY_FIELD_NAME,
            MessageFormat.format(Messages.FieldSourceNewComposite_validateFieldExists, fieldName));
        return;
      }
    }
    // validate that field is valid identifier
    {
      IStatus status = JavaConventions.validateFieldName(fieldName);
      if (!status.isOK()) {
        setStatus(KEY_FIELD_NAME, status);
        return;
      }
    }
    // all is good
    setValid(KEY_FIELD_NAME);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Display
  //
  ////////////////////////////////////////////////////////////////////////////
  public static String getTitle() {
    return Messages.FieldSourceNewComposite_title;
  }

  @Override
  public String getSample() {
    return "private static final BUNDLE = ResourceBundle.getBundle(\"full.bundle.name\");\n"
        + "...\n"
        + "button.setText( BUNDLE.getString(\"some.key\") );";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creating
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public IEditableSource createEditableSource(Object o) {
    SourceParameters parameters = (SourceParameters) o;
    try {
      // create editable source
      IEditableSource editableSource;
      {
        // check, may be we already have such property file
        if (parameters.m_propertyFileExists) {
          String fieldName = m_fieldNameField.getText();
          // use existing property file
          FieldSource source = new FieldSource(m_root, parameters.m_propertyBundleName, fieldName);
          editableSource = source.getEditable();
        } else {
          editableSource = createEmptyEditable(parameters.m_propertyBundleName);
        }
      }
      // configure editable source and return
      editableSource.setKeyGeneratorStrategy(AbstractBundleSource.KEY_GENERATOR);
      return editableSource;
    } catch (Throwable e) {
      throw ReflectionUtils.propagate(e);
    }
  }

  @Override
  public Object createParametersObject() throws Exception {
    SourceParameters parameters = new SourceParameters();
    parameters.m_fieldName = m_fieldNameField.getText();
    fillPropertyParameters(parameters);
    return parameters;
  }
}
