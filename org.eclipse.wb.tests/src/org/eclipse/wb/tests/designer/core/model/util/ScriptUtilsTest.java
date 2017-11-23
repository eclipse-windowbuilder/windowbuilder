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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.eclipse.wb.internal.core.model.JavaInfoUtils;
import org.eclipse.wb.internal.core.model.util.ScriptUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.internal.swing.model.component.ContainerInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

/**
 * Tests for {@link ScriptUtils}.
 * 
 * @author scheglov_ke
 */
public class ScriptUtilsTest extends SwingModelTest {
  ////////////////////////////////////////////////////////////////////////////
  //
  // Exit zone :-) XXX
  //
  ////////////////////////////////////////////////////////////////////////////
  public void _test_exit() throws Exception {
    System.exit(0);
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // Tests
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ScriptUtils#evaluate(String)}.
   */
  public void test_evaluate_noVariables() throws Exception {
    assertEquals(5, ScriptUtils.evaluate("2 + 3"));
    assertEquals("abc", ScriptUtils.evaluate("'a' + 'bc'"));
  }

  /**
   * Use {@link ReflectionUtils} methods.
   */
  public void test_evaluate_ReflectionUtils() throws Exception {
    String script = getSource("return ReflectionUtils.invokeMethod2('abc', 'length')");
    assertEquals(3, ScriptUtils.evaluate(script));
  }

  /**
   * Test for {@link ScriptUtils#evaluate(String, Object)}.
   */
  public void test_evaluate_withContext() throws Exception {
    assertEquals(3, ScriptUtils.evaluate("size()", ImmutableList.of(1, 2, 3)));
  }

  /**
   * Test for {@link ScriptUtils#evaluate(String, Map)}.
   */
  public void test_evaluate_withVariables() throws Exception {
    Map<String, Object> variables = ImmutableMap.<String, Object>of("a", 2, "b", 3);
    assertEquals(6, ScriptUtils.evaluate("a * b", variables));
    assertEquals(6, ScriptUtils.evaluate("c = a * b; return c;", variables));
    // variables should not be changed
    assertThat(variables).hasSize(2);
  }

  /**
   * Test for {@link ScriptUtils#evaluate(String, String, Object, String, Object)}.
   */
  public void test_evaluate_withOneVariable() throws Exception {
    assertEquals(10, ScriptUtils.evaluate("a * 2", "a", 5));
  }

  /**
   * Test for {@link ScriptUtils#evaluate(ClassLoader, String, String, Object, String, Object)}.
   */
  public void test_evaluate_withTwoVariables() throws Exception {
    assertEquals(15, ScriptUtils.evaluate("a * b", "a", 5, "b", 3));
  }

  /**
   * Test for {@link ScriptUtils#evaluate(String)}.
   */
  public void test_evaluate_accessToSwingClasses() throws Exception {
    assertEquals(
        javax.swing.SwingConstants.LEFT,
        ScriptUtils.evaluate("javax.swing.SwingConstants.LEFT"));
  }

  ////////////////////////////////////////////////////////////////////////////
  //
  // With JavaInfo context
  //
  ////////////////////////////////////////////////////////////////////////////
  /**
   * Test for {@link ScriptUtils#evaluate(ClassLoader, String)}.
   */
  public void test_evaluate_useDesignerClassLoader_noVariables() throws Exception {
    ContainerInfo panel =
        parseContainer(
            "// filler filler filler",
            "public class Test extends JPanel {",
            "  public Test() {",
            "  }",
            "}");
    // prepare expected
    Object expected;
    {
      ClassLoader classLoader = JavaInfoUtils.getClassLoader(panel);
      expected =
          classLoader.loadClass("com.jgoodies.forms.layout.Sizes").getField("DLUX1").get(null);
    }
    // try first time
    {
      Object actual = ScriptUtils.evaluate(m_lastLoader, "com.jgoodies.forms.layout.Sizes.DLUX1");
      assertSame(expected, actual);
    }
    // try second time (cache should be used)
    {
      Object actual = ScriptUtils.evaluate(m_lastLoader, "com.jgoodies.forms.layout.Sizes.DLUX1");
      assertSame(expected, actual);
    }
  }

  /**
   * Test for {@link ScriptUtils#evaluate(ClassLoader, String)}.
   * <p>
   * There was bug with creating new {@link Object} with patched MVEL.
   */
  public void test_evaluate_useDesignerClassLoader_createObject() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "public class MyButton extends JButton {",
            "}"));
    waitForAutoBuild();
    // parse
    parseContainer(
        "// filler filler filler filler filler",
        "// filler filler filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    // evaluate
    Object result = ScriptUtils.evaluate(m_lastLoader, "new test.MyButton()");
    assertEquals("test.MyButton", result.getClass().getCanonicalName());
  }

  /**
   * Test for {@link ScriptUtils#evaluate(ClassLoader, String, Map)}.
   */
  public void test_evaluate_useDesignerClassLoader_withVariables() throws Exception {
    parseContainer(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    //
    Object actual =
        ScriptUtils.evaluate(
            m_lastLoader,
            "a + (com.jgoodies.forms.layout.Sizes.DLUX1).value",
            ImmutableMap.<String, Object>of("a", 5.0));
    assertEquals(6.0, ((Double) actual).doubleValue(), 0.001);
  }

  /**
   * Test for {@link ScriptUtils#evaluate(ClassLoader, String, String, Object)}.
   */
  public void test_evaluate_useDesignerClassLoader_withOneVariable() throws Exception {
    setFileContentSrc(
        "test/Constants.java",
        getSource("package test;", "public interface Constants {", "  int A = 2;", "}"));
    waitForAutoBuild();
    parseContainer(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    //
    Object actual = ScriptUtils.evaluate(m_lastLoader, "test.Constants.A * a", "a", 3);
    assertEquals(2 * 3, actual);
  }

  /**
   * Test for {@link ScriptUtils#evaluate(ClassLoader, String, String, Object, String, Object)}.
   */
  public void test_evaluate_useDesignerClassLoader_withTwoVariables() throws Exception {
    setFileContentSrc(
        "test/Constants.java",
        getSource(
            "package test;",
            "public interface Constants {",
            "  int A = 2;",
            "  int B = 3;",
            "}"));
    waitForAutoBuild();
    parseContainer(
        "// filler filler filler",
        "public class Test extends JPanel {",
        "  public Test() {",
        "  }",
        "}");
    //
    Object actual =
        ScriptUtils.evaluate(
            m_lastLoader,
            "test.Constants.A * a + test.Constants.B * b",
            "a",
            2,
            "b",
            3);
    assertEquals(2 * 2 + 3 * 3, actual);
  }
}
