/*******************************************************************************
 * Copyright (c) 2011 Google, Inc.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.core.model.util;

import org.eclipse.wb.internal.core.model.util.GenericTypeResolverJavaInfo;
import org.eclipse.wb.internal.swing.model.component.ComponentInfo;
import org.eclipse.wb.tests.designer.swing.SwingModelTest;

import org.eclipse.jdt.core.dom.ClassInstanceCreation;

import org.junit.jupiter.api.Test;

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
	@Test
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
	 * No type argument in {@link ClassInstanceCreation} itself, but in Java7 we can use "diamond".
	 */
	@Test
	public void test_hasTypeArgument_java7() throws Exception {
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
				"      MyButton<String> button = new MyButton<>();",
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
	@Test
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
	@Test
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
