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
package org.eclipse.wb.internal.swing.FormLayout.parser;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.creation.CreationSupport;
import org.eclipse.wb.internal.core.parser.AbstractParseFactory;
import org.eclipse.wb.internal.core.parser.IJavaInfoParseResolver;
import org.eclipse.wb.internal.core.parser.IParseFactory;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.swing.FormLayout.Activator;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * {@link IParseFactory} for JGoodies.
 * 
 * @author scheglov_ke
 * @coverage swing.FormLayout.model
 */
public class ParseFactory extends AbstractParseFactory {
  ////////////////////////////////////////////////////////////////////////////
  //
  // IParseFactory
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public JavaInfo create(AstEditor editor,
      MethodInvocation invocation,
      IMethodBinding methodBinding,
      Expression arguments[],
      JavaInfo expressionInfo,
      JavaInfo argumentInfos[],
      IJavaInfoParseResolver javaInfoResolver) throws Exception {
    if (invocation.getExpression() != null
        && AstNodeUtils.isSuccessorOf(
            invocation.getExpression(),
            "com.jgoodies.forms.factories.DefaultComponentFactory")
        && invocation.getName().getIdentifier().startsWith("create")) {
      Class<?> clazz = getClass(editor, methodBinding.getReturnType());
      CreationSupport creationSupport = new DefaultComponentFactoryCreationSupport(invocation);
      return JavaInfoUtils.createJavaInfo(editor, clazz, creationSupport);
    }
    return null;
  }

  @Override
  protected String getToolkitId() {
    return Activator.PLUGIN_ID;
  }
}
