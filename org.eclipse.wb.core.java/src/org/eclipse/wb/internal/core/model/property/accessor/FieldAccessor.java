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
package org.eclipse.wb.internal.core.model.property.accessor;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.description.ComponentDescription;
import org.eclipse.wb.internal.core.model.property.Property;
import org.eclipse.wb.internal.core.model.property.table.PropertyTooltipProvider;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.Statement;

import java.lang.reflect.Field;
import java.util.List;

/**
 * The implementation of {@link ExpressionAccessor} for <code>.xxx = value</code> support public
 * field access.
 *
 * @author scheglov_ke
 * @author lobas_av
 * @coverage core.model.property.accessor
 */
public final class FieldAccessor extends ExpressionAccessor {
  private final Field m_field;
  private final String m_fieldName;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public FieldAccessor(Field field) {
    Assert.isNotNull(field);
    m_field = field;
    m_fieldName = m_field.getName();
    // initialize IAdaptable
    m_accessibleAccessor = AccessorUtils.IAccessibleExpressionAccessor_forField(m_field);
    m_tooltipProvider = AccessorUtils.PropertyTooltipProvider_forField(m_field);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visiting
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void visit(JavaInfo javaInfo, int state) throws Exception {
    super.visit(javaInfo, state);
    // remember default value
    if (isTimeToGetDefaultValue(javaInfo, state)) {
      Object defaultValue = getFieldValue(javaInfo);
      javaInfo.putArbitraryValue(this, defaultValue);
    }
  }

  private boolean isTimeToGetDefaultValue(JavaInfo javaInfo, int state) {
    return state == STATE_OBJECT_READY;
  }

  /**
   * Returns values of field from {@link JavaInfo} object. Note, that we can not use
   * {@link #m_field} directly because sometimes object of {@link JavaInfo} has different class than
   * specified in {@link ComponentDescription}, so declared this {@link #m_field}. For example
   * <code>GridLayout</code> and its <code>GridLayout2</code>.
   */
  private Object getFieldValue(final JavaInfo javaInfo) {
    return ExecutionUtils.runObjectIgnore(new RunnableObjectEx<Object>() {
      public Object runObject() throws Exception {
        Object object = javaInfo.getObject();
        return ReflectionUtils.getFieldObject(object, m_fieldName);
      }
    }, Property.UNKNOWN_VALUE);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ExpressionAccessor
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public Expression getExpression(JavaInfo javaInfo) throws Exception {
    Assignment assignment = getFieldAssignment(javaInfo);
    return getExpression(assignment);
  }

  @Override
  public boolean setExpression(final JavaInfo javaInfo, final String source) throws Exception {
    boolean hasAssignment = !javaInfo.getFieldAssignments(m_fieldName).isEmpty();
    if (source == null) {
      if (hasAssignment) {
        ExecutionUtils.run(javaInfo, new RunnableEx() {
          public void run() throws Exception {
            javaInfo.removeFieldAssignments(m_fieldName);
          }
        });
      }
    } else if (hasAssignment) {
      Assignment assignment = getFieldAssignmentForUpdate(javaInfo);
      final Expression oldExpression = getExpression(assignment);
      if (!javaInfo.getEditor().getSource(oldExpression).equals(source)) {
        ExecutionUtils.run(javaInfo, new RunnableEx() {
          public void run() throws Exception {
            javaInfo.getEditor().replaceExpression(oldExpression, source);
          }
        });
      }
    } else {
      ExecutionUtils.run(javaInfo, new RunnableEx() {
        public void run() throws Exception {
          javaInfo.addFieldAssignment(m_fieldName, source);
        }
      });
    }
    // success
    return true;
  }

  @Override
  public Object getDefaultValue(JavaInfo javaInfo) throws Exception {
    return javaInfo.getArbitraryValue(this);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IAdaptable
  //
  ////////////////////////////////////////////////////////////////////////////
  private final IAccessibleExpressionAccessor m_accessibleAccessor;
  private final PropertyTooltipProvider m_tooltipProvider;

  @Override
  public <T> T getAdapter(Class<T> adapter) {
    if (adapter == IAccessibleExpressionAccessor.class) {
      return adapter.cast(m_accessibleAccessor);
    }
    if (adapter == IExposableExpressionAccessor.class) {
      return adapter.cast(m_exposableAccessor);
    }
    if (adapter == PropertyTooltipProvider.class) {
      return adapter.cast(m_tooltipProvider);
    }
    // other
    return super.getAdapter(adapter);
  }

  /**
   * Implementation of {@link IExposableExpressionAccessor}.
   */
  private final IExposableExpressionAccessor m_exposableAccessor =
      new IExposableExpressionAccessor() {
        public Class<?> getValueClass(JavaInfo javaInfo) {
          return m_field.getType();
        }

        public String getGetterCode(JavaInfo javaInfo) throws Exception {
          return m_fieldName;
        }

        public String getSetterCode(JavaInfo javaInfo, String source) throws Exception {
          return m_fieldName + " = " + source;
        }
      };

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link Assignment} of this field accessor for given {@link JavaInfo}.
   */
  private Assignment getFieldAssignment(JavaInfo javaInfo) throws Exception {
    return javaInfo.getFieldAssignment(m_fieldName);
  }

  /**
   * @return right hand side of given assignment expression.
   */
  private static Expression getExpression(Assignment assignment) {
    return assignment == null ? null : assignment.getRightHandSide();
  }

  /**
   * @return the {@link Assignment} of this field accessor for given {@link JavaInfo}. If
   *         {@link Assignment} was part of {@link Assignment}'s sequence, then this sequence will
   *         be splitted on separate {@link Statement}'s with {@link Assignment}'s.
   */
  private Assignment getFieldAssignmentForUpdate(JavaInfo javaInfo) throws Exception {
    Assignment targetAssignment = getFieldAssignment(javaInfo);
    if (targetAssignment != null) {
      AssignmentsSequence sequence = getAssignmentsSequence(targetAssignment);
      if (sequence.getAssignments().size() == 1) {
        return targetAssignment;
      } else {
        return splitAssignmentsSequence(javaInfo, targetAssignment, sequence);
      }
    }
    // not found
    return null;
  }

  private static AssignmentsSequence getAssignmentsSequence(Assignment targetAssignment) {
    List<Assignment> assignments = Lists.newArrayList();
    // add assignment's on left side
    {
      Assignment leftAssignment = targetAssignment;
      while (leftAssignment.getParent() instanceof Assignment) {
        leftAssignment = (Assignment) leftAssignment.getParent();
        assignments.add(leftAssignment);
      }
      Assert.isTrue(
          leftAssignment.getParent() instanceof ExpressionStatement,
          "Assignments are expected to be directly in ExpressionStatement, but %s found.",
          leftAssignment.getParent());
    }
    // add target assignment
    assignments.add(targetAssignment);
    // add assignments on right side
    Expression assignedExpression;
    {
      Assignment rightAssignment = targetAssignment;
      while (rightAssignment.getRightHandSide() instanceof Assignment) {
        rightAssignment = (Assignment) rightAssignment.getRightHandSide();
        assignments.add(rightAssignment);
      }
      // remember assigned expression
      assignedExpression = rightAssignment.getRightHandSide();
    }
    return new AssignmentsSequence(assignments, assignedExpression);
  }

  private static Assignment splitAssignmentsSequence(JavaInfo javaInfo,
      Assignment targetAssignment,
      AssignmentsSequence sequence) throws Exception {
    Assignment newTargetAssignment = null;
    {
      AstEditor editor = javaInfo.getEditor();
      String assignedSource = editor.getSource(sequence.getAssignedExpression());
      Statement targetStatement = AstNodeUtils.getEnclosingStatement(targetAssignment);
      for (Assignment assignment : sequence.getAssignments()) {
        String source = editor.getSource(assignment.getLeftHandSide()) + " = " + assignedSource;
        // add new assignment
        StatementTarget target = new StatementTarget(targetStatement, false);
        Assignment newAssignment = (Assignment) javaInfo.addExpressionStatement(target, source);
        if (assignment == targetAssignment) {
          newTargetAssignment = newAssignment;
        }
        // prepare new statement target
        targetStatement = AstNodeUtils.getEnclosingStatement(newAssignment);
      }
      // remove old statement
      editor.removeEnclosingStatement(targetAssignment);
    }
    // use new target assignment
    Assert.isNotNull(newTargetAssignment);
    return newTargetAssignment;
  }
}