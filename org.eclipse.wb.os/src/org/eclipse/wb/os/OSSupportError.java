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
package org.eclipse.wb.os;

/**
 * Error thrown when no appropriate {@link OSSupport} instance found for runtime OS or WS.
 * 
 * @author mitin_aa
 * @coverage os.core
 */
public class OSSupportError extends Error {
  private static final long serialVersionUID = 1L;
  public static final int ERROR_CODE = 900;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public OSSupportError(String string, Throwable e) {
    super(string, e);
  }

  public OSSupportError(String string) {
    super(string);
  }
}
