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

import com.google.common.collect.ImmutableSet;

import org.eclipse.wb.internal.core.EnvironmentUtils;
import org.eclipse.wb.tests.designer.tests.DesignerTestCase;

import java.util.Locale;

/**
 * Test for {@link EnvironmentUtils}.
 * 
 * @author scheglov_ke
 */
public class EnvironmentUtilsTest extends DesignerTestCase {
  /**
   * Test for known state of host and development flag.
   */
  public void test_DEVELOPER_HOST() throws Exception {
    if ("SCHEGLOV-KE".equals(EnvironmentUtils.HOST_NAME)) {
      assertTrue(EnvironmentUtils.DEVELOPER_HOST);
    }
  }

  /**
   * Test for OS.
   */
  public void test_OS() throws Exception {
    boolean isWindows;
    boolean isLinux;
    boolean isMac;
    {
      String hostName = EnvironmentUtils.HOST_NAME.toUpperCase(Locale.ENGLISH);
      isWindows =
          ImmutableSet.of("SCHEGLOV-KE", "SCHEGLOV-WIN", "FLANKER-WINDOWS", "SABLIN-AA").contains(
              hostName);
      isLinux = ImmutableSet.of("FLANKER-LINUX").contains(hostName);
      isMac = ImmutableSet.of("MITIN-AA").contains(hostName);
    }
    assertEquals(isWindows, EnvironmentUtils.IS_WINDOWS);
    assertEquals(isLinux, EnvironmentUtils.IS_LINUX);
    assertEquals(isMac, EnvironmentUtils.IS_MAC);
  }

  /**
   * Test for {@link EnvironmentUtils#isJavaIBM()}.
   */
  public void test_isJavaIBM() throws Exception {
    assertFalse(EnvironmentUtils.isJavaIBM());
    {
      // switch IBM emulation
      EnvironmentUtils.setForcedIBM(true);
      try {
        assertTrue(EnvironmentUtils.isJavaIBM());
      } finally {
        EnvironmentUtils.setForcedIBM(false);
      }
    }
    assertFalse(EnvironmentUtils.isJavaIBM());
  }

  /**
   * Test for {@link EnvironmentUtils#getJavaVersion()}.
   */
  public void test_getJavaVersion() throws Exception {
    assertEquals(1.6, EnvironmentUtils.getJavaVersion(), 0.001);
    {
      // specify version 1.5
      EnvironmentUtils.setForcedJavaVersion(1.5f);
      try {
        assertEquals(1.5, EnvironmentUtils.getJavaVersion(), 0.001);
      } finally {
        EnvironmentUtils.setForcedJavaVersion(null);
      }
    }
    assertEquals(1.6, EnvironmentUtils.getJavaVersion(), 0.001);
  }

  /**
   * Test for {@link EnvironmentUtils#isTestingTime()}.
   */
  public void test_isDevelopmentTime() throws Exception {
    assertTrue(EnvironmentUtils.isTestingTime());
    {
      // switch development time
      EnvironmentUtils.setTestingTime(false);
      try {
        assertFalse(EnvironmentUtils.isTestingTime());
      } finally {
        EnvironmentUtils.setTestingTime(true);
      }
    }
    assertTrue(EnvironmentUtils.isTestingTime());
  }
}
