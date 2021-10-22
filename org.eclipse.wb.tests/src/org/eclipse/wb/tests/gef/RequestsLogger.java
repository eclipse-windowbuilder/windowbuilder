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

import org.eclipse.wb.gef.core.EditPart;
import org.eclipse.wb.gef.core.requests.Request;

import junit.framework.Assert;

import org.apache.commons.lang.StringUtils;

import java.util.List;

/**
 * Helper class for logging {@link EditPart}'s and {@link Request}'s to them.
 *
 * @author scheglov_ke
 */
public final class RequestsLogger {
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
   * Asserts that this {@link RequestsLogger} has no events.
   */
  public void assertEmpty() {
    Assert.assertTrue(m_events.isEmpty());
  }

  /**
   * Asserts that this {@link RequestsLogger} contains same events as in expected one.
   */
  public void assertEquals(RequestsLogger expectedLogger) {
    Assert.assertEquals(getString(expectedLogger), getString(this));
  }

  /**
   * Asserts with given <b>actual</b> {@link RequestsLogger}.
   */
  public void backAssertEquals(RequestsLogger actualLogger) {
    actualLogger.assertEquals(this);
  }

  /**
   * @return the single {@link String} from events of given {@link RequestsLogger}.
   */
  private static String getString(RequestsLogger logger) {
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
  public void log(EditPart source, String event) {
    m_events.add(source + " = " + event);
  }

  /**
   * Logs new event with given {@link Request}.
   */
  public void log(EditPart source, String event, Request request) {
    m_events.add(source + " = " + event + "[ " + request + " ]");
  }

  /**
   * Logs new events with given {@link Request}.
   */
  public void log(EditPart source, String[] events, Request request) {
    for (int i = 0; i < events.length; i++) {
      log(source, events[i], request);
    }
  }
}
