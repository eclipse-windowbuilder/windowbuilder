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
package org.eclipse.wb.tests.designer.core.model.util;

import com.google.common.base.Predicate;

import org.eclipse.wb.internal.core.model.util.predicate.AlwaysPredicate;
import org.eclipse.wb.internal.core.model.util.predicate.ComponentSubclassPredicate;
import org.eclipse.wb.internal.core.model.util.predicate.ExpressionPredicate;
import org.eclipse.wb.internal.core.model.util.predicate.SubclassPredicate;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

/**
 * Test for {@link Predicate} implementations.
 * 
 * @author scheglov_ke
 */
public class PredicatesTest extends SwingModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link AlwaysPredicate}.
   */
  public void test_AlwaysPredicate_true() throws Exception {
    Predicate<Object> predicate = new AlwaysPredicate(true);
    assertTrue(predicate.apply("yes"));
    assertTrue(predicate.apply(this));
    assertTrue(predicate.apply(null));
  }

  /**
   * Test for {@link AlwaysPredicate}.
   */
  public void test_AlwaysPredicate_false() throws Exception {
    Predicate<Object> predicate = new AlwaysPredicate(false);
    assertFalse(predicate.apply("yes"));
    assertFalse(predicate.apply(this));
    assertFalse(predicate.apply(null));
  }

  /**
   * Test for {@link SubclassPredicate}.
   */
  public void test_SubclassPredicate() throws Exception {
    Predicate<Object> predicate = new SubclassPredicate(String.class);
    assertTrue(predicate.apply("yes"));
    assertFalse(predicate.apply(this));
    assertTrue(predicate.apply(null));
  }

  /**
   * Test for {@link ComponentSubclassPredicate}.
   */
  public void test_ComponentSubclassPredicate() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    ComponentSubclassPredicate predicate = new ComponentSubclassPredicate("java.awt.Component");
    assertEquals("java.awt.Component", predicate.toString());
    assertTrue(predicate.apply(panel));
    assertFalse(predicate.apply(panel.getLayout()));
    assertFalse(predicate.apply("not JavaInfo"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Expression predicate
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ExpressionPredicate}.
   */
  public void test_ExpressionPredicate_alwaysTrue() throws Exception {
    Predicate<Object> predicate = new ExpressionPredicate<Object>("true");
    assertEquals("true", predicate.toString());
    assertTrue(predicate.apply(null));
  }

  /**
   * Test for {@link ExpressionPredicate}.
   */
  public void test_ExpressionPredicate_alwaysFalse() throws Exception {
    Predicate<Object> predicate = new ExpressionPredicate<Object>("false");
    assertFalse(predicate.apply(null));
  }

  /**
   * Test for {@link ExpressionPredicate}.
   */
  public void test_ExpressionPredicate_checkLength() throws Exception {
    Predicate<Object> predicate = new ExpressionPredicate<Object>("length() > 5");
    assertFalse(predicate.apply("123"));
    assertTrue(predicate.apply("123456"));
  }
}
