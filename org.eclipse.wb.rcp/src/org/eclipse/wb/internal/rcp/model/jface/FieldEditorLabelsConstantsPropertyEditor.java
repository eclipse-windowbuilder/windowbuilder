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
package org.eclipse.wb.internal.rcp.model.jface;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.eclipse.wb.core.controls.jface.preference.ComboFieldEditor;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.JavaInfoEvaluationHelper;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.converter.StringConverter;
import org.eclipse.wb.internal.core.model.property.editor.ConstantSelectionPropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDialogPropertyEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.dialogfields.AbstractValidationTitleAreaDialog;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.jdt.core.CodeUtils;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.rcp.Activator;
import org.eclipse.wb.internal.rcp.model.ModelMessages;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;
import java.util.List;
import java.util.Set;

/**
 * {@link PropertyEditor} for labels/constants property of {@link RadioGroupFieldEditor} or
 * {@link ComboFieldEditor}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.jface
 */
public final class FieldEditorLabelsConstantsPropertyEditor extends TextDialogPropertyEditor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final PropertyEditor INSTANCE = new FieldEditorLabelsConstantsPropertyEditor();

  private FieldEditorLabelsConstantsPropertyEditor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getText(Property property) throws Exception {
    return ModelMessages.FieldEditorLabelsConstantsPropertyEditor_text;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void openDialog(Property _property) throws Exception {
    GenericProperty property = (GenericProperty) _property;
    LabelsValues_Dialog dialog = new LabelsValues_Dialog(DesignerPlugin.getShell(), property);
    if (dialog.open() == Window.OK) {
      setLabelsFields(dialog.m_resultLabels, dialog.m_resultFields, property);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing implementation
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the textual presentation of {@link Expression} for given {@link GenericProperty}, may
   *         be empty {@link String}. Ignores invalid elements of array.
   */
  private static String getTextForEditing(GenericProperty property) throws Exception {
    if (property.isModified()) {
      StringBuilder stringBuilder = new StringBuilder();
      JavaInfo javaInfo = property.getJavaInfo();
      IJavaProject javaProject = javaInfo.getEditor().getJavaProject();
      // analyze Expression's
      ArrayCreation itemsCreation = (ArrayCreation) property.getExpression();
      ArrayInitializer itemsInitializer = itemsCreation.getInitializer();
      for (Expression itemExpression : DomGenerics.expressions(itemsInitializer)) {
        // prepare {label, value} initializer
        ArrayInitializer itemInitializer;
        if (itemExpression instanceof ArrayInitializer) {
          itemInitializer = (ArrayInitializer) itemExpression;
        } else {
          itemInitializer = ((ArrayCreation) itemExpression).getInitializer();
        }
        // check that item has label/value pair
        List<Expression> itemExpressions = DomGenerics.expressions(itemInitializer);
        if (itemExpressions.size() == 2) {
          String label = (String) JavaInfoEvaluationHelper.getValue(itemExpressions.get(0));
          IField field =
              ConstantSelectionPropertyEditor.getField(javaProject, itemExpressions.get(1));
          if (field != null) {
            stringBuilder.append(label);
            stringBuilder.append(" ");
            stringBuilder.append(ConstantSelectionPropertyEditor.getFieldCode(javaInfo, field));
            stringBuilder.append(Text.DELIMITER);
          }
        }
      }
      return stringBuilder.toString();
    } else {
      return StringUtils.EMPTY;
    }
  }

  /**
   * @return the error message or <code>null</code>.
   */
  private static String prepareLabelsFields(List<String> resultLabels,
      List<IField> resultFields,
      GenericProperty property,
      String text) throws Exception {
    IJavaProject javaProject;
    String topTypeName;
    {
      JavaInfo javaInfo = property.getJavaInfo();
      javaProject = javaInfo.getEditor().getJavaProject();
      TypeDeclaration typeDeclaration = JavaInfoUtils.getTypeDeclaration(javaInfo);
      topTypeName = AstNodeUtils.getFullyQualifiedName(typeDeclaration, false);
    }
    // prepare containers
    List<String> tmpLabels = Lists.newArrayList();
    List<IField> tmpFields = Lists.newArrayList();
    resultLabels.clear();
    resultFields.clear();
    // analyze each line
    String[] lines = StringUtils.split(text, Text.DELIMITER);
    for (String line : lines) {
      line = line.trim();
      String[] words = StringUtils.split(line);
      if (words.length < 2) {
        return MessageFormat.format(
            ModelMessages.FieldEditorLabelsConstantsPropertyEditor_errLabelField,
            line);
      }
      // prepare "label" and "field" code
      int lastSpaceIndex = StringUtils.lastIndexOf(line, " ");
      String label = line.substring(0, lastSpaceIndex).trim();
      String fieldCode = line.substring(lastSpaceIndex).trim();
      // convert "fieldCode" into IField
      IField field;
      if (fieldCode.contains(".")) {
        String typeName = StringUtils.substringBeforeLast(fieldCode, ".");
        String fieldName = StringUtils.substringAfterLast(fieldCode, ".");
        field = CodeUtils.findField(javaProject, typeName, fieldName);
      } else {
        field = CodeUtils.findField(javaProject, topTypeName, fieldCode);
      }
      // we should have IField
      if (field == null) {
        return MessageFormat.format(
            ModelMessages.FieldEditorLabelsConstantsPropertyEditor_errInvalidFieldName,
            line);
      }
      // add label/field
      tmpLabels.add(label);
      tmpFields.add(field);
    }
    // OK
    resultLabels.addAll(tmpLabels);
    resultFields.addAll(tmpFields);
    return null;
  }

  /**
   * Sets the <code>String[][]</code> value using given labels and fields {@link List}'s.
   */
  private static void setLabelsFields(List<String> resultLabels,
      List<IField> resultFields,
      GenericProperty property) throws Exception {
    JavaInfo javaInfo = property.getJavaInfo();
    String source = "new String[][]{";
    for (int i = 0; i < resultLabels.size(); i++) {
      String label = resultLabels.get(i);
      IField field = resultFields.get(i);
      if (i != 0) {
        source += ", ";
      }
      source += "{";
      source += StringConverter.INSTANCE.toJavaSource(javaInfo, label);
      source += ", ";
      source += ConstantSelectionPropertyEditor.getFieldCode(javaInfo, field);
      source += "}";
    }
    source += "}";
    property.setExpression(source, Property.UNKNOWN_VALUE);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Dialog
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * The {@link Dialog} for editing label/value pairs.
   */
  private static final class LabelsValues_Dialog extends AbstractValidationTitleAreaDialog {
    private final GenericProperty m_property;
    private final JavaInfo m_javaInfo;
    private final String m_text;
    private final List<String> m_resultLabels = Lists.newArrayList();
    private final List<IField> m_resultFields = Lists.newArrayList();
    private final Set<IType> m_allTypes = Sets.newHashSet();

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public LabelsValues_Dialog(Shell parentShell, GenericProperty property) throws Exception {
      super(parentShell,
          Activator.getDefault(),
          property.getTitle(),
          ModelMessages.FieldEditorLabelsConstantsPropertyEditor_dialogTitle,
          null,
          ModelMessages.FieldEditorLabelsConstantsPropertyEditor_dialogMessage);
      m_property = property;
      m_javaInfo = m_property.getJavaInfo();
      m_text = getTextForEditing(property);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // GUI
    //
    ////////////////////////////////////////////////////////////////////////////
    private Text m_textWidget;

    @Override
    protected void createControls(Composite container) {
      GridLayoutFactory.create(container);
      // header
      new Label(container, SWT.NONE).setText(ModelMessages.FieldEditorLabelsConstantsPropertyEditor_dialogTextLabel);
      // Text widget
      {
        m_textWidget = new Text(container, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        GridDataFactory.create(m_textWidget).grab().fill().hintVC(10);
        m_textWidget.setText(m_text);
        // add listeners
        m_textWidget.addListener(SWT.Modify, new Listener() {
          @Override
          public void handleEvent(Event event) {
            validateAll();
          }
        });
        m_textWidget.addListener(SWT.KeyDown, new Listener() {
          @Override
          public void handleEvent(Event event) {
            if (event.stateMask == SWT.CTRL && event.keyCode == 'f') {
              event.doit = false;
              ExecutionUtils.runLog(new RunnableEx() {
                @Override
                public void run() throws Exception {
                  editLineField();
                }
              });
            }
          }
        });
      }
      // footer
      new Label(container, SWT.NONE).setText(ModelMessages.FieldEditorLabelsConstantsPropertyEditor_dialogFooter);
    }

    /**
     * Edit {@link IField} of currently selected line.
     */
    private void editLineField() throws Exception {
      Document document = new Document(m_textWidget.getText());
      int selectionOffset = m_textWidget.getSelection().x;
      int line = document.getLineOfOffset(selectionOffset);
      int lineOffset = document.getLineOffset(line);
      int lineLength = document.getLineLength(line);
      if (document.getLineDelimiter(line) != null) {
        lineLength -= document.getLineDelimiter(line).length();
      }
      // select new IField
      IField currentField = line < m_resultFields.size() ? m_resultFields.get(line) : null;
      IField selectedField =
          ConstantSelectionPropertyEditor.selectField(
              getShell(),
              "java.lang.String",
              m_javaInfo,
              m_allTypes,
              currentField);
      // update model
      if (selectedField != null) {
        String label;
        if (m_resultLabels.isEmpty()) {
          label = document.get(lineOffset, lineLength);
        } else {
          label = m_resultLabels.get(line);
        }
        // replace line text
        String newLineText =
            label.trim()
                + " "
                + ConstantSelectionPropertyEditor.getFieldCode(m_javaInfo, selectedField);
        document.replace(lineOffset, lineLength, newLineText);
        // update Text widget
        m_textWidget.setText(document.get());
        m_textWidget.setSelection(lineOffset + newLineText.length());
        // re-validate updated text
        validateAll();
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Validation
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected String validate() throws Exception {
      String errorMessage =
          prepareLabelsFields(m_resultLabels, m_resultFields, m_property, m_textWidget.getText());
      // remember all used IType's
      for (IField field : m_resultFields) {
        m_allTypes.add(field.getDeclaringType());
      }
      // may be OK, may be not...
      return errorMessage;
    }
  }
}
