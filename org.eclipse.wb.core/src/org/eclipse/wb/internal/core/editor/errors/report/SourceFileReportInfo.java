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

import org.eclipse.wb.internal.core.editor.errors.report.ErrorReport.SourceInfo;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;

/**
 * Attaches the compilation unit in which error happens. If the source lesser than 20K then just
 * mimed but not packed in zip.
 * 
 * @author mitin_aa
 * @coverage core.editor.errors.report
 */
public final class SourceFileReportInfo extends ZipSingleFileReportInfo {
  private final SourceInfo m_sourceFile;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public SourceFileReportInfo(SourceInfo sourceFile) {
    m_sourceFile = sourceFile;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IReportInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected InputStream getDataStream() throws Exception {
    return IOUtils.toInputStream(m_sourceFile.getContent());
  }

  @Override
  protected String getFileName() {
    return m_sourceFile.getName();
  }

  @Override
  protected boolean shouldCompress() {
    return m_sourceFile.getContent().getBytes().length > 20 * 1024;
  }

  @Override
  protected String getFileNameAttribute() {
    if (!shouldCompress()) {
      return getFileName();
    }
    return FilenameUtils.getBaseName(getFileName()) + ".zip";
  }

  @Override
  protected boolean hasData() {
    return m_sourceFile != null && m_sourceFile.isValid();
  }
}
