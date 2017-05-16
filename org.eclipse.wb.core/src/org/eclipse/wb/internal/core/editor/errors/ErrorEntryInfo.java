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
package org.eclipse.wb.internal.core.editor.errors;

/**
 * Structure representing error entry in "exceptions.xml" file.
 *
 * @author mitin_aa
 * @coverage core.editor.errors
 */
public final class ErrorEntryInfo {
  private final int m_code;
  private final boolean m_warning;
  private final String m_title;
  private final String m_description;
  private final String m_altDescription;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @param code
   *          the code of the error.
   * @param warning
   *          is this entry should be threat as warning.
   * @param title
   *          the title of the error.
   * @param description
   *          html-based description of the error.
   */
  public ErrorEntryInfo(int code, boolean warning, String title, String description) {
    this(code, warning, title, description, null);
  }

  /**
   * @param code
   *          the code of the error.
   * @param warning
   *          is this entry should be threat as warning.
   * @param title
   *          the title of the error.
   * @param description
   *          html-based description of the error.
   * @param altDescription
   *          plain-text description of the error (optional, can be <code>null</code>).
   */
  public ErrorEntryInfo(int code,
      boolean warning,
      String title,
      String description,
      String altDescription) {
    m_code = code;
    m_warning = warning;
    m_title = title;
    m_description = description;
    m_altDescription = altDescription;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Getters
  //
  ////////////////////////////////////////////////////////////////////////////
  public int getCode() {
    return m_code;
  }

  public boolean isWarning() {
    return m_warning;
  }

  public String getTitle() {
    return m_title;
  }

  public String getDescription() {
    return m_description;
  }

  public String getAltDescription() {
    return m_altDescription;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Object
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public String toString() {
    return Integer.toString(getCode()) + ": " + getTitle();
  }
}
