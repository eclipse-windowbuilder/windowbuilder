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
package org.eclipse.wb.internal.core.editor.errors.report;

import org.eclipse.core.runtime.IProgressMonitor;

/**
 * Interface for report submitting. Known ways of report submitting are e-mail and web-direct.
 * 
 * @author mitin_aa
 * @coverage core.editor.errors.report
 */
public interface IReportSubmitter {
  /**
   * 
   * Submits prepared report via network.
   * 
   * @param filePath
   *          the full path to prepared xml report file.
   * @param monitor
   *          the progress monitor.
   * @throws Exception
   */
  void submit(String filePath, IProgressMonitor monitor) throws Exception;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Known impls
  //
  ////////////////////////////////////////////////////////////////////////////
  IReportSubmitter WEB = new WebReportSubmitter();
  IReportSubmitter MAIL = new MailReportSubmitter();
}
