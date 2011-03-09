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
package org.eclipse.wb.tests.designer.core.eval.primities;

import org.eclipse.wb.tests.designer.core.eval.AbstractEngineTest;

/**
 * @author scheglov_ke
 */
public class LongTest extends AbstractEngineTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Project creation
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_setUp() throws Exception {
    do_projectCreate();
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // long
  //
  ////////////////////////////////////////////////////////////////////////////
  public void test_long_value1() throws Exception {
    check_long("1L", 1L);
  }

  public void test_long_value2() throws Exception {
    check_long("2l", 2l);
  }

  public void test_long_value_hex() throws Exception {
    check_long("0x0AL", 0x0AL);
  }

  public void test_long_value_oct() throws Exception {
    check_long("020L", 020L);
  }

  public void test_long_positive_value() throws Exception {
    check_long("+3l", +3l);
  }

  public void test_long_negative_value() throws Exception {
    check_long("-3l", -3l);
  }

  public void test_long_plus() throws Exception {
    check_long("1L + 2L", 1L + 2L);
  }

  public void test_long_plus3() throws Exception {
    check_long("1L + 2L + 3L", 1L + 2L + 3L);
  }

  public void test_long_minus() throws Exception {
    check_long("5L - 1L", 5L - 1L);
  }

  public void test_long_mul() throws Exception {
    check_long("2L * 3L", 2L * 3L);
  }

  public void test_long_div() throws Exception {
    check_long("6L / 2L", 6L / 2L);
  }

  public void test_long_div2() throws Exception {
    check_long("5L / 2L", 5L / 2L);
  }

  public void test_long_mod() throws Exception {
    check_long("5L % 2L", 5L % 2L);
  }

  public void test_long_mod2() throws Exception {
    check_long("-5L % 3L", -5L % 3L);
  }

  public void test_long_or() throws Exception {
    check_long("1L | 2L", 1L | 2L);
  }

  public void test_long_and() throws Exception {
    check_long("5L & 2L", 5L & 2L);
  }

  public void test_long_mix_int() throws Exception {
    check_long("1L + 2", 1L + 2);
  }

  public void test_long_mix_char() throws Exception {
    check_long("1L + '0'", 1L + '0');
  }

  public void test_long_cast_to() throws Exception {
    check_long("((long)1) + 2", (long) 1 + 2);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private void check_long(String expression, long expected) throws Exception {
    assertEquals(new Long(expected), evaluateExpression(expression, "long"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Project disposing
  //
  ////////////////////////////////////////////////////////////////////////////
  @Override
  public void test_tearDown() throws Exception {
    do_projectDispose();
  }
}
