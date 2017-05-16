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
package org.eclipse.wb.internal.core.editor.errors.report2;

import org.eclipse.core.resources.IProject;

/**
 * Interface for working with error report.
 *
 * @author mitin_aa
 * @coverage core.editor.errors.report2
 */
public interface IErrorReport {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Screenshot
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if report has default screenshot set.
   */
  boolean hasDefaultScreenshot();

  /**
   * @param filePath
   *          the full path to file with screenshot.
   * @return <code>true</code> if this screenshot already added.
   */
  boolean hasScreenshot(String filePath);

  /**
   * Includes screenshot defined by <code>filePath</code> to being sent list.
   *
   * @param filePath
   *          the full path to file with screenshot.
   * @param include
   *          if <code>true</code> then screenshot would be included to being sent.
   */
  void includeScreenshot(String filePath, boolean include);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Arbitrary Files
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @param filePath
   *          the full path to file.
   * @return <code>true</code> if this file already added.
   */
  boolean hasFile(String filePath);

  /**
   * Includes file defined by <code>filePath</code> to being sent list.
   *
   * @param filePath
   *          the full path to file.
   * @param include
   *          if <code>true</code> then file would be included to being sent list.
   */
  void includeFile(String filePath, boolean include);

  ////////////////////////////////////////////////////////////////////////////
  //
  // Source/Project
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return <code>true</code> if the report has a source file.
   */
  boolean hasSourceFile();

  /**
   * @return the project for reporting error.
   */
  IProject getProject();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Entries Management
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @param entry
   *          the report entry to add into report.
   */
  void addEntry(IReportEntry entry);

  /**
   * @param entry
   *          previously added report entry to remove, method does nothing if <code>null</code> or
   *          wasn't added before.
   */
  void removeEntry(IReportEntry entry);
}