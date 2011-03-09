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
package org.eclipse.wb.internal.swt.model.layout.absolute;

import org.eclipse.wb.core.model.JavaInfo;
import org.eclipse.wb.internal.core.model.variable.VariableSupport;
import org.eclipse.wb.internal.core.utils.ast.NodeTarget;
import org.eclipse.wb.internal.core.utils.ast.StatementTarget;

import org.eclipse.jdt.core.dom.NullLiteral;

/**
 * Implementation of {@link VariableSupport} for {@link AbsoluteLayoutInfo} specified as
 * {@link NullLiteral} in "setLayout(null)"
 * 
 * @author lobas_av
 * @coverage swt.model.layout
 */
public final class AbsoluteLayoutVariableSupport extends VariableSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbsoluteLayoutVariableSupport(JavaInfo javaInfo) {
    super(javaInfo);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return "absolute";
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Name
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean hasName() {
    return false;
  }

  @Override
  public String getName() {
    throw new IllegalStateException();
  }

  @Override
  public void setName(String newName) throws Exception {
    throw new IllegalStateException();
  }

  @Override
  public String getTitle() throws Exception {
    throw new IllegalStateException();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Expressions
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String getReferenceExpression(NodeTarget target) throws Exception {
    throw new IllegalStateException();
  }

  @Override
  public String getAccessExpression(NodeTarget target) throws Exception {
    throw new IllegalStateException();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Conversion
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public boolean canConvertLocalToField() {
    return false;
  }

  @Override
  public void convertLocalToField() throws Exception {
    throw new IllegalStateException();
  }

  @Override
  public boolean canConvertFieldToLocal() {
    return false;
  }

  @Override
  public void convertFieldToLocal() throws Exception {
    throw new IllegalStateException();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Target
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public StatementTarget getStatementTarget() throws Exception {
    throw new IllegalStateException();
  }
}