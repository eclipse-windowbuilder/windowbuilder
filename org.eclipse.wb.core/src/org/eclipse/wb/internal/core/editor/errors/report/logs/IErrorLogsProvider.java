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
package org.eclipse.wb.internal.core.editor.errors.report.logs;

import org.eclipse.core.resources.IProject;

import java.io.File;
import java.util.List;

/**
 * The provider for error logs which further will be included into error report.
 * 
 * @author mitin_aa
 * @coverage core.editor.errors.report
 */
public interface IErrorLogsProvider {
  /**
   * @param project
   * @return the list of {@link File} which should be included into report. Files must exists and be
   *         accessible for reading.
   */
  List<File> getLogFiles(IProject project) throws Exception;
}