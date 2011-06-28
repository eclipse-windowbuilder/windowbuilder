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
package org.eclipse.wb.tests.designer.core;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Fluent factory for creating ZIP/JAR files.
 * 
 * @author scheglov_ke
 */
public final class ZipFileFactory {
  private final ZipOutputStream m_zipStream;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ZipFileFactory(OutputStream outputStream) throws IOException {
    m_zipStream = new ZipOutputStream(outputStream);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  public void close() throws IOException {
    m_zipStream.close();
  }

  public ZipFileFactory add(String path, String content) throws IOException {
    add(path, new ByteArrayInputStream(content.getBytes()));
    return this;
  }

  public ZipFileFactory add(String path, InputStream inputStream) throws IOException {
    m_zipStream.putNextEntry(new ZipEntry(path));
    IOUtils.copy(inputStream, m_zipStream);
    IOUtils.closeQuietly(inputStream);
    m_zipStream.closeEntry();
    return this;
  }

  public ZipFileFactory add(String path, File file) throws IOException {
    path += "/" + file.getName();
    if (file.isFile()) {
      add(path, new FileInputStream(file));
    }
    if (file.isDirectory()) {
      for (File child : file.listFiles()) {
        add(path, child);
      }
    }
    return this;
  }

  public ZipFileFactory addClass(Class<?> clazz) throws IOException {
    String path = clazz.getName().replace('.', '/') + ".class";
    InputStream classBytes = TestBundle.getClassBytes(clazz);
    add(path, classBytes);
    return this;
  }
}
