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
package org.eclipse.wb.internal.rcp.databinding.model.beans;

import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;
import org.eclipse.wb.internal.core.databinding.parser.IModelResolver;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;

import org.eclipse.jdt.core.dom.Expression;

/**
 * Description for single AST creation.
 *
 * @author lobas_av
 * @coverage bindings.rcp.model.beans
 */
public class ModelCreator<T> {
  public int startIndex0 = -1;
  public int startIndex1 = -1;
  public int startIndex2 = -1;
  public boolean isPojo;
  private final IModelCreator<T> m_creator;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public ModelCreator(IModelCreator<T> creator) {
    m_creator = creator;
  }

  public ModelCreator(int startIndex, IModelCreator<T> creator) {
    this.startIndex0 = startIndex;
    m_creator = creator;
  }

  public ModelCreator(int startIndex0, int startIndex1, IModelCreator<T> creator) {
    this.startIndex0 = startIndex0;
    this.startIndex1 = startIndex1;
    m_creator = creator;
  }

  public ModelCreator(int startIndex0, int startIndex1, int startIndex2, IModelCreator<T> creator) {
    this.startIndex0 = startIndex0;
    this.startIndex1 = startIndex1;
    this.startIndex2 = startIndex2;
    m_creator = creator;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Creation
  //
  ////////////////////////////////////////////////////////////////////////////
  public AstObjectInfo create(T container,
      AstEditor editor,
      Expression[] arguments,
      IModelResolver resolver) throws Exception {
    return m_creator.create(container, editor, arguments, resolver, this);
  }
}