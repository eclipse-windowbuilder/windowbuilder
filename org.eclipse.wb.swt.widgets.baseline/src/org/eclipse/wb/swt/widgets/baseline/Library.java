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
package org.eclipse.wb.swt.widgets.baseline;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;

/**
 * Loader for native libs, based on SWT.Library class.
 */
public class Library {
  private static final String SEPARATOR = System.getProperty("file.separator");
  private static final String OS_ARCH = System.getProperty("os.arch");
  private static final String OS_NAME = System.getProperty("os.name");
  private static final boolean IS_MAC = OS_NAME.startsWith("Mac");
  private static final boolean IS_WINDOWS = OS_NAME.startsWith("Windows");
  private static final boolean IS_64BIT_OS = OS_ARCH.indexOf("64") != -1;

  //
  private static boolean extract(String fileName, String mappedName) {
    FileOutputStream os = null;
    InputStream is = null;
    File file = new File(fileName);
    file.deleteOnExit();
    boolean extracted = false;
    try {
      if (!file.exists()) {
        is = Library.class.getResourceAsStream(mappedName);
        if (is != null) {
          extracted = true;
          int read;
          byte[] buffer = new byte[4096];
          os = new FileOutputStream(fileName);
          while ((read = is.read(buffer)) != -1) {
            os.write(buffer, 0, read);
          }
          os.close();
          is.close();
          if (!IS_WINDOWS) {
            try {
              Runtime.getRuntime().exec(new String[]{"chmod", "755", fileName}).waitFor();
            } catch (Throwable e) {
            }
          }
        }
      }
      if (load(fileName)) {
        return true;
      }
    } catch (Throwable e) {
      try {
        if (os != null) {
          os.close();
        }
      } catch (IOException e1) {
      }
      try {
        if (is != null) {
          is.close();
        }
      } catch (IOException e1) {
      }
      if (extracted && file.exists()) {
        file.delete();
      }
    }
    return false;
  }

  private static boolean load(String libName) {
    try {
      if (libName.indexOf(SEPARATOR) != -1) {
        System.load(libName);
      } else {
        System.loadLibrary(libName);
      }
      return true;
    } catch (UnsatisfiedLinkError e) {
    }
    return false;
  }

  /**
   * Loads the shared library that matches the version of the Java code which is currently running.
   * SWT shared libraries follow an encoding scheme where the major, minor and revision numbers are
   * embedded in the library name and this along with <code>name</code> is used to load the library.
   * If this fails, <code>name</code> is used in another attempt to load the library, this time
   * ignoring the SWT version encoding scheme.
   *
   * @param name
   *          the name of the library to load
   */
  public static void loadLibrary(String name) {
    if (IS_64BIT_OS && !IS_MAC) {
      name += "64";
    }
    String fileName = mapLibraryName(name);
    String path = System.getProperty("java.io.tmpdir");
    File dir = new File(path, "wbp-baseline");
    boolean make = false;
    if (dir.exists() && dir.isDirectory() || (make = dir.mkdir())) {
      path = dir.getAbsolutePath();
      if (make && !IS_WINDOWS) {
        try {
          Runtime.getRuntime().exec(new String[]{"chmod", "777", path}).waitFor();
        } catch (Throwable e) {
        }
      }
    }
    if (load(path + SEPARATOR + fileName)) {
      return;
    }
    // Try extracting and loading library from jar
    if (path != null) {
      if (extract(path + SEPARATOR + fileName, fileName)) {
        return;
      }
    }
    throw new UnsatisfiedLinkError(MessageFormat.format(BaselineMessages.Library_canNotLoad, name));
  }

  private static String mapLibraryName(String libName) {
    // SWT libraries in the Macintosh use the extension .jnilib but the some VMs map to .dylib.
    libName = System.mapLibraryName(libName);
    String ext = ".dylib";
    if (libName.endsWith(ext)) {
      libName = libName.substring(0, libName.length() - ext.length()) + ".jnilib";
    }
    return libName;
  }
}
