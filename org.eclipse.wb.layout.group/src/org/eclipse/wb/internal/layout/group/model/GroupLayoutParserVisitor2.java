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
package org.eclipse.wb.internal.layout.group.model;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfoUtils;
import org.eclipse.wb.internal.core.model.JavaInfoEvaluationHelper;
import org.eclipse.wb.internal.core.parser.JavaInfoResolver;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableObjectEx;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import org.netbeans.modules.form.layoutdesign.LayoutComponent;
import org.netbeans.modules.form.layoutdesign.LayoutConstants;
import org.netbeans.modules.form.layoutdesign.LayoutInterval;
import org.netbeans.modules.form.layoutdesign.LayoutModel;

final class GroupLayoutParserVisitor2 extends ASTVisitor implements LayoutConstants {
  private final String m_methodName;
  private final AbstractComponentInfo m_container;
  private LayoutInterval m_rootGroup;
  private final LayoutModel m_layoutModel;
  private final int m_dimension;
  private final GroupLayoutCodeSupport m_codeSupport;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GroupLayoutParserVisitor2(AbstractComponentInfo container,
      int dimension,
      GroupLayoutCodeSupport codeSupport) {
    m_codeSupport = codeSupport;
    m_layoutModel = codeSupport.getLayoutModel();
    m_methodName =
        dimension == HORIZONTAL
            ? codeSupport.SIGNATURE_SET_HORIZONTAL_GROUP
            : codeSupport.SIGNATURE_SET_VERTICAL_GROUP;
    m_container = container;
    m_dimension = dimension;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visitors
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void endVisit(MethodInvocation node) {
    String identifier = node.getName().getIdentifier();
    Expression nodeExpression = node.getExpression();
    int argumentsSize = node.arguments().size();
    if (identifier.equals(m_codeSupport.ID_ADD_GROUP)
        || identifier.equals(m_codeSupport.ID_ADD_COMPONENT)
        || identifier.equals(m_codeSupport.ID_ADD_GAP)) {
      if (argumentsSize == 0) {
        throw new IllegalArgumentException("add*() methods with no arguments are not supported.");
      }
      Expression arg0 = (Expression) node.arguments().get(0);
      LayoutInterval parentGroup =
          (LayoutInterval) nodeExpression.getProperty(GroupLayoutCodeSupport.PROPERTY_NAME_GROUP);
      // addGroup(Group group)
      if (AstNodeUtils.isSuccessorOf(arg0, m_codeSupport.GROUP_LAYOUT_GROUP_CLASS_NAME)) {
        // add group into group
        LayoutInterval group =
            (LayoutInterval) arg0.getProperty(GroupLayoutCodeSupport.PROPERTY_NAME_GROUP);
        parentGroup.add(group, -1);
        node.setProperty(GroupLayoutCodeSupport.PROPERTY_NAME_GROUP, parentGroup);
      }
      if (argumentsSize == 2) {
        Expression arg1 = (Expression) node.arguments().get(1);
        // addGroup(Aligment, Group group)
        if (AstNodeUtils.isSuccessorOf(arg1, m_codeSupport.GROUP_LAYOUT_GROUP_CLASS_NAME)
            && identifier.equals(m_codeSupport.ID_ADD_GROUP)) {
          // add group into group
          LayoutInterval group =
              (LayoutInterval) arg1.getProperty(GroupLayoutCodeSupport.PROPERTY_NAME_GROUP);
          m_codeSupport.setGroupAlignment(parentGroup, arg0);
          parentGroup.add(group, -1);
          node.setProperty(GroupLayoutCodeSupport.PROPERTY_NAME_GROUP, parentGroup);
        }
      }
      // addComponent(Component component)
      if ((argumentsSize == 1 || argumentsSize == 4) && m_codeSupport.isComponent(arg0)) {
        JavaInfo widget = JavaInfoResolver.getJavaInfo(m_container, arg0);
        LayoutInterval group =
            (LayoutInterval) nodeExpression.getProperty(GroupLayoutCodeSupport.PROPERTY_NAME_GROUP);
        if (widget != null) {
          LayoutInterval interval = addChild((AbstractComponentInfo) widget, arg0);
          // addComponent(Component component, int min, int pref, int max)
          if (argumentsSize == 4) {
            setSizes(
                interval,
                (Expression) node.arguments().get(1),
                (Expression) node.arguments().get(2),
                (Expression) node.arguments().get(3));
          }
          group.add(interval, -1);
        }
        node.setProperty(GroupLayoutCodeSupport.PROPERTY_NAME_GROUP, group);
        return;
      }
      // addGap(int pref)
      if (AstNodeUtils.isSuccessorOf(arg0, int.class) && argumentsSize == 1) {
        LayoutInterval group =
            (LayoutInterval) nodeExpression.getProperty(GroupLayoutCodeSupport.PROPERTY_NAME_GROUP);
        LayoutInterval space = new LayoutInterval(SINGLE);
        setSize(space, (Expression) node.arguments().get(0));
        group.add(space, -1);
        node.setProperty(GroupLayoutCodeSupport.PROPERTY_NAME_GROUP, group);
      }
      // addGap(int min, int pref, int max)
      if (AstNodeUtils.isSuccessorOf(arg0, int.class) && argumentsSize == 3) {
        LayoutInterval group =
            (LayoutInterval) nodeExpression.getProperty(GroupLayoutCodeSupport.PROPERTY_NAME_GROUP);
        LayoutInterval space = new LayoutInterval(SINGLE);
        setSizes(
            space,
            (Expression) node.arguments().get(0),
            (Expression) node.arguments().get(1),
            (Expression) node.arguments().get(2));
        group.add(space, -1);
        node.setProperty(GroupLayoutCodeSupport.PROPERTY_NAME_GROUP, group);
      }
      if (argumentsSize > 1
          && argumentsSize != 4
          && identifier.equals(m_codeSupport.ID_ADD_COMPONENT)) {
        // addComponent(Component component, Alignment alignment)
        JavaInfo widget = null;
        LayoutInterval group =
            (LayoutInterval) nodeExpression.getProperty(GroupLayoutCodeSupport.PROPERTY_NAME_GROUP);
        Expression arg1 = (Expression) node.arguments().get(1);
        Expression widgetArg;
        Expression alignmentArg;
        if (m_codeSupport.isComponent(arg0)) {
          // JDK GL
          widgetArg = arg0;
          alignmentArg = arg1;
        } else {
          // standalone GL & SWT GL
          widgetArg = arg1;
          alignmentArg = arg0;
        }
        widget = JavaInfoResolver.getJavaInfo(m_container, widgetArg);
        if (widget != null) {
          LayoutInterval interval = addChild((AbstractComponentInfo) widget, widgetArg);
          m_codeSupport.setAlignment(interval, alignmentArg);
          // addComponent(Component component, Alignment alignment, int min, int pref, int max)
          if (argumentsSize == 5) {
            setSizes(
                interval,
                (Expression) node.arguments().get(2),
                (Expression) node.arguments().get(3),
                (Expression) node.arguments().get(4));
          }
          group.add(interval, -1);
        }
        node.setProperty(GroupLayoutCodeSupport.PROPERTY_NAME_GROUP, group);
      }
    } else if (identifier.equals(GroupLayoutCodeSupport.ID_ADD_PREFERRED_GAP)) {
      LayoutInterval group =
          (LayoutInterval) nodeExpression.getProperty(GroupLayoutCodeSupport.PROPERTY_NAME_GROUP);
      if (group.isSequential()) {
        LayoutInterval space = new LayoutInterval(SINGLE);
        switch (argumentsSize) {
          case 1 :
            // addPreferredGap(ComponentPlacement)
            m_codeSupport.setPaddingType(space, (Expression) node.arguments().get(0));
            break;
          case 3 :
          case 4 : {
            // also cover standalone lib methods
            Expression arg0 = (Expression) node.arguments().get(0);
            Expression arg1 = (Expression) node.arguments().get(1);
            if (m_codeSupport.isComponent(arg0) && m_codeSupport.isComponent(arg1)) {
              // addPreferredGap(JComponent, JComponent, ComponentPlacement)
              m_codeSupport.setPaddingType(space, (Expression) node.arguments().get(2));
            } else {
              // addPreferredGap(ComponentPlacement, int, int)
              m_codeSupport.setPaddingType(space, (Expression) node.arguments().get(0));
              setSizes(
                  space,
                  (Expression) node.arguments().get(1),
                  (Expression) node.arguments().get(2));
            }
            break;
          }
          case 5 :
            // addPreferredGap(JComponent, JComponent, ComponentPlacement, int, int)
            m_codeSupport.setPaddingType(space, (Expression) node.arguments().get(2));
            setSizes(
                space,
                (Expression) node.arguments().get(3),
                (Expression) node.arguments().get(4));
            break;
        }
        group.add(space, -1);
      }
      node.setProperty(GroupLayoutCodeSupport.PROPERTY_NAME_GROUP, group);
    } else if (identifier.equals(GroupLayoutCodeSupport.ID_ADD_CONTAINER_GAP)) {
      LayoutInterval group =
          (LayoutInterval) nodeExpression.getProperty(GroupLayoutCodeSupport.PROPERTY_NAME_GROUP);
      LayoutInterval space = new LayoutInterval(SINGLE);
      if (argumentsSize == 2) {
        setGapSizes(
            space,
            (Expression) node.arguments().get(0),
            (Expression) node.arguments().get(1));
      }
      group.add(space, -1);
      node.setProperty(GroupLayoutCodeSupport.PROPERTY_NAME_GROUP, group);
    } else if (identifier.equals(GroupLayoutCodeSupport.ID_CREATE_SEQUENTIAL_GROUP)
        && argumentsSize == 0) {
      LayoutInterval sequentialGroup = new LayoutInterval(SEQUENTIAL);
      node.setProperty(GroupLayoutCodeSupport.PROPERTY_NAME_GROUP, sequentialGroup);
    } else if (identifier.equals(GroupLayoutCodeSupport.ID_CREATE_PARALLEL_GROUP)) {
      LayoutInterval parallelGroup = new LayoutInterval(PARALLEL);
      if (argumentsSize > 0) {
        m_codeSupport.setGroupAlignment(parallelGroup, (Expression) node.arguments().get(0));
        // createParallelGroup(Alignment, boolean)
        if (argumentsSize > 1) {
          setGroupResizeable(parallelGroup, (Expression) node.arguments().get(1));
        }
      }
      node.setProperty(GroupLayoutCodeSupport.PROPERTY_NAME_GROUP, parallelGroup);
    } else if (identifier.equals(GroupLayoutCodeSupport.ID_CREATE_BASELINE_GROUP)) {
      // createBaselineGroup(boolean, boolean)
      LayoutInterval parallelGroup = new LayoutInterval(PARALLEL);
      parallelGroup.setGroupAlignment(BASELINE);
      if (argumentsSize > 0) {
        setGroupResizeable(parallelGroup, (Expression) node.arguments().get(0));
      }
      node.setProperty(GroupLayoutCodeSupport.PROPERTY_NAME_GROUP, parallelGroup);
    } else if (m_methodName.startsWith(identifier) && node.arguments().size() == 1) {
      ASTNode arg0 = (ASTNode) node.arguments().get(0);
      if (arg0 instanceof MethodInvocation) {
        MethodInvocation mi = (MethodInvocation) arg0;
        m_rootGroup = (LayoutInterval) mi.getProperty(GroupLayoutCodeSupport.PROPERTY_NAME_GROUP);
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Helper methods
  //
  ////////////////////////////////////////////////////////////////////////////
  private LayoutInterval addChild(final AbstractComponentInfo widget,
      final Expression associationExpression) {
    return ExecutionUtils.runObject(new RunnableObjectEx<LayoutInterval>() {
      public LayoutInterval runObject() throws Exception {
        // widget may be already added to parent using previous dimension parsing
        if (widget.getParent() == null && !m_container.getChildren().contains(widget)) {
          m_container.addChild(widget);
        }
        // remember last association expression
        widget.putArbitraryValue(
            GroupLayoutCodeSupport.ASSOCIATION_EXPRESSION_KEY,
            associationExpression);
        //
        String id = ObjectInfoUtils.getId(widget);
        LayoutComponent layoutComponent = m_layoutModel.getLayoutComponent(id);
        if (layoutComponent == null) {
          layoutComponent = new LayoutComponent(id, false);
        }
        if (layoutComponent.getParent() == null) {
          LayoutComponent root =
              m_layoutModel.getLayoutComponent(ObjectInfoUtils.getId(m_container));
          m_layoutModel.addComponent(layoutComponent, root, -1);
        }
        m_codeSupport.checkComponent(widget, m_dimension);
        return layoutComponent.getLayoutInterval(m_dimension);
      }
    });
  }

  private void setSize(LayoutInterval interval, Expression val) {
    Number value = (Number) JavaInfoEvaluationHelper.getValue(val);
    int intValue = value.intValue();
    interval.setSizes(intValue, intValue, intValue);
  }

  private void setSizes(LayoutInterval interval, Expression min, Expression pref, Expression max) {
    Number minValue = (Number) JavaInfoEvaluationHelper.getValue(min);
    Number prefValue = (Number) JavaInfoEvaluationHelper.getValue(pref);
    Number maxValue = (Number) JavaInfoEvaluationHelper.getValue(max);
    interval.setSizes(minValue.intValue(), prefValue.intValue(), maxValue.intValue());
  }

  private void setSizes(LayoutInterval interval, Expression pref, Expression max) {
    Number prefValue = (Number) JavaInfoEvaluationHelper.getValue(pref);
    Number maxValue = (Number) JavaInfoEvaluationHelper.getValue(max);
    interval.setSizes(NOT_EXPLICITLY_DEFINED, prefValue.intValue(), maxValue.intValue());
  }

  private void setGapSizes(LayoutInterval interval, Expression pref, Expression max) {
    Number prefValue = (Number) JavaInfoEvaluationHelper.getValue(pref);
    Number maxValue = (Number) JavaInfoEvaluationHelper.getValue(max);
    interval.setPreferredSize(prefValue.intValue());
    interval.setMaximumSize(maxValue.intValue());
  }

  private void setGroupResizeable(final LayoutInterval group, final Expression arg0) {
    Boolean resizeable = (Boolean) JavaInfoEvaluationHelper.getValue(arg0);
    if (!resizeable) {
      group.setMaximumSize(USE_PREFERRED_SIZE);
    }
  }

  public LayoutInterval getRootGroup() {
    return m_rootGroup;
  }
}