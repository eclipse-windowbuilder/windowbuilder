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
package org.eclipse.wb.internal.swing.model.property;

import com.google.common.collect.Lists;

import org.eclipse.wb.core.model.AbstractComponentInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.internal.core.model.ObjectInfoVisitor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.jdt.core.ProjectUtils;
import org.eclipse.wb.internal.swing.model.ModelMessages;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;

import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.util.List;

/**
 * Property for editing components tab order.
 * 
 * @author lobas_av
 * @coverage swing.property.order
 */
public class TabOrderProperty
    extends
      org.eclipse.wb.internal.core.model.property.order.TabOrderProperty {
  private static final String FOCUS_TRAVERSAL_CLASS = "org.eclipse.wb.swing.FocusTraversalOnArray";
  private static final String FOCUS_TRAVERSAL_METHOD_SIGNATURE =
      "setFocusTraversalPolicy(java.awt.FocusTraversalPolicy)";
  private static final String TITLE_TOOLTIP = ModelMessages.TabOrderProperty_tooltip;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public TabOrderProperty(ContainerInfo container) {
    super(container);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Value
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected ArrayInitializer getOrderedArray() throws Exception {
    MethodInvocation invocation = m_container.getMethodInvocation(FOCUS_TRAVERSAL_METHOD_SIGNATURE);
    if (invocation != null) {
      Object traversalPolicy = invocation.arguments().get(0);
      if (traversalPolicy instanceof ClassInstanceCreation) {
        ClassInstanceCreation traversalPolicyCreation = (ClassInstanceCreation) traversalPolicy;
        if (FOCUS_TRAVERSAL_CLASS.equals(AstNodeUtils.getFullyQualifiedName(
            traversalPolicyCreation,
            false))) {
          Object focusTraversalOnArray = traversalPolicyCreation.arguments().get(0);
          if (focusTraversalOnArray instanceof ArrayCreation) {
            ArrayCreation creation = (ArrayCreation) focusTraversalOnArray;
            return creation.getInitializer();
          }
        }
      }
    }
    return null;
  }

  @Override
  protected void removePropertyAssociation() throws Exception {
    m_container.removeMethodInvocations(FOCUS_TRAVERSAL_METHOD_SIGNATURE);
  }

  @Override
  protected void setOrderedArraySource(String source) throws Exception {
    String newSource =
        "new org.eclipse.wb.swing.FocusTraversalOnArray(new java.awt.Component[]" + source + ")";
    MethodInvocation invocation = m_container.getMethodInvocation(FOCUS_TRAVERSAL_METHOD_SIGNATURE);
    if (invocation == null) {
      ProjectUtils.ensureResourceType(
          m_container.getEditor().getJavaProject(),
          m_container.getDescription().getToolkit().getBundle(),
          FOCUS_TRAVERSAL_CLASS);
      m_container.addMethodInvocation(FOCUS_TRAVERSAL_METHOD_SIGNATURE, newSource);
    } else {
      Expression argument = DomGenerics.arguments(invocation).get(0);
      m_container.replaceExpression(argument, newSource);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Children
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected List<? extends AbstractComponentInfo> getTabPossibleChildren() throws Exception {
    final List<AbstractComponentInfo> children = Lists.newArrayList();
    m_container.accept(new ObjectInfoVisitor() {
      @Override
      public boolean visit(ObjectInfo objectInfo) throws Exception {
        if (objectInfo != m_container && objectInfo instanceof ComponentInfo) {
          children.add((AbstractComponentInfo) objectInfo);
        }
        return true;
      }
    });
    return children;
  }

  @Override
  protected boolean isDefaultOrdered(AbstractComponentInfo component) throws Exception {
    return true;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean hasOrderElement(ObjectInfo parent, ObjectInfo child) throws Exception {
    while (parent != null) {
      if (parent == m_container) {
        return true;
      }
      parent = parent.getParent();
    }
    return false;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tooltip
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getPropertyTooltipText() {
    return TITLE_TOOLTIP;
  }
}