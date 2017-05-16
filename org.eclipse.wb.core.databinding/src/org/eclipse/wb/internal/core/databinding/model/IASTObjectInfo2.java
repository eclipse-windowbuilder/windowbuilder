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
package org.eclipse.wb.internal.core.databinding.model;

import org.eclipse.wb.core.model.JavaInfo;

/**
 *
 * @author lobas_av
 *
 */
public interface IASTObjectInfo2 {
  boolean isField();

  void setField();

  String getVariableIdentifier() throws Exception;

  void setVariableIdentifier(JavaInfo javaInfoRoot, String variable, boolean field);
}