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
package org.eclipse.wb.internal.core.editor.errors.report2.logs;

import com.google.common.collect.Lists;

import org.eclipse.wb.internal.core.editor.errors.report2.IReportEntry;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * The provider of Eclipse log files.
 * 
 * @author mitin_aa
 * @coverage core.editor.errors.report2
 */
public final class EclipseErrorLogsReportEntry implements IReportEntry {
  private List<File> getFiles() {
    // get path to Eclipse .log file(s) and get it's directory 
    IPath logsPath = Platform.getLogFileLocation().makeAbsolute().removeLastSegments(1);
    File logsPathAsFile = logsPath.toFile();
    // get list of .log files
    File[] logFiles = logsPathAsFile.listFiles(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.endsWith(".log");
      }
    });
    return Lists.newArrayList(logFiles);
  }

  public void write(ZipOutputStream zipStream) throws Exception {
    List<File> files = getFiles();
    for (File file : files) {
      InputStream fileStream = new FileInputStream(file);
      // remove leading slash
      String filePath = "eclipse-logs/" + FilenameUtils.getName(file.getAbsolutePath());
      zipStream.putNextEntry(new ZipEntry(filePath));
      try {
        IOUtils.copy(fileStream, zipStream);
      } finally {
        zipStream.closeEntry();
        IOUtils.closeQuietly(fileStream);
      }
    }
  }
}
