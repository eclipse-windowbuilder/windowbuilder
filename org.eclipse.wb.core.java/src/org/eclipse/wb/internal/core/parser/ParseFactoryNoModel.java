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

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * This interface is used to identify {@link ASTNode} which is definitely not {@link JavaInfo}. So
 * we throw it away right now, without checking using other {@link ParseFactoryNoModel} and
 * {@link IParseFactory}.
 *
 * @author scheglov_ke
 * @author sablin_aa
 * @coverage core.model.parser
 */
public abstract class ParseFactoryNoModel {
  /**
   * @return <code>true</code> if given {@link ASTNode} is not {@link JavaInfo}.
   */
  public boolean noModel(ASTNode node) {
    return false;
  }

  /**
   * @return <code>true</code> if given {@link ClassInstanceCreation} is not {@link JavaInfo}.
   */
  public boolean noModel(ClassInstanceCreation creation, ITypeBinding typeBinding) {
    return false;
  }
}
