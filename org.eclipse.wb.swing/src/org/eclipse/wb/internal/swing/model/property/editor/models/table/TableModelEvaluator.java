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

import org.eclipse.wb.core.eval.AstEvaluationEngine;
import org.eclipse.wb.core.eval.EvaluationContext;
import org.eclipse.wb.core.eval.IExpressionEvaluator;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;

import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.VariableDeclaration;

import java.util.List;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/**
 * {@link IExpressionEvaluator} for evaluating anonymous implementation of {@link DefaultTableModel}
 * for {@link JTable}.
 * 
 * <pre>
		table.setModel(new DefaultTableModel(
			new Object[][] {
				{"string", Boolean.TRUE, new Object(), Integer.valueOf(5)},
				{null, null, null, null},
			},
			new String[] {
				"Col_1", "Col_2", "Col_3", "Col_4"
			}
		) {
			Class[] columnTypes = new Class[] {
				String.class, Boolean.class, Object.class, Integer.class
			};
			public Class getColumnClass(int columnIndex) {
				return columnTypes[columnIndex];
			}
		});
 * </pre>
 * 
 * @author scheglov_ke
 * @coverage swing.model
 */
public final class TableModelEvaluator implements IExpressionEvaluator {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IExpressionEvaluator
  //
  ////////////////////////////////////////////////////////////////////////////
  public Object evaluate(EvaluationContext context,
      Expression expression,
      ITypeBinding typeBinding,
      String typeQualifiedName) throws Exception {
    if (expression instanceof ClassInstanceCreation) {
      ClassInstanceCreation creation = (ClassInstanceCreation) expression;
      if (isAnonymous_DefaultTableModel(creation)) {
        List<Expression> arguments = DomGenerics.arguments(creation);
        Object[][] values = (Object[][]) AstEvaluationEngine.evaluate(context, arguments.get(0));
        Object[] columnNames = (Object[]) AstEvaluationEngine.evaluate(context, arguments.get(1));
        final Class<?>[] columnTypes = getAnonymousColumnTypes(context, creation);
        final boolean[] columnEditables = getAnonymousColumnEditables(context, creation);
        return new DefaultTableModel(values, columnNames) {
          private static final long serialVersionUID = 0L;

          @Override
          public Class<?> getColumnClass(int columnIndex) {
            if (columnTypes != null) {
              return columnTypes[columnIndex];
            }
            return super.getColumnClass(columnIndex);
          }

          @Override
          public boolean isCellEditable(int row, int column) {
            if (columnEditables != null) {
              return columnEditables[column];
            }
            return super.isCellEditable(row, column);
          }
        };
      }
    }
    // we don't understand given expression
    return AstEvaluationEngine.UNKNOWN;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private static boolean isAnonymous_DefaultTableModel(ClassInstanceCreation creation) {
    return creation.getAnonymousClassDeclaration() != null
        && AstNodeUtils.isSuccessorOf(
            AstNodeUtils.getTypeBinding(creation),
            DefaultTableModel.class)
        && AstNodeUtils.getCreationSignature(creation).equals(
            "<init>(java.lang.Object[][],java.lang.Object[])");
  }

  private static Class<?>[] getAnonymousColumnTypes(EvaluationContext context,
      ClassInstanceCreation creation) throws Exception {
    return (Class<?>[]) getAnonymousFieldValue(
        context,
        creation,
        "columnTypes",
        "java.lang.Class[]");
  }

  private static boolean[] getAnonymousColumnEditables(EvaluationContext context,
      ClassInstanceCreation creation) throws Exception {
    return (boolean[]) getAnonymousFieldValue(context, creation, "columnEditables", "boolean[]");
  }

  private static Object getAnonymousFieldValue(EvaluationContext context,
      ClassInstanceCreation creation,
      String fieldName,
      String fieldTypeName) throws Exception {
    AnonymousClassDeclaration anonymous = creation.getAnonymousClassDeclaration();
    List<BodyDeclaration> declarations = DomGenerics.bodyDeclarations(anonymous);
    if (!declarations.isEmpty() && declarations.get(0) instanceof FieldDeclaration) {
      FieldDeclaration field = (FieldDeclaration) declarations.get(0);
      if (field.fragments().size() == 1 && hasTypeName(field, fieldTypeName)) {
        VariableDeclaration fragment = (VariableDeclaration) field.fragments().get(0);
        if (fragment.getName().getIdentifier().equals(fieldName)) {
          return AstEvaluationEngine.evaluate(context, fragment.getInitializer());
        }
      }
    }
    return null;
  }

  private static boolean hasTypeName(FieldDeclaration field, String typeName) {
    return AstNodeUtils.getFullyQualifiedName(field.getType(), false).equals(typeName);
  }
}
