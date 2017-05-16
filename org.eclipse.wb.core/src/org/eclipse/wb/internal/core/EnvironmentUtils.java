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
package org.eclipse.wb.internal.core;

import org.eclipse.swt.SWT;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;

/**
 * Helper for environment state access.
 *
 * @author scheglov_ke
 * @coverage core
 */
public final class EnvironmentUtils extends AbstractUIPlugin {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Host
  //
  ////////////////////////////////////////////////////////////////////////////
  public static final String HOST_NAME = getHostName();
  public static final boolean DEVELOPER_HOST;
  static {
    String host = HOST_NAME.toUpperCase(Locale.ENGLISH);
    DEVELOPER_HOST =
        "SCHEGLOV-KE".equals(host)
            || "SCHEGLOV-MACPRO".equals(host)
            || "SCHEGLOV-WIN".equals(host)
            || "SCHEGLOV".equals(host)
            || "MITIN-AA".equals(host)
            || "MITIN-AA-MAC".equals(host)
            || "SABLIN-AA".equals(host)
            || "FLANKER-WINDOWS".equals(host);
  }
  ////////////////////////////////////////////////////////////////////////////
  //
  // Operating systems
  //
  ////////////////////////////////////////////////////////////////////////////
  /** True if this is running on 64-bit OS */
  public static final boolean IS_64BIT_OS;
  /** True if this is running on Windows */
  public static final boolean IS_WINDOWS;
  /** True if this is running on Unix */
  public static final boolean IS_UNIX;
  /** True if this is running on Mac */
  public static final boolean IS_MAC;
  /** True if this is running on Mac with Cocoa SWT */
  public static final boolean IS_MAC_COCOA;
  /** True if this is running on OS2 */
  public static final boolean IS_OS2;
  /** True if this is running on Linux */
  public static final boolean IS_LINUX;
  /** True if this is running on Motif */
  public static final boolean IS_MOTIF;
  private static final String OS_NAME_OSX_PREFIX = "Mac OS X";
  public static final boolean IS_MAC_10_6 = getOSMatches(OS_NAME_OSX_PREFIX, "10.6");
  private static final String OS_NAME_WINDOWS_PREFIX = "Windows";
  public static final boolean IS_WINDOWS_XP = getOSMatches(OS_NAME_WINDOWS_PREFIX, "5.1");
  public static final boolean IS_WINDOWS_VISTA = getOSMatches(OS_NAME_WINDOWS_PREFIX, "6.0");
  public static final boolean IS_WINDOWS_7 = getOSMatches(OS_NAME_WINDOWS_PREFIX, "6.1");
  /*
   * The JLS doesn't seem to specify an exact naming convention for the
   * os.name. We ensure a uniform naming here.
   */
  static {
    if (SystemUtils.OS_NAME.startsWith("Windows")) {
      IS_WINDOWS = true;
      IS_UNIX = false;
      IS_MAC = false;
      IS_OS2 = false;
      IS_LINUX = false;
    } else if (SystemUtils.OS_NAME.startsWith("SunOS")) {
      IS_WINDOWS = false;
      IS_UNIX = true;
      IS_MAC = false;
      IS_OS2 = false;
      IS_LINUX = false;
    } else if (SystemUtils.OS_NAME.startsWith("Solaris")) {
      IS_WINDOWS = false;
      IS_UNIX = true;
      IS_MAC = false;
      IS_OS2 = false;
      IS_LINUX = false;
    } else if (SystemUtils.OS_NAME.startsWith("Linux")) {
      IS_WINDOWS = false;
      IS_UNIX = true;
      IS_MAC = false;
      IS_OS2 = false;
      IS_LINUX = true;
    } else if (SystemUtils.OS_NAME.startsWith("HP-UX")) {
      IS_WINDOWS = false;
      IS_UNIX = true;
      IS_MAC = false;
      IS_OS2 = false;
      IS_LINUX = false;
    } else if (SystemUtils.OS_NAME.startsWith("AIX")) {
      IS_WINDOWS = false;
      IS_UNIX = true;
      IS_MAC = false;
      IS_OS2 = false;
      IS_LINUX = false;
    } else if (SystemUtils.OS_NAME.startsWith("Irix")) {
      IS_WINDOWS = false;
      IS_UNIX = true;
      IS_MAC = false;
      IS_OS2 = false;
      IS_LINUX = false;
    } else if (SystemUtils.OS_NAME.startsWith("Digital Unix")) {
      IS_WINDOWS = false;
      IS_UNIX = true;
      IS_MAC = false;
      IS_OS2 = false;
      IS_LINUX = false;
    } else if (SystemUtils.OS_NAME.startsWith("OS/400")) {
      IS_WINDOWS = false;
      IS_UNIX = true;
      IS_MAC = false;
      IS_OS2 = false;
      IS_LINUX = false;
    } else if (SystemUtils.OS_NAME.startsWith("OS/2")) {
      IS_WINDOWS = false;
      IS_UNIX = false;
      IS_MAC = false;
      IS_OS2 = true;
      IS_LINUX = false;
    } else if (SystemUtils.OS_NAME.startsWith(OS_NAME_OSX_PREFIX)) {
      IS_WINDOWS = false;
      IS_UNIX = true;
      IS_MAC = true;
      IS_OS2 = false;
      IS_LINUX = false;
    } else if (SystemUtils.OS_NAME.startsWith("Mac")) {
      IS_WINDOWS = false;
      IS_UNIX = false;
      IS_MAC = true;
      IS_OS2 = false;
      IS_LINUX = false;
    } else {
      IS_WINDOWS = false;
      IS_UNIX = false;
      IS_MAC = false;
      IS_OS2 = false;
      IS_LINUX = false;
    }
    IS_MAC_COCOA = IS_MAC && "cocoa".equals(SWT.getPlatform());
    IS_MOTIF = "motif".equals(SWT.getPlatform());
    IS_64BIT_OS = SystemUtils.OS_ARCH.indexOf("64") != -1;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // System utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private static String getHostName() {
    String hostName = "";
    try {
      hostName = InetAddress.getLocalHost().getHostName();
      String[] names = StringUtils.split(hostName, '.');
      for (String name : names) {
        if (StringUtils.isNumeric(name)) {
          // getHostName() returned in a IP-address form
          return hostName;
        }
      }
      hostName = names[0];
    } catch (UnknownHostException e) {
    }
    return hostName;
  }

  /**
   * Decides if the operating system matches.
   *
   * @param osNamePrefix
   *          the prefix for the OS name.
   * @param osVersionPrefix
   *          the prefix for the version.
   * @return <code>true</code> if matches, or <code>false</code> if not or can't determine.
   */
  private static boolean getOSMatches(String osNamePrefix, String osVersionPrefix) {
    if (SystemUtils.OS_NAME == null || SystemUtils.OS_VERSION == null) {
      return false;
    }
    return SystemUtils.OS_NAME.startsWith(osNamePrefix)
        && SystemUtils.OS_VERSION.startsWith(osVersionPrefix);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // JVM version
  //
  ////////////////////////////////////////////////////////////////////////////
  private static Float m_forcedJavaVersion = null;

  /**
   * @return the Java version number as <code>float</code>. For example <code>1.5</code> for JDK
   *         1.5.
   */
  public static float getJavaVersion() {
    if (m_forcedJavaVersion != null) {
      return m_forcedJavaVersion;
    }
    return SystemUtils.JAVA_VERSION_FLOAT;
  }

  /**
   * Allows temporary (for tests) specify different Java version.
   */
  public static void setForcedJavaVersion(Float version) {
    m_forcedJavaVersion = version;
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // IBM Java
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String WBP_JAVA_VM_VENDOR = "wbp java.vm.vendor";

  /**
   * IBM Java has its own glitches.
   */
  public static boolean isJavaIBM() {
    return getTestSensetive_JAVA_VM_VENDOR().toUpperCase(Locale.ENGLISH).contains("IBM");
  }

  private static String getTestSensetive_JAVA_VM_VENDOR() {
    String forcedVendor = System.getProperty(WBP_JAVA_VM_VENDOR);
    if (forcedVendor != null) {
      return forcedVendor;
    } else {
      return SystemUtils.JAVA_VM_VENDOR;
    }
  }

  public static void setForcedIBM(boolean forced) {
    if (forced) {
      System.setProperty(WBP_JAVA_VM_VENDOR, "Fake IBM");
    } else {
      System.clearProperty(WBP_JAVA_VM_VENDOR);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Development
  //
  ////////////////////////////////////////////////////////////////////////////
  private static final String WBP_TESTING_TIME = "wbp.testing.time";

  //
  public static boolean isTestingTime() {
    return "true".equals(System.getProperty(WBP_TESTING_TIME));
  }

  public static void setTestingTime(boolean value) {
    System.setProperty(WBP_TESTING_TIME, value ? "true" : "false");
  }
}
