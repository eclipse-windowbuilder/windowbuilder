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
package org.eclipse.wb.internal.rcp.model.jface.action;

import com.google.common.collect.ImmutableList;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.ObjectInfo;
import org.eclipse.wb.core.model.association.AssociationObjects;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.ConstructorCreationSupport;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.generation.statement.PureFlatStatementGenerator;
import org.eclipse.wb.internal.core.model.presentation.DefaultObjectPresentation;
import org.eclipse.wb.internal.core.model.presentation.IObjectPresentation;
import org.eclipse.wb.internal.core.model.variable.FieldUniqueVariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.BodyDeclarationTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;
import org.eclipse.wb.internal.core.utils.check.Assert;
import org.eclipse.wb.internal.rcp.Activator;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.graphics.Image;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.util.Collections;
import java.util.List;

/**
 * Container for {@link ActionInfo}, direct child of root {@link JavaInfo}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.jface
 */
public final class ActionContainerInfo extends ObjectInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "{org.eclipse.wb.internal.rcp.model.jface.action.ActionContainerInfo}";
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
  // New Action utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private final static String KEY_NEW_ACTION = "KEY_NEW_ACTION";

  /**
   * @return the {@link ActionInfo} for adding direct subclass of {@link Action}.
   */
  public static ActionInfo createNew(JavaInfo root) throws Exception {
    AstEditor editor = root.getEditor();
    ActionInfo action =
        (ActionInfo) JavaInfoUtils.createJavaInfo(
            editor,
            "org.eclipse.jface.action.Action",
            new ConstructorCreationSupport());
    action.putArbitraryValue(KEY_NEW_ACTION, Boolean.TRUE);
    return action;
  }

  /**
   * Ensures that given {@link ActionInfo} has instance in source, adds into source and to the
   * {@link ActionContainerInfo} if needed.
   *
   * @param action
   *          the {@link ActionInfo} to ensure.
   */
  public static void ensureInstance(JavaInfo root, final ActionInfo action) throws Exception {
    if (action.getParent() == null) {
      final AstEditor editor = root.getEditor();
      // prepare "actionsMethod"
      MethodDeclaration actionsMethod;
      {
        // prepare parameters for "actionsMethod"
        String methodSignature = getExistingParameter(root, "actionContainer.signature");
        String methodHeader = getExistingParameter(root, "actionContainer.header");
        String methodInvocation = JavaInfoUtils.getParameter(root, "actionContainer.invocation");
        // ensure "actionsMethod"
        TypeDeclaration typeDeclaration = JavaInfoUtils.getTypeDeclaration(root);
        actionsMethod = AstNodeUtils.getMethodBySignature(typeDeclaration, methodSignature);
        if (actionsMethod == null) {
          StatementTarget rootTarget = root.getVariableSupport().getStatementTarget();
          MethodDeclaration rootMethod = editor.getEnclosingMethod(rootTarget.getPosition());
          actionsMethod =
              editor.addMethodDeclaration(
                  methodHeader,
                  ImmutableList.<String>of(),
                  new BodyDeclarationTarget(rootMethod, false));
          // if needed, invoke from constructor
          if (methodInvocation != null) {
            root.addExpressionStatement(rootTarget, methodInvocation);
          }
        }
      }
      // prepare target for new Action
      final StatementTarget actionTarget;
      {
        Block targetBlock =
            (Block) editor.addStatement(
                ImmutableList.of("{", "}"),
                new StatementTarget(actionsMethod, false));
        actionTarget = new StatementTarget(targetBlock, true);
      }
      // set CreationSupport with two implemented methods:
      //  - add_getSource()
      //  - add_setSourceExpression(Expression)
      if (action.getArbitraryValue(KEY_NEW_ACTION) == Boolean.TRUE) {
        action.removeArbitraryValue(KEY_NEW_ACTION);
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(CreationSupport.class);
        enhancer.setCallback(new MethodInterceptor() {
          public Object intercept(Object obj,
              java.lang.reflect.Method method,
              Object[] args,
              MethodProxy proxy) throws Throwable {
            // use anonymous creation
            if (method.getName().equals("add_getSource")) {
              String eol = editor.getGeneration().getEndOfLine();
              String source = "";
              source += "new org.eclipse.jface.action.Action(\"New Action\") {" + eol;
              source += "}";
              return source;
            }
            // replace this CreationSupport with ConstructorCreationSupport
            if (method.getName().equals("add_setSourceExpression")) {
              ClassInstanceCreation creation = (ClassInstanceCreation) args[0];
              action.setCreationSupport(new ConstructorCreationSupport(creation));
              action.bindToExpression(creation);
              return null;
            }
            // handle in CreationSupport
            return proxy.invokeSuper(obj, args);
          }
        });
        CreationSupport creationSupport = (CreationSupport) enhancer.create();
        action.setCreationSupport(creationSupport);
      }
      // do add new Action
      JavaInfoUtils.add(
          action,
          new FieldUniqueVariableSupport(action),
          PureFlatStatementGenerator.INSTANCE,
          AssociationObjects.empty(),
          root,
          null,
          actionTarget);
      root.removeChild(action);
      ActionContainerInfo.get(root).addChild(action);
    }
  }

  /**
   * @return the value of parameter from {@link JavaInfo}, checks that it is not <code>null</code>.
   */
  private static String getExistingParameter(JavaInfo javaInfo, String parameterName) {
    String parameterValue = JavaInfoUtils.getParameter(javaInfo, parameterName);
    Assert.isNotNull(
        parameterValue,
        "No '%s' parameter for %s.",
        parameterName,
        javaInfo.getDescription().getComponentClass());
    return parameterValue;
  }
}