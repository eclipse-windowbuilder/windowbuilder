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
import com.google.common.collect.Maps;

import org.eclipse.wb.internal.core.utils.base64.Base64;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.FileInputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Able to collect files, zip them into single zip file, represent file (zipped or not) as mimed in
 * xml.
 * 
 * @author mitin_aa
 * @coverage core.editor.errors.report
 */
public class ZipMultipleFilesReportInfo extends FileReportInfo {
  // field
  private Map<String, Boolean> m_filesMap = Maps.newTreeMap();
  private String m_commonPath;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Operations
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Adds file to be sent.
   * 
   * @param filePath
   *          the full path to file.
   */
  public void addFile(String filePath) {
    m_filesMap.put(filePath, true);
  }

  /**
   * @param filePath
   *          the full path to file.
   * @return <code>true</code> if this file already added.
   */
  public boolean hasFile(String filePath) {
    return m_filesMap.get(filePath) != null;
  }

  /**
   * Includes file defined by <code>filePath</code> to being sent list.
   * 
   * @param filePath
   *          the full path to file.
   * @param include
   *          if <code>true</code> then file would be included to being sent list.
   */
  public void includeFile(String filePath, boolean include) {
    m_filesMap.put(filePath, include);
  }

  /**
   * Removes all file from being sent list.
   */
  public void removeAllFiles() {
    m_filesMap = Maps.newTreeMap();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Zip/Encode
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected boolean hasData() {
    ReportInfoUtils.checkFiles(m_filesMap);
    return !m_filesMap.isEmpty() && !ReportInfoUtils.isAllFilesExcluded(m_filesMap);
  }

  /**
   * Finds the common path for files added.
   */
  private void findCommonPath() {
    List<String> files = Lists.newArrayList();
    for (String filename : m_filesMap.keySet()) {
      if (!m_filesMap.get(filename)) {
        continue;
      }
      files.add(StringUtils.substring(filename, FilenameUtils.getPrefixLength(filename)));
    }
    if (files.size() == 1) {
      m_commonPath = FilenameUtils.getPath(files.get(0));
    } else {
      m_commonPath = StringUtils.getCommonPrefix(files.toArray(new String[files.size()]));
    }
  }

  /**
   * Zip files into zip file and encode it as base64.
   */
  @Override
  protected void encode(OutputStream outStream) throws Exception {
    findCommonPath();
    ZipOutputStream zipStream = new ZipOutputStream(new Base64.OutputStream(outStream, false));
    zipStream.setLevel(9);
    // compress the files
    for (String filename : m_filesMap.keySet()) {
      if (!m_filesMap.get(filename)) {
        continue;
      }
      FileInputStream fileStream = new FileInputStream(filename);
      zipStream.putNextEntry(new ZipEntry(getZipEntryName(filename)));
      IOUtils.copy(fileStream, zipStream);
      zipStream.closeEntry();
      IOUtils.closeQuietly(fileStream);
    }
    IOUtils.closeQuietly(zipStream);
  }

  protected String getZipEntryName(String filename) {
    String withoutPrefix = StringUtils.substring(filename, FilenameUtils.getPrefixLength(filename));
    if (StringUtils.isEmpty(m_commonPath)) {
      return withoutPrefix;
    }
    return StringUtils.substring(withoutPrefix, m_commonPath.length());
  }

  @Override
  protected String getFileNameAttribute() {
    return "user-attachments.zip";
  }
}
