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

import org.eclipse.wb.core.branding.BrandingUtils;
import org.eclipse.wb.internal.core.DesignerPlugin;
import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.core.utils.platform.PlatformInfo;
import org.eclipse.wb.internal.core.utils.platform.PluginUtilities;
import org.eclipse.wb.internal.core.utils.product.ProductInfo;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.widgets.Shell;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;

/**
 * Intended to gather needed software and hardware info.
 *
 * @author mitin_aa
 * @coverage core.editor.errors.report2
 */
public final class EnvironmentFileReportInfo extends FileReportEntry {
  // constants
  private static final String CR = "\n";
  private final IProject m_project;
  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public EnvironmentFileReportInfo(IProject project) {
    super("environment.txt");
    m_project = project;
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Contents
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  protected InputStream getContents() throws Exception {
    String contents = createContents(m_project);
    return IOUtils.toInputStream(contents);
  }
  private static String createContents(IProject project) {
    String c = "";
    c += "Product Name: " + BrandingUtils.getBranding().getProductName() + CR;
    c +=
        "Product Version: "
            + ProductInfo.getProduct().getVersion()
            + "["
            + ProductInfo.getProduct().getBuild()
            + "]"
            + CR;
    c += "Installation Path: " + getInstallationPath() + CR;
    c += "Eclipse Version: " + PlatformInfo.getEclipseVersion().toString() + CR;
    c += "Eclipse Build Name: " + PlatformInfo.getEclipseBuildName() + CR;
    c += "Eclipse Build ID: " + PlatformInfo.getEclipseBuildId() + CR;
    c += "IDE Name: " + PlatformInfo.getIDEName() + CR;
    c += "IDE Version: " + PlatformInfo.getIDEVersionString() + CR;
    c += "IDE NL: " + PlatformInfo.getIDENL() + CR;
    c +=
        "Eclipse Commands: "
            + StringUtils.replaceChars(getSystemProperty("eclipse.commands"), "\n\r", " ")
            + CR;
    c += "Eclipse VM: " + getSystemProperty("eclipse.vm") + CR;
    c += "Eclipse VM Args: " + getSystemProperty("eclipse.vmargs") + CR;
    c += "OS Name: " + getSystemProperty("os.name") + CR;
    c += "OS Arch: " + getSystemProperty("os.arch") + CR;
    c += "OS Version: " + getSystemProperty("os.version") + CR;
    String linuxDescription = getLinuxDescription();
    if (!StringUtils.isEmpty(linuxDescription)) {
      c += "Linux Description: " + linuxDescription + CR;
    }
    String m_mozillaResult = tryCreateMozilla();
    if (!StringUtils.isEmpty(m_mozillaResult)) {
      c += "Browser Creation Result: " + m_mozillaResult + CR;
    }
    Runtime runtime = Runtime.getRuntime();
    c += "Available Processors: " + runtime.availableProcessors() + CR;
    c += "Memory Max: " + runtime.maxMemory() + CR;
    c += "Memory Total: " + runtime.totalMemory() + CR;
    c += "Memory Free: " + runtime.freeMemory() + CR;
    c += "Java Vendor: " + getSystemProperty("java.vendor") + CR;
    c += "Java Version: " + getSystemProperty("java.version") + CR;
    c += "Java Library Path: " + CR + getSystemProperty("java.library.path") + CR;
    c += "Project Class Path: " + CR + getClassPath(project) + CR;
    return c;
  }
  private static String getClassPath(IProject project) {
    // java classpath
    String classPath = "";
    // TODO(scheglov)
//    try {
//      IJavaProject javaProject = JavaCore.create(project);
//      if (javaProject.exists()) {
//        String[] entries = ProjectClassLoader.getClasspath(javaProject);
//        for (String entry : entries) {
//          classPath += entry + File.pathSeparator;
//        }
//      }
//      // remove trailing path separator
//      classPath = StringUtils.removeEnd(classPath, File.pathSeparator);
//    } catch (Throwable e) {
//      // ignore
//    }
    return classPath;
  }
  private static String getInstallationPath() {
    URL installUrl = PluginUtilities.getInstallUrl(ProductInfo.getProduct().getPluginId());
    String installationPath = "Unknown";
    try {
      if (installUrl != null) {
        installationPath = FileLocator.toFileURL(installUrl).getPath();
        if (installationPath.length() > 3
            && installationPath.charAt(0) == '/'
            && installationPath.charAt(2) == ':') {
          installationPath = installationPath.substring(1);
        }
      }
    } catch (Throwable e) {
    }
    return installationPath;
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Get system property and return empty string if no such property.
   *
   * @param prop
   *          the property name.
   */
  private static String getSystemProperty(String prop) {
    String propValue = System.getProperty(prop);
    return propValue == null ? "" : propValue;
  }
  /**
   * Returns the contents of '/etc/lsb-release' (and/or others).
   */
  private static String getLinuxDescription() {
    StringBuilder result = new StringBuilder();
    if (EnvironmentUtils.IS_LINUX) {
      String[] files =
          new String[]{
              "/etc/lsb-release",
              "/etc/lsb_release",
              "/etc/system-release",
              "/etc/fedora-release",
              "/etc/SuSE-release",
              "/etc/redhat-release",
              "/etc/release",
              "/proc/version_signature",
              "/proc/version",
              "/etc/issue",};
      for (int i = 0; i < files.length; i++) {
        File file = new File(files[i]);
        if (file.exists() && file.canRead()) {
          try {
            String version = IOUtils2.readString(file).trim();
            if (version != null && result.indexOf(version) == -1) {
              result.append(version);
              result.append("\n");
            }
          } catch (Throwable e) {
            // just ignore
          }
        }
      }
    }
    return result.toString();
  }
  private static String tryCreateMozilla() {
    if (EnvironmentUtils.IS_LINUX) {
      boolean oldDebug = Device.DEBUG;
      Device.DEBUG = true;
      PrintStream oldOut = System.out;
      Shell shell = null;
      PrintStream newOut = null;
      try {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        newOut = new PrintStream(baos);
        // replace the out since the Mozilla output debug results into stdout.
        System.setOut(newOut);
        shell = new Shell();
        try {
          new Browser(shell, SWT.NONE);
        } catch (Throwable e) {
          DesignerPlugin.log(e);
        }
        return baos.toString();
      } catch (Throwable e1) {
        // ignore
      } finally {
        if (shell != null) {
          shell.dispose();
        }
        System.setOut(oldOut);
        IOUtils.closeQuietly(newOut);
        Device.DEBUG = oldDebug;
      }
    }
    return "";
  }
}
