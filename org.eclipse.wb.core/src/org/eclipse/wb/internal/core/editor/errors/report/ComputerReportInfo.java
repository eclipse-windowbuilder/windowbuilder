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

import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.internal.core.utils.IOUtils2;
import org.eclipse.wb.internal.core.utils.XmlWriter;
import org.eclipse.wb.internal.core.utils.platform.PlatformInfo;
import org.eclipse.wb.internal.core.utils.platform.PluginUtilities;
import org.eclipse.wb.internal.core.utils.product.ProductInfo;
import org.eclipse.wb.internal.core.utils.reflect.ProjectClassLoader;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.widgets.Shell;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.URL;

/**
 * Intended to gather needed software and hardware info and represent it as xml and html.
 * 
 * @author mitin_aa
 * @coverage core.editor.errors.report
 */
public class ComputerReportInfo implements IReportInfo {
  // constants
  private static final String CR = "\n";
  private static final String TD_TR = "</td></tr>";
  private static final String TR_TD = "<tr><td style=\"white-space:nowrap;vertical-align:top;\">";
  private static final String TD_TD = "</td><td style=\"vertical-align:top;\">";
  // xml tags
  private static final String XML_TAG_PLATFORM = "platform";
  // fields
  // software
  private String m_installationPath;
  private final String m_eclipseVersion;
  private final String m_eclipseBuildName;
  private final String m_eclipseBuildId;
  private final String m_eclipseCommands;
  private final String m_eclipseVmargs;
  private final String m_eclipseVm;
  private final String m_IDEName;
  private final String m_IDEVersion;
  private final String m_IDENL;
  private final String m_OSName;
  private final String m_OSArch;
  private final String m_OSVersion;
  // linux related
  private String m_linuxDescription = "";
  private String m_mozillaResult = "";
  // hardware
  //private final List<String> m_macs = Lists.newArrayList();
  private final int m_availableProcessors;
  private final long m_totalMemory;
  private final long m_freeMemory;
  private final long m_maxMemory;
  private final String m_javaVendor;
  private final String m_javaVersion;
  private final String m_javaLibraryPath;
  private String m_classPath;

  ////////////////////////////////////////////////////////////////////////////
  //
  // Constructor
  //
  ////////////////////////////////////////////////////////////////////////////
  public ComputerReportInfo(IProject project) {
    URL installUrl = PluginUtilities.getInstallUrl(ProductInfo.getProduct().getPluginId());
    m_installationPath = "Unknown";
    try {
      if (installUrl != null) {
        m_installationPath = FileLocator.toFileURL(installUrl).getPath();
        if (m_installationPath.length() > 3
            && m_installationPath.charAt(0) == '/'
            && m_installationPath.charAt(2) == ':') {
          m_installationPath = m_installationPath.substring(1);
        }
      }
    } catch (Throwable e) {
    }
    m_eclipseVersion = PlatformInfo.getEclipseVersion().toString();
    m_eclipseBuildName = PlatformInfo.getEclipseBuildName();
    m_eclipseBuildId = PlatformInfo.getEclipseBuildId();
    m_eclipseCommands = getSystemProperty("eclipse.commands");
    m_eclipseVm = getSystemProperty("eclipse.vm");
    m_eclipseVmargs = getSystemProperty("eclipse.vmargs");
    m_IDEName = PlatformInfo.getIDEName();
    m_IDEVersion = PlatformInfo.getIDEVersionString();
    m_IDENL = PlatformInfo.getIDENL();
    m_OSName = getSystemProperty("os.name");
    m_OSArch = getSystemProperty("os.arch");
    m_OSVersion = getSystemProperty("os.version");
    //
    //IMachineCode[] localAddresses = Machines.getLocalCodes();
    //for (IMachineCode machineCode : localAddresses) {
    //m_macs.add(machineCode.getText());
    //}
    m_javaVendor = getSystemProperty("java.vendor");
    m_javaVersion = getSystemProperty("java.version");
    m_javaLibraryPath = getSystemProperty("java.library.path");
    final Runtime runtime = Runtime.getRuntime();
    m_availableProcessors = runtime.availableProcessors();
    m_totalMemory = runtime.totalMemory();
    m_freeMemory = runtime.freeMemory();
    m_maxMemory = runtime.maxMemory();
    // java classpath
    m_classPath = "";
    try {
      IJavaProject javaProject = JavaCore.create(project);
      if (javaProject.exists()) {
        String[] entries = ProjectClassLoader.computeFullRuntimeClassPath(javaProject);
        for (String entry : entries) {
          m_classPath += entry + File.pathSeparator;
        }
      }
      // remove trailing path separator
      m_classPath = StringUtils.removeEnd(m_classPath, File.pathSeparator);
    } catch (Throwable e) {
      // ignore
    }
    if (EnvironmentUtils.IS_LINUX) {
      // Linux distribution name
      m_linuxDescription = getLinuxDescription();
      // GRE
      m_mozillaResult = tryCreateMozilla();
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IReportInfo
  //
  ////////////////////////////////////////////////////////////////////////////
  public void writeXML(XmlWriter xmlWriter) {
    xmlWriter.openTag(XML_TAG_PLATFORM);
    String platformInfo = "";
    platformInfo += "Installation Path: " + m_installationPath + CR;
    platformInfo += "Eclipse Version: " + m_eclipseVersion + CR;
    platformInfo += "Eclipse Build Name: " + m_eclipseBuildName + CR;
    platformInfo += "Eclipse Build ID: " + m_eclipseBuildId + CR;
    platformInfo += "IDE Name: " + m_IDEName + CR;
    platformInfo += "IDE Version: " + m_IDEVersion + CR;
    platformInfo += "IDE NL: " + m_IDENL + CR;
    platformInfo +=
        "Eclipse Commands: " + StringUtils.replaceChars(m_eclipseCommands, "\n\r", " ") + CR;
    platformInfo += "Eclipse VM: " + m_eclipseVm + CR;
    platformInfo += "Eclipse VM Args: " + m_eclipseVmargs + CR;
    platformInfo += "OS Name: " + m_OSName + CR;
    platformInfo += "OS Arch: " + m_OSArch + CR;
    platformInfo += "OS Version: " + m_OSVersion + CR;
    if (!StringUtils.isEmpty(m_linuxDescription)) {
      platformInfo += "Linux Description: " + m_linuxDescription + CR;
    }
    if (!StringUtils.isEmpty(m_mozillaResult)) {
      platformInfo += "Browser Creation Result: " + m_mozillaResult + CR;
    }
    //platformInfo += "Network Addresses: ";
    //for (int i = 0; i < m_macs.size(); i++) {
    //if (i > 0) {
    //platformInfo += ", ";
    //}
    //platformInfo += m_macs.get(i);
    //}
    //platformInfo += CR;
    platformInfo += "Available Processors: " + m_availableProcessors + CR;
    platformInfo += "Memory Max: " + m_maxMemory + CR;
    platformInfo += "Memory Total: " + m_totalMemory + CR;
    platformInfo += "Memory Free: " + m_freeMemory + CR;
    platformInfo += "Java Vendor: " + m_javaVendor + CR;
    platformInfo += "Java Version: " + m_javaVersion + CR;
    platformInfo += "Java Library Path: " + CR + m_javaLibraryPath + CR;
    platformInfo += "Project Class Path: " + CR + m_classPath + CR;
    xmlWriter.write(platformInfo);
    xmlWriter.closeTag(); // XML_TAG_PLATFORM
  }

  public String getHTML() {
    String html = "<table cellspacing=\"2\" border=\"0\">";
    html += TR_TD + "Installation Path:" + TD_TD + m_installationPath + TD_TR;
    html += TR_TD + "Eclipse Version:" + TD_TD + m_eclipseVersion + TD_TR;
    html += TR_TD + "Eclipse Build Name:" + TD_TD + m_eclipseBuildName + TD_TR;
    html += TR_TD + "Eclipse Build ID:" + TD_TD + m_eclipseBuildId + TD_TR;
    html += TR_TD + "IDE Name:" + TD_TD + m_IDEName + TD_TR;
    html += TR_TD + "IDE Version:" + TD_TD + m_IDEVersion + TD_TR;
    html += TR_TD + "IDE NL:" + TD_TD + m_IDENL + TD_TR;
    html += TR_TD + "Eclipse Commands:" + TD_TD + m_eclipseCommands + TD_TR;
    html += TR_TD + "Eclipse VM:" + TD_TD + m_eclipseVm + TD_TR;
    html += TR_TD + "Eclipse VM Args:" + TD_TD + m_eclipseVmargs + TD_TR;
    html += TR_TD + "OS Name:" + TD_TD + m_OSName + TD_TR;
    html += TR_TD + "OS Arch:" + TD_TD + m_OSArch + TD_TR;
    html += TR_TD + "OS Version:" + TD_TD + m_OSVersion + TD_TR;
    if (m_linuxDescription.length() != 0) {
      html += TR_TD + "Linux Description:" + TD_TD + m_linuxDescription + TD_TR;
    }
    if (m_mozillaResult.length() != 0) {
      html += TR_TD + "Browser Creation Result: " + TD_TD + m_mozillaResult + TD_TR;
    }
    //html += TR_TD + "Network Addresses:" + TD_TD + "<table border=\"0\">";
    //for (String mac : m_macs) {
    //html += TR_TD + mac + TD_TR;
    //}
    //html += "</table>" + TD_TR;
    html += TR_TD + "Available Processors:" + TD_TD + m_availableProcessors + TD_TR;
    html += TR_TD + "Memory Max:" + TD_TD + m_maxMemory + TD_TR;
    html += TR_TD + "Memory Total:" + TD_TD + m_totalMemory + TD_TR;
    html += TR_TD + "Memory Free:" + TD_TD + m_freeMemory + TD_TR;
    html += TR_TD + "Java Vendor:" + TD_TD + m_javaVendor + TD_TR;
    html += TR_TD + "Java Version:" + TD_TD + m_javaVersion + TD_TR;
    html += TR_TD + "Java Library Path:" + TD_TD + m_javaLibraryPath + TD_TR;
    html += TR_TD + "Project Class Path:" + TD_TD + m_classPath + TD_TR;
    return html + "</table>";
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
  private String getSystemProperty(String prop) {
    String propValue = System.getProperty(prop);
    return propValue == null ? "" : propValue;
  }

  /**
   * Calls 'cat /etc/lsb-release' (and others) and returns it's content.
   */
  private String getLinuxDescription() {
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
    StringBuilder result = new StringBuilder();
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
    return result.toString();
  }

  private String tryCreateMozilla() {
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
        System.out.println(e.getMessage());
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
    return "";
  }
}
