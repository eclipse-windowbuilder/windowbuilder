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

import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

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
    try {
      Field testsField = TestSuite.class.getDeclaredField("fTests");
      testsField.setAccessible(true);
      @SuppressWarnings("unchecked")
      Vector<TestCase> tests = (Vector<TestCase>) testsField.get(suite);
      Collections.sort(tests, new Comparator<TestCase>() {
        public int compare(TestCase o1, TestCase o2) {
          String method_1 = o1.getName();
          String method_2 = o2.getName();
          if ("test_setUp".equals(method_1)) {
            return -1;
          }
          if ("test_setUp".equals(method_2)) {
            return 1;
          }
          if ("test_tearDown".equals(method_1)) {
            return 1;
          }
          if ("test_tearDown".equals(method_2)) {
            return -1;
          }
          return method_1.compareTo(method_2);
        }
      });
    } catch (Throwable e) {
      e.printStackTrace();
    }
    return suite;
  }
}
