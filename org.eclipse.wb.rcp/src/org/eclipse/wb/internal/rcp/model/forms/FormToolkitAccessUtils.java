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
package org.eclipse.wb.internal.rcp.model.forms;

import org.eclipse.wb.core.eval.ExecutionFlowUtils2;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.core.model.broadcast.ExecutionFlowEnterFrame;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.model.creation.MethodParameterCreationSupport;
import org.eclipse.wb.internal.core.model.creation.factory.InstanceFactoryInfo;
import org.eclipse.wb.internal.core.model.util.GlobalStateJava;
import org.eclipse.wb.internal.core.model.variable.MethodParameterVariableSupport;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.internal.core.utils.state.EditorState;
import org.eclipse.wb.internal.rcp.model.RcpMethodParameterEvaluator;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Helper for using {@link FormToolkitAccess}.
 *
 * @author scheglov_ke
 * @coverage rcp.model.forms
 */
public final class FormToolkitAccessUtils {
  /**
   * Creates {@link InstanceFactoryInfo} for {@link FormToolkit} using {@link FormToolkitAccess}.
   * This method should be used during {@link JavaInfo} initializing of "this" component, i.e.
   * practically during parsing.
   *
   * @return the created {@link InstanceFactoryInfo}.
   */
  public static InstanceFactoryInfo createFormToolkit_usingAccess(JavaInfo hostJavaInfo)
      throws Exception {
    TypeDeclaration typeDeclaration = JavaInfoUtils.getTypeDeclaration(hostJavaInfo);
    // prepare "toolkit"
    FormToolkitAccess toolkitAccess = FormToolkitAccess.getOrFail(typeDeclaration);
    CreationSupport creationSupport = new FormToolkitCreationSupport(hostJavaInfo, toolkitAccess);
    InstanceFactoryInfo toolkit = createFormToolkit(hostJavaInfo.getEditor(), creationSupport);
    {
      VariableSupport variableSupport =
          new FormToolkitVariableSupport(toolkit, hostJavaInfo, toolkitAccess);
      toolkit.setVariableSupport(variableSupport);
    }
    return toolkit;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // FormToolkit as constructor parameter
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Create {@link InstanceFactoryInfo} for {@link FormToolkit} parameter of
   * {@link MethodDeclaration}.
   */
  public static void createFormToolkit_asMethodParameter(AstEditor editor,
      final MethodDeclaration method) throws Exception {
    for (SingleVariableDeclaration parameter : DomGenerics.parameters(method)) {
      if (AstNodeUtils.isSuccessorOf(parameter, "org.eclipse.ui.forms.widgets.FormToolkit")) {
        CreationSupport creationSupport = new MethodParameterCreationSupport(parameter);
        final InstanceFactoryInfo toolkit = createFormToolkit(editor, creationSupport);
        // we may be don't have hierarchy yet
        GlobalStateJava.activate(toolkit);
        // bind to variable
        ExecutionFlowUtils2.ensurePermanentValue(parameter.getName()).setModel(toolkit);
        {
          VariableSupport variableSupport = new MethodParameterVariableSupport(toolkit, parameter);
          toolkit.setVariableSupport(variableSupport);
        }
        // set object during evaluation
        toolkit.addBroadcastListener(new ExecutionFlowEnterFrame() {
          @Override
          public void invoke(ASTNode node) throws Exception {
            if (node == method) {
              toolkit.setObject(RcpMethodParameterEvaluator.FORM_TOOLKIT);
            }
          }
        });
      }
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private static InstanceFactoryInfo createFormToolkit(AstEditor editor,
      CreationSupport creationSupport) throws Exception {
    EditorState editorState = EditorState.get(editor);
    ClassLoader editorLoader = editorState.getEditorLoader();
    InstanceFactoryInfo toolkit =
        InstanceFactoryInfo.createFactory(
            editor,
            editorLoader.loadClass("org.eclipse.ui.forms.widgets.FormToolkit"),
            creationSupport);
    editorState.getTmp_Components().add(toolkit);
    return toolkit;
  }
}
