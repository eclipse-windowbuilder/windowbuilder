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
package org.eclipse.wb.tests.css;

import org.eclipse.wb.internal.css.semantics.Semantics;
import org.eclipse.wb.internal.css.semantics.SimpleSidedProperty;

/**
 * Test for {@link Semantics}.
 * 
 * @author scheglov_ke
 */
public class SemanticsTest extends AbstractCssTest {
  /**
   * Basic test for parsing all 4 values of sided property.
   */
  public void test_border_color_4values() throws Exception {
    Semantics semantics =
        parseFirstRuleSemantics(
            "a {",
            "  border-color: #000 #111 #222 #333",
            "}",
            "/* filler filler filler filler */");
    SimpleSidedProperty color = semantics.m_border.getColor();
    assertSidedProperty_toString(color, "top={#000},right={#111},bottom={#222},left={#333}");
  }

  /**
   * We should handle function as whole string, even if there are spaces between arguments.
   */
  public void test_border_color_functions() throws Exception {
    Semantics semantics =
        parseFirstRuleSemantics(
            "a {",
            "  border-color: #000 rgb(1, 2, 3) #222 #333",
            "}",
            "/* filler filler filler filler */");
    SimpleSidedProperty color = semantics.m_border.getColor();
    assertSidedProperty_toString(color, "top={#000},right={rgb(1, 2, 3)},bottom={#222},left={#333}");
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Utils
  //
  ////////////////////////////////////////////////////////////////////////////
  private static void assertSidedProperty_toString(SimpleSidedProperty property, String expected) {
    assertEquals("SimpleSidedProperty{" + expected + "}", property.toString());
  }
}
