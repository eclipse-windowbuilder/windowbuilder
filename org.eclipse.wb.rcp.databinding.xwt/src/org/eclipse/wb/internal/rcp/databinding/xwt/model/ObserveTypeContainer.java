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
package org.eclipse.wb.internal.rcp.databinding.xwt.model;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;
import org.eclipse.wb.internal.core.databinding.model.IDatabindingsProvider;
import org.eclipse.wb.internal.core.databinding.parser.IModelResolver;
import org.eclipse.wb.internal.core.databinding.ui.ObserveType;
import org.eclipse.wb.internal.core.utils.ast.AstEditor;
import org.eclipse.wb.internal.core.xml.model.XmlObjectInfo;
import org.eclipse.wb.internal.rcp.databinding.xwt.DatabindingsProvider;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * 
 * @author lobas_av
 * 
 */
public abstract class ObserveTypeContainer
    extends
      org.eclipse.wb.internal.core.databinding.model.ObserveTypeContainer {
  protected DatabindingsProvider m_provider;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ObserveTypeContainer(ObserveType observeType, boolean isTarget, boolean isModel) {
    super(observeType, isTarget, isModel);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Initialize
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final void initialize(IDatabindingsProvider provider) throws Exception {
    m_provider = (DatabindingsProvider) provider;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IObserveInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final void createObservables(JavaInfo root,
      IModelResolver resolver,
      AstEditor editor,
      TypeDeclaration rootNode) throws Exception {
  }

  @Override
  public final void synchronizeObserves(JavaInfo root, AstEditor editor, TypeDeclaration rootNode)
      throws Exception {
  }

  public void synchronizeObserves() throws Exception {
  }

  public abstract void createObservables(XmlObjectInfo xmlObjectRoot) throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Parser
  //
  ////////////////////////////////////////////////////////////////////////////
  public final AstObjectInfo parseExpression(AstEditor editor,
      String signature,
      ClassInstanceCreation creation,
      Expression[] arguments,
      IModelResolver resolver,
      IDatabindingsProvider provider) throws Exception {
    return null;
  }

  public final AstObjectInfo parseExpression(AstEditor editor,
      String signature,
      MethodInvocation invocation,
      Expression[] arguments,
      IModelResolver resolver) throws Exception {
    return null;
  }
}