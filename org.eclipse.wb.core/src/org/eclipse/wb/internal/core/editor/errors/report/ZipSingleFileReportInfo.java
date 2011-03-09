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

import org.eclipse.wb.internal.core.utils.base64.Base64;

import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Generates mimed zipped (if needed) file from various sources. Contains only one zip file entry.
 * 
 * @author mitin_aa
 * @coverage core.editor.errors.report
 */
public abstract class ZipSingleFileReportInfo extends FileReportInfo {
  /**
   * @return the open {@link InputStream} instance which provides data to be zipped and encoded. May
   *         return <code>null</code> if some error occurred during preparing data.
   * @throws Exception
   */
  protected abstract InputStream getDataStream() throws Exception;

  /**
   * @return the file name to include into zip entry.
   */
  protected abstract String getFileName();

  /**
   * @return <code>true</code> if the source should be compressed.
   */
  protected abstract boolean shouldCompress();

  /**
   * Zip and encode data from {@link #getDataStream()} into zip file with
   * {@link #getFileNameAttribute()} name.
   */
  @Override
  protected void encode(OutputStream outStream) throws Exception {
    InputStream fileStream = getDataStream();
    if (shouldCompress()) {
      // compress
      ZipOutputStream zipStream = new ZipOutputStream(new Base64.OutputStream(outStream, false));
      zipStream.setLevel(9);
      zipStream.putNextEntry(new ZipEntry(getFileName()));
      IOUtils.copy(fileStream, zipStream);
      zipStream.closeEntry();
      IOUtils.closeQuietly(zipStream);
    } else {
      // just mime without compressing
      OutputStream outputStream = new Base64.OutputStream(outStream, false);
      IOUtils.copy(fileStream, outputStream);
      IOUtils.closeQuietly(outputStream);
    }
    IOUtils.closeQuietly(fileStream);
  }
}
