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

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDialogPropertyEditor;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.dialogfields.AbstractValidationTitleAreaDialog;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogField;
import org.eclipse.wb.internal.core.utils.dialogfields.DialogFieldUtils;
import org.eclipse.wb.internal.core.utils.dialogfields.StringDialogField;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.ui.GridDataFactory;
import org.eclipse.wb.internal.core.utils.ui.GridLayoutFactory;
import org.eclipse.wb.internal.core.utils.ui.TableFactory;
import org.eclipse.wb.internal.core.utils.ui.dialogs.ResizableDialog;
import org.eclipse.wb.internal.rcp.Activator;
import org.eclipse.wb.internal.rcp.model.ModelMessages;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link PropertyEditor} for ID of {@link Button} on {@link DialogInfo} "button
 * bar".
 *
 * @author scheglov_ke
 * @coverage rcp.model.jface
 */
public final class DialogButtonIdPropertyEditor extends TextDialogPropertyEditor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getText(Property _property) throws Exception {
    GenericProperty property = (GenericProperty) _property;
    Object value = property.getValue();
    // return title for value
    if (value instanceof Integer) {
      Expression expression = property.getExpression();
      // check for IDialogConstants.*_ID
      if (isDialogConstantsQualifiedName(expression)) {
        QualifiedName qualifiedName = (QualifiedName) expression;
        if (qualifiedName != null) {
          return qualifiedName.getName().getIdentifier();
        }
      }
      // check for custom ID
      if (expression instanceof SimpleName) {
        return ((SimpleName) expression).getIdentifier();
      }
      // just Integer
      return Integer.toString((Integer) value);
    }
    // unknown value
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Internal access
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String BUTTON_NAME_PROPERTY = "BUTTON_NAME_PROPERTY";
  private static final String BUTTON_OFFSET_PROPERTY = "BUTTON_OFFSET_PROPERTY";

  /**
   * @return <code>true</code> if given {@link Expression} is constant from {@link IDialogConstants}
   *         .
   */
  private static boolean isDialogConstantsQualifiedName(Expression expression) {
    if (expression instanceof QualifiedName) {
      QualifiedName qualifiedName = (QualifiedName) expression;
      return AstNodeUtils.getFullyQualifiedName(qualifiedName.getQualifier(), false).equals(
          "org.eclipse.jface.dialogs.IDialogConstants");
    }
    return false;
  }

  /**
   * @return {@link FieldDeclaration}'s with {@link IDialogConstants#CLIENT_ID} based custom button
   *         ID's.
   */
  private static List<FieldDeclaration> getCustomIDs(GenericProperty property) {
    List<FieldDeclaration> idList = new ArrayList<>();
    TypeDeclaration typeDeclaration = JavaInfoUtils.getTypeDeclaration(property.getJavaInfo());
    for (FieldDeclaration fieldDeclaration : typeDeclaration.getFields()) {
      // check that field is "static final"
      {
        int staticFinal = Modifier.STATIC | Modifier.FINAL;
        if ((fieldDeclaration.getModifiers() & staticFinal) != staticFinal) {
          continue;
        }
      }
      // check that field has single fragment
      VariableDeclarationFragment fragment;
      {
        List<VariableDeclarationFragment> fragments = DomGenerics.fragments(fieldDeclaration);
        if (fragments.size() != 1) {
          continue;
        }
        fragment = fragments.get(0);
      }
      // check that fragment is initialized with infix expression
      InfixExpression infixExpression;
      {
        Expression initializer = fragment.getInitializer();
        if (!(initializer instanceof InfixExpression)) {
          continue;
        }
        infixExpression = (InfixExpression) initializer;
      }
      // check that field is based on IDialogConstants.CLIENT_ID
      Expression leftOperand = infixExpression.getLeftOperand();
      Expression rightOperand = infixExpression.getRightOperand();
      if (!isDialogConstantsQualifiedName(leftOperand)) {
        continue;
      }
      if (infixExpression.getOperator() != InfixExpression.Operator.PLUS
          || !(rightOperand instanceof NumberLiteral)) {
        continue;
      }
      QualifiedName qualifiedLeftOperand = (QualifiedName) leftOperand;
      if (!qualifiedLeftOperand.getName().getIdentifier().equals("CLIENT_ID")) {
        continue;
      }
      // all checks were successful, so add this FieldDeclaration
      fieldDeclaration.setProperty(BUTTON_NAME_PROPERTY, fragment.getName().getIdentifier());
      fieldDeclaration.setProperty(
          BUTTON_OFFSET_PROPERTY,
          Integer.valueOf(rightOperand.toString()));
      idList.add(fieldDeclaration);
    }
    return idList;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void openDialog(Property _property) throws Exception {
    GenericProperty property = (GenericProperty) _property;
    SelectIdDialog dialog = new SelectIdDialog(DesignerPlugin.getShell(), property);
    if (dialog.open() == Window.OK) {
      property.setExpression(dialog.getSourceCode(), Property.UNKNOWN_VALUE);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Inner Classes
  //
  ////////////////////////////////////////////////////////////////////////////
  static final String[] STANDARD_IDS = new String[]{
      "ABORT_ID",
      "BACK_ID",
      "CANCEL_ID",
      "CLOSE_ID",
      "FINISH_ID",
      "HELP_ID",
      "IGNORE_ID",
      "NEXT_ID",
      "NO_ID",
      "NO_TO_ALL_ID",
      "OK_ID",
      "OPEN_ID",
      "PROCEED_ID",
      "RETRY_ID",
      "SKIP_ID",
      "STOP_ID",
      "YES_ID",
      "YES_TO_ALL_ID"};

  /**
   * {@link Dialog} for selecting ID of "button" on "button bar".
   *
   * @author scheglov_ke
   */
  private class SelectIdDialog extends ResizableDialog {
    private static final int NEW = IDialogConstants.CLIENT_ID + 1;
    private final GenericProperty m_property;
    private Tree m_categoriesTree;
    private TreeItem m_customItem;
    private TreeItem m_standardItem;
    private TableViewer m_viewer;
    private String m_sourceCode;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public SelectIdDialog(Shell parentShell, GenericProperty property) {
      super(parentShell, Activator.getDefault());
      m_property = property;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Access
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * @return the source code to use for ID.
     */
    public String getSourceCode() {
      return m_sourceCode;
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // GUI
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected Control createDialogArea(Composite parent) {
      Composite container = (Composite) super.createDialogArea(parent);
      GridLayoutFactory.create(container).columns(2);
      // create categories Tree
      {
        m_categoriesTree = new Tree(container, SWT.BORDER);
        GridDataFactory.create(m_categoriesTree).hintHC(20).alignHF().grabV().fillV();
        //
        m_customItem = new TreeItem(m_categoriesTree, SWT.NONE);
        m_customItem.setText(ModelMessages.DialogButtonIdPropertyEditor_categoryCustom);
        //
        m_standardItem = new TreeItem(m_categoriesTree, SWT.NONE);
        m_standardItem.setText(ModelMessages.DialogButtonIdPropertyEditor_categoryStandard);
        //
        m_categoriesTree.addSelectionListener(new SelectionAdapter() {
          @Override
          public void widgetSelected(SelectionEvent e) {
            boolean showStandardIDs = e.item == m_standardItem;
            showIDs(showStandardIDs);
          }
        });
      }
      // create ID's viewer
      {
        m_viewer = new TableViewer(container, SWT.BORDER | SWT.FULL_SELECTION);
        {
          Table table = m_viewer.getTable();
          GridDataFactory.create(table).hintC(50, 13).grab().fill();
          TableFactory.modify(table).newColumn().widthC(47).text(
              ModelMessages.DialogButtonIdPropertyEditor_columnId);
        }
        m_viewer.setComparator(new IdSorter());
        m_viewer.setLabelProvider(new IdLabelProvider());
        m_viewer.setContentProvider(new ArrayContentProvider());
        m_viewer.addDoubleClickListener(new IDoubleClickListener() {
          public void doubleClick(DoubleClickEvent e) {
            okPressed();
          }
        });
        // show initial elements
        ExecutionUtils.runLog(new RunnableEx() {
          public void run() throws Exception {
            boolean showStandardIDs = isDialogConstantsQualifiedName(m_property.getExpression());
            // select standard/custom category
            m_categoriesTree.setSelection(
                new TreeItem[]{showStandardIDs ? m_standardItem : m_customItem});
            // show ID's
            showIDs(showStandardIDs);
            highlightID(getText(m_property));
            m_viewer.getTable().setFocus();
          }
        });
      }
      //
      return container;
    }

    @Override
    protected void configureShell(Shell newShell) {
      super.configureShell(newShell);
      newShell.setText(ModelMessages.DialogButtonIdPropertyEditor_dialogTitle);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Buttons
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
      createButton(parent, NEW, ModelMessages.DialogButtonIdPropertyEditor_newButton, false);
      createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
      createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }

    @Override
    protected void okPressed() {
      IStructuredSelection selection = (IStructuredSelection) m_viewer.getSelection();
      Object o = selection.getFirstElement();
      if (o instanceof FieldDeclaration) {
        FieldDeclaration fieldDeclaration = (FieldDeclaration) o;
        m_sourceCode = DomGenerics.fragments(fieldDeclaration).get(0).getName().getIdentifier();
      } else if (o instanceof String) {
        m_sourceCode = "org.eclipse.jface.dialogs.IDialogConstants." + (String) o;
      }
      super.okPressed();
    }

    @Override
    protected void buttonPressed(int buttonId) {
      super.buttonPressed(buttonId);
      if (buttonId == NEW) {
        onNewID();
        return;
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Internal
    //
    ////////////////////////////////////////////////////////////////////////////
    /**
     * Shows standard or custom button ID's.
     */
    private void showIDs(boolean showStandardIDs) {
      m_viewer.setInput(showStandardIDs ? STANDARD_IDS : getCustomIDs(m_property));
    }

    /**
     * Highlights in {@link #m_viewer} item with same text as given.
     */
    private void highlightID(String idText) {
      Table table = m_viewer.getTable();
      for (TableItem item : table.getItems()) {
        if (item.getText().equals(idText)) {
          table.setSelection(item);
          break;
        }
      }
    }

    private void onNewID() {
      NewIdDialog dialog = new NewIdDialog(getShell(), m_property);
      if (dialog.open() != OK) {
        return;
      }
      // add new FieldDeclaration
      final String newName = dialog.m_nameField.getText();
      final String newValue = dialog.m_valueField.getText();
      final JavaInfo javaInfo = m_property.getJavaInfo();
      ExecutionUtils.run(javaInfo, new RunnableEx() {
        public void run() throws Exception {
          AstEditor editor = javaInfo.getEditor();
          TypeDeclaration typeDeclaration = JavaInfoUtils.getTypeDeclaration(javaInfo);
          String code = "private static final int "
              + newName
              + " = IDialogConstants.CLIENT_ID + "
              + newValue
              + ";";
          editor.addFieldDeclaration(code, new BodyDeclarationTarget(typeDeclaration, true));
        }
      });
      // show "custom" page
      m_categoriesTree.setSelection(new TreeItem[]{m_customItem});
      m_viewer.setInput(getCustomIDs(m_property));
      highlightID(newName);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Viewer elements
    //
    ////////////////////////////////////////////////////////////////////////////
    private class IdSorter extends ViewerComparator {
      @Override
      public int compare(Viewer viewer, Object e1, Object e2) {
        IdLabelProvider labelProvider = (IdLabelProvider) m_viewer.getLabelProvider();
        String s1 = labelProvider.getColumnText(e1, 0);
        String s2 = labelProvider.getColumnText(e2, 0);
        return s1.compareTo(s2);
      }
    }
    private class IdLabelProvider extends LabelProvider implements ITableLabelProvider {
      public String getColumnText(Object element, int columnIndex) {
        if (element instanceof FieldDeclaration) {
          FieldDeclaration fieldDeclaration = (FieldDeclaration) element;
          return (String) fieldDeclaration.getProperty(BUTTON_NAME_PROPERTY);
        }
        return element.toString();
      }

      public Image getColumnImage(Object element, int columnIndex) {
        return null;
      }
    }
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // NewIDDialog
  //
  ////////////////////////////////////////////////////////////////////////////
  private static class NewIdDialog extends AbstractValidationTitleAreaDialog {
    private final GenericProperty m_property;

    ////////////////////////////////////////////////////////////////////////////
    //
    // Constructor
    //
    ////////////////////////////////////////////////////////////////////////////
    public NewIdDialog(Shell parentShell, GenericProperty property) {
      super(parentShell,
          Activator.getDefault(),
          ModelMessages.DialogButtonIdPropertyEditor_newDialogTitle,
          ModelMessages.DialogButtonIdPropertyEditor_newDialogTitle,
          Activator.getImage("info/Dialog/newfield_wiz.gif"),
          ModelMessages.DialogButtonIdPropertyEditor_newDialogMessage);
      m_property = property;
      setShellStyle(SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM | SWT.SHELL_TRIM);
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // GUI
    //
    ////////////////////////////////////////////////////////////////////////////
    private StringDialogField m_nameField;
    private StringDialogField m_valueField;

    @Override
    protected void createControls(Composite container) {
      m_fieldsContainer = container;
      GridLayoutFactory.create(container).columns(2);
      // name
      {
        m_nameField = new StringDialogField();
        doCreateField(m_nameField, ModelMessages.DialogButtonIdPropertyEditor_newDialogName);
        // initial value
        m_nameField.setText("NEW_ID");
        m_nameField.selectAll();
      }
      // value
      {
        m_valueField = new StringDialogField();
        doCreateField(m_valueField, "Value (ID_CLIENT +):");
        // initial value
        {
          int maxValue = 0;
          for (FieldDeclaration fieldDeclaration : getCustomIDs(m_property)) {
            int value = (Integer) fieldDeclaration.getProperty(BUTTON_OFFSET_PROPERTY);
            maxValue = Math.max(maxValue, value);
          }
          m_valueField.setText("" + (maxValue + 1));
          m_valueField.selectAll();
        }
      }
    }

    ////////////////////////////////////////////////////////////////////////////
    //
    // Validation
    //
    ////////////////////////////////////////////////////////////////////////////
    @Override
    protected String validate() {
      // name of field
      {
        final String name = m_nameField.getText();
        // check for valid identifier
        {
          IStatus status = JavaConventions.validateIdentifier(name);
          if (status.getSeverity() == IStatus.ERROR) {
            return status.getMessage();
          }
        }
        // check for unique field name
        {
          TypeDeclaration typeDeclaration =
              JavaInfoUtils.getTypeDeclaration(m_property.getJavaInfo());
          final boolean[] hasSuchField = new boolean[1];
          for (FieldDeclaration fieldDeclaration : typeDeclaration.getFields()) {
            fieldDeclaration.accept(new ASTVisitor() {
              @Override
              public void endVisit(VariableDeclarationFragment node) {
                hasSuchField[0] |= node.getName().getIdentifier().equals(name);
              }
            });
          }
          if (hasSuchField[0]) {
            return MessageFormat.format(
                ModelMessages.DialogButtonIdPropertyEditor_errFieldAlreadyExists,
                name);
          }
        }
      }
      // value
      {
        String text = m_valueField.getText();
        // prepare new value
        int newValue = 0;
        try {
          newValue = Integer.parseInt(text);
        } catch (Throwable e) {
          return MessageFormat.format(
              ModelMessages.DialogButtonIdPropertyEditor_errInvalidFieldValue,
              text);
        }
        // check that this value is unique
        List<FieldDeclaration> idList = getCustomIDs(m_property);
        for (FieldDeclaration fieldDeclaration : idList) {
          Integer value = (Integer) fieldDeclaration.getProperty(BUTTON_OFFSET_PROPERTY);
          if (newValue == value.intValue()) {
            String fieldName = (String) fieldDeclaration.getProperty(BUTTON_NAME_PROPERTY);
            return MessageFormat.format(
                ModelMessages.DialogButtonIdPropertyEditor_errUsedFieldValue,
                text,
                fieldName);
          }
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
      DialogFieldUtils.fillControls(m_fieldsContainer, dialogField, 2, 60);
    }
  }
}
