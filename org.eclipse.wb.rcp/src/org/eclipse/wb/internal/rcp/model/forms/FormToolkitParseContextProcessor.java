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

import org.eclipse.wb.core.eval.ExecutionFlowDescription;
import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.parser.IParseContextProcessor;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

import org.eclipse.jdt.core.dom.MethodDeclaration;

import java.util.List;

/**
 * {@link IParseContextProcessor} for Forms API.
 * 
 * @author scheglov_ke
 * @coverage rcp.model.forms
 */
public final class FormToolkitParseContextProcessor implements IParseContextProcessor {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Instance
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final Object INSTANCE = new FormToolkitParseContextProcessor();

  private FormToolkitParseContextProcessor() {
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IParseContextProcessor
  //
  ////////////////////////////////////////////////////////////////////////////
  public void process(AstEditor editor,
      ExecutionFlowDescription flowDescription,
      List<JavaInfo> components) throws Exception {
    List<MethodDeclaration> methods = flowDescription.getStartMethods();
    if (!methods.isEmpty()) {
      MethodDeclaration method = methods.get(0);
      FormToolkitAccessUtils.createFormToolkit_asMethodParameter(editor, method);
    }
  }
}
