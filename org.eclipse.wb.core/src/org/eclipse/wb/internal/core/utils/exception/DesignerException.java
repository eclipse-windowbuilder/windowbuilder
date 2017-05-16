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
package org.eclipse.wb.internal.core.utils.exception;

import org.apache.commons.lang.StringUtils;

/**
 * Exception class that should be used each time when Designer wants to throw "expected" exception,
 * for known error situations.
 *
 * For example, parser can throw exception if is can not find root methods for beginning parsing.
 *
 * @author scheglov_ke
 * @coverage core.util
 */
public class DesignerException extends Error {
  private static final long serialVersionUID = 0L;
  private final int m_code;
  private final String[] m_parameters;
  private final Throwable m_cause;
  private int m_sourcePosition = -1;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructors
  //
  ////////////////////////////////////////////////////////////////////////////
  public DesignerException(int code, String... parameters) {
    this(code, null, parameters);
  }

  public DesignerException(int code, Throwable cause, String... parameters) {
    super(cause);
    m_code = code;
    m_parameters = parameters;
    m_cause = cause;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public int getCode() {
    return m_code;
  }

  public String[] getParameters() {
    return m_parameters;
  }

  @Override
  public String getMessage() {
    String message = m_code + " (" + DesignerExceptionUtils.getExceptionTitle(m_code) + ").";
    if (m_parameters.length != 0) {
      message += " " + StringUtils.join(m_parameters, " ");
    }
    return message;
  }

  @Override
  public Throwable getCause() {
    return m_cause;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Position
  //
  ////////////////////////////////////////////////////////////////////////////
  public void setSourcePosition(int sourcePosition) {
    m_sourcePosition = sourcePosition;
  }

  public int getSourcePosition() {
    return m_sourcePosition;
  }
}
