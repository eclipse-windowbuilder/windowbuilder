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
 * Interface for resolving {@link Expressions}'s into {@link AstObjectInfo}.
 *
 * @author lobas_av
 * @coverage bindings.parser
 */
public interface IModelResolver {
  /**
   * Add default {@link AstObjectInfo} model resolver for given {@link Expression}.
   */
  void addModel(AstObjectInfo model, Expression creation) throws Exception;

  /**
   * @return {@link AstObjectInfo} for given {@link Expression} or <code>null</code> if given
   *         {@link Expression} does not represent {@link AstObjectInfo}.
   */
  AstObjectInfo getModel(Expression expression) throws Exception;

  /**
   * XXX
   */
  AstObjectInfo getModel(Expression expression, IModelResolverFilter filter) throws Exception;

  /**
   * Add {@link IModelSupport} special {@link AstObjectInfo} model resolver.
   */
  void addModelSupport(IModelSupport modelSupport);

  /**
   * @return {@link IModelSupport} for given {@link Expression} or <code>null</code> if given
   *         {@link Expression} does not represent {@link IModelSupport}.
   */
  IModelSupport getModelSupport(Expression expression) throws Exception;
}