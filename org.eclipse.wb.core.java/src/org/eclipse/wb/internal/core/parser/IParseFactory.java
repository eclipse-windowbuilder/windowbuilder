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
package org.eclipse.wb.internal.core.parser;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * This interface is contributed via extension point and used by {@link JavaInfoParser} for creating
 * {@link JavaInfo}'s for root and any {@link ClassInstanceCreation} and {@link MethodInvocation}.
 *
 * @author scheglov_ke
 * @coverage core.model.parser
 */
public interface IParseFactory {
  /**
   * @return the {@link ParseRootContext} - information about root {@link JavaInfo} for given
   *         {@link TypeDeclaration}.
   */
  ParseRootContext getRootContext(AstEditor editor,
      TypeDeclaration typeDeclaration,
      ITypeBinding typeBinding) throws Exception;

  /**
   * Informs {@link IParseFactory} about some {@link Expression}.
   *
   * @return {@link JavaInfo} model corresponding to given {@link Expression}, or <code>null</code>.
   */
  JavaInfo create(AstEditor editor, Expression expression) throws Exception;

  /**
   * Informs {@link IParseFactory} about {@link ClassInstanceCreation}.
   *
   * @return {@link JavaInfo} model corresponding to given {@link ClassInstanceCreation}.
   */
  JavaInfo create(AstEditor editor,
      ClassInstanceCreation creation,
      IMethodBinding methodBinding,
      ITypeBinding typeBinding,
      Expression arguments[],
      JavaInfo argumentInfos[]) throws Exception;

  /**
   * Informs {@link IParseFactory} about {@link MethodInvocation}. Factory can create new
   * {@link JavaInfo}, add new parent/child link, etc.
   *
   * @return {@link JavaInfo} model corresponding to given {@link ClassInstanceCreation}.
   */
  JavaInfo create(AstEditor editor,
      MethodInvocation invocation,
      IMethodBinding methodBinding,
      Expression arguments[],
      JavaInfo expressionInfo,
      JavaInfo argumentInfos[],
      IJavaInfoParseResolver javaInfoResolver) throws Exception;
}
