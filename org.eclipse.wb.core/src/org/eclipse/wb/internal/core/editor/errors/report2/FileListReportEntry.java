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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Abstract class adding multiple files under some directory.
 *
 * @author mitin_aa
 * @coverage core.editor.errors.report2
 */
public abstract class FileListReportEntry implements IReportEntry {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Abstract
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the List of Files to be included into report.
   */
  protected abstract List<File> getFiles();

  /**
   * @return the prefix to create directory.
   */
  protected abstract String getPrefix();

  ////////////////////////////////////////////////////////////////////////////
  //
  // IReportEntry
  //
  ////////////////////////////////////////////////////////////////////////////
  public void write(ZipOutputStream zipStream) throws Exception {
    List<File> files = getFiles();
    for (File file : files) {
      InputStream fileStream = new FileInputStream(file);
      // get name with prefix
      String filePath = getPrefix() + FilenameUtils.getName(file.getAbsolutePath());
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