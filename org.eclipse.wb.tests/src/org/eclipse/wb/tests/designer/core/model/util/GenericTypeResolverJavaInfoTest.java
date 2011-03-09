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

import org.eclipse.wb.internal.core.model.util.GenericTypeResolverJavaInfo;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;

import java.lang.reflect.TypeVariable;

/**
 * Tests for {@link GenericTypeResolverJavaInfo}.
 * 
 * @author scheglov_ke
 */
public class GenericTypeResolverJavaInfoTest extends SwingModelTest {
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
   * When {@link ClassInstanceCreation} has type argument for type parameter.
   */
  public void test_hasTypeArgument() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "// filler filler filler",
            "public class MyButton<T> extends JButton {",
            "}"));
    waitForAutoBuild();
    // parse
    parseContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      MyButton<String> button = new MyButton<String>();",
        "      add(button);",
        "    }",
        "  }",
        "}");
    //
    ComponentInfo button = getJavaInfoByName("button");
    GenericTypeResolverJavaInfo resolver = new GenericTypeResolverJavaInfo(button);
    {
      Class<?> buttonClass = button.getDescription().getComponentClass();
      TypeVariable<?> typeVariable = buttonClass.getTypeParameters()[0];
      assertEquals("java.lang.String", resolver.resolve(typeVariable));
    }
  }

  /**
   * Type parameter was not specified.
   */
  public void test_noTypeArgument_hasBounds() throws Exception {
    setFileContentSrc(
        "test/MyModel.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "// filler filler filler",
            "public class MyModel {",
            "}"));
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "// filler filler filler",
            "public class MyButton<T extends MyModel> extends JButton {",
            "}"));
    waitForAutoBuild();
    // parse
    parseContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      MyButton button = new MyButton();",
        "      add(button);",
        "    }",
        "  }",
        "}");
    //
    ComponentInfo button = getJavaInfoByName("button");
    GenericTypeResolverJavaInfo resolver = new GenericTypeResolverJavaInfo(button);
    {
      Class<?> buttonClass = button.getDescription().getComponentClass();
      TypeVariable<?> typeVariable = buttonClass.getTypeParameters()[0];
      assertEquals("test.MyModel", resolver.resolve(typeVariable));
    }
  }

  /**
   * Type parameter was not specified, no bounds, so {@link Object}.
   */
  public void test_noTypeArgument_noBounds() throws Exception {
    setFileContentSrc(
        "test/MyButton.java",
        getTestSource(
            "// filler filler filler filler filler",
            "// filler filler filler filler filler",
            "// filler filler filler",
            "public class MyButton<T> extends JButton {",
            "}"));
    waitForAutoBuild();
    // parse
    parseContainer(
        "public class Test extends JPanel {",
        "  public Test() {",
        "    {",
        "      MyButton button = new MyButton();",
        "      add(button);",
        "    }",
        "  }",
        "}");
    //
    ComponentInfo button = getJavaInfoByName("button");
    GenericTypeResolverJavaInfo resolver = new GenericTypeResolverJavaInfo(button);
    {
      Class<?> buttonClass = button.getDescription().getComponentClass();
      TypeVariable<?> typeVariable = buttonClass.getTypeParameters()[0];
      assertEquals("java.lang.Object", resolver.resolve(typeVariable));
    }
  }
}
