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
package org.eclipse.wb.internal.swing.model.property.editor.models.table;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.property.GenericProperty;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.editor.PropertyEditor;
import org.eclipse.wb.internal.core.model.property.editor.TextDialogPropertyEditor;
import org.eclipse.wb.internal.core.model.util.TemplateUtils;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jface.window.Window;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

/**
 * {@link PropertyEditor} for {@link DefaultTableModel}.
 * 
 * @author scheglov_ke
 * @coverage swing.property.editor
 */
public final class TableModelPropertyEditor extends TextDialogPropertyEditor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final PropertyEditor INSTANCE = new TableModelPropertyEditor();

  private TableModelPropertyEditor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getText(Property property) throws Exception {
    if (property.isModified()) {
      Object value = property.getValue();
      if (value instanceof TableModel) {
        TableModel tableModel = (TableModel) value;
        return tableModel.getColumnCount() + " columns, " + tableModel.getRowCount() + " rows";
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Editing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected void openDialog(Property property) throws Exception {
    GenericProperty genericProperty = (GenericProperty) property;
    JTable table = (JTable) genericProperty.getJavaInfo().getObject();
    TableModelDescription model = new TableModelDescription(table);
    TableModelDialog dialog =
        new TableModelDialog(DesignerPlugin.getShell(), property.getTitle(), model);
    // open dialog
    if (dialog.open() == Window.OK) {
      setModel(genericProperty, model);
    }
  }

  private static void setModel(final GenericProperty genericProperty,
      final TableModelDescription model) throws Exception {
    final ComponentInfo table = (ComponentInfo) genericProperty.getJavaInfo();
    ExecutionUtils.run(table, new RunnableEx() {
      public void run() throws Exception {
        setModelEx(table, genericProperty, model);
      }
    });
  }

  private static void setModelEx(ComponentInfo table,
      GenericProperty genericProperty,
      TableModelDescription model) throws Exception {
    genericProperty.setExpression(model.getModelSource(), Property.UNKNOWN_VALUE);
    // remove existing column invocations
    table.removeMethodInvocations("getColumnModel()");
    // add new column invocations
    ASTNode prevNode = table.getMethodInvocation("setModel(javax.swing.table.TableModel)");
    for (String columnInvocation : model.getColumnModelInvocations()) {
      String newSource = TemplateUtils.getExpression(table) + "." + columnInvocation;
      prevNode = addExpressionStatement(table, prevNode, newSource);
    }
  }

  private static ASTNode addExpressionStatement(JavaInfo javaInfo,
      ASTNode nodeOfPrevStatement,
      String expressionSource) throws Exception {
    Statement prevStatement = AstNodeUtils.getEnclosingStatement(nodeOfPrevStatement);
    StatementTarget target = new StatementTarget(prevStatement, false);
    return javaInfo.addExpressionStatement(target, expressionSource);
  }
}
