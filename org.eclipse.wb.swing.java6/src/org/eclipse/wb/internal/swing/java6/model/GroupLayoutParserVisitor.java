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
package org.eclipse.wb.internal.swing.java6.model;

import org.eclipse.wb.core.model.IAbstractComponentInfo;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.association.EmptyAssociation;
import org.eclipse.wb.internal.core.model.JavaInfoEvaluationHelper;
import org.eclipse.wb.internal.core.parser.JavaInfoResolver;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.java6.Messages;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.awt.Component;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComponent;
import javax.swing.LayoutStyle.ComponentPlacement;

/**
 * Parser for Swing {@link GroupLayout}.
 * 
 * @author mitin_aa
 * @coverage swing.model.layout.group
 */
final class GroupLayoutParserVisitor extends ASTVisitor {
  private final String m_methodName;
  private final ContainerInfo m_container;
  private final Map<IAbstractComponentInfo, WidgetSpringInfo> m_mappedWidgets;
  private SpringInfo m_rootGroup;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public GroupLayoutParserVisitor(Map<IAbstractComponentInfo, WidgetSpringInfo> mappedWidgets,
      String methodName,
      ContainerInfo container) {
    super();
    m_mappedWidgets = mappedWidgets;
    m_methodName = methodName;
    m_container = container;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Visitor
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void endVisit(final MethodInvocation node) {
    try {
      final String identifier = node.getName().getIdentifier();
      final Expression nodeExpression = node.getExpression();
      final int argumentsSize = node.arguments().size();
      if (identifier.equals(GroupLayoutInfo.IDENTIFIER_ADD_GROUP)
          || identifier.equals(GroupLayoutInfo.IDENTIFIER_ADD_COMPONENT)
          || identifier.equals(GroupLayoutInfo.IDENTIFIER_ADD_GAP)) {
        if (argumentsSize == 0) {
          throw new IllegalArgumentException(Messages.GroupLayoutParserVisitor_addWithoutArguments);
        }
        final Expression arg0 = (Expression) node.arguments().get(0);
        final GroupInfo parentGroup =
            (GroupInfo) nodeExpression.getProperty(GroupLayoutInfo.PROPERTY_NAME_GROUP);
        // addGroup(Group group)
        if (AstNodeUtils.isSuccessorOf(arg0, GroupLayoutInfo.GROUP_LAYOUT_GROUP_CLASS_NAME)) {
          // add group into group
          final GroupInfo group = (GroupInfo) arg0.getProperty(GroupLayoutInfo.PROPERTY_NAME_GROUP);
          parentGroup.add(group);
          node.setProperty(GroupLayoutInfo.PROPERTY_NAME_GROUP, parentGroup);
        }
        if (argumentsSize == 2) {
          Expression arg1 = (Expression) node.arguments().get(1);
          // addGroup(Aligment, Group group)
          if (AstNodeUtils.isSuccessorOf(arg1, GroupLayoutInfo.GROUP_LAYOUT_GROUP_CLASS_NAME)
              && identifier.equals(GroupLayoutInfo.IDENTIFIER_ADD_GROUP)) {
            // add group into group
            final ParallelGroupInfo group =
                (ParallelGroupInfo) arg1.getProperty(GroupLayoutInfo.PROPERTY_NAME_GROUP);
            setGroupAlignment(group, arg0);
            parentGroup.add(group);
            node.setProperty(GroupLayoutInfo.PROPERTY_NAME_GROUP, parentGroup);
          }
        }
        // addComponent(Component component)
        if ((argumentsSize == 1 || argumentsSize == 4)
            && AstNodeUtils.isSuccessorOf(arg0, Component.class)) {
          final JavaInfo widget = JavaInfoResolver.getJavaInfo(m_container, arg0);
          final GroupInfo group =
              (GroupInfo) nodeExpression.getProperty(GroupLayoutInfo.PROPERTY_NAME_GROUP);
          if (widget != null) {
            WidgetSpringInfo widgetSpring = addChild((IAbstractComponentInfo) widget, m_container);
            //	TODO: checkForJTextField(widget, interval);
            // addComponent(Component component, int min, int pref, int max)
            if (argumentsSize == 4) {
              setSizes(
                  widgetSpring,
                  (Expression) node.arguments().get(1),
                  (Expression) node.arguments().get(2),
                  (Expression) node.arguments().get(3));
            }
            group.add(widgetSpring);
          }
          node.setProperty(GroupLayoutInfo.PROPERTY_NAME_GROUP, group);
        }
        // addGap(int pref)
        if (AstNodeUtils.isSuccessorOf(arg0, int.class) && argumentsSize == 1) {
          final GroupInfo group =
              (GroupInfo) nodeExpression.getProperty(GroupLayoutInfo.PROPERTY_NAME_GROUP);
          GapSpringInfo gapSpring = new GapSpringInfo();
          setSize(gapSpring, (Expression) node.arguments().get(0));
          group.add(gapSpring);
          node.setProperty(GroupLayoutInfo.PROPERTY_NAME_GROUP, group);
        }
        // addGap(int min, int pref, int max)
        if (AstNodeUtils.isSuccessorOf(arg0, int.class) && argumentsSize == 3) {
          final GroupInfo group =
              (GroupInfo) nodeExpression.getProperty(GroupLayoutInfo.PROPERTY_NAME_GROUP);
          GapSpringInfo gapSpring = new GapSpringInfo();
          setSizes(
              gapSpring,
              (Expression) node.arguments().get(0),
              (Expression) node.arguments().get(1),
              (Expression) node.arguments().get(2));
          group.add(gapSpring);
          node.setProperty(GroupLayoutInfo.PROPERTY_NAME_GROUP, group);
        }
        if (argumentsSize > 1 && argumentsSize != 4) {
          // addComponent(Component component, Alignment alignment)
          JavaInfo widget = null;
          final GroupInfo group =
              (GroupInfo) nodeExpression.getProperty(GroupLayoutInfo.PROPERTY_NAME_GROUP);
          Expression arg1 = (Expression) node.arguments().get(1);
          if (AstNodeUtils.isSuccessorOf(arg0, Component.class)
              && identifier.equals(GroupLayoutInfo.IDENTIFIER_ADD_COMPONENT)) {
            widget = JavaInfoResolver.getJavaInfo(m_container, arg0);
          }
          if (widget != null) {
            WidgetSpringInfo widgetSpring = addChild((IAbstractComponentInfo) widget, m_container);
            Alignment alignment = (Alignment) JavaInfoEvaluationHelper.getValue(arg1);
            widgetSpring.setAlignment(alignment);
            //						checkForJTextField(widget, interval);
            // addComponent(Component component, Alignment alignment, int min, int pref, int max)
            if (argumentsSize == 5) {
              setSizes(
                  widgetSpring,
                  (Expression) node.arguments().get(2),
                  (Expression) node.arguments().get(3),
                  (Expression) node.arguments().get(4));
            }
            group.add(widgetSpring);
          }
          node.setProperty(GroupLayoutInfo.PROPERTY_NAME_GROUP, group);
        }
      } else if (identifier.equals(GroupLayoutInfo.IDENTIFIER_ADD_PREFERRED_GAP)) {
        final GroupInfo group =
            (GroupInfo) nodeExpression.getProperty(GroupLayoutInfo.PROPERTY_NAME_GROUP);
        // parent group should be sequential
        Assert.instanceOf(SequentialGroupInfo.class, group);
        GapSpringInfo gapSpring = new GapSpringInfo();
        switch (argumentsSize) {
          case 1 : {
            // addPreferredGap(ComponentPlacement)
            setPlacementType(gapSpring, (Expression) node.arguments().get(0));
            break;
          }
          case 3 : {
            final Expression arg0 = (Expression) node.arguments().get(0);
            final Expression arg1 = (Expression) node.arguments().get(1);
            final Expression arg2 = (Expression) node.arguments().get(2);
            // addPreferredGap(JComponent, JComponent, ComponentPlacement)
            if (AstNodeUtils.isSuccessorOf(arg0, JComponent.class)) {
              setGapWidgets(gapSpring, arg0, arg1);
              setPlacementType(gapSpring, arg2);
            } else {
              // addPreferredGap(ComponentPlacement, int, int)
              setPlacementType(gapSpring, arg0);
              setGapSizes(gapSpring, arg1, arg2);
            }
            break;
          }
          case 5 : {
            // addPreferredGap(JComponent, JComponent, ComponentPlacement, int, int)
            final Expression arg0 = (Expression) node.arguments().get(0);
            final Expression arg1 = (Expression) node.arguments().get(1);
            final Expression arg2 = (Expression) node.arguments().get(2);
            final Expression arg3 = (Expression) node.arguments().get(3);
            final Expression arg4 = (Expression) node.arguments().get(4);
            setGapWidgets(gapSpring, arg0, arg1);
            setPlacementType(gapSpring, arg2);
            setGapSizes(gapSpring, arg3, arg4);
            break;
          }
        }
        group.add(gapSpring);
        node.setProperty(GroupLayoutInfo.PROPERTY_NAME_GROUP, group);
      } else if (identifier.equals(GroupLayoutInfo.IDENTIFIER_ADD_CONTAINER_GAP)) {
        final GroupInfo group =
            (GroupInfo) nodeExpression.getProperty(GroupLayoutInfo.PROPERTY_NAME_GROUP);
        // parent group should be sequential
        Assert.instanceOf(SequentialGroupInfo.class, group);
        GapSpringInfo gapSpring = new GapSpringInfo(true);
        if (argumentsSize == 2) {
          setGapSizes(
              gapSpring,
              (Expression) node.arguments().get(0),
              (Expression) node.arguments().get(1));
        }
        group.add(gapSpring);
        node.setProperty(GroupLayoutInfo.PROPERTY_NAME_GROUP, group);
      } else if (identifier.equals(GroupLayoutInfo.IDENTIFIER_CREATE_SEQUENTIAL_GROUP)
          && argumentsSize == 0) {
        final SequentialGroupInfo sequentialGroup = new SequentialGroupInfo();
        node.setProperty(GroupLayoutInfo.PROPERTY_NAME_GROUP, sequentialGroup);
      } else if (identifier.equals(GroupLayoutInfo.IDENTIFIER_CREATE_PARALLEL_GROUP)) {
        // createParallelGroup()
        final ParallelGroupInfo parallelGroup = new ParallelGroupInfo();
        if (argumentsSize > 0) {
          setGroupAlignment(parallelGroup, (Expression) node.arguments().get(0));
          // createParallelGroup(Alignment, boolean)
          if (argumentsSize > 1) {
            setGroupResizeable(parallelGroup, (Expression) node.arguments().get(1));
          }
        }
        node.setProperty(GroupLayoutInfo.PROPERTY_NAME_GROUP, parallelGroup);
      } else if (identifier.equals(GroupLayoutInfo.IDENTIFIER_CREATE_BASELINE_GROUP)
          && argumentsSize == 2) {
        // createBaselineGroup(boolean, boolean)
        final ParallelGroupInfo parallelGroup = new ParallelGroupInfo();
        parallelGroup.setGroupAlignment(Alignment.BASELINE);
        setGroupResizeable(parallelGroup, (Expression) node.arguments().get(0));
        setGroupAnchorBaselineToTop(parallelGroup, (Expression) node.arguments().get(1));
        node.setProperty(GroupLayoutInfo.PROPERTY_NAME_GROUP, parallelGroup);
      } else if (m_methodName.startsWith(identifier) && node.arguments().size() == 1) {
        final ASTNode arg0 = (ASTNode) node.arguments().get(0);
        if (arg0 instanceof MethodInvocation) {
          final MethodInvocation mi = (MethodInvocation) arg0;
          m_rootGroup = (SpringInfo) mi.getProperty(GroupLayoutInfo.PROPERTY_NAME_GROUP);
        }
      }
    } catch (Throwable e) {
      throw ReflectionUtils.propagate(e);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Helper methods
  //
  ////////////////////////////////////////////////////////////////////////////
  //		private void checkForJTextField(JavaInfo model, final LayoutInterval interval) {
  //			if (((JavaBeanInfo) model).isKindOf(JTextField.class) && m_dimension == HORIZONTAL) {
  //				interval.setSizes(LayoutConstants.NOT_EXPLICITLY_DEFINED,
  //					LayoutConstants.NOT_EXPLICITLY_DEFINED,
  //					LayoutConstants.NOT_EXPLICITLY_DEFINED);
  //			}
  //		}
  private WidgetSpringInfo addChild(final IAbstractComponentInfo component, ContainerInfo container)
      throws Exception {
    ComponentInfo widget = (ComponentInfo) component.getUnderlyingModel();
    // widget may be already added to parent using previous dimension parsing
    if (widget.getParent() == null && !container.getChildren().contains(widget)) {
      container.addChild(widget);
      widget.setAssociation(new EmptyAssociation());
    }
    WidgetSpringInfo widgetSpringInfo = new WidgetSpringInfo(widget);
    m_mappedWidgets.put(widget, widgetSpringInfo);
    return widgetSpringInfo;
  }

  private void setSize(SpringInfo spring, Expression val) throws Exception {
    final Number value = (Number) JavaInfoEvaluationHelper.getValue(val);
    int intValue = value.intValue();
    spring.setSizes(intValue, intValue, intValue);
  }

  private void setSizes(SpringInfo spring, Expression min, Expression pref, Expression max)
      throws Exception {
    final Number minValue = (Number) JavaInfoEvaluationHelper.getValue(min);
    final Number prefValue = (Number) JavaInfoEvaluationHelper.getValue(pref);
    final Number maxValue = (Number) JavaInfoEvaluationHelper.getValue(max);
    spring.setSizes(minValue.intValue(), prefValue.intValue(), maxValue.intValue());
  }

  private void setGapSizes(GapSpringInfo spring, Expression pref, Expression max) throws Exception {
    final Number prefValue = (Number) JavaInfoEvaluationHelper.getValue(pref);
    final Number maxValue = (Number) JavaInfoEvaluationHelper.getValue(max);
    spring.setPreferredSize(prefValue.intValue());
    spring.setMaximumSize(maxValue.intValue());
  }

  private void setGroupAlignment(final ParallelGroupInfo group, final Expression arg0) {
    Alignment alignment = (Alignment) JavaInfoEvaluationHelper.getValue(arg0);
    group.setGroupAlignment(alignment);
  }

  private void setGroupResizeable(final ParallelGroupInfo group, final Expression arg0) {
    Boolean resizeable = (Boolean) JavaInfoEvaluationHelper.getValue(arg0);
    group.setGroupResizeable(resizeable);
  }

  private void setGroupAnchorBaselineToTop(final ParallelGroupInfo group, final Expression arg0) {
    Boolean anchorToTop = (Boolean) JavaInfoEvaluationHelper.getValue(arg0);
    group.setGroupAnchorBaselineToTop(anchorToTop);
  }

  private void setGapWidgets(GapSpringInfo gapSpring, final Expression arg0, final Expression arg1) {
    JavaInfo widget1 = JavaInfoResolver.getJavaInfo(m_container, arg0);
    JavaInfo widget2 = JavaInfoResolver.getJavaInfo(m_container, arg1);
    gapSpring.setGapWidgets((IAbstractComponentInfo) widget1, (IAbstractComponentInfo) widget2);
  }

  private void setPlacementType(GapSpringInfo gapSpring, final Expression arg0) {
    ComponentPlacement placement = (ComponentPlacement) JavaInfoEvaluationHelper.getValue(arg0);
    gapSpring.setPlacementType(placement);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public final SpringInfo getRootGroup() {
    return m_rootGroup;
  }
}