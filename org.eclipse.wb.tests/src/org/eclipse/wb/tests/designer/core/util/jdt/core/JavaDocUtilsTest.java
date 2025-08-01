/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
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
package org.eclipse.wb.tests.designer.core.util.jdt.core;

import org.eclipse.wb.internal.core.utils.jdt.core.JavaDocUtils;
import org.eclipse.wb.internal.core.utils.reflect.ReflectionUtils;
import org.eclipse.wb.tests.designer.core.AbstractJavaTest;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link JavaDocUtils}.
 *
 * @author scheglov_ke
 */
public class JavaDocUtilsTest extends AbstractJavaTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Life cycle
	//
	////////////////////////////////////////////////////////////////////////////
	@Override
	@BeforeEach
	public void setUp() throws Exception {
		super.setUp();
		if (m_testProject == null) {
			do_projectCreate();
		}
	}

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
	// getTooltip
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_getTooltip_basic() throws Exception {
		TypeDeclaration typeDeclaration = createTypeDeclaration_Test("""
				class Test {
					void test() {
						foo_1();
						foo_2();
						bar();
						new java.util.concurrent.locks.ReentrantLock().lock();
					}
					/**
					 * My   tooltip.
					 */
					void foo_1() {
					}
					/**
					 * My tooltip with <code>tags</code>.
					 */
					void foo_2() {
					}
					void bar() {
					}
				}""");
		IMethodBinding[] methodBindings = CodeUtilsTest.getInvocationBindings(typeDeclaration, 0);
		assertEquals("My tooltip.", getTooltip(methodBindings[0]));
		assertEquals("My tooltip with <code>tags</code>.", getTooltip(methodBindings[1]));
		assertNull(getTooltip(methodBindings[2]));
		assertNull(getTooltip(methodBindings[2]));
	}

	/**
	 * Test for {@link JavaDocUtils#getTooltip(IMember)}. It should use short names of types, because
	 * long names often don't fit into small tooltip window.
	 */
	@Test
	public void test_getTooltip_shortenTypeReferences() throws Exception {
		IType type =
				createModelType(
						"test",
						"Test.java",
						getSource(
								"package test;",
								"class Test {",
								"  /**",
								"  * Method {@link java.util.List#add(Object)} reference with long type.",
								"  */",
								"  void foo() {",
								"  }",
								"}"));
		String tooltip = JavaDocUtils.getTooltip(type.getMethods()[0]);
		assertEquals("Method {@link List#add(Object)} reference with long type.", tooltip);
	}

	private String getTooltip(IMethodBinding methodBinding) throws Exception {
		return JavaDocUtils.getTooltip(m_lastEditor.getJavaProject(), methodBinding);
	}

	/**
	 * Test for inherited comment.
	 */
	@Test
	public void test_getTooltip_useInherited() throws Exception {
		createModelCompilationUnit(
				"test",
				"A.java",
				getSource(
						"package test;",
						"class A {",
						"  /**",
						"  * Inherited Javadoc.",
						"  */",
						"  void foo() {",
						"  }",
						"}"));
		ICompilationUnit bUnit =
				createModelCompilationUnit(
						"test",
						"B.java",
						getSource("package test;", "class B extends A {", "  void foo() {", "  }", "}"));
		//
		IMethod method = bUnit.getTypes()[0].getMethods()[0];
		String javaDoc = JavaDocUtils.getTooltip(method);
		assertEquals("Inherited Javadoc.", javaDoc);
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Test for private getTooltip_useShortTypeNames()
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_getTooltip_useShortTypeNames() throws Exception {
		// empty string
		{
			String input = "";
			String expected = input;
			String actual = call_getTooltip_useShortTypeNames(input);
			assertEquals(expected, actual);
		}
		// @link not closed
		{
			String input = "The {@link java.util.List#add(Object) method.";
			String expected = input;
			String actual = call_getTooltip_useShortTypeNames(input);
			assertEquals(expected, actual);
		}
		// already short type
		{
			String input = "The {@link List#add(Object)} method.";
			String expected = input;
			String actual = call_getTooltip_useShortTypeNames(input);
			assertEquals(expected, actual);
		}
		// make type short
		{
			String input = "The {@link java.util.List#add(Object)} method.";
			String expected = "The {@link List#add(Object)} method.";
			String actual = call_getTooltip_useShortTypeNames(input);
			assertEquals(expected, actual);
		}
	}

	private static String call_getTooltip_useShortTypeNames(String s) {
		return (String) ReflectionUtils.invokeMethodEx(
				JavaDocUtils.class,
				"getTooltip_useShortTypeNames(java.lang.String)",
				s);
	}
}
