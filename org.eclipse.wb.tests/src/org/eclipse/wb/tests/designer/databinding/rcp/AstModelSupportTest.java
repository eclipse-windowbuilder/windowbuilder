/*******************************************************************************
 * Copyright (c) 2011, 2025 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Google, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.wb.tests.designer.databinding.rcp;

import org.eclipse.wb.internal.core.databinding.model.AstObjectInfo;
import org.eclipse.wb.internal.core.databinding.parser.AstModelSupport;
import org.eclipse.wb.internal.core.utils.ast.AstNodeUtils;
import org.eclipse.wb.internal.core.utils.ast.DomGenerics;
import org.eclipse.wb.tests.designer.core.AbstractJavaTest;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

/**
 * @author lobas_av
 *
 */
public class AstModelSupportTest extends AbstractJavaTest {
	////////////////////////////////////////////////////////////////////////////
	//
	// Project creation
	//
	////////////////////////////////////////////////////////////////////////////
	@BeforeClass
	public static void setUpClass() throws Exception {
		do_projectCreate();
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Method creation
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_method() throws Exception {
		TypeDeclaration type = createTypeDeclaration_Test("""
				public class Test {
					public void foo() {
						create("aaa");
						String name = create("aaa");
						foo(name, create("aaa"));
					}
					static String create(String value) {
						return "zzz: " + value;
					}
					void foo(String name, String value) {
					}
				}""");
		//
		MethodDeclaration method = AstNodeUtils.getMethodBySignature(type, "foo()");
		assertNotNull(method);
		//
		List<?> statements = method.getBody().statements();
		//
		assertNotNull(statements);
		assertEquals(3, statements.size());
		//
		MethodInvocation invocation0 = UtilsTest.getMethodInvocation(statements, 0);
		//
		TestModel model = new TestModel();
		assertNull(model.getVariableIdentifier());
		//
		AstModelSupport support = new AstModelSupport(model, invocation0);
		assertNull(model.getVariableIdentifier());
		assertSame(model, support.getModel());
		//
		assertTrue(support.isRepresentedBy(invocation0));
		//
		MethodInvocation invocation2 = UtilsTest.getMethodInvocation(statements, 2);
		assertFalse(support.isRepresentedBy(DomGenerics.arguments(invocation2).get(0)));
		assertFalse(support.isRepresentedBy(DomGenerics.arguments(invocation2).get(1)));
	}

	@Test
	public void test_field_assignment_method() throws Exception {
		TypeDeclaration type = createTypeDeclaration_Test("""
				public class Test {
					private String m_name;
						public void foo() {
						m_name = create("aaa");
						String name = create("aaa");
						foo(name, m_name);
					}
					static String create(String value) {
						return "zzz: " + value;
					}
					void foo(String name, String value) {
					}
				}""");
		//
		MethodDeclaration method = AstNodeUtils.getMethodBySignature(type, "foo()");
		assertNotNull(method);
		//
		List<?> statements = method.getBody().statements();
		//
		assertNotNull(statements);
		assertEquals(3, statements.size());
		//
		Assignment assignment = UtilsTest.getAssignment(statements, 0);
		assertInstanceOf(MethodInvocation.class, assignment.getRightHandSide());
		//
		TestModel model = new TestModel();
		assertNull(model.getVariableIdentifier());
		//
		AstModelSupport support = new AstModelSupport(model, assignment.getRightHandSide());
		assertEquals("m_name", model.getVariableIdentifier());
		assertSame(model, support.getModel());
		//
		MethodInvocation invocation = UtilsTest.getMethodInvocation(statements, 2);
		assertFalse(support.isRepresentedBy(DomGenerics.arguments(invocation).get(0)));
		assertTrue(support.isRepresentedBy(DomGenerics.arguments(invocation).get(1)));
		assertTrue(support.isRepresentedBy(DomGenerics.arguments(invocation).get(1)));
	}

	@Test
	public void test_local_variable_method() throws Exception {
		TypeDeclaration type = createTypeDeclaration_Test("""
				public class Test {
					public void foo() {
						String name = create("aaa");
						String name2 = create("aaa");
						foo(name, name2);
					}
					static String create(String value) {
						return "zzz: " + value;
					}
					void foo(String name, String value) {
					}
				}""");
		//
		MethodDeclaration method = AstNodeUtils.getMethodBySignature(type, "foo()");
		assertNotNull(method);
		//
		List<?> statements = method.getBody().statements();
		//
		assertNotNull(statements);
		assertEquals(3, statements.size());
		//
		VariableDeclarationStatement statement = (VariableDeclarationStatement) statements.get(0);
		//
		TestModel model = new TestModel();
		assertNull(model.getVariableIdentifier());
		//
		AstModelSupport support =
				new AstModelSupport(model, DomGenerics.fragments(statement).get(0).getInitializer());
		assertEquals("name", model.getVariableIdentifier());
		assertSame(model, support.getModel());
		//
		MethodInvocation invocation2 = UtilsTest.getMethodInvocation(statements, 2);
		assertTrue(support.isRepresentedBy(DomGenerics.arguments(invocation2).get(0)));
		assertTrue(support.isRepresentedBy(DomGenerics.arguments(invocation2).get(0)));
		assertFalse(support.isRepresentedBy(DomGenerics.arguments(invocation2).get(1)));
	}

	@Test
	public void test_local_variable_assignment_method() throws Exception {
		TypeDeclaration type = createTypeDeclaration_Test("""
				public class Test {
					public void foo() {
						String name;
						String name2 = create("aaa");
						name = create("aaa");
						foo(name, name2);
					}
					static String create(String value) {
						return "zzz: " + value;
					}
					void foo(String name, String value) {
					}
				}""");
		//
		MethodDeclaration method = AstNodeUtils.getMethodBySignature(type, "foo()");
		assertNotNull(method);
		//
		List<?> statements = method.getBody().statements();
		//
		assertNotNull(statements);
		assertEquals(4, statements.size());
		//
		Assignment statement2 = UtilsTest.getAssignment(statements, 2);
		//
		TestModel model = new TestModel();
		assertNull(model.getVariableIdentifier());
		//
		AstModelSupport support = new AstModelSupport(model, statement2.getRightHandSide());
		assertEquals("name", model.getVariableIdentifier());
		assertSame(model, support.getModel());
		//
		MethodInvocation invocation3 = UtilsTest.getMethodInvocation(statements, 3);
		assertTrue(support.isRepresentedBy(DomGenerics.arguments(invocation3).get(0)));
		assertTrue(support.isRepresentedBy(DomGenerics.arguments(invocation3).get(0)));
		assertFalse(support.isRepresentedBy(DomGenerics.arguments(invocation3).get(1)));
	}

	////////////////////////////////////////////////////////////////////////////
	//
	// Constructor creation
	//
	////////////////////////////////////////////////////////////////////////////
	@Test
	public void test_constructor() throws Exception {
		TypeDeclaration type = createTypeDeclaration_Test("""
				public class Test {
					public void foo() {
						new String("aaa");
						String name = new String("aaa");
						foo(name, new String("aaa"));
					}
					void foo(String name, String value) {
					}
				}""");
		//
		MethodDeclaration method = AstNodeUtils.getMethodBySignature(type, "foo()");
		assertNotNull(method);
		//
		List<?> statements = method.getBody().statements();
		//
		assertNotNull(statements);
		assertEquals(3, statements.size());
		//
		ClassInstanceCreation creation0 = UtilsTest.getClassInstanceCreation(statements, 0);
		//
		TestModel model = new TestModel();
		assertNull(model.getVariableIdentifier());
		//
		AstModelSupport support = new AstModelSupport(model, creation0);
		assertNull(model.getVariableIdentifier());
		assertSame(model, support.getModel());
		//
		assertTrue(support.isRepresentedBy(creation0));
		//
		MethodInvocation invocation2 = UtilsTest.getMethodInvocation(statements, 2);
		assertFalse(support.isRepresentedBy(DomGenerics.arguments(invocation2).get(0)));
		assertFalse(support.isRepresentedBy(DomGenerics.arguments(invocation2).get(1)));
	}

	@Test
	public void test_field_assignment_constructor() throws Exception {
		TypeDeclaration type = createTypeDeclaration_Test("""
				public class Test {
					private String m_name;
					public void foo() {
						m_name = new String("aaa");
						String name = new String("aaa");
						foo(name, m_name);
					}
					void foo(String name, String value) {
					}
				}""");
		//
		MethodDeclaration method = AstNodeUtils.getMethodBySignature(type, "foo()");
		assertNotNull(method);
		//
		List<?> statements = method.getBody().statements();
		//
		assertNotNull(statements);
		assertEquals(3, statements.size());
		//
		Assignment assignment = UtilsTest.getAssignment(statements, 0);
		//
		TestModel model = new TestModel();
		assertNull(model.getVariableIdentifier());
		//
		//
		AstModelSupport support = new AstModelSupport(model, assignment.getRightHandSide());
		assertEquals("m_name", model.getVariableIdentifier());
		assertSame(model, support.getModel());
		//
		MethodInvocation invocation = UtilsTest.getMethodInvocation(statements, 2);
		assertFalse(support.isRepresentedBy(DomGenerics.arguments(invocation).get(0)));
		assertTrue(support.isRepresentedBy(DomGenerics.arguments(invocation).get(1)));
		assertTrue(support.isRepresentedBy(DomGenerics.arguments(invocation).get(1)));
	}

	@Test
	public void test_local_variable_constructor() throws Exception {
		TypeDeclaration type = createTypeDeclaration_Test("""
				public class Test {
					public void foo() {
						String name = new String("aaa");
						String name2 = new String("aaa");
						foo(name, name2);
					}
					void foo(String name, String value) {
					}
				}""");
		//
		MethodDeclaration method = AstNodeUtils.getMethodBySignature(type, "foo()");
		assertNotNull(method);
		//
		List<?> statements = method.getBody().statements();
		//
		assertNotNull(statements);
		assertEquals(3, statements.size());
		//
		VariableDeclarationStatement statement = (VariableDeclarationStatement) statements.get(0);
		//
		TestModel model = new TestModel();
		assertNull(model.getVariableIdentifier());
		//
		AstModelSupport support =
				new AstModelSupport(model, DomGenerics.fragments(statement).get(0).getInitializer());
		assertEquals("name", model.getVariableIdentifier());
		assertSame(model, support.getModel());
		//
		MethodInvocation invocation2 = UtilsTest.getMethodInvocation(statements, 2);
		assertTrue(support.isRepresentedBy(DomGenerics.arguments(invocation2).get(0)));
		assertTrue(support.isRepresentedBy(DomGenerics.arguments(invocation2).get(0)));
		assertFalse(support.isRepresentedBy(DomGenerics.arguments(invocation2).get(1)));
	}

	@Test
	public void test_local_variable_assignment_constructor() throws Exception {
		TypeDeclaration type = createTypeDeclaration_Test("""
				public class Test {
					public void foo() {
						String name;
						String name2 = new String("aaa");
						name = new String("aaa");
						foo(name, name2);
					}
					void foo(String name, String value) {
					}
				}""");
		//
		MethodDeclaration method = AstNodeUtils.getMethodBySignature(type, "foo()");
		assertNotNull(method);
		//
		List<?> statements = method.getBody().statements();
		//
		assertNotNull(statements);
		assertEquals(4, statements.size());
		//
		Assignment statement2 = UtilsTest.getAssignment(statements, 2);
		//
		TestModel model = new TestModel();
		assertNull(model.getVariableIdentifier());
		//
		AstModelSupport support = new AstModelSupport(model, statement2.getRightHandSide());
		assertEquals("name", model.getVariableIdentifier());
		assertSame(model, support.getModel());
		//
		MethodInvocation invocation3 = UtilsTest.getMethodInvocation(statements, 3);
		assertTrue(support.isRepresentedBy(DomGenerics.arguments(invocation3).get(0)));
		assertTrue(support.isRepresentedBy(DomGenerics.arguments(invocation3).get(0)));
		assertFalse(support.isRepresentedBy(DomGenerics.arguments(invocation3).get(1)));
	}

	private static class TestModel extends AstObjectInfo {
	}
}