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

import org.eclipse.wb.tests.designer.TestUtils;

import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Abstract {@link TestCase} for suite. In particular, calls "test_setUp" as first test and
 * "test_tearDown" as last.
 *
 * @author scheglov_ke
 */
public abstract class DesignerSuiteTests extends TestCase {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Suite
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * @return the {@link TestSuite} for given class with test_setUp/test_tearDown as first/last.
   */
  protected static TestSuite createSingleSuite(Class<?> clazz) {
    TestSuite suite = new TestSuite(clazz);
    TestUtils.sortTestSuiteMethods(clazz, suite);
    return suite;
  }
}
