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
package org.eclipse.wb.internal.core.databinding.parser;

import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;

import org.eclipse.jdt.core.dom.Expression;

/**
 * Helper class that know as {@link AstObjectInfo} represented on AST.
 *
 * @author lobas_av
 * @coverage bindings.parser
 */
public interface IModelSupport {
  /**
   * @return {@link AstObjectInfo} host model.
   */
  AstObjectInfo getModel();

  /**
   * @return <code>true</code> if given {@link Expression} represented host model.
   */
  boolean isRepresentedBy(Expression expression) throws Exception;
}