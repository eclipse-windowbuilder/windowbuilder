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
package org.eclipse.wb.internal.swing.model.bean;

import org.eclipse.wb.core.eval.ExecutionFlowDescription;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.core.model.association.EmptyAssociation;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.generation.GenerationUtils;
import org.eclipse.wb.internal.core.model.generation.statement.PureFlatStatementGenerator;
import org.eclipse.wb.internal.core.model.generation.statement.StatementGenerator;
import org.eclipse.wb.internal.core.model.presentation.DefaultObjectPresentation;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.variable.FieldInitializerVariableSupport;
import org.eclipse.wb.internal.core.model.variable.LazyVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.swing.Activator;

import org.eclipse.swt.graphics.Image;

import java.util.Collections;
import java.util.List;

/**
 * Container for {@link ActionInfo}, direct child of root {@link JavaInfo}.
 * 
 * @author scheglov_ke
 * @coverage swing.model
 */
public final class ActionContainerInfo extends ObjectInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "{org.eclipse.wb.internal.swing.model.bean.ActionContainerInfo}";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the existing or new {@link ActionContainerInfo} for given root.
   */
  public static ActionContainerInfo get(JavaInfo root) throws Exception {
    // try to find existing container
    ActionContainerInfo container = findContainer(root);
    if (container != null) {
      return container;
    }
    // add new container
    container = new ActionContainerInfo();
    root.addChild(container);
    return container;
  }

  /**
   * @return all {@link ActionInfo}'s for given root.
   */
  public static List<ActionInfo> getActions(JavaInfo root) {
    ActionContainerInfo container = findContainer(root);
    if (container != null) {
      return container.getChildren(ActionInfo.class);
    }
    return Collections.emptyList();
  }

  /**
   * Adds {@link ActionInfo} and to the {@link ActionContainerInfo}.
   * 
   * @param action
   *          the {@link ActionInfo} to add.
   */
  public static void add(JavaInfo root, ActionInfo action) throws Exception {
    // prepare code generation settings
    VariableSupport variableSupport = GenerationUtils.getVariableSupport(action);
    StatementGenerator statementGenerator = GenerationUtils.getStatementGenerator(action);
    if (!(variableSupport instanceof LazyVariableSupport)) {
      variableSupport = new FieldInitializerVariableSupport(action);
      statementGenerator = PureFlatStatementGenerator.INSTANCE;
    }
    // do add
    JavaInfoUtils.add(
        action,
        variableSupport,
        statementGenerator,
        AssociationObjects.empty(),
        root,
        null);
    root.removeChild(action);
    ActionContainerInfo.get(root).addChild(action);
    // If "lazy" Action just added and not attached later, it will be not if execution flow.
    // Include it now.
    if (action.getVariableSupport() instanceof LazyVariableSupport) {
      LazyVariableSupport lazyVariable = (LazyVariableSupport) action.getVariableSupport();
      ExecutionFlowDescription flowDescription =
          JavaInfoUtils.getState(action).getFlowDescription();
      flowDescription.addStartMethod(lazyVariable.m_accessor);
    }
  }

  /**
   * @return find the existing {@link ActionContainerInfo} for given root.
   */
  private static ActionContainerInfo findContainer(JavaInfo root) {
    for (ObjectInfo child : root.getChildren()) {
      if (child instanceof ActionContainerInfo) {
        return (ActionContainerInfo) child;
      }
    }
    return null;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds {@link ActionInfo} as {@link ObjectInfo} child.
   */
  void addAction(ActionInfo action) throws Exception {
    action.setAssociation(new EmptyAssociation());
    addChild(action);
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
        return "(actions)";
      }

      @Override
      public Image getIcon() throws Exception {
        return Activator.getImage("info/Action/container.gif");
      }
    };
  }
}