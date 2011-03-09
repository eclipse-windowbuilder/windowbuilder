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

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.editor.errors.report.logs.IErrorLogsProvider;
import org.eclipse.wb.internal.core.utils.execution.ExecutionUtils;
import org.eclipse.wb.internal.core.utils.execution.RunnableEx;
import org.eclipse.wb.internal.core.utils.external.ExternalFactoriesHelper;

import org.eclipse.core.resources.IProject;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.List;

/**
 * Includes log file(s) into report using "errorLogsProviders" ext. point.
 * 
 * @author mitin_aa
 * @coverage core.editor.errors.report
 */
public final class LogsReportInfo extends ZipMultipleFilesReportInfo {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public LogsReportInfo(final IProject project) {
    ExecutionUtils.runLog(new RunnableEx() {
      public void run() throws Exception {
        List<File> logFiles = getLogFiles(project);
        // prepare it to zip
        for (File logFile : logFiles) {
          if (logFile.exists()) {
            addFile(logFile.getAbsolutePath());
          }
        }
      }
    });
  }

  private List<File> getLogFiles(IProject project) throws Exception {
    List<File> files = Lists.newArrayList();
    for (IErrorLogsProvider provider : getErrorLogsProviders()) {
      files.addAll(provider.getLogFiles(project));
    }
    return files;
  }

  /**
   * @return the instances of {@link IErrorLogsProvider}.
   */
  private static List<IErrorLogsProvider> getErrorLogsProviders() throws Exception {
    return ExternalFactoriesHelper.getElementsInstances(
        IErrorLogsProvider.class,
        "org.eclipse.wb.core.errorLogsProviders",
        "provider");
  }

  @Override
  protected String getZipEntryName(String filename) {
    return FilenameUtils.getName(filename);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // ZipFileReportInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected String getFileNameAttribute() {
    return "eclipse-logs.zip";
  }
}
