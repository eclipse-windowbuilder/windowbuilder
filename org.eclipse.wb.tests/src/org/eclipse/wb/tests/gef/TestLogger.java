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
package org.eclipse.wb.tests.gef;

import com.google.common.collect.Lists;

import junit.framework.Assert;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Helper class for logging into test cases.
 * 
 * @author lobas_av
 */
public class TestLogger {
  private final List<String> m_events = Lists.newArrayList();

  ////////////////////////////////////////////////////////////////////////////
  //
  // Access
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Removes all logged events.
   */
  public void clear() {
    m_events.clear();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Assert
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Asserts that this {@link TestLogger} has no events.
   */
  public void assertEmpty() {
    Assert.assertTrue(m_events.isEmpty());
  }

  /**
   * Asserts that this {@link TestLogger} contains same events as in expected one.
   */
  public void assertEquals(TestLogger expectedLogger) {
    assertEquals(expectedLogger, this);
  }

  /**
   * Asserts that two objects are equal. If they are not an AssertionFailedError is thrown.
   */
  public static void assertEquals(TestLogger expectedLogger, TestLogger actualLoogger) {
    Assert.assertEquals(getString(expectedLogger), getString(actualLoogger));
    expectedLogger.clear();
    actualLoogger.clear();
  }

  /**
   * @return the single {@link String} from events of given {@link TestLogger}.
   */
  private static String getString(TestLogger logger) {
    return StringUtils.join(logger.m_events.iterator(), "\n");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Logging
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Logs new event.
   */
  public void log(String message) {
    m_events.add(message);
  }
}