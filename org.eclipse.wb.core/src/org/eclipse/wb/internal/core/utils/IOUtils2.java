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
package org.eclipse.wb.internal.core.utils;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

/**
 * Additional I/O utils for {@link IOUtils}.
 *
 * @author scheglov_ke
 */
public class IOUtils2 {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Directory
  //
  ////////////////////////////////////////////////////////////////////////////
  //@edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "RV_RETURN_VALUE_IGNORED_BAD_PRACTIC")
  public static File createTempDirectory(String prefix, String suffix) throws IOException {
    File tempFile = File.createTempFile(prefix, suffix);
    tempFile.delete();
    tempFile.mkdirs();
    return tempFile;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // File names
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the absolute location of given {@link URL}. It is expected that {@link URL} represents
   *         such absolute location file.
   */
  public static String getPortableAbsolutePath(URL url) {
    File f;
    try {
      f = new File(url.toURI());
    } catch (URISyntaxException e) {
      f = new File(url.getPath());
    }
    String path = f.getAbsolutePath();
    path = StringUtils.replace(path, "%20", " ");
    return toPortablePath(path);
  }

  /**
   * Converts all separators to the Unix separator of forward slash.
   */
  public static String toPortablePath(String path) {
    return FilenameUtils.separatorsToUnix(path);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Reading
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link String} content of given {@link File}.
   */
  public static String readString(File file) throws IOException {
    InputStream input = new FileInputStream(file);
    return readString(input);
  }

  /**
   * @return the {@link String} content of given {@link InputStream} and closes stream.
   */
  public static String readString(InputStream input) throws IOException {
    try {
      return IOUtils.toString(input);
    } finally {
      input.close();
    }
  }

  /**
   * @return the {@link String} content of given {@link Reader} and closes it.
   */
  public static String readString(Reader input) throws IOException {
    try {
      return IOUtils.toString(input);
    } finally {
      input.close();
    }
  }

  /**
   * @return the the first line from a {@link InputStream}, using default charset. The line does not
   *         include line-termination characters, but does include other leading and trailing
   *         whitespace.
   */
  public static String readFirstLine(InputStream input) throws IOException {
    try {
      BufferedReader br = new BufferedReader(new InputStreamReader(input));
      return br.readLine();
    } finally {
      input.close();
    }
  }

  /**
   * @return the {@link String} content of given {@link IFile}.
   */
  public static String readString(IFile file) throws Exception {
    return readString(file.getContents(true));
  }

  /**
   * @return the byte[] content of given {@link File}.
   */
  public static byte[] readBytes(File file) throws IOException {
    InputStream input = new FileInputStream(file);
    return readBytes(input);
  }

  /**
   * Reads content of given {@link InputStream} into byte array and closes stream.
   */
  public static byte[] readBytes(InputStream input) throws IOException {
    try {
      return IOUtils.toByteArray(input);
    } finally {
      input.close();
    }
  }

  /**
   * @return the byte[] array, read length and then bytes.
   */
  public static byte[] readByteArray(ObjectInputStream ois) throws IOException {
    int length = ois.readInt();
    byte[] bytes = new byte[length];
    ois.readFully(bytes);
    return bytes;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Writing
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Writes bytes into {@link File}, i.e. replaces its content.
   */
  public static void writeBytes(File file, InputStream input) throws IOException {
    copyAndClose(new FileOutputStream(file), input);
  }

  /**
   * Copies bytes from {@link InputStream} into {@link OutputStream} and closes both streams.
   */
  public static void copyAndClose(OutputStream output, InputStream input) throws IOException {
    try {
      IOUtils.copy(input, output);
    } finally {
      IOUtils.closeQuietly(input);
      IOUtils.closeQuietly(output);
    }
  }

  /**
   * Writes bytes into {@link File}, i.e. replaces its content.
   */
  public static void writeBytes(File file, byte[] input) throws IOException {
    writeBytes(new FileOutputStream(file), input);
  }

  /**
   * Writes give bytes into {@link OutputStream} and closes stream.
   */
  public static void writeBytes(OutputStream output, byte[] input) throws IOException {
    try {
      output.write(input);
    } finally {
      output.close();
    }
  }

  /**
   * Writes byte array as length and bytes.
   */
  public static void writeByteArray(ObjectOutputStream oos, byte[] bytes) throws IOException {
    oos.writeInt(bytes.length);
    oos.write(bytes);
  }

  /**
   * Writes string into {@link OutputStream} and does NOT close the stream.
   */
  public static void writeString(OutputStream output, String outString) throws IOException {
    IOUtils.copy(IOUtils.toInputStream(outString), output);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Eclipse IFile utilities
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Creates or modifies given {@link IFile} with given {@link InputStream}.
   *
   * @return <code>true</code> if {@link IFile} was created.
   */
  public static boolean setFileContents(IFile file, InputStream inputStream) throws CoreException {
    if (file.exists()) {
      file.setContents(inputStream, true, false, null);
      return false;
    } else {
      IOUtils2.ensureFolderExists(file);
      file.create(inputStream, true, null);
      return true;
    }
  }

  /**
   * Creates or modifies given {@link IFile} with given {@link String}.
   */
  public static void setFileContents(IFile file, String contents) throws CoreException {
    InputStream inputStream = new ByteArrayInputStream(contents.getBytes());
    setFileContents(file, inputStream);
  }

  /**
   * Saves given {@link Properties} into given {@link IFile}.
   */
  public static void storeProperties(IFile file, Properties properties) throws CoreException,
      IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    properties.store(baos, null);
    //
    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    setFileContents(file, bais);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IFolder utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Ensures that all parent {@link IFolder}'s to the given {@link IFile} are exist.
   */
  public static void ensureFolderExists(IFile file) throws CoreException {
    if (file.getParent() instanceof IFolder) {
      ensureFolderExists((IFolder) file.getParent());
    }
  }

  /**
   * Ensures that {@link IFolder} with given name exists, so exist all its parent {@link IFolder}'s.
   */
  public static IFolder ensureFolderExists(IProject project, String path) throws CoreException {
    IFolder folder = project.getFolder(new Path(path));
    ensureFolderExists(folder);
    return folder;
  }

  /**
   * Ensures that given {@link IFolder} exists, so exist all its parent {@link IFolder}'s.
   */
  public static void ensureFolderExists(IFolder folder) throws CoreException {
    if (!folder.exists()) {
      // ensure parent folder
      {
        IContainer parent = folder.getParent();
        if (parent instanceof IFolder) {
          ensureFolderExists((IFolder) parent);
        }
      }
      // create this folder
      folder.create(true, true, null);
    }
  }
}
