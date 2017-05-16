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
package org.eclipse.wb.internal.core.model.nonvisual;

import org.eclipse.wb.core.eval.ExecutionFlowDescription;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.draw2d.geometry.Point;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.generation.GenerationUtils;
import org.eclipse.wb.internal.core.model.generation.statement.PureFlatStatementGenerator;
import org.eclipse.wb.internal.core.model.generation.statement.StatementGenerator;
import org.eclipse.wb.internal.core.model.generation.statement.lazy.LazyStatementGenerator;
import org.eclipse.wb.internal.core.model.presentation.DefaultObjectPresentation;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.variable.FieldInitializerVariableSupport;
import org.eclipse.wb.internal.core.model.variable.LazyVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.model.variable.description.LazyVariableDescription;
import org.eclipse.wb.internal.core.model.variable.description.VariableSupportDescription;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.swt.graphics.Image;

/**
 * Container for <i>non-visual beans</i>, direct child of root {@link JavaInfo}.
 *
 * @author lobas_av
 * @coverage core.model.nonvisual
 */
public final class NonVisualBeanContainerInfo extends ObjectInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the existing or new {@link ActionContainerInfo} for given root.
   */
  public static NonVisualBeanContainerInfo get(JavaInfo root) throws Exception {
    // try to find existing container
    NonVisualBeanContainerInfo container = find(root);
    if (container != null) {
      return container;
    }
    // add new container
    container = new NonVisualBeanContainerInfo();
    root.addChild(container);
    return container;
  }

  /**
   * @return find the existing {@link NonVisualBeanContainerInfo} for given root, otherwise
   *         <code>null</code>.
   */
  public static NonVisualBeanContainerInfo find(JavaInfo rootInfo) {
    for (ObjectInfo child : rootInfo.getChildren()) {
      if (child instanceof NonVisualBeanContainerInfo) {
        return (NonVisualBeanContainerInfo) child;
      }
    }
    return null;
  }

  /**
   * @return {@link NonVisualBeanInfo} if given {@link ASTNode} contains special non-visual comment
   *         or <code>null</code>.
   */
  public static NonVisualBeanInfo getNonVisualInfo(ASTNode creationNode) throws Exception {
    BodyDeclaration member = AstNodeUtils.getEnclosingNode(creationNode, BodyDeclaration.class);
    if (member != null) {
      return JavadocNonVisualBeanInfo.getNonVisualBeanInfo(member);
    }
    return null;
  }

  /**
   * Add new <i>non-visual bean</i> to container for giver root and location.
   */
  public static void add(JavaInfo root, JavaInfo bean, Point location) throws Exception {
    // prepare code generation settings
    VariableSupport variableSupport;
    StatementGenerator statementGenerator;
    {
      VariableSupportDescription variableDescription = GenerationUtils.getVariableDescription(root);
      if (variableDescription == LazyVariableDescription.INSTANCE) {
        variableSupport = new LazyVariableSupport(bean);
        statementGenerator = LazyStatementGenerator.INSTANCE;
      } else {
        variableSupport = new FieldInitializerVariableSupport(bean);
        statementGenerator = PureFlatStatementGenerator.INSTANCE;
      }
    }
    // do add
    JavaInfoUtils.add(
        bean,
        variableSupport,
        statementGenerator,
        AssociationObjects.nonVisual(),
        root,
        null);
    root.removeChild(bean);
    // create non-visual model
    {
      BodyDeclaration member =
          AstNodeUtils.getEnclosingNode(bean.getCreationSupport().getNode(), BodyDeclaration.class);
      NonVisualBeanInfo nonVisualInfo = new JavadocNonVisualBeanInfo(member);
      nonVisualInfo.setJavaInfo(bean);
      nonVisualInfo.moveLocation(location);
    }
    // add to container
    get(root).addChild(bean);
    // If "lazy" Action just added and not attached later, it will be not if execution flow.
    // Include it now.
    if (bean.getVariableSupport() instanceof LazyVariableSupport) {
      LazyVariableSupport lazyVariable = (LazyVariableSupport) bean.getVariableSupport();
      ExecutionFlowDescription flowDescription = JavaInfoUtils.getState(bean).getFlowDescription();
      flowDescription.addStartMethod(lazyVariable.m_accessor);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "{NonVisualBeans}";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Presentation
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public IObjectPresentation getPresentation() {
    return new DefaultObjectPresentation(this) {
      public String getText() throws Exception {
        return "(non-visual beans)";
      }

      @Override
      public Image getIcon() throws Exception {
        return DesignerPlugin.getImage("components/non_visual_beans_container.gif");
      }
    };
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Delete
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean canDelete() {
    return false;
  }

  @Override
  public void delete() throws Exception {
  }
}