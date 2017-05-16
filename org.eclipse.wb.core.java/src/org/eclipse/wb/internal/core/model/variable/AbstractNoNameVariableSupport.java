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
package org.eclipse.wb.internal.core.model.variable;

import org.eclipse.wb.core.model.JavaInfo;

/**
 * Implementation of {@link VariableSupport} for cases when there are no variable name, so no
 * implementation of methods that operates this name.
 *
 * @author scheglov_ke
 * @coverage core.model.variable
 */
public abstract class AbstractNoNameVariableSupport extends VariableSupport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public AbstractNoNameVariableSupport(JavaInfo javaInfo) {
    super(javaInfo);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Name
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final boolean hasName() {
    return false;
  }

  @Override
  public final String getName() {
    throw new IllegalStateException();
  }

  @Override
  public final void setName(String newName) throws Exception {
    throw new IllegalStateException();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Conversion
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public final boolean canConvertLocalToField() {
    return false;
  }

  @Override
  public final void convertLocalToField() throws Exception {
    throw new IllegalStateException();
  }

  @Override
  public final boolean canConvertFieldToLocal() {
    return false;
  }

  @Override
  public final void convertFieldToLocal() throws Exception {
    throw new IllegalStateException();
  }
}
